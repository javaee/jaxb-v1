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

package com.sun.tools.xjc.generator.cls;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Locator;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.*;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.util.CodeModelClassFactory;

/**
 * This {@link ImplStructureStrategy} generates a content interface and
 * a separate implementation class by using two packages and pararell
 * class hierarchies.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class PararellStructureStrategy implements ImplStructureStrategy {

    private final Map intf2impl = new HashMap();
    
    private final CodeModelClassFactory codeModelClassFactory;

    public PararellStructureStrategy(CodeModelClassFactory _codeModelClassFactory) {
        codeModelClassFactory = _codeModelClassFactory;
    }
    
    /**
     * Returns the implementation class
     * for the specified generated interface.
     */
    private JDefinedClass determineImplClass( JDefinedClass intf ) {
        JDefinedClass d = (JDefinedClass)intf2impl.get(intf);
        if(d!=null) return d;
        
        JClassContainer parent = intf.parentContainer();
        int mod = JMod.PUBLIC;
        
        if(parent instanceof JPackage)
            parent = ((JPackage)parent).subPackage("impl");
        else {
            parent = determineImplClass( (JDefinedClass) parent );
            mod |= JMod.STATIC;
        }
        
        d = codeModelClassFactory.createClass(
            parent,
            mod,
            intf.name()+"Impl",
            (Locator)intf.metadata );   // use the source location of the interface
        intf2impl.put(intf,d);
        return d;
    }

    public JDefinedClass createImplClass(ClassItem ci) {
        JDefinedClass impl = determineImplClass(ci.getTypeAsDefined());
        
        impl._implements(ci.getTypeAsDefined());
        
        // generate a method in each impl class that supplies the name of the
        // primary interface it implements:
        //    public static final Class PRIMARY_INTERFACE_CLASS() {
        //        return com.acme.FooCard.class;
        //    }
        // This feature is used for supporting the setImplementation method.
        impl.method( JMod.PRIVATE|JMod.STATIC|JMod.FINAL,
                 Class.class,
                 "PRIMARY_INTERFACE_CLASS" )
             .body()._return(ci.getTypeAsDefined().dotclass());
        
        return impl;
    }

    public MethodWriter createMethodWriter(final ClassContext target) {
        return new MethodWriter(target) {
            private final JDefinedClass intf = target.ref;
            private final JDefinedClass impl = target.implClass;

            private JMethod intfMethod;
            private JMethod implMethod;
            
            public JVar addParameter(JType type, String name) {
                intfMethod.param(type,name);
                return implMethod.param(type,name);
            }

            public JMethod declareMethod(JType returnType, String methodName) {
                intfMethod = intf.method( 0, returnType, methodName );
                implMethod = impl.method( JMod.PUBLIC, returnType, methodName );
                return implMethod;
            }

            public JDocComment javadoc() {
                return intfMethod.javadoc();
            }
        };
    }
}
