/******************************************************************************
 * Copyright (c) 2004-2008 Whirlwind Match Limited. All rights reserved.
 *
 * This is open source software; you can use, redistribute and/or modify
 * it under the terms of the Open Software Licence v 3.0 as published by the 
 * Open Source Initiative.
 *
 * You should have received a copy of the Open Software Licence along with this
 * application. if not, contact the Open Source Initiative (www.opensource.org)
 *****************************************************************************/
package com.wwm.attrs.dimensions;


import com.wwm.attrs.internal.Attribute;
import com.wwm.attrs.util.Range3D;
import com.wwm.model.dimensions.IPoint3D;
import com.wwm.model.dimensions.IRange3D;

public abstract class RangePreference3D extends Attribute implements IRange3D {
	/**
	 * @return Returns the max.
	 */
	public IPoint3D getMax() {
		return range3d.getMax();
	}
	/**
	 * @return Returns the min.
	 */
	public IPoint3D getMin() {
		return range3d.getMin();
	}
	/**
	 * @param min
	 * @param max
	 */
	public RangePreference3D(int attrId, IPoint3D min, IPoint3D max) {
		super( attrId );
		range3d = new Range3D(min, max);
	}
	public boolean contains(IPoint3D point) {
		return range3d.contains(point);
	}
	
	private Range3D range3d;
}
