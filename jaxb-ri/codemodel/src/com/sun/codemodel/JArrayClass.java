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
package com.sun.codemodel;

import java.util.Iterator;

/**
 * Array class.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JArrayClass extends JClass {
    
    // array component type
    private final JType componentType;
    
    
    JArrayClass( JCodeModel owner, JType component ) {
        super(owner);
        this.componentType = component;
    }
    
    
    public String name() {
        return componentType.name()+"[]";
    }
    
    public String fullName() {
        return componentType.fullName()+"[]";
    }
    
    public String binaryName() {
        return componentType.binaryName()+"[]";
    }

    public JPackage _package() {
        return owner().rootPackage();
    }

    public JClass _extends() {
        return null;
    }

    public Iterator _implements() {
        return emptyIterator;
    }

    public boolean isInterface() {
        return false;
    }

    public void generate(JFormatter f) {
        componentType.generate(f);
        f.p("[]");
    }

    
    /** Iterator that walks on an empty list. */
    private static Iterator emptyIterator = new Iterator() {
        public boolean hasNext() { return false; }
        public Object next() { throw new IllegalStateException(); }
        public void remove() { throw new UnsupportedOperationException(); }
    };
    
    public JType elementType() {
        return componentType;
    }

    public boolean isArray() {
        return true;
    }


    //
    // Equality is based on value
    //

    public boolean equals(Object obj) {
        if(!(obj instanceof JArrayClass))   return false;
        
        if( componentType.equals( ((JArrayClass)obj).componentType ) )
            return true;
        
        return false;
    }

    public int hashCode() {
        return componentType.hashCode();
    }

}
