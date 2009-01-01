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

import java.io.Serializable;

import com.wwm.attrs.Score.Direction;
import com.wwm.attrs.internal.GlobalDecorators;
import com.wwm.attrs.internal.IConstraintMap;
import com.wwm.db.whirlwind.internal.IAttribute;
import com.wwm.db.whirlwind.internal.IAttributeConstraint;
import com.wwm.db.whirlwind.internal.IAttributeMap;




/**
 * FIXME:
 * Changes to do:
 * 	- Scorer should have own signature/id against which it is entered into the score table.
 * 		This allows there to be multiple attributes scoring the same attributes, and also allows single
 *      value scorers to work.
 * @author Neale
 */
public abstract class Scorer implements Serializable {

    private static final long serialVersionUID = 3356377787369878175L;

    /** Name by which to describe the scorer in results */
    private String name;
    
    protected int scorerAttrId;


    /** Maximum score */
    static protected final float maxScore = 1.0f;

    /**
     * Minimum score - configurable, as might want to have
     * scorer that is 1.0 for match and 0.5 for no match
     */
    protected float minScore = 0.0f;


    // Properties
    private Score.Direction noScoreDirection; // Null == TwoWay search
    private boolean filter = false;
    private float weight = 1.0f; // FIXME: Find way of ensuring this is set by XStream.  Currently default c'tor doesn't get called.. and this ends up as zero.
    // Workaround is that we check for weight==0 (which is nonsense) and if so, set it to 1.0. 
    
    private boolean scoreNull;
    private float scoreOnNull;

    public Scorer( int scoreAttrId ) {
        assert scoreAttrId != 0;
        this.scorerAttrId = scoreAttrId;
    }


    final public int getScorerAttrId() {
        return scorerAttrId;
    }



    /**
     * Checks if this scorer allows this score direction.
     * @param direction Direction of the search
     * @return
     */
    final public boolean getCanScore(Score.Direction direction) {
        return direction != noScoreDirection;
    }
    final public Score.Direction getNoScoreDirection() {
        return noScoreDirection;
    }
    final public void setNoScoreDirection(Score.Direction noScoreDirection) {
        this.noScoreDirection = noScoreDirection;
    }
    final public boolean isFilter() {
        return filter;
    }
    public void setFilter(boolean filter) {
        this.filter = filter;
    }
    final public float getWeight() {
    	if (weight == 0.0f) {
    		weight = 1.0f;	// workaround to sort out problem that XStream doesn't call default constructors.. somehow!
    	}
        return weight;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }

    final public boolean isScoreNull() {
        return scoreNull;
    }

    /**
     * Set true if we do want to score if the searchAttribute is null.  This allows us two options:
     * to allow a middling "don't know" score for something like gender, so if it is unspecified, then
     * both male and female score, say 50% (see {@link Scorer.setScoreOnNull} ), but otherwise
     * maxScore or minScore if the search (i.e. wanted attribute) is present.
     * 
     * For Enums, this also allows some optimisation by allowing 'null' to be shorthand for a commonly
     * used default or majority attribute. e.g. it would be sensible to encode sexuality=heterosexual
     * using null if your website were for the general population.
     * In this case, null matching null scores 100%.  See EnumExclusiveScorerExclusive for more details.
     * @param scoreNull
     */
    public void setScoreNull(boolean scoreNull) {
        this.scoreNull = scoreNull;
    }

    final public float getScoreOnNull() {
        return scoreOnNull;
    }

    public void setScoreOnNull(float scoreOnNull) {
        this.scoreOnNull = scoreOnNull;
    }


    /**
     * Scores a search spec item to an index item container, or a index item to a score container. Two cases.
     * NOTE: This is called twice with d = both Direction.forwards & reverse
     */
    abstract public void scoreItemToItem(Score score, Direction d, IAttributeMap<IAttribute> otherAttrs, IAttributeMap<IAttribute> scoreAttrs);
    /*
        MIGRATION CODE: Remove na from method params, and insert following at top of method

        IAttribute attr = scoreAttrs.findAttr(scorerAttrId);
		if (attr == null) {
			return; // If we do not have the scorer attr present in the search direction, we do not score - it wasn't 'wanted'
		}
     */

    /**
     * FIXME: Make these abstract and migrate all the scorers
     * Scores a search spec item to a node container, one case only
     * NOTE: d is always Direction.forwards in this case.
     * The item typically being scored is the constraint, itemAttrs(scoreAttrId) to nodeAttrs(otherAttrId)
     */
    abstract public void scoreSearchToNode(Score score, Direction d, IConstraintMap otherAttrs, IAttributeMap<? extends IAttribute> scoreAttrs);
    /*
        MIGRATION CODE: Remove na from method params, and insert following at top of method

        IAttribute attr = scoreAttrs.findAttr(scorerAttrId);
		if (attr == null) {
			return; // If we do not have the scorer attr present in the search direction, we do not score - it wasn't 'wanted'
		}
     */

    /**
     * Scores a Node item to a search spec container, one case only<br>
     * NOTE: d is always Direction.reverse in this case.<br>
     * The item typically being scored is constraints(scoreAttrId) to itemAttrs(otherAttrId)
     */
    abstract public void scoreNodeToSearch(Score score, Direction d, IAttributeMap<IAttributeConstraint> constraints, IAttributeMap<IAttribute> searchAttrs);
    /*
        MIGRATION CODE: Remove na from method params, and insert following at top of method

    	IAttributeConstraint na = (IAttributeConstraint)nodeConstraints.findAttr(scorerAttrId);
		if (na == null) {
			return; // If we do not have the scorer attr present in the search direction, we do not score - it wasn't 'wanted'
		}
     */

    /** Get description of this scorer - for use in results table */
    public String getName() {
		return name;
	}
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + GlobalDecorators.getInstance().getAttrName(this.scorerAttrId) + ": noScoreDir = " + this.noScoreDirection 
        + ": name = " + name;
    }
}
