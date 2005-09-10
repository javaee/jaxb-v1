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
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.StringTokenizer;
import java.util.Vector;

import org.dom4j.Element;
import org.xml.sax.Locator;

import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * &lt;constructor> declaration in the binding file.
 * 
 * <p>
 * Since JAXB will generate both interfaces and implementations,
 * A constructor declaration will create:
 * 
 * <ul>
 *  <li> a method declaration in the factory interface
 *  <li> a method implementation in the factory implementation class
 *  <li> a constructor implementation in the actual implementation class
 * </ul>
 * 
 * <p>
 * Also, to create these methods, we need to know the actual type
 * of properties, which are available only after the normalization of
 * annotations.
 * <p>
 * Therefore, the code generation for the constructor works in two phases.
 * During the parsing, when a {@link ClassItem} object for the element
 * declaration is found, the parser calls {@link #createPatcher} method.
 * <p>
 * This method returns a patcher, which will be invoked after the
 * normalization. This patcher will modify CodeModel and create actual
 * methods.
 */
public class BIConstructor
{
    BIConstructor( Element _node ) {
        this.dom = _node;
        
        StringTokenizer tokens = new StringTokenizer(
            _node.attributeValue("properties"));
        
        Vector vec = new Vector();
        while(tokens.hasMoreTokens())
            vec.add(tokens.nextToken());
        properties = (String[])vec.toArray(new String[0]);
        
        if( properties.length==0 )
            throw new JAXBAssertionError("this error should be catched by the validator");
    }
    
    /** &lt;constructor> element in the source binding file. */
    private final Element dom;
    
    /** properties specified by @properties. */
    private final String[] properties;
    
    /**
     * Creates a constructor declaration into the ClassItem.
     * 
     * @param   cls
     *      ClassItem object that corresponds to the
     *      element declaration that contains this declaration.
     */
    public void createDeclaration(
            final ClassItem cls, final AnnotatorController controller ) {
        
        cls.addConstructor(properties);
    }

    /** Gets the location where this declaration is declared. */
    public Locator getSourceLocation() {
        return DOM4JLocator.getLocationInfo(dom);
    }
    

}
