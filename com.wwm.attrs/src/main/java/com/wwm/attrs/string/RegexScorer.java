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

import java.util.regex.Matcher;


import com.wwm.attrs.Score;
import com.wwm.attrs.internal.IConstraintMap;
import com.wwm.attrs.internal.TwoAttrScorer;
import com.wwm.db.whirlwind.internal.IAttribute;
import com.wwm.db.whirlwind.internal.IAttributeConstraint;
import com.wwm.db.whirlwind.internal.IAttributeMap;



public class RegexScorer extends TwoAttrScorer { 
    
    private static final long serialVersionUID = 6358946451731357494L;
    
    public RegexScorer(int attrId, int otherAttrId) {
        super( attrId, otherAttrId );
    }
    
    @Override
    public void scoreSearchToNode(Score score, Score.Direction d, IConstraintMap c, IAttributeMap<? extends IAttribute> scoreAttrs) {
        // na == null All Items under this node have null for this attribute
        // na.hasValue() do any Items under this node have this attribute
        // na.isIncludesNotSpecified Some Items under this node have null for this attribute

        IAttribute attr = scoreAttrs.findAttr(scorerAttrId);
		if (attr == null) {
			return; // If we do not have the scorer attr present in the search direction, we do not score - it wasn't 'wanted'
		}

        RegexValue bAttr = (RegexValue) attr;
        IAttributeConstraint bOtherNa = c.findAttr(otherAttrId);

        // If there is no Node Data then we only score null 
        if (bOtherNa == null ) { // || !bOtherNa.hasValue()
            score.addNull(this, d);
            return;
        }
        
        float result = 0.0f;

        // If there are nulls underneath this 
        // node include a null score
        if (bOtherNa.isIncludesNotSpecified() && isScoreNull()) {
            result = getScoreOnNull();
        }        
            
        StringConstraint bc = (StringConstraint)bOtherNa;
        result = Math.max(result, calcScore(bAttr, bc));
        score.add(this, result, d);
    }
    
    @Override
    public void scoreNodeToSearch(Score score, Score.Direction d, IAttributeMap<IAttributeConstraint> c, IAttributeMap<IAttribute> searchAttrs) {
    	IAttributeConstraint bNa = c.findAttr(scorerAttrId);
		if (bNa == null) {
			return; // If we do not have the scorer attr present in the search direction, we do not score - it wasn't 'wanted'
		}
        
        // na == null All Items under this node have null for this attribute
        // na.hasValue() do any Items under this node have this attribute
        // na.isIncludesNotSpecified Some Items under this node have null for this attribute

        StringValue otherAttr = null;
        StringMultiValue otherMultiAttr = null;
        if (c.findAttr(otherAttrId) instanceof StringValue) {
            otherAttr = (StringValue) searchAttrs.findAttr(otherAttrId);
        } else {
            otherMultiAttr = (StringMultiValue) searchAttrs.findAttr(otherAttrId);
        }

        // If some nulls under this node then Score 1 so 
        // as not to push this node down in score 
        if (bNa.isIncludesNotSpecified()) {
            score.add(this, 1.0f, d);
            return;
        }

//        if (!bNa.hasValue()) {
//            return;
//        }

        // If there is no Attr Data then we only score null 
        if (otherAttr == null) {
            score.addNull(this, d);
            return;
        }
        
        RegexConstraint bc = (RegexConstraint)bNa;
        if (bc != null) {
            if (otherAttr != null) {
                score.add(this, calcScore(bc, otherAttr), d);
            } else {
                score.add(this, calcScore(bc, otherMultiAttr), d);
            }
        }        
    }
    
    @Override
    public void scoreItemToItem(Score score, Score.Direction d, IAttributeMap<IAttribute> c, IAttributeMap<IAttribute> scoreAttrs) {
        IAttribute attr = scoreAttrs.findAttr(scorerAttrId);
		if (attr == null) {
			return; // If we do not have the scorer attr present in the search direction, we do not score - it wasn't 'wanted'
		}

        RegexValue bAttr = (RegexValue) attr;
        StringValue otherAttr = null;
        StringMultiValue otherMultiAttr = null;
        if (c.findAttr(otherAttrId) instanceof StringValue) {
            otherAttr = (StringValue) c.findAttr(otherAttrId);
        } else {
            otherMultiAttr = (StringMultiValue) c.findAttr(otherAttrId);
        }
        
        // Ignore if not scoring null
        if (otherAttr == null && otherMultiAttr == null) {
            score.addNull(this, d);
            return;
        }
        
        if (otherAttr != null) {
            score.add(this, calcScore(bAttr, otherAttr), d);
        } else {
            score.add(this, calcScore(bAttr, otherMultiAttr), d);
        }
    }    

    private float calcScore(RegexValue thisAttr, StringValue otherAttr) {
        Matcher m = thisAttr.getValue().matcher(otherAttr.getValue());
        if (m.matches()) {
            return maxScore;
        }
        return minScore;
    }
    
    private float calcScore(RegexValue thisAttr, StringMultiValue otherAttr) {
        for (String val: otherAttr.getValue()) {
            Matcher m = thisAttr.getValue().matcher(val);
            if (m.matches()) {
                return maxScore;
            }
        }
        return minScore;
    }
    
    private float calcScore(RegexConstraint bc, StringValue attr) {
        return maxScore;
    }    
    private float calcScore(RegexValue attr, StringConstraint bc) {
        return maxScore;
    }    
    private float calcScore(RegexConstraint bc, StringMultiValue attr) {
        return maxScore;
    }
    
}
