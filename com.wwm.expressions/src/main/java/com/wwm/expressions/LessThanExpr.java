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
package com.wwm.expressions;

import com.wwm.db.core.exceptions.ArchException;

public class LessThanExpr extends RelExpr {

	private static final long serialVersionUID = 3690197624867074609L;

	public LessThanExpr(ComparableExpr left, ComparableExpr right) {
		super(left, right);
	}

	@Override
	public boolean evaluate(ExprContext context) throws ArchException {
		return left.evaluate(context).compareTo(right.evaluate(context)) < 0;
	}
}
