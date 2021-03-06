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

import com.wwm.db.internal.server.ServerTransaction.Mode;

public class TransactionState implements TransactionControl {
	private long visibleVersion;
	private Long commitVersion = null;
	private Mode mode = Mode.Normal;
	private DatabaseVersionState vp;

	public TransactionState(DatabaseVersionState vp) {
		this.vp = vp;
		this.visibleVersion = vp.getCurrentDbVersion();
	}
	
	public long getVisibleVersion() {
		if (mode == Mode.Normal) {
			return visibleVersion;
		}
		
		if (mode == Mode.IndexRebuild) {
			return vp.getCurrentDbVersion();
		}

		if (mode == Mode.IndexWrite) {
			return commitVersion;
		}
		
		throw new Error();
	}

	public Long getCommitVersion() {
		if (mode == Mode.Normal || mode == Mode.IndexWrite) {
			return commitVersion;
		}
		
		if (mode == Mode.IndexRebuild) {
			return vp.getCurrentDbVersion();
		}
		
		throw new Error();
	}

	public void setCommitVersion(Long commitVersion) {
		this.commitVersion = commitVersion;
	}

	public void setMode(Mode mode) {
		if (mode == Mode.Normal) {
			assert(this.mode != Mode.Normal);
		}
		
		if (mode == Mode.IndexRebuild || mode == Mode.IndexWrite) {
			assert(this.mode == Mode.Normal);
		}
		
		if (mode == Mode.IndexWrite) {
			assert(isInCommitPhase());
		}
	
		this.mode = mode;
	}

	public boolean isInCommitPhase() {
		return commitVersion != null;
	}

}