/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
