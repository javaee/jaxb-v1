/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Use is subject to the license terms.
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
