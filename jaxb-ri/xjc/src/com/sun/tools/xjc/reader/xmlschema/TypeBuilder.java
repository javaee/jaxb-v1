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
package com.sun.tools.xjc.reader.xmlschema;

import java.util.Iterator;

import javax.xml.bind.Element;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.ext.WildcardItem;
import com.sun.tools.xjc.grammar.xducer.NilTransducer;
import com.sun.tools.xjc.reader.Const;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;

/**
 * Builds an expression that has {@link TypeItem} as its top-level
 * binding expression.
 * 
 * <p>
 * There is at least one ugly code that you need to aware of
 * when you are modifying the code. See the documentation
 * about <a href="package.html#stref_cust">
 * "simple type customization at the point of reference."</a>
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TypeBuilder extends AbstractXSFunctionImpl implements BGMBuilder.ParticleHandler {
    TypeBuilder( BGMBuilder _builder ) {
        this.builder = _builder;
        this.pool = builder.pool;
    }
    
    /** Type-safe wrapper method. */
    public final Expression build( XSComponent sc ) {
        return (Expression)sc.apply(this);
    }
    
    private final BGMBuilder builder;
    private final ExpressionPool pool;




    public Object attGroupDecl(XSAttGroupDecl agd) {
        Expression exp = builder.selector.bindToType(agd);
        if(exp!=null)   return exp;
        
        // we will not aggregate a whole attribute group into one type.
        _assert(false);
        return null;
    }
    
    public Object attributeDecl(XSAttributeDecl decl) {
        return _attributeDecl(decl);
    }
    
    public AttributeExp _attributeDecl(XSAttributeDecl decl) {
// attribute decls can't map to a type object by itself
// otherwise we cannot handle value constraints specified on attribute uses.
//        Expression exp = builder.selector.bindToType(decl);
//        if(exp!=null)   return exp;

        // UGLY CODE WARNING:
        // set the referer so that the base type can find this attribute decl
        // if it's a simple type. See comments to the referer field of
        // SimpleTypeBuilder.
        // builder.simpleTypeBuilder.referer = decl;
        builder.simpleTypeBuilder.refererStack.push( decl );
        
        AttributeExp exp = (AttributeExp)pool.createAttribute(
            new SimpleNameClass(decl.getTargetNamespace(),decl.getName()),
            builder.simpleTypeBuilder.build(decl.getType()));
            
        builder.simpleTypeBuilder.refererStack.pop();
        
        return exp;
    }

    public Object attributeUse(XSAttributeUse use) {
        Expression exp = builder.selector.bindToType(use);
        if(exp!=null)   return exp;
        
        // attribute use will be always mapped to a property
        _assert(false);
        return null;
    }

    public Object complexType(XSComplexType type) {
        return builder.selector.bindToType(type);
    }

    public Object simpleType(XSSimpleType type) {
        Expression exp = builder.selector.bindToType(type);
        if(exp!=null)   return exp;
        
        return builder.simpleTypeBuilder.build(type);
    }

    public Object particle(XSParticle p) {
        Expression exp = builder.selector.bindToType(p);
        if(exp!=null)   return exp;
        
        return builder.processMinMax( build(p.getTerm()), p );
    }

    public Object empty(XSContentType empty) {
        Expression exp = builder.selector.bindToType(empty);
        if(exp!=null)   return exp;
        
        return Expression.epsilon;
    }

    public Object wildcard(XSWildcard wc) {
        Expression exp = builder.selector.bindToType(wc);
        if(exp!=null)   return exp;
        
        // choice of all global element declarations
        return new WildcardItem( builder.grammar.codeModel, wc );
    }

    public Object modelGroupDecl(XSModelGroupDecl decl) {
        Expression exp = builder.selector.bindToType(decl);
        if(exp!=null)   return exp;
        
        // push a new JClassFactory so that the new classes will be prefixed by
        // the model group name
        builder.selector.pushClassFactory(
            new PrefixedJClassFactoryImpl( builder, decl ) );
        
        exp = build(decl.getModelGroup());
        
        builder.selector.popClassFactory();
        
        return exp;
    }

    public Object modelGroup(XSModelGroup mg) {
        Expression exp = builder.selector.bindToType(mg);
        if(exp!=null)   return exp;
        
        return builder.applyRecursively( mg, this );
    }

    public Object elementDecl(XSElementDecl decl) {
        Expression exp = Expression.nullSet;
        
        for( Iterator itr=decl.getSubstitutables().iterator(); itr.hasNext(); ) {
            XSElementDecl e = (XSElementDecl)itr.next();
            if(e.isAbstract())  continue;
            exp = pool.createChoice( exp,
                    elementDeclWithoutSubstGroup(e) );
        }
        
        return exp;
    }
    
    /**
     * Bind an element decl to a class without considering
     * its substitutables.
     */
    private TypeItem elementDeclWithoutSubstGroup(XSElementDecl decl) {
        TypeItem ti = builder.selector.bindToType(decl);
        if(ti!=null)   return ti;
        
        JDefinedClass cls = builder.selector.getClassFactory().create(
            builder.getNameConverter().toClassName(decl.getName()),
            decl.getLocator() );
            
        // if an element declaration is mapped to a class,
        // implement the marker interface
        cls._implements(Element.class);
        
        ClassItem ci = builder.grammar.createClassItem(
            cls,Expression.epsilon,decl.getLocator());
        
        builder.selector.queueBuild(decl,ci);
        return ci;
    }

    
    /**
     * Binds an element declaration by assuming that the element itself
     * is not going to be mapped to a class.
     * 
     * This is a special case for an element reference.
     */
    protected ElementPattern elementDeclFlat(XSElementDecl decl) {
        Expression body;
        
        // if we build a complex type without building the element itself,
        // we will be in a big trouble.
        builder.selector.bindToType(decl);
        
        Expression type = builder.selector.bindToType(decl.getType());
        
        if(type!=null) {
            // simply make a property out of this element, and its type will
            // be the type of the schema type of this element.
            body = type;
        } else {
            // otherwise make a type out of the schema type and
            // wrap it.
            
            // UGLY CODE WARNING:
            // set the referer so that the base type can find this element type
            // if it's a simple type. See comments to the referer field of
            // SimpleTypeBuilder.
            // builder.simpleTypeBuilder.referer = decl;
            
            builder.simpleTypeBuilder.refererStack.push( decl );

            body = builder.typeBuilder.build(decl.getType());
            
            builder.simpleTypeBuilder.refererStack.pop();
        }
        
        if( decl.isNillable() )
            // add nilable transducer to materialize the nillable semantics
            body = pool.createChoice( buildXsiNilExpForProperty(), body );
        
        // TODO: there can be more than one anomaly.
        if( decl.getType().isComplexType()
        && builder.getGlobalBinding().isTypeSubstitutionSupportEnabled() )
            body = pool.createChoice(
                // The core type expression has to come before the substitutable
                // type list, otherwise you'll always get @xsi:type.
                body,
                builder.getTypeSubstitutionList(decl.getType().asComplexType(),false) );
        else
            body= pool.createSequence( body, builder.createXsiTypeExp(decl) );
        
        
        SimpleNameClass name =
            new SimpleNameClass(decl.getTargetNamespace(),decl.getName());
        
        return new ElementPattern(name,body);
    }
    
    /**
     * Builds a BGM type fragment for the xsi:nil attribute.
     * Returned expression shall be used to add xsi:nil support
     * for properties.
     */
    private Expression buildXsiNilExpForProperty() {
        // <attribute name="xsi:nil">
        //   <primitive xducer="NilTransducer">
        //     <value type="boolean">true</value>
        //   </primitive>
        // </attribute>
        return new AttributeExp(
            new SimpleNameClass(Const.XMLSchemaInstanceNSURI,"nil"),
            builder.grammar.createPrimitiveItem(
                new NilTransducer(builder.grammar.codeModel),
                StringType.theInstance,
                pool.createValue( BooleanType.theInstance, Boolean.TRUE ),
                null
            )
        );
    }
}
