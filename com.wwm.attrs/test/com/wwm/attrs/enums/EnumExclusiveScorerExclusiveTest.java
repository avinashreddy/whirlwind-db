package com.wwm.attrs.enums;

import org.junit.Before;
import org.junit.Test;

import com.wwm.attrs.AttributeMapFactory;
import com.wwm.attrs.ItemScore;
import com.wwm.attrs.internal.ScoreConfiguration;
import com.wwm.db.whirlwind.SearchSpec.SearchMode;
import com.wwm.db.whirlwind.internal.IAttribute;
import com.wwm.db.whirlwind.internal.IAttributeMap;

import static org.junit.Assert.*;

public class EnumExclusiveScorerExclusiveTest {

//  private static final AttributeFactory attributeFactory = AttributeFactory.getInstance();
	protected int statusId = 1; 
	protected int statusWantId = 2; 

	private ScoreConfiguration scoreConfig = new ScoreConfiguration();
	
	protected EnumExclusiveValue wantNullStatus = new EnumExclusiveValue(statusWantId, (short) 0, EnumExclusiveValue.WANT_NULL_VALUE);
	protected EnumExclusiveValue statusIsOne = new EnumExclusiveValue(statusId, (short)0, (short) 1);
	
    @Before
	public void setUpConfig() throws Exception {
        
        // If we find a sexWant, score it against a sexHave

        EnumExclusiveScorerExclusive scorer = new EnumExclusiveScorerExclusive(statusWantId, statusId);
        scorer.setScoreNull(true); // we want null to only match null, and otherwise to score zero
		scoreConfig.add( scorer );

//        attributeFactory.setDecorator(sexHaveId, new BooleanDecorator( "sexHave", "m", "f" ) );
    }

	/**
	 * Test that a null entry scores 1 if it's null in other map too, and zero if there is an attribute
	 */
    @Test public void testWantNull() {
		
		IAttributeMap<IAttribute> search = AttributeMapFactory.newInstance(IAttribute.class);
		IAttributeMap<IAttribute> profile = AttributeMapFactory.newInstance(IAttribute.class);
			
		search.putAttr(wantNullStatus);	// looking for null
		// profile.putAttr( nowt !! );
		
		ItemScore score = new ItemScore();
		scoreConfig.scoreAllItemToItem(score, search, profile, SearchMode.Forwards);
		assertTrue(score.total() == 1.0f);


		profile.putAttr(statusIsOne);
		score = new ItemScore();
		scoreConfig.scoreAllItemToItem(score, search, profile, SearchMode.Forwards);
		assertTrue(score.total() == 0.0f);
    }


    @Test public void testGetMultiEnumScore() {
    	EnumExclusiveScorerExclusive s = new EnumExclusiveScorerExclusive(1, 1);
    	
    	assertEquals(1f, s.getMultiEnumScore(2, 2));
    	assertEquals(0.5f, s.getMultiEnumScore(1, 2));
    	assertEquals(0.75f, s.getMultiEnumScore(3, 4));
    	
    	s.setWeight(0.5f);
    }
}

