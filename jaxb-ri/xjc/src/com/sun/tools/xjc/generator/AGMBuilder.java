/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.xml.bind.GrammarImpl;

/**
 * Construct an AGM for the whole grammar by combining pieces from
 * the ClassItem.getAGM method.
 * 
 * <p>
 * The result is used to perform unmarshal time validation.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AGMBuilder extends ExpressionCloner
{
    /** from ClassItem to their AGMFragment wrapped in a ReferenceExp. */
    private final Map class2agm = new HashMap();
    
    /**
     * From each {@link ReferenceExp} in the original grammar
     * to its processed form {@link Expression}.
     */
    private final Map ref2exp = new HashMap();
    
    /**
     * From each {@link ElementExp} in the original grammar
     * to its processed form {@link ElementExp}.
     */
    private final Map elem2exp = new HashMap();
    
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
    
    /**
     * Plug objects that will be associated with the grammar.
     */
    private final ArrayList plugs = new ArrayList();
    
    private final GrammarImpl grammar = new GrammarImpl(new ExpressionPool());
    
    public static Grammar remove( AnnotatedGrammar src ) {
        AGMBuilder builder = new AGMBuilder(src);
        
        builder.grammar.setTopLevel(src.getTopLevel().visit(builder));
        builder.grammar.setPlugs( (GrammarImpl.Plug[]) builder.plugs.toArray(new GrammarImpl.Plug[0]) );
        
        return builder.grammar;
    }
    
    
    private AGMBuilder( AnnotatedGrammar grammar ) {
        super(new ExpressionPool());
        
        // build anyContent
        anyContent = new ReferenceExp("anyContent");
        anyContent.exp = pool.createZeroOrMore(
            pool.createChoice(
                new ElementPattern(NameClass.ALL,anyContent),
                pool.createChoice(
                    pool.createAttribute(NameClass.ALL),
                    Expression.anyString)));
        
        // build initial forest
        ClassItem[] ci = grammar.getClasses();
        for( int i=0; i<ci.length; i++ ) {
            if( ci[i].agm.exp==null )   ci[i].agm.exp=ci[i].exp; // UGLY
            class2agm.put( ci[i], new ReferenceExp(null,ci[i].agm) );
        }
                
        // connect those forests to each other
        for( int i=0; i<ci.length; i++ ) {
            ReferenceExp e = (ReferenceExp)class2agm.get(ci[i]);
             e.exp = e.exp.visit(this);
        }
    }
    
    public Expression onRef( ReferenceExp exp ) {
        Expression e = (Expression)ref2exp.get(exp);
        if( e!=null )   return e;

        e = exp.exp.visit(this);
        ref2exp.put(exp,e);
        
        return e;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( exp instanceof ExternalItem ) {
            Expression e = ((ExternalItem)exp).createAGM(pool);
            if( e instanceof GrammarImpl.Plug )
                plugs.add(e);
            return e;
        }
        if( exp instanceof ClassItem ) {
            return (Expression)class2agm.get(exp);
        }
        // ignore all OtherExps
        return exp.exp.visit(this);
    }
    
    public Expression onAttribute( AttributeExp exp ) {
        return pool.createAttribute(exp.nameClass,exp.exp.visit(this));
    }
    
    public Expression onElement( ElementExp exp ) {
        ElementExp result = (ElementExp)elem2exp.get(exp);
        if(result==null) {
            result = grammar.createElement( exp.getNameClass(), Expression.nullSet );
            elem2exp.put(exp,result);
            result.contentModel = exp.getContentModel().visit(this);
            result.ignoreUndeclaredAttributes = exp.ignoreUndeclaredAttributes;
        }
        return result;
    }
}
