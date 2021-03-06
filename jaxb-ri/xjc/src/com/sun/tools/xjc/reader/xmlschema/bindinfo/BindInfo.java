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

import java.util.Vector;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.xsom.XSComponent;

/**
 * Container for customization declarations.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
public final class BindInfo {
    public BindInfo( Locator loc ) {
        this.location = loc;
    }
    
    private final Locator location;
    
    /**
     * Documentation taken from &lt;xs:documentation>s. 
     */
    private String documentation;
    
    private boolean _hasTitleInDocumentation=false;
    
    /**
     * Gets the location of this annotation in the source file.
     * 
     * @return
     *      If the declarations are in fact specified in the source
     *      code, a non-null valid object will be returned.
     *      If this BindInfo is generated internally by XJC, then
     *      null will be returned.
     */
    public Locator getSourceLocation() { return location; }
    
    
    private XSComponent owner;
    /**
     * Sets the owner schema component and a reference to BGMBuilder.
     * This method is called from the BGMBuilder before
     * any BIDeclaration inside it is used.
     */
    public void setOwner( BGMBuilder _builder, XSComponent _owner ) {
        this.owner = _owner;
        this.builder = _builder;
    }
    public XSComponent getOwner() { return owner; }
    
    
    private BGMBuilder builder;
    /**
     * Back pointer to the BGMBuilder which is building
     * a BGM from schema components including this customization.
     */
    public BGMBuilder getBuilder() { return builder; }
    
    
    /** list of individual declarations. */
    private final Vector decls = new Vector();
    
    /** Adds a new declaration. */
    public void addDecl( BIDeclaration decl ) {
        if(decl==null)  throw new IllegalArgumentException();
        decl.setParent(this);
        decls.add(decl);
    }
    
    /**
     * Gets the first declaration with a given name, or null
     * if none is found.
     */
    public BIDeclaration get( QName name ) {
        int len = decls.size();
        for( int i=0; i<len; i++ ) {
            BIDeclaration decl = (BIDeclaration)decls.get(i);
            if( decl.getName().equals(name) )
                return decl;
        }
        return null; // not found
    }
   
    /**
     * Gets all the declarations
     */ 
    public BIDeclaration[] getDecls() {
        return (BIDeclaration[]) decls.toArray(new BIDeclaration[decls.size()]);
    }
    
    /**
     * Returns true if the string returned from {@link #getDocumentation()}
     * probably contains the "title text" (a human readable text that
     * ends with '.')
     * 
     * <p>
     * The code generator can use this information to decide if it
     * should generate the title text by itself or use the user-specified one.
     * 
     * <p>
     * Since we don't do any semantic analysis this is a guess at the best.
     */
    public boolean hasTitleInDocumentation() {
        return _hasTitleInDocumentation;
    }
    
    /**
     * Gets the documentation parsed from &lt;xs:documentation>s.
     * @return  maybe null.
     */
    public String getDocumentation() {
        return documentation;
    }
    
    /**
     * Adds a new chunk of text to the documentation.
     * 
     * @param hasTitleInDocumentation
     *      true if the caller is guessing that the title text
     *      is included in this fragment. false if not.
     *      to avoid frustrating users, pass in true if
     *      the caller is unsure (true will put precedence to the
     *      user-specified text)
     */
    public void appendDocumentation( String fragment, boolean hasTitleInDocumentation ) {
        // insert space between each fragment
        // so that the combined result is easier to see.
        if(documentation==null) {
            documentation = fragment;
            this._hasTitleInDocumentation = hasTitleInDocumentation;
        } else {
            documentation += "\n\n"+fragment;
        }
    }
    
    /**
     * Merges all the declarations inside the given BindInfo
     * to this BindInfo.
     */
    public void absorb( BindInfo bi ) {
        for( int i=0; i<bi.decls.size(); i++ )
            ((BIDeclaration)bi.decls.get(i)).setParent(this);
        this.decls.addAll( bi.decls ); 
        appendDocumentation(bi.documentation,bi.hasTitleInDocumentation());
    } 
    
    /** Gets the number of declarations. */
    public int size() { return decls.size(); }
    
    public BIDeclaration get( int idx ) { return (BIDeclaration)decls.get(idx); }
    
    
    /** An instance with the empty contents. */
    public final static BindInfo empty = new BindInfo(null);
}

