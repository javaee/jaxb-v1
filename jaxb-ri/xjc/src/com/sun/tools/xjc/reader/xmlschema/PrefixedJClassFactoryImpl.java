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
import org.xml.sax.Locator;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.xml.xsom.XSModelGroupDecl;

// TODO: should be moved to the cs subpackage.

/**
 * JClassFactory that cretes a new class by using
 * an existing class name as a prefix (instead of
 * nesting them as inner classes)
 */
public class PrefixedJClassFactoryImpl implements JClassFactory {
    /** Parent context. */
    private final JClassFactory parent;
    
    /** Prefix to prepend. */
    private final String prefix;
    
    /** Package to which a new class should be created. */
    private final JClassContainer pkg;
    
    private final BGMBuilder builder;
    
    /**
     * This implementation is primarily for model group declarations.
     * This constructor makes it easy to instanciate a new
     * PrefixedJClassFactoryImpl for a model group impl.
     */
    public PrefixedJClassFactoryImpl( BGMBuilder builder, JDefinedClass parentClass ) {
        this.builder = builder;
        this.parent = builder.selector.getClassFactory();
        this.prefix = parentClass.name();
        
        this.pkg = parentClass.parentContainer();
    }

    public PrefixedJClassFactoryImpl( BGMBuilder builder, XSModelGroupDecl decl ) {
        if( decl.isLocal() )
            throw new IllegalArgumentException();   // assertion failure
        
        this.builder = builder;
        this.parent = builder.selector.getClassFactory();
        this.prefix = builder.getNameConverter().toClassName(decl.getName());
        
        this.pkg = builder.selector.getPackage( decl.getTargetNamespace() );
    }
    
    public JDefinedClass create( String name, Locator sourceLocation ) {
        return builder.selector.codeModelClassFactory.createInterface(
            pkg,
            prefix + name,
            sourceLocation );
    }

    public JClassFactory getParentFactory() {
        return parent;
    }
}