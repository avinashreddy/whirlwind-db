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
package com.wwm.db.whirlwind;


import com.wwm.db.marker.IAttributeContainer;
import com.wwm.util.IntMap;

/**
 * Int to V map including remap() capability
 *
 * @param <V>
 */
public interface CardinalAttributeMap<V> extends IntMap<V>, IAttributeContainer {

	public StringAttributeMap<V> remap(AttributeRemapper remapper);
}
