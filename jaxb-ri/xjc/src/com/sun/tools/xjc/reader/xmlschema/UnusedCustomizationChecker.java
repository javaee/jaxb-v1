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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * Reports all unacknowledged customizations as errors.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class UnusedCustomizationChecker implements XSVisitor, XSSimpleTypeVisitor {

    UnusedCustomizationChecker( BGMBuilder _builder ) {
        this.builder = _builder;
    }
    
    /** Runs the check. */
    void run() {
        for (Iterator itr = builder.schemas.iterateSchema(); itr.hasNext();) {
            XSSchema s = (XSSchema) itr.next();
            
            schema(s);
            run( s.iterateAttGroupDecls() );
            run( s.iterateAttributeDecls() );
            run( s.iterateComplexTypes() );
            run( s.iterateElementDecls() );
            run( s.iterateModelGroupDecls() );
            run( s.iterateNotations() );
            run( s.iterateSimpleTypes() );
        }
    }
    
    private void run( Iterator itr ) {
        while( itr.hasNext() )
            ((XSComponent)itr.next()).visit(this);
    }

    private final BGMBuilder builder;

    private Set visitedComponents = new HashSet();
    
    /**
     * Checks unused customizations on this component
     * and returns true if this is the first time this
     * component is checked.
     */
    private boolean check( XSComponent c ) {
        if( !visitedComponents.add(c) )
            return false;   // already processed
        
        BIDeclaration[] decls = builder.getBindInfo(c).getDecls();
        for (int i = 0; i < decls.length; i++) {
            if( !decls[i].isAcknowledged() ) {
                builder.errorReporter.error(
                    decls[i].getLocation(),
                    ERR_UNACKNOWLEDGED_CUSTOMIZATION,
                    decls[i].getName().getLocalPart()
                    );
                // mark it as acknowledged to avoid
                // duplicated error messages.
                decls[i].markAsAcknowledged();
            }
        }
        
        return true;
    }


    public void annotation(XSAnnotation ann) {}
    
    public void attGroupDecl(XSAttGroupDecl decl) {
        if(check(decl))
            attContainer(decl);
    }

    public void attributeDecl(XSAttributeDecl decl) {
        if(check(decl))
            decl.getType().visit((XSSimpleTypeVisitor)this);
    }

    public void attributeUse(XSAttributeUse use) {
        if(check(use))
            use.getDecl().visit(this);
    }

    public void complexType(XSComplexType type) {
        if(check(type)) {
            // don't need to check the base type -- it must be global, thus
            // it is covered already
            type.getContentType().visit(this);
            attContainer(type);
        }
    }
    
    private void attContainer( XSAttContainer cont ) {
        for( Iterator itr = cont.iterateAttGroups(); itr.hasNext(); )
            ((XSAttGroupDecl)itr.next()).visit(this);
            
        for( Iterator itr = cont.iterateDeclaredAttributeUses(); itr.hasNext(); )
            ((XSAttributeUse)itr.next()).visit(this);
        
        XSWildcard wc = cont.getAttributeWildcard();
        if(wc!=null)        wc.visit(this);
    }

    public void schema(XSSchema schema) {
        check(schema);
    }

    public void facet(XSFacet facet) {
        check(facet);
    }

    public void notation(XSNotation notation) {
        check(notation);
    }

    public void wildcard(XSWildcard wc) {
        check(wc);
    }

    public void modelGroupDecl(XSModelGroupDecl decl) {
        if(check(decl))
            decl.getModelGroup().visit(this);
    }

    public void modelGroup(XSModelGroup group) {
        if(check(group)) {
            for( int i=0; i<group.getSize(); i++ )
                group.getChild(i).visit(this);
        }
    }

    public void elementDecl(XSElementDecl decl) {
        if(check(decl))
            decl.getType().visit(this);
    }

    public void simpleType(XSSimpleType simpleType) {
        if(check(simpleType))
            simpleType.visit( (XSSimpleTypeVisitor)this );
    }

    public void particle(XSParticle particle) {
        if(check(particle))
            particle.getTerm().visit(this);
    }

    public void empty(XSContentType empty) {
        check(empty);
    }

    public void listSimpleType(XSListSimpleType type) {
        if(check(type))
            type.getItemType().visit((XSSimpleTypeVisitor)this);
    }

    public void restrictionSimpleType(XSRestrictionSimpleType type) {
        if(check(type))
            type.getBaseType().visit(this);
    }

    public void unionSimpleType(XSUnionSimpleType type) {
        if(check(type)) {
            for( int i=0; i<type.getMemberSize(); i++ )
                type.getMember(i).visit((XSSimpleTypeVisitor)this);
        }
    }


    static final String ERR_UNACKNOWLEDGED_CUSTOMIZATION =
        "UnusedCustomizationChecker.UnacknolwedgedCustomization"; // arg:1
}
