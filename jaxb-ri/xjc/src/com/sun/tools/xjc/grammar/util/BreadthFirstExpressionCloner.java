/*
 * @(#)$Id: BreadthFirstExpressionCloner.java,v 1.2 2005-09-10 18:20:19 kohsuke Exp $
 */

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
package com.sun.tools.xjc.grammar.util;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * A modified version of {@link ExpressionCloner}.
 *   
 * <p>
 * {@link ExpressionCloner} works in the depth-first fashion,
 * which is fast but requires a large stack when compiling
 * a large schema.
 * 
 * <p>
 * This modified version works for expression rewriters that
 * doesn't touch {@link ReferenceExp}, {@link OtherExp},
 * and {@link ElementExp}.
 * It splits the whole expression tree into a series of sub-trees
 * rooted at those three kinds of {@link Expression]s,
 * then process them one by one.
 * 
 * <p>
 * In this way, at most one sub-tree worth of the stack is used,
 * so it's much less stack intensive.  
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class BreadthFirstExpressionCloner extends ExpressionCloner {
    
    /**
     * Expressions that
     * are either already processed or in the processing queue. 
     */
    private final Set visitedExps = new HashSet();
    
    /**
     * Expressions that needs to be processed. 
     */
    private final Stack queue = new Stack();
    
    /**
     * If the expression is visited from the {@link #processQueue()} method,
     * this flag is set to true to invoke {@link #processQueue()} multiple times.
     */
    private boolean inLoop = false;
    
    protected BreadthFirstExpressionCloner(ExpressionPool pool) {
        super(pool);
    }

    public final Expression onElement(ElementExp exp) {
        if( visitedExps.add(exp) ) {
            queue.push(exp);
            processQueue();
        }
        return exp;
    }

    public final Expression onRef(ReferenceExp exp) {
        if( visitedExps.add(exp) ) {
            queue.push(exp);
            processQueue();
        }
        return exp;
    }
    
    public final Expression onOther( OtherExp exp ) {
        if( visitedExps.add(exp) ) {
            queue.push(exp);
            processQueue();
        }
        return exp;
    }
    
    public final Expression onAttribute( AttributeExp exp ) {
        // this one we cannot put in the queue since attributes are immutable.
        // but we can cehck if the attribute is already in the rewritten form
        // and if so, we can skip processing it.
        if( visitedExps.contains(exp) )    return exp;
        
        Expression e = pool.createAttribute( exp.nameClass, exp.exp.visit(this) );
        visitedExps.add(e);
        return e;
    }
    

    private void processQueue() {
        if( inLoop )
            return;
        
        inLoop = true;
        
        // process work items in the queue.
        while(!queue.isEmpty()) {
            Expression e = (Expression)queue.pop();
            if( e instanceof ElementExp ) {
                ElementExp ee = (ElementExp)e;
                ee.contentModel = ee.contentModel.visit(this);
            } else
            if( e instanceof ReferenceExp ) {
                ReferenceExp re = (ReferenceExp)e;
                re.exp = re.exp.visit(this);
            } else
            if( e instanceof OtherExp ) {
                OtherExp oe = (OtherExp)e;
                oe.exp = oe.exp.visit(this);
            } else
                throw new JAXBAssertionError();
        }
        
        inLoop = false;
    }
}
