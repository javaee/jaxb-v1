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
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.generator.cls.ImplStructureStrategy;
import com.sun.tools.xjc.grammar.ClassItem;

/**
 * Context object that provides per-{@link ClassItem} information
 * for filling in methods/fields for a class item.
 * 
 * This interface is accessible from {@link GeneratorContext}
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ClassContext {
    
    /**
     * A {@link GeneratorContext} encloses all the class contexts.
     */
    public final GeneratorContext parent;
    
    /**
     * {@link PackageContext} that hosts this class.
     */
    public final PackageContext _package;
    
    /**
     * This {@link ClassContext} holds information about this {@link ClassItem}.
     */
    public final ClassItem target;
    
    /**
     * The implementation class that shall be used for reference.
     * <p>
     * Usually this field holds the same value as the impl method,
     * but sometimes it holds the user-specified implementation class
     * when it is specified.
     */
    public final JClass implRef;
    
    /**
     * The default implementation class for a given ClassItem.
     * The actual place where fields/methods should be generated into.
     */
    public final JDefinedClass implClass;
    
    /**
     * The publicly visible type of the given ClassItem.
     * 
     * implClass is always assignable to this type. 
     * <p>
     * Usually this is the public content interface, but
     * it could be the same as the implClass. 
     */
    public final JDefinedClass ref;
    
    private final ImplStructureStrategy strategy; 
    
    
    public MethodWriter createMethodWriter() {
        return strategy.createMethodWriter(this);
    }
    
    protected ClassContext( GeneratorContext _parent, ImplStructureStrategy _strategy, ClassItem _target ) {
        this.parent = _parent;
        this.target = _target;
        this.strategy = _strategy;
        this.ref = _target.getTypeAsDefined();
        this._package = parent.getPackageContext(ref._package());
        this.implClass = _strategy.createImplClass(_target);
        
        if( target.getUserSpecifiedImplClass()!=null ) {
            // create a place holder for a user-specified class.
            JDefinedClass usr;
            try {
                usr = parent.getCodeModel()._class(target.getUserSpecifiedImplClass());
                // but hide that file so that it won't be generated.
                usr.hide();
            } catch( JClassAlreadyExistsException e ) {
                // it's OK for this to collide.
                usr = e.getExistingClass();
            }
            usr._extends(implClass);
            this.implRef = usr;
        } else
            this.implRef = implClass;
    }
}
