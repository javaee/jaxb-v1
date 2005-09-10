/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
