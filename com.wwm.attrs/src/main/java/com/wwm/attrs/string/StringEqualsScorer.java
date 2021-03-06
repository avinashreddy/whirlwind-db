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
package com.wwm.attrs.string;

public class StringEqualsScorer extends StringBaseScorer { 
    
    private static final long serialVersionUID = 6358946451731357494L;
    
    public StringEqualsScorer(int attrId, int otherAttrId) {
        super( attrId, otherAttrId );
    }

    @Override
    protected float calcScore(StringValue thisAttr, StringValue otherAttr) {
        if (otherAttr.getValue().equals(thisAttr.getValue())) {
            return maxScore;
        }
        return minScore;
    }
    
    @Override
    protected float calcScore(StringValue thisAttr, StringMultiValue otherAttr) {
        for (String val: otherAttr.getValue()) {
            if (val.equals(thisAttr.getValue())) {
                return maxScore;
            }
        }
        return minScore;
    }
}
