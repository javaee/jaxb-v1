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
package com.sun.tools.xjc.reader.annotator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.relaxng.datatype.Datatype;

import com.sun.codemodel.JPackage;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.DataOrValueExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.EnumerationXducer;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.util.CodeModelClassFactory;

/**
 * Adds PrimitiveItems to an AGM.
 * 
 * <ul>
 *  <li>
 *   replace Expression.anyString by &lt;data type="string"/>
 * 
 *  <li>
 *   wrap DataExps by PrimitiveItems.
 * 
 *  <li>
 *   expand list and enumeration in XSDatatype to ListExp and
 *   ValueExps wrapped by ChoiceExp, respectively.
 * </ul>
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
class PrimitiveTypeAnnotator extends ExpressionCloner {
    
    /**
     * @param _grammar
     *        The target grammar object which we are processing.
     */
    PrimitiveTypeAnnotator( AnnotatedGrammar _grammar,AnnotatorController _controller ) {
        super(_grammar.getPool());
        this.grammar = _grammar;
        this.controller = _controller;
        this.classFactory = new CodeModelClassFactory(controller.getErrorReceiver());
        
        currentPackage = _grammar.codeModel._package("");
    }
    
    /**
     * Target grammar object which we are processing.
     */
    private final AnnotatedGrammar grammar;
    
    private final AnnotatorController controller;
    
    private final CodeModelClassFactory classFactory;
    
    /**
     * processed Expressions. used to prevent infinite recursion.
     */
    private final Set visitedExps = new java.util.HashSet();
    
    /**
     * a map from TypedStringExp to the PrimitiveItem which wraps it.
     * used to unify PrimitiveItems.
     */
    private final Map primitiveItems = new java.util.HashMap();
    
    /**
     * The JPackage object that is associated to the currently visited
     * Expression.
     */
    private JPackage currentPackage;
    
    public Expression onRef( ReferenceExp exp ) {
        JPackage oldPackage = currentPackage;
        if( controller.getPackageTracker().get(exp)!=null )
            currentPackage = controller.getPackageTracker().get(exp);
        
        if( visitedExps.add(exp) ) {
            Expression e = processEnumeration(exp.name,exp.exp);
            if(e==null)    e = exp.exp.visit(this);
            exp.exp = e;
        }
        
        currentPackage = oldPackage;
        return exp;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( exp instanceof PrimitiveItem )
            return exp; // no need to annotate inside
        if( exp instanceof IgnoreItem )
            return exp; // we won't care about this.
        
        if( visitedExps.add(exp) ) {
            String name = null;
            if( exp instanceof ClassItem )
                name = ((ClassItem)exp).name;
            if( exp instanceof ClassCandidateItem )
                name = ((ClassCandidateItem)exp).name;
            
            Expression e = null;
            if( name!=null )
                e = processEnumeration(name,exp.exp);
            if( e==null )
                e = exp.exp.visit(this);
                
            exp.exp = e;
        }
        return exp;
    }
    
    public Expression onElement(ElementExp exp) {
        if (visitedExps.add(exp)) {
            Expression e = processEnumeration(exp);
            if (e == null)
                e = exp.contentModel.visit(this);
            exp.contentModel = e;
        }
        return exp;
    }

    public Expression onAttribute(AttributeExp exp) {
        if (visitedExps.contains(exp))
            return exp;

        Expression e = processEnumeration(exp);
        if (e == null)
            e = exp.exp.visit(this);

        e = pool.createAttribute(exp.nameClass, e);
        visitedExps.add(e);
        return e;
    }
    
    
    /**
     * processes an enumeration by using element/attribute name as the name.
     */
    public Expression processEnumeration(NameClassAndExpression exp) {
        NameClass nc = exp.getNameClass();
        if (!(nc instanceof SimpleNameClass))
            return null; // unable to get the name.
        
        return processEnumeration(
        // add suffix to make the meaning clear and avoid name conflicts.
         ((SimpleNameClass)nc).localName + "Type", exp.getContentModel());
    }
    
    /**
     * checks choice of ValueExps and wrap it by
     * EnumerationXducer.
     * 
     * @param    className
     *        name of the generated enumeration class.
     * @param    exp
     *        The expression to be tested.
     * @return
     *        null if the given expression is not a choice of ValueExps.
     *        If it is in fact a choice of ValueExps, then
     *        the expression wrapped by a PrimitiveItem will be returned.
     */
    public Expression processEnumeration( String className, final Expression exp ) {
        if( className==null)
            // name is not provided
            return null;
        
        // expand the expression until we hit child element,attribute, or
        // other JavaItems (OtherExps).
        //
        // because there can be some unused 
        Expression e = exp.visit( new ExpressionCloner(pool) {
            public Expression onAttribute( AttributeExp exp ) { return exp; }
            public Expression onElement( ElementExp exp )     { return exp; }
            public Expression onOther( OtherExp exp )         { return exp; }
            public Expression onRef( ReferenceExp exp ) {
                // expand anonymous refs.
                // if this is a named ReferenceExp, we'll process this later
                // independently. So we shouldn't expand this.
                // see the onRef method.
                if(exp.name==null)
                    return exp.exp.visit(this);
                else
                    return exp;
            }
        });

        if (!(e instanceof ChoiceExp))
            // this expression expands to something other than a choice.
            return null;

        ChoiceExp cexp = (ChoiceExp)e;
        Expression[] children = cexp.getChildren();
        for (int i = 0; i < children.length; i++)
            if (!(children[i] instanceof ValueExp))
                // this choice contains something other then values.
                return null;

        // this expression is a choice of values.

        // find an unique name
        String decoratedClassName;
        int cnt=1;
        
        do {
            decoratedClassName = controller.getNameConverter().toClassName(className)
                + (cnt++==1?"":String.valueOf(cnt));
        } while( currentPackage._getClass(decoratedClassName)!=null );
        
        // wrap it by EnumerationXducer.
        PrimitiveItem p = grammar.createPrimitiveItem(
            new EnumerationXducer(
                controller.getNameConverter(),
                classFactory.createClass(
                    currentPackage, decoratedClassName, null /*TODO: location support*/ ),
                cexp, new HashMap(), null/*TODO:location support*/),
            StringType.theInstance, // no guard
            cexp, null);
        primitiveItems.put(exp, p);
        return p;
    }
    
    
    
    
    
    
    public Expression onData( DataExp exp ) { return onDataOrValue(exp); }
    public Expression onValue( ValueExp exp ) { return onDataOrValue(exp); }
    
    /** Wraps naked DataExp/ValueExp with PrimitiveItems. */
    private Expression onDataOrValue( DataOrValueExp exp ) {
            
        if( primitiveItems.containsKey(exp) )
            // if this exp is already wrapped, use it instead of creating another one.
            // this will reduce the size of the LL grammar for data-binding.
            return (Expression)primitiveItems.get(exp);
        else {
            // if this is the first time, wrap it and memorize it.
            Datatype dt = exp.getType();
            XSDatatype guard;
            
            Transducer xducer;
            
            if(dt instanceof XSDatatype) {
                xducer = BuiltinDatatypeTransducerFactory.get(
                    grammar,(XSDatatype)dt);
                guard = (XSDatatype)dt;
            } else {
                // otherwise, use the identity transducer
                xducer = new IdentityTransducer(grammar.codeModel);
                guard = StringType.theInstance; // no guard
            }
            
            PrimitiveItem p = grammar.createPrimitiveItem(xducer,guard,(Expression)exp,null);
            primitiveItems.put( exp, p );
            return p;
        }
    }
}
