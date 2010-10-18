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
