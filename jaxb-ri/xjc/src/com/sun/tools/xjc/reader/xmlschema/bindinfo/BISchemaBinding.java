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

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.tools.xjc.reader.Const;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSType;

/**
 * Schema-wide binding customization.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class BISchemaBinding extends AbstractDeclarationImpl {

    // naming rules
    private final NamingRule typeNamingRule;
    private final NamingRule elementNamingRule;
    private final NamingRule attributeNamingRule;
    private final NamingRule modelGroupNamingRule;
    private final NamingRule anonymousTypeNamingRule;
    
    private String packageName;
    private final String javadoc;

    /**
     * Default naming rule, that doesn't change the name.
     */
    private static final NamingRule defaultNamingRule = new NamingRule("","");
    

    /**
     * Default naming rules of the generated interfaces.
     * 
     * It simply adds prefix and suffix to the name, but
     * the caller shouldn't care how the name mangling is
     * done.
     */
    public static final class NamingRule {
        private final String prefix;
        private final String suffix;
        
        public NamingRule( String _prefix, String _suffix ) {
            this.prefix = _prefix;
            this.suffix = _suffix;
        }
        
        /** Changes the name according to the rule. */
        public String mangle( String originalName ) {
            return prefix+originalName+suffix;
        }
    }
    
    public BISchemaBinding( String _packageName, String _javadoc,
        NamingRule rType, NamingRule rElement, NamingRule rAttribute,
        NamingRule rModelGroup, NamingRule rAnonymousType, Locator _loc ) {
            
        super(_loc);
        this.packageName = _packageName;
        this.javadoc = _javadoc;
        
        if(rType==null)             rType           = defaultNamingRule;
        if(rElement==null)          rElement        = defaultNamingRule;
        if(rAttribute==null)        rAttribute      = defaultNamingRule;
        if(rModelGroup==null)       rModelGroup     = defaultNamingRule;
        if(rAnonymousType==null)    rAnonymousType  = new NamingRule("","Type");
        
        this.typeNamingRule = rType;
        this.elementNamingRule = rElement;
        this.attributeNamingRule = rAttribute;
        this.modelGroupNamingRule = rModelGroup;
        this.anonymousTypeNamingRule = rAnonymousType;
        
        // schema-wide customizations are always considered as acknowledged.
        markAsAcknowledged();
    }
    
    
    /**
     * Transforms the default name produced from XML name
     * by following the customization.
     * 
     * This shouldn't be applied to a class name specified
     * by a customization.
     * 
     * @param cmp
     *      The schema component from which the default name is derived.
     */
    public String mangleClassName( String name, XSComponent cmp ) {
        if( cmp instanceof XSType )
            return typeNamingRule.mangle(name);
        if( cmp instanceof XSElementDecl )
            return elementNamingRule.mangle(name);
        if( cmp instanceof XSAttributeDecl )
            return attributeNamingRule.mangle(name);
        if( cmp instanceof XSModelGroup || cmp instanceof XSModelGroupDecl )
            return modelGroupNamingRule.mangle(name);
        
        // otherwise no modification
        return name;
    }
    
    public String mangleAnonymousTypeClassName( String name ) {
        return anonymousTypeNamingRule.mangle(name);
    }
    
    
    public void setPackageName( String val ) { packageName=val; }
    public String getPackageName() { return packageName; }
    
    public String getJavadoc() { return javadoc; }
    
    public QName getName() { return NAME; }
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "schemaBinding" );
}
