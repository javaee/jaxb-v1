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
package com.sun.tools.xjc.reader.xmlschema.cs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.ext.WildcardItem;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.xmlschema.AbstractXSFunctionImpl;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.xsom.XSAttContainer;
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
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;

/**
 * Builds the exact AGM representation of a ClassItem
 * (that contains child ClassItems as terminal symbols.)
 * 
 * <p>
 * To find terminal ClassItems, this builder needs to call
 * the <code>builder.selector.select</code> method for
 * every schema component it encounters.
 * 
 * Then if it's not bound to a class, it builds an expression.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class AGMFragmentBuilder extends AbstractXSFunctionImpl {
    AGMFragmentBuilder( BGMBuilder builder ) {
        this.builder = builder;
        this.pool = builder.pool;
    }
    
    private final BGMBuilder builder;
    private final ExpressionPool pool;
    private XSComponent root;
    private ClassItem superClass;

    /** External entry point. */
    public Expression build( XSComponent sc, ClassItem owner ) {
        this.superClass = findSuperClass(owner);
        this.root = sc;
        return (Expression)sc.apply(this);
    }









    public Object attGroupDecl(XSAttGroupDecl decl) {
        return attributeContainer(decl);
    }
    
    private Expression attributeContainer( XSAttContainer cont ) {
        Expression exp = Expression.epsilon;

        for( Iterator itr = cont.iterateAttributeUses(); itr.hasNext(); ) {
            exp = pool.createSequence( exp, recurse((XSAttributeUse)itr.next()) );
        }
        
        return exp;
    }



    public Object attributeDecl(XSAttributeDecl decl) {
        return attribute( decl, decl.getFixedValue(), decl.getContext() );
    }
    
    private Expression attribute( XSAttributeDecl decl, String fixedValue, ValidationContext context ) {
        SimpleNameClass name = new SimpleNameClass(decl.getTargetNamespace(),decl.getName());
        Datatype dt = builder.simpleTypeBuilder.datatypeBuilder.build(decl.getType());
        
        if(fixedValue!=null) {
            Object value = dt.createValue( fixedValue, context );
            return pool.createAttribute(name, pool.createValue(dt,null,value));
        } else
            return pool.createAttribute( name, pool.createData(dt,null) );
    }

    public Object attributeUse(XSAttributeUse use) {
        Expression e = attribute( use.getDecl(), use.getFixedValue(), use.getContext() );
        if(use.isRequired())    return e;
        else                    return pool.createOptional(e);
    }

    public Object complexType(XSComplexType type) {
        XSContentType content = type.getContentType();
        Expression body = recurse(content);
                
        if( type.isMixed() )
            body = pool.createMixed(body);
        body = pool.createSequence(body,attributeContainer(type));
        return body;
    }

    public Object empty(XSContentType empty) {
        return Expression.epsilon;
    }

    public Object particle(XSParticle particle) {
        XSTerm t = particle.getTerm();
        
        Expression exp;
        if( builder.particlesWithGlobalElementSkip.contains(particle) ) {
            XSElementDecl e = t.asElementDecl();

            if( e.isAbstract() ) {
                exp = Expression.nullSet;  // not allowed
            } else {
                ElementPattern ep = _elementDecl(e);
                if( e.getType().isComplexType() ) {
                    // UGLY CODE
                    // if the element is skipped, connect directly to the type
                    // and wrap it by a skippable ElementExp instead of a regular
                    // ElementExp.
                    // (<foo>FooType</foo>)|Foo)
                    exp = pool.createChoice( builder.selector.bindToType(e), ep );
                } else
                    exp = ep;
            }
        } else
            exp = recurse(t);
                
        // include substitution groups if applicable
        if( t.isElementDecl() )
            exp = pool.createChoice( exp, builder.getSubstitionGroupList(t.asElementDecl()) );
        
        return builder.processMinMax(exp,particle);
    }

    public Object simpleType(XSSimpleType simpleType) {
        return pool.createData(
            builder.simpleTypeBuilder.datatypeBuilder.build(simpleType));
    }

    public Object elementDecl(XSElementDecl decl) {
        if( decl.isAbstract() )
            return Expression.nullSet;  // not allowed
            
        return _elementDecl(decl);
    }
    private ElementPattern _elementDecl(XSElementDecl decl) {
        Expression body = recurse(decl.getType(),root==decl);

        if( decl.getType() instanceof XSComplexType
        && builder.getGlobalBinding().isTypeSubstitutionSupportEnabled() ) {
            if( decl.getType().asComplexType().isAbstract())
                body = Expression.nullSet;
            body = pool.createChoice( body, builder.getTypeSubstitutionList(
                (XSComplexType)decl.getType(),true ));
        } else
            body = pool.createSequence( body, builder.createXsiTypeExp(decl) );
        
        if( decl.isNillable() ) {
            body = pool.createChoice( pool.createAttribute(
                new SimpleNameClass(Const.XMLSchemaInstanceNSURI,"nil"),
                    pool.createValue(BooleanType.theInstance,Boolean.TRUE)), body );
        }
        
        return new ElementPattern(
            new SimpleNameClass(decl.getTargetNamespace(),decl.getName()),
            body );
    }

    public Object modelGroup(XSModelGroup group) {
        Expression exp;
        XSModelGroup.Compositor comp = group.getCompositor();
        
        if(comp==XSModelGroup.CHOICE)   exp = Expression.nullSet;
        else                            exp = Expression.epsilon;
        
        for( int i=0; i<group.getSize(); i++ ) {
            Expression item = recurse(group.getChild(i));
            
            if( comp==XSModelGroup.CHOICE)
                exp = pool.createChoice(exp,item);
            if( comp==XSModelGroup.SEQUENCE)
                exp = pool.createSequence(exp,item);
            if( comp==XSModelGroup.ALL)
                exp = pool.createInterleave(exp,item);
        }
        
        return exp;
    }

    public Object modelGroupDecl(XSModelGroupDecl decl) {
        return recurse(decl.getModelGroup());
    }

    public Object wildcard(XSWildcard wc) {
        return new WildcardItem(builder.grammar.codeModel,wc);
    }
    
    private Expression recurse( XSComponent sc) {
        return recurse(sc,true);
    }
    
    private Expression recurse( XSComponent sc, boolean superClassCheck ) {
        Expression e = builder.selector.bindToType(sc);
        if(e!=null) {
            if( superClass==e && superClassCheck )
                // add AGM of the super class.
                return superClass.agm;
            else 
                return e;
        }
        else            return (Expression)sc.apply(this);
    }
    
    /** Finds the super class ClassItem. */
    private ClassItem findSuperClass( ClassItem parent ) {
        if(parent==null)    return null;
        
        final ClassItem[] result = new ClassItem[1];
        
        parent.exp.visit(new BGMWalker() {
            private boolean inSuper;
            private final Set visitedExps = new HashSet(); // inifinite recursion control 
             
            public void onElement(ElementExp exp) {
                if( visitedExps.add(exp) )
                    super.onElement(exp);
            }
            public Object onClass(ClassItem item) {
                if(inSuper)
                    result[0] = item;
                return null;
            }

            public Object onSuper(SuperClassItem item) {
                inSuper = true;
                visitedExps.clear();
                item.exp.visit(this);
                inSuper = false;
                return null;
            }
        });
        
        return result[0];
    }
}
