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
package com.wwm.attrs.location;


import com.wwm.attrs.Score;
import com.wwm.attrs.Score.Direction;
import com.wwm.attrs.dimensions.DimensionsRangeConstraint;
import com.wwm.attrs.internal.IConstraintMap;
import com.wwm.attrs.internal.TwoAttrScorer;
import com.wwm.attrs.simple.FloatConstraint;
import com.wwm.attrs.simple.FloatHave;
import com.wwm.db.whirlwind.internal.IAttribute;
import com.wwm.db.whirlwind.internal.IAttributeConstraint;
import com.wwm.db.whirlwind.internal.IAttributeMap;
import com.wwm.util.ScoreMapper;


/**
 * FIXME: Write unit tests for this (should be directly equiv of RangePreference tests)
 * FIXME: This needs reviewing cf RangePreference, as splitters might work differently.
 * 
 * Different approach to scoring, based on having <code>EcefVector location</code>
 * and <code>FloatHave prefDistance</code> attributes in the search, to score against
 * the EcefVector in "other".
 * @author Neale
 */
public class LocationAndRangeScorer extends TwoAttrScorer {

    private static final long serialVersionUID = 8753919170439779617L;

    private static final boolean preferClose = true; // hard coded for now.

    protected int scorerRangeAttrId;
    protected ScoreMapper scoreMapper;


    public LocationAndRangeScorer(int scoreAttrId, int scoreRangeAttrId, int otherAttrId, ScoreMapper scoreMapper) {
        super(scoreAttrId, otherAttrId);
        this.scorerRangeAttrId = scoreRangeAttrId;
        this.scoreMapper = scoreMapper;
    }

    /**
     * Scores {EcefVector location,float wantRange, this.preferClose) against a range of EcefVectors
     * 
     */
    @Override
    public void scoreSearchToNode(Score score, Score.Direction d, IConstraintMap c, IAttributeMap<? extends IAttribute> scoreAttrs) {
        assert( d == Direction.forwards ); // Always the case

        IAttribute scoreLocAttr = scoreAttrs.findAttr(scorerAttrId);
        IAttribute rangeAttr = scoreAttrs.findAttr(scorerRangeAttrId);
        if (scoreLocAttr == null || rangeAttr == null) {
            return; // If we do not have the scorer attrs present in the search direction, we do not score - it wasn't 'wanted'
        }

        // If non-null then should pass this lot
        assert( scoreLocAttr.getAttrId() == scorerAttrId );
        assert( scoreLocAttr instanceof EcefVector );
        assert( rangeAttr.getAttrId() == scorerRangeAttrId );
        assert( rangeAttr instanceof FloatHave );


        // Find constraint in node
        IAttributeConstraint nodeConstraint = c.findAttr(otherAttrId);

        // If no constraint is found, then we return without having scored
        if (nodeConstraint == null) {
            return;
        }

        // If constraint is found, but within it, there are some unspecified, then we score 1.  TODO: Review whether this should be "scoreOnNull" etc
        if (nodeConstraint.isIncludesNotSpecified()) {
            score.add(this, 1.0f, d);
            return;
        }

        EcefVector want = (EcefVector)scoreLocAttr;

        DimensionsRangeConstraint vectorConstraint = (DimensionsRangeConstraint) nodeConstraint;

        // if location is in box, there could be an exact match, so return 1.0f
        if (vectorConstraint.consistent(want)) {
            score.add(this, 1.0f, d);
            return;
        }

        // Else score based on distance from closest point of box, to the centre of this RangePreference
        float distance = vectorConstraint.getDistance( want ); // dist from

        assert(distance >= 0); // not consistent means that it's outside the box (>= as could round to 0)

        float prefDistance = ((FloatHave) rangeAttr).getValue();
        scoreDistance(score, d, prefDistance, distance, preferClose);
        return;
    }


