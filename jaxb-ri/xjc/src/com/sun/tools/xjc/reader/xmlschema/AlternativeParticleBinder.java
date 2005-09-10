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

import java.text.ParseException;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;

/**
 * Performs the "alternative binding" on {@link XSParticle}
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AlternativeParticleBinder
    extends ParticleBinder
    implements XSTermFunction, BGMBuilder.ParticleHandler {
    
    
    /**
     * When we are processing terms, this particle will point to 
     * the parent particle of the term.
     */
    private XSParticle parent;
    
    

    AlternativeParticleBinder( BGMBuilder builder ) {
        super(builder);
    }
    

    public Expression build(XSParticle p, ClassItem superClass) {
        return (Expression)particle(p);
    }
    
    public boolean checkFallback( XSParticle p, ClassItem superClass ) {
        return false;    // no fall back guaranted
    }


    public Object particle(XSParticle p) {
        builder.selector.bindToType(p);
        
        XSParticle oldParent = parent;
        
        parent = p;

        Expression exp;
        XSTerm t = p.getTerm();
        
        if( needSkip(t) ) {
            XSElementDecl e = t.asElementDecl();
            // skip the class bound to the element and directly
            // bind to the type
            
            // UGLY CODE
            // remember that this particle is skipping the corresponding
            // global element class so that AGMFragmentBuilder can correctly
            // generate the fragment.
            //
            // and allow elements to be set instead of types when those two are
            // interchangable.
            builder.particlesWithGlobalElementSkip.add(p);
            
            ElementPattern eexp = builder.typeBuilder.elementDeclFlat(e);
                        
            if( needSkippableElement(e) )
                // (<foo>FooType</foo>)|Foo)
                exp = pool.createChoice( eexp, builder.selector.bindToType(e) );
            else
                exp = eexp;

            // include substitution groups
            exp = pool.createChoice( builder.getSubstitionGroupList(e), exp );

            exp = builder.fieldBuilder.createFieldItem( computeLabel(p), false, exp, p );
        } else {
            exp = (Expression)t.apply(this);
        }
        parent = oldParent;
        return builder.processMinMax( exp, p );
    }





    public Object elementDecl(XSElementDecl decl) {
        // check if this element decl wants to be a class.
        Expression typeExp = builder.selector.bindToType(decl);
        
        if( typeExp!=null ) {
            // if so, wrap this into a property and return.
            return builder.fieldBuilder.createFieldItem( typeExp, decl, false );
        } else {
            return builder.fieldBuilder.elementDecl(decl);
        }
    }

    public Object modelGroup(XSModelGroup group) {
        // check if this model group wants to be a class.
        Expression typeExp = builder.selector.bindToType(group);

        if( typeExp==null ) {
            if( group.getCompositor()==XSModelGroup.CHOICE
            ||  getLocalPropCustomization(parent)!=null ) {
                // if this is a choice and it's not a class, make a property from a choice
                return builder.fieldBuilder.createFieldItem(
                    builder.typeBuilder.build(group), group );
            }
        
            if( parent.getMaxOccurs()!=1 ) {
                // this is a repetitive particle.
                // this needs to be mapped to a class anyway,
                // even if the user didn't specify a customization (ci==null)
                try {
                    
                    JDefinedClass cls = builder.selector.getClassFactory().create(
                        builder.getNameConverter().toClassName(NameGenerator.getName(builder,group)),
                        group.getLocator() );
                        
                    ClassItem ci = builder.grammar.createClassItem(
                        cls, Expression.epsilon, group.getLocator() );
    
                    builder.selector.queueBuild(group,ci);
                    typeExp = ci;
                } catch( ParseException e ) {
                    // unable to generate a name.
                    builder.errorReporter.error(group.getLocator(),
                        Messages.ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP);
                    
                    // recover by assuming that this is not gonna be mapped to a class
                    typeExp = null;
                }
            }
        }
        
        if(typeExp!=null)
            // if so, wrap this into a property and return.
            return builder.fieldBuilder.createFieldItem( typeExp, group );
        
        // otherwise just apply things recursively.
        return builder.applyRecursively( group, this );
    }

    public Object modelGroupDecl(XSModelGroupDecl decl) {
        // check if this model group decl wants to be a class.
        Expression typeExp = builder.selector.bindToType(decl);
        
        if(typeExp!=null) {
            // if so, wrap this into a property and return.
            return builder.fieldBuilder.createFieldItem( typeExp, decl, false );
        } else {
            // otherwise just recursively process its content
            
            // push a new JClassFactory so that the new classes will be prefixed by
            // the model group name
            builder.selector.pushClassFactory(
                new PrefixedJClassFactoryImpl( builder, decl ) );
        
            Object r = modelGroup(decl.getModelGroup());
            
            builder.selector.popClassFactory();
            
            return r;
        }
    }

    public Object wildcard(XSWildcard wc) {
        // check if this model group decl wants to be a class.
        Expression typeExp = builder.selector.bindToType(wc);
        
        if( typeExp==null ) {
            // if not, force it to a typeExp
            typeExp = builder.typeBuilder.build(wc);
        }
        
        // wrap it by the property
        return builder.fieldBuilder.createFieldItem( "any", false, typeExp, wc );
    }
//
//    /**
//     * Used to process inside a choice content property.
//     */
//    private class ChoiceBodyBinder implements XSTermFunction, BGMBuilder.ParticleHandler {
//        public Object elementDecl(XSElementDecl decl) {
//            return builder.typeBuilder.elementDecl(decl);
//        }
//
//        public Object modelGroup(XSModelGroup group) {
//            return null;
//        }
//
//        public Object modelGroupDecl(XSModelGroupDecl decl) {
//            return null;
//        }
//
//        public Object wildcard(XSWildcard wc) {
//            return null;
//        }
//
//        public Object particle(XSParticle p) {
//            return null;
//        }
//    }
}
