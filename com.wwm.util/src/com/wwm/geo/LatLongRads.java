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
package com.wwm.geo;

public final class LatLongRads {
	public final double lat;	
	public final double lon;

	public LatLongRads(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public LatLongDegs toDegrees() {
		return new LatLongDegs(Math.toDegrees(lat), Math.toDegrees(lon));
	}
	
}
