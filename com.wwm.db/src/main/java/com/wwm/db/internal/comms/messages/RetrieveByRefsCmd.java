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
package com.wwm.db.internal.comms.messages;

import java.util.Collection;

import com.wwm.db.Ref;
import com.wwm.db.internal.RefImpl;

@SuppressWarnings("serial")
public class RetrieveByRefsCmd extends TransactionCommand {
	private final RefImpl<?>[] refs;

	public RetrieveByRefsCmd(int storeId, int cid, int tid, Collection<Ref> refs) {
		super(storeId, cid, tid);
		// Following involves a seemingly unnecessary System.arrayCopy
		// but we'd rather do on client that server.
		this.refs = refs.toArray( new RefImpl[0] );
	}

	public RefImpl<?>[] getRefs() {
		return refs;
	}

}
