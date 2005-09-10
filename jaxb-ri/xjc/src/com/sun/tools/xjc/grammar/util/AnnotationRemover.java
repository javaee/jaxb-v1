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

import java.util.Map;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.msv.grammar.trex.TREXGrammar;
import com.sun.tools.xjc.grammar.ExternalItem;

/** Removes binding annotation and returns a pure AGM. */
public class AnnotationRemover extends ExpressionCloner
{    
    /** Set of ReferenceExps. */
    private final Map bookmarks = new java.util.HashMap();

    /** Used to unify element expressions. */
    private final Map elements = new java.util.HashMap();
    
    /**
     * <pre><xmp>
     * <define name="anyContent">
     *   <zeroOrMore>
     *     <choice>
     *       <element>
     *         <anyName/>
     *         <ref name="anyContent"/>
     *       </element>
     *       <attribute>
     *         <anyName/>
     *         <text/>
     *       </anyName>
     *       <text/>
     *     </choice>
     *   </zeroOrMore>
     * </define>
     */
    private final ReferenceExp anyContent;
    
    public static Grammar remove( Grammar src ) {
        // use a fresh pool
        ExpressionPool newPool = new ExpressionPool();
        
        Expression newTop = src.getTopLevel().visit(new AnnotationRemover(newPool));
        
        // use TREXGrammar as an implementation
        // anything will do, but we have to use a class which
        // is available at run-time, because the purpose here is
        // to serialize the grammar
        TREXGrammar grammar = new TREXGrammar(newPool);
        grammar.exp = newTop;
        
        return grammar;
    }

    public static Expression remove( Expression exp, ExpressionPool pool ) {
        return exp.visit(new AnnotationRemover(pool));
    }
    
    private AnnotationRemover( ExpressionPool pool ) {
        super(pool);
        anyContent = new ReferenceExp("anyContent");
        anyContent.exp = pool.createZeroOrMore(
            pool.createChoice(
                new ElementPattern(NameClass.ALL,anyContent),
                pool.createChoice(
                    pool.createAttribute(NameClass.ALL),
                    Expression.anyString)));
        
    }
    
    public Expression onRef( ReferenceExp exp ) {
        if(!bookmarks.containsKey(exp))
            // we don't care about this expression
            return exp.exp.visit(this);
        
        // otherwise we need to keep a reference to this object.
        ReferenceExp target = (ReferenceExp)bookmarks.get(exp);
        if(target==null) {
            // first time to visit this ReferenceExp.
            target = new ReferenceExp(exp.name);
            target.exp = exp.exp.visit(this);
            
            bookmarks.put(exp,target);
        }
        
        return target;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( exp instanceof ExternalItem ) {
            // replace it by the corresponding AGM fragment,
            // which should most accurately describe it.
            return ((ExternalItem)exp).createAGM(pool);
        }
        // ignore all OtherExps
        return exp.exp.visit(this);
    }
    
    public Expression onAttribute( AttributeExp exp ) {
        return pool.createAttribute(exp.nameClass,exp.exp.visit(this));
    }
    
    public Expression onElement( ElementExp exp ) {
        ElementExp result = (ElementExp)elements.get(exp);
        if(result!=null)    return result;
        
        result = new ElementPattern( exp.getNameClass(), Expression.nullSet );
        elements.put(exp,result);
        // register it to the map before visiting the
        // content model.
        result.contentModel = exp.contentModel.visit(this);
        result.ignoreUndeclaredAttributes = exp.ignoreUndeclaredAttributes;
        
        return result;
    }
}