    /**
     * Score a {range of Location,range of prefRange} against an EcefVector
     * @see com.wwm.attrs.Scorer#scoreNodeToSearch(com.wwm.attrs.Score, com.wwm.attrs.Score.Direction, com.wwm.db.core.whirlwind.internal.IAttributeMap, com.wwm.db.core.whirlwind.internal.IAttributeMap)
     */
    @Override
    public void scoreNodeToSearch(Score score, Score.Direction d, IAttributeMap<IAttributeConstraint> constraints, IAttributeMap<IAttribute> searchAttrs) {
        assert( d == Direction.reverse ); // Always the case
        DimensionsRangeConstraint want = (DimensionsRangeConstraint)constraints.findAttr(scorerAttrId); // min/man of a 3D (x,y,z) want
        FloatConstraint wantDistance = (FloatConstraint)constraints.findAttr(scorerRangeAttrId); // min/max of Float
        if (want == null || wantDistance == null) {
            return; // If we do not have the scorer attr present in the search direction, we do not score - it wasn't 'wanted'
        }
        if (want.isIncludesNotSpecified() || wantDistance.isIncludesNotSpecified() ) {
            return; // at least one item has a null, so would return without scoring in scoreItemToItem()
        }
        assert( want.getAttrId() == scorerAttrId );
        assert( wantDistance.getAttrId() == scorerRangeAttrId );

        // scoring from a node to a search
        // Node attrs are min/max of EcefVector (x, y, z) and min/max of range
        // Attr is single point
        // Result is either: point is within possible ranges or not.
        // As range preferences may or may not be 'preferCloser' then
        // this falls back to false, so scoreFactor is either 1.0 or 0.0 within
        // the range.
        // Outside of the range, something better is possible, based on the
        // largest value of 'range' within the node, and the closest point.

        EcefVector location = (EcefVector) searchAttrs.findAttr(otherAttrId);
        if (location == null) {
            return;
        }

        //		Dimensions low = new Dimensions( want.getMin() );
        //        Dimensions hi = new Dimensions( want.getMax() );

        float scoreFactor;
        //    	DimensionsRangeConstraint box = new DimensionsRangeConstraint(0, low, hi);
        if (/*box dunno why it was box*/want.consistent(location)) {
            // location is within range of x,y,z's of want, so could be zero miles
            scoreFactor = 1.0f;
        }
        else {
            // see if it is within max want.range of closest point of box

            float distance = /*was box*/want.getDistance( location ); // dist from
            float maxWantRange = EcefVector.ecefToMiles(wantDistance.getMax()); // largest range in this node

            if (distance < maxWantRange) {
                scoreFactor = 1.0f;
            }
            else { // This did just return zero (and will for linear ScoreMapper)
                // but now returns highest score factor based on distance outside
                scoreFactor = 1.0f - distance / maxWantRange;
                assert( scoreFactor <= 0.0f );
            }
        }
        float convertedScore = scoreMapper.getScore(scoreFactor);
        // scale down for larger ranges.  Highest score will be for smallest range
        float minWantRange = EcefVector.ecefToMiles(wantDistance.getMin());
        convertedScore *= rangePenaltyFactor(minWantRange);
        score.add(this, convertedScore, d);
    }

    @Override
    public void scoreItemToItem(Score score, Score.Direction d, IAttributeMap<IAttribute> otherAttrs, IAttributeMap<IAttribute> scoreAttrs) {
        IAttribute scoreLocAttr = scoreAttrs.findAttr(scorerAttrId);
        IAttribute rangeAttr = scoreAttrs.findAttr(scorerRangeAttrId);
        if (scoreLocAttr == null || rangeAttr == null) {
            return; // If we do not have the scorer attrs present in the search direction, we do not score - it wasn't 'wanted'
        }
        assert( scoreLocAttr.getAttrId() == scorerAttrId );
        assert( scoreLocAttr instanceof EcefVector );
        assert( rangeAttr.getAttrId() == scorerRangeAttrId );
        assert( rangeAttr instanceof FloatHave );

        // We must be scoring a search to an item or an Item to a search
        EcefVector want = (EcefVector)scoreLocAttr;
        EcefVector location = (EcefVector) otherAttrs.findAttr(otherAttrId);
        if (location == null) {
            return;
        }

        float distance = location.distance(want);	// actual distance in miles
        float prefDistance = ((FloatHave) rangeAttr).getValue();
        scoreDistance(score, d, prefDistance, distance, preferClose);
        score.setScorerAttribute("Distance", distance );
    }

    /**
     * Calculate score for supplied distance for the given location preference.
     * @param score
     * @param d
     * @param locationPref
     * @param distance
     */
    private void scoreDistance(Score score, Score.Direction d, float prefDistance, float distance, boolean preferClose) {
        // Find out how far inside or outside the preferred range location is
        // 1 down to 0 is within range, and negative is outside
        float scoreFactor = 1f - (distance / prefDistance);

        // If preferClose is false, score any value within range as 1
        if (!preferClose && scoreFactor >= 0f ) {
            scoreFactor = 1f;
        }

        float convertedScore = scoreMapper.getScore( scoreFactor );
        // scale down for larger ranges.  Highest score will be for smallest range
        convertedScore *= rangePenaltyFactor(prefDistance);
        score.add(this, convertedScore, d);
    }

    /**
     * Penalise higher ranges to give more selective low range nodes a chance.
     * @param range
     * @return
     */
    private float rangePenaltyFactor(float range) {
        return 1f / ( 1f + range / 1000f);
    }


    public ScoreMapper getScoreMapper() {
        return scoreMapper;
    }

    public void setScoreMapper(ScoreMapper scoreMapper) {
        this.scoreMapper = scoreMapper;
    }



}
