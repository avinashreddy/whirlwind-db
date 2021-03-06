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
package com.wwm.db.marker;


import com.wwm.db.whirlwind.internal.IAttribute;
import com.wwm.db.whirlwind.internal.IAttributeMap;

/**
 * 
 */
public interface IAttributeContainer {

	/**
	 * All attribute maps sent to the server must be able to deliver a representation that the server expects
	 * FIXME: This isn't actually used on the server, as other interfaces provide this functionality
	 */
	IAttributeMap<IAttribute> getAttributeMap();

}
