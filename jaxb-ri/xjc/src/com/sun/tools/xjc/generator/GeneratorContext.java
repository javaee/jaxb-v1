/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.util.CodeModelClassFactory;

/**
 * Context object that provides vital information when
 * generating marshaller, unmarshaller, validator, and etc.
 * 
 * This object is created as a result of skeleton generation,
 * and provides the information about the generated skeleton.
 * 
 * This object also provides access to varioues utilities, such as
 * error reporting, etc.
 * 
 * @author Kohsuke KAWAGUCHI
 */
public interface GeneratorContext
{
    AnnotatedGrammar getGrammar();
    
    JCodeModel getCodeModel();
    
    /**
     * Obtains a reference to a component that
     * handles the optimization based table look-up.  
     */
    LookupTableBuilder getLookupTableBuilder();
    
    /**
     * Gets a JClass object that represents a copy of the specified
     * runtime class.
     * 
     * <p>
     * For example, you can pass in {@link GrammarInfo}.class to
     * obtain a reference to the runtime copy of the GrammarInfo class.
     * 
     * <p>
     * As a side-effect, this causes the referenced class to be generated
     * into the runtime package.
     * 
     * @return
     *      always return non-null valid object.   
     */
    JClass getRuntime( Class clazz );
    
    
    /** Gets the object that wraps the generated field for a given FieldUse. */
    FieldRenderer getField( FieldUse fu );
    
    /**
     * Gets per-package context information.
     * 
     * This method works for every visible package
     * (those packages which are supposed to be used by client applications.)
     * 
     * @return
     *      If this grammar doesn't produce anything in the specified
     *      package, return null.
     */
    PackageContext getPackageContext( JPackage _Package );
    
    /**
     * Obtains per-class context information.
     */
    ClassContext getClassContext( ClassItem clazz );
    
    /** Gets all package-wise contexts at once. */
    PackageContext[] getAllPackageContexts();
   
    /**
     * Gets a reference to
     * <code>new CodeModelClassFactory(getErrorHandler())</code>.
     */
    CodeModelClassFactory getClassFactory();
    
    /**
     * Any error during the back-end proccessing should be
     * sent to this object.
     */
    ErrorReceiver getErrorReceiver();
}
