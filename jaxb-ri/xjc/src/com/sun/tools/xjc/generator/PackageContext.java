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

package com.sun.tools.xjc.generator;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;

/**
 * Context object that provides per-package information when
 * generating marshaller, unmarshaller, validator, and etc.
 * 
 * This interface is accessible from {@link GeneratorContext}
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class PackageContext {
    
    /**
     * The package this context is representing.
     */
    public final JPackage _package;
    
    /**
     * Generated ObjectFactory from package
     * 
     * The ObjectFactory class will be generated for every visible package
     * (those packages which are supposed to be used by client applications.)
     * 
     * This method allows a caller to obtain a reference to such
     * ObjectFactory from its package.
     * 
     * if no ObjectFactory is generated for a given package, return null.
     */
    public final JDefinedClass objectFactory;
    
    /**
     * Root tag map describes the element name to content interface association.
     */
    public final JVar rootTagMap;
    
        
    /**
     * Generates a version class for this package.
     */
    protected final VersionGenerator versionGenerator;
    
    /**
     * Generates an ObjectFactory class for this package. 
     */
    protected final ObjectFactoryGenerator objectFactoryGenerator; 
    
    
    protected PackageContext( 
        GeneratorContext _context,
        AnnotatedGrammar _grammar, Options _opt, JPackage _pkg ) {

        this._package = _pkg;
        
        this.versionGenerator = new VersionGenerator(_context, _grammar, _pkg.subPackage("impl") );
        
        objectFactoryGenerator = new ObjectFactoryGenerator(_context,_grammar,_opt,_pkg);
        
        this.objectFactory = objectFactoryGenerator.getObjectFactory();
        this.rootTagMap = objectFactoryGenerator.getRootTagMap();
            
        // add a version field. TODO: not sure if this is the right place to do this.
        versionGenerator.generateVersionReference( objectFactory );
    }
}
