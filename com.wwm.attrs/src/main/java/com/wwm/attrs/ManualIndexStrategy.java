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
package com.wwm.attrs;

import java.util.ArrayList;
import java.util.Collection;


import com.wwm.db.whirlwind.IndexStrategy;



public class ManualIndexStrategy implements IndexStrategy {

    private static final long serialVersionUID = 1L;

    private String name;

    private ArrayList<SplitConfiguration> splitConfigurations = new ArrayList<SplitConfiguration>();

    public ManualIndexStrategy( String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(SplitConfiguration sc) {
        splitConfigurations.add( sc);
    }

    //	public Map<String, SplitConfiguration> getSplitConfigurations() {
    public Collection<SplitConfiguration> getSplitConfigurations() {
        return splitConfigurations;
    }


}
