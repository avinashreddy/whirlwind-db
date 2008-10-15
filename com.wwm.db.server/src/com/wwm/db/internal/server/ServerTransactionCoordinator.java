/******************************************************************************
 * Copyright (c) 2005-2008 Whirlwind Match Limited. All rights reserved.
 *
 * This is open source software; you can use, redistribute and/or modify
 * it under the terms of the Open Software Licence v 3.0 as published by the 
 * Open Source Initiative.
 *
 * You should have received a copy of the Open Software Licence along with this
 * application. if not, contact the Open Source Initiative (www.opensource.org)
 *****************************************************************************/
package com.wwm.db.internal.server;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.wwm.db.core.Settings;
import com.wwm.db.exceptions.UnknownTransactionException;
import com.wwm.db.internal.server.PersistentServerTransaction.Key;
import com.wwm.db.internal.server.txlog.TxLogSink;
import com.wwm.io.packet.layer2.MessageInterface;
import com.wwm.io.packet.messages.Command;
import com.wwm.util.FastSemaphore;

/**
 * This class coordinates the assignment of 'write privileges' between worker threads.
 * In the distributed version of the server this will become a distributed transaction coordinator.
 * Worker threads need write privileges to modify the database in any way. Only one thread may
 * acquire write privileges at a time.
 * Write privileges are required to: a) Commit a transaction, b) reorganise content e.g. lazy inserts on indexes.
 */
public class ServerTransactionCoordinator extends Thread implements TransactionCoordinator {

	private static final int transactionTimeoutSecs = Settings.getInstance().getTransactionTimeToLiveSecs();
	private static final int transactionInactivityTimeoutSecs = Settings.getInstance().getTransactionInactivityTimeoutSecs();
	
	private final FastSemaphore exclusiveLock = new FastSemaphore(1);
	private Thread privilegedThread = null;
	private final DatabaseVersionState dbVersionState;
	private final Repository repository;
	private final Map<Key, PersistentServerTransaction> transactions = new HashMap<Key, PersistentServerTransaction>();
	
	private boolean closing = false;
	private TxLogSink txLog;
	
	public ServerTransactionCoordinator(DatabaseVersionState dbVersionState, Repository repository) {
		super("Transaction Monitor");
		this.dbVersionState = dbVersionState;
		this.repository = repository;
		super.setDaemon(true);
		super.setPriority(Thread.MIN_PRIORITY);
		super.start();
	}

	public long getOldestTransactionVersion() {
		long oldest = dbVersionState.getCurrentDbVersion();
		synchronized (transactions) {
			for (PersistentServerTransaction transaction : transactions.values()) {
				oldest = Math.min(oldest, transaction.getVisibleVersion());
			}
		}
		return oldest;
	}
	
	/**
	 * Every so often, check transactions collection for expired transactions, and if
	 * they're expired and not busy, remove them. 
	 */
	@Override
	public void run() {
		for (;;) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) { 
				if (!closing) {
					e.printStackTrace(); // interrupt is unexpected if we're not closing.
				}
			}
			if (closing) return;
			
			synchronized (transactions) {
				GregorianCalendar gc = new GregorianCalendar();
				gc.add(Calendar.SECOND, -transactionTimeoutSecs);
				Date timeoutTime = gc.getTime();
				
				gc = new GregorianCalendar();
				gc.add(Calendar.SECOND, -transactionInactivityTimeoutSecs);
				Date inactiveTime = gc.getTime();
				
				Iterator<Entry<Key, PersistentServerTransaction>> i = transactions.entrySet().iterator();
				while (i.hasNext()) {
					Entry<Key, PersistentServerTransaction> entry = i.next();
					PersistentServerTransaction transaction = entry.getValue();
					if ( ( transaction.getLastUsedTime().before(inactiveTime) 
							|| transaction.getStartedTime().before(timeoutTime)
						  ) && !transaction.isBusy() ) {
						i.remove();
					}
				}
			}
		}
	}
	
	public void addTransaction(PersistentServerTransaction transaction) {
		synchronized (transactions) {
			transactions.put(transaction.getKey(), transaction);
		}
	}

	public void tryRemoveTransaction(MessageInterface source, int tid) {
		synchronized (transactions) {
			Key key = new Key(source, tid);
			PersistentServerTransaction transaction = transactions.get(key);
			if (transaction != null && !transaction.isBusy()) {
				transactions.remove(key);
			}
		}
	}
	
	public void endAction(PersistentServerTransaction transaction) {
		synchronized (transactions) {
			assert(transaction.isBusy());
			transaction.markIdle();
			transaction.touchLastUsedTime();
			CurrentTransactionHolder.setTransaction(null);
			if (transaction.isCompleted()) {
				transactions.remove(transaction.getKey());
			}
		}
	}

	public PersistentServerTransaction beginAction(MessageInterface source, int tid) throws UnknownTransactionException {
		synchronized (transactions) {
			Key key = new Key(source, tid);
			PersistentServerTransaction transaction = transactions.get(key);
			if (transaction == null) throw new UnknownTransactionException(tid);
			CurrentTransactionHolder.setTransaction(transaction);
			// Due to server threads the start of a new action can occur before another server thread has retired the transaction
			// from the previous action, causing this assert to trigger.
//			assert(!transaction.isBusy());
			transaction.markBusy();
			return transaction;
		}
	}
		
	// FIXME: Please document, and say what the + 1 is for
	public long acquireWritePrivilege() {
		Thread thread = Thread.currentThread();
		try {
			exclusiveLock.acquire();
		} catch (InterruptedException e) {
			throw new Error(e);
		}
		synchronized (this) {
			assert(privilegedThread == null);
			privilegedThread = thread;
			return dbVersionState.getCurrentDbVersion() + 1;
		}
	}

	public synchronized void releaseWritePrivilege() {
		assert(privilegedThread == Thread.currentThread());
		privilegedThread = null;
		dbVersionState.upissue();
		exclusiveLock.release();
	}

	public synchronized void abortWritePrivilege() {
		assert(privilegedThread == Thread.currentThread());
		privilegedThread = null;
		exclusiveLock.release();
	}
	
	
	public void close() {
		closing = true;
		super.interrupt();
		while (super.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) { e.printStackTrace(); } // FIXME: Document this exception
		}
	}
	
	public void writeToTransactionLog(Command command) throws Error {
		if (txLog != null) {
			try {
				txLog.write(dbVersionState.getCurrentDbVersion(), command);
				txLog.flush();
			} catch (IOException e) {
				throw new Error(e);
			}
		}
	}

	public void useTxLog(TxLogSink txLog) {
		this.txLog = txLog;
	}

	public DatabaseVersionState getDatabaseVersionState() {
		return dbVersionState;
	}

	public Repository getRepository() {
		return repository;
	}

}
