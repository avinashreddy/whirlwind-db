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
package com.wwm.model.attributes;

/**
 * Bean representation of named attributes, such as a boolean value,
 * enum value, or location preference.
 * Values should be strings or intrinsic types (float, Date, String[], etc)
 */
public abstract class Attribute {

    private String name;

    public Attribute( String name) {
        assert name != null;
        this.name = name;
    }

    /**
     * Get the name of this attribute
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
