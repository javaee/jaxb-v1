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
package com.sun.tools.xjc.reader;

import java.util.Stack;

import org.xml.sax.Attributes;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

/**
 * This class adopts the stack model and reads the package
 * information from "jaxb:package" attribute.
 * 
 * The startElement and the endElement event must be called
 * for every such event.
 */
public class StackPackageManager implements PackageManager
{
    /**
     * @param   pkg
     *      The initial current package.
     */
    public StackPackageManager( JPackage pkg ) {
        this.codeModel = pkg.owner();
        stack.push(pkg);
    }
    
    private final JCodeModel codeModel;
    
    /**
     * Gets the "current" package.
     * 
     * Typically, classes or interfaces are generated
     * into the returned package.
     * 
     * @return
     *      Non-null valid JPackage object.
     */
    public final JPackage getCurrentPackage() { return (JPackage)stack.peek(); }
    
    
    
    private final Stack stack = new Stack();
    
    public final void startElement( Attributes atts ) {
        if( atts.getIndex(Const.JAXB_NSURI,"package")!=-1 ) {
            // handle "t:package" attribute here.
            String name = atts.getValue(Const.JAXB_NSURI,"package");
            stack.push( codeModel._package(name) );
        } else
            // continue using the current one
            stack.push(stack.peek());
    }
    public final void endElement() {
        stack.pop();
    }
}
