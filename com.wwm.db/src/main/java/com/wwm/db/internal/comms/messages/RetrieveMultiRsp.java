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

@SuppressWarnings("serial")
public class RetrieveMultiRsp extends OkRsp {
	private final Object[] objects;
	
	public RetrieveMultiRsp(int storeId, int cid, Object[] objects) {
		super(storeId, cid);
		this.objects = objects;
	}

	public Object[] getCompactedObjects() {
		return objects;
	}

}
