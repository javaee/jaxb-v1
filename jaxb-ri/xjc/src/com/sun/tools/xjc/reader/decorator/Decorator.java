/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.decorator;

import com.sun.msv.grammar.Expression;
import com.sun.msv.reader.State;

/**
 * Represents an object which annotates the specified expression.
 * 
 * <p.
 * It typically does so according to some binding specification
 * (be it the default binding, or explicitly specified bindings.)
 * 
 * @author  Kohsuke Kawaguchi
 */
public interface Decorator
{
    /**
     * Modifies a given expression (according to whatever rule
     * this object has.)
     * 
     * @param   state
     *      The state object which creates the expression
     * @param   exp
     *      The expression to be modified
     */
    Expression decorate( State state, Expression exp );
}
