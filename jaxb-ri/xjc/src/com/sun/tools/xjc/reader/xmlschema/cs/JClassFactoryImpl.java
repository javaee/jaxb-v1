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
package com.sun.tools.xjc.reader.xmlschema.cs;
import org.xml.sax.Locator;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.reader.xmlschema.JClassFactory;

/**
 * JClassFactory that puts new class under a package or another class.
 */
class JClassFactoryImpl implements JClassFactory {
    
    private final ClassSelector owner;
    
    /** Parent context. */
    private final JClassFactory parent;
    
    private final JClassContainer container;
    
    JClassFactoryImpl( ClassSelector owner, JClassContainer _cont ) {
        this.parent = owner.getClassFactory();
        this.container = _cont;
        this.owner = owner;
    }
    
    public JDefinedClass create( String name, Locator sourceLocation ) {
        return owner.codeModelClassFactory.createInterface(
            container, name, sourceLocation );
    }

    public JClassFactory getParentFactory() {
        return parent;
    }
}