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
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;

/**
 * Represents a generated interface.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class InterfaceItem extends TypeItem {
    
    protected InterfaceItem( JClass _type, Expression body, Locator loc ) {
        super(_type.name(),loc);
        this.type = _type;
        this.exp = body;
    }

    private final JClass type;
    public JType getType() { return type; }
    
    /**
     * Returns the same object as the getType method but
     * this method exploits the fact that the getType method
     * always return an object of the type JClass.
     */
    public JClass getTypeAsClass() { return type; }
    
    public ClassItem getSuperType() { return null; } // interfaces do not have the super type.

    public Object visitJI( JavaItemVisitor visitor ) {
        return visitor.onInterface(this);
    }
}
