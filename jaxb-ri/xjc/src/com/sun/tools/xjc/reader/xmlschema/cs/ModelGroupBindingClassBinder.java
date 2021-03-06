/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.xjc.reader.xmlschema.cs;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.xmlschema.NameGenerator;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
/**
 * Special Checker that adds choice content interface semantics
 * to another ClassBinder.
 */
class ModelGroupBindingClassBinder extends AbstractBinderImpl
{
    /**
     * A model group binding class binder works as a wrapper around
     * another ClassBinder.
     */
    private final ClassBinder base;
    
    /**
     * Store top-level choice model groups. Those are not mapped to
     * classes (see the spec 5.9.9).
     */
    private final Set topLevelChoices = new HashSet();
    
    ModelGroupBindingClassBinder( ClassSelector classSelector, ClassBinder base ) {
        super(classSelector);
        this.base = base;
    }
    
    public Object modelGroup(XSModelGroup mgroup) {
        // let this model group mapped to a class.
        ClassItem ci = (ClassItem)base.modelGroup(mgroup);
        
        if( mgroup.getCompositor()==XSModelGroup.CHOICE && !topLevelChoices.contains(mgroup)) {
            if( ci==null && !builder.getGlobalBinding().isChoiceContentPropertyModelGroupBinding() ) {
                // force ci to map to a class.
                try {
                    JDefinedClass clazz = owner.getClassFactory().create(
                        NameGenerator.getName(owner.builder,mgroup),
                        mgroup.getLocator() );
        
                    ci = wrapByClassItem( mgroup, clazz );
                } catch( ParseException e ) {
                    // unable to derive a valid default name
                    builder.errorReceiver.error(mgroup.getLocator(),
                        Messages.format(Messages.ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP));
                
                    // recover by pretending that this MG is not gonna be mapped to a class
                    ci = null;
                }
            }

            if( ci!=null )
                // choice model group bound to a class automatically behaves as
                // the choice content interface.
                ci.hasGetContentMethod = true;
        }
            
        return ci;
    }
    
    public Object complexType(XSComplexType type) {
        ClassItem ci = (ClassItem)base.complexType(type);
        if(ci==null)    return null;
        
        if( needsToHaveChoiceContentProperty(type) ) {
            topLevelChoices.add( type.getContentType().asParticle().getTerm() );
            ci.hasGetContentMethod = true;
        }
            
        return ci;
    }
    
    public Object modelGroupDecl(XSModelGroupDecl decl) {
        ClassItem ci = (ClassItem)base.modelGroupDecl(decl);
        if(ci!=null)    return ci;
        
        // all model group decls will become classes.
        JPackage pkg = owner.getPackage(decl.getTargetNamespace());

        JDefinedClass clazz = owner.codeModelClassFactory.createInterface(
            pkg, deriveName(decl), decl.getLocator() );
        
        ci = wrapByClassItem( decl, clazz );
        
        if( needsToHaveChoiceContentProperty(decl) )
            ci.hasGetContentMethod = true;
        
        return ci;
    }
    
    
    /**
     * Check if a class generated from this complex type needs to
     * have the getContent method.
     */
    private boolean needsToHaveChoiceContentProperty(XSComplexType type) {
        
        if( type.iterateDeclaredAttributeUses().hasNext() )
            return false;   // if there's attribute, don't make it a choice content interface
        
        XSParticle p = type.getContentType().asParticle();
        if(p==null)     return false;

        if( p.getMaxOccurs()!=1 )   return false;
        
        XSModelGroup mg = p.getTerm().asModelGroup();
        if(mg==null)    return false;
        
        if( builder.getGlobalBinding().isChoiceContentPropertyModelGroupBinding() )
            return false;
        
        return mg.getCompositor()==XSModelGroup.CHOICE;
    }
    
    private boolean needsToHaveChoiceContentProperty(XSModelGroupDecl decl) {
        return decl.getModelGroup().getCompositor()==XSModelGroup.CHOICE;
    }
    
    
//
// simply delegate to the wrapped ClassBinder
//
    public Object annotation(XSAnnotation ann) {
        return base.annotation(ann);
    }
    public Object attGroupDecl(XSAttGroupDecl decl) {
        return base.attGroupDecl(decl);
    }
    public Object attributeDecl(XSAttributeDecl decl) {
        return base.attributeDecl(decl);
    }
    public Object attributeUse(XSAttributeUse use) {
        return base.attributeUse(use);
    }
    public Object facet(XSFacet facet) {
        return base.facet(facet);
    }
    public Object notation(XSNotation notation) {
        return base.notation(notation);
    }
    public Object schema(XSSchema schema) {
        return base.schema(schema);
    }
    public Object empty(XSContentType empty) {
        return base.empty(empty);
    }
    public Object particle(XSParticle particle) {
        return base.particle(particle);
    }
    public Object simpleType(XSSimpleType simpleType) {
        return base.simpleType(simpleType);
    }
    public Object elementDecl(XSElementDecl decl) {
        return base.elementDecl(decl);
    }
    public Object wildcard(XSWildcard wc) {
        return base.wildcard(wc);
    }
}
