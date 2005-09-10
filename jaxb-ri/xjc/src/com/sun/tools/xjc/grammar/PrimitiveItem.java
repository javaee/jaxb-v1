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

import com.sun.codemodel.JType;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.xducer.Transducer;

/**
 * Represents a "primitive" item.
 * 
 * <p>
 * This item causes a transformation from a string to an object.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class PrimitiveItem extends TypeItem {

    protected PrimitiveItem( Transducer _xducer, DatabindableDatatype _guard, Expression _exp, Locator loc ) {
        // name is used only for debug purpose.
        super(_xducer.toString(),loc);
        
        this.xducer = _xducer;
        this.exp = _exp;
        this.guard = _guard;
    }
    
    public JType getType() { return xducer.getReturnType(); }
    
    /** Transducer that handles actual conversion. */
    public final Transducer xducer;

    /**
     * Datatype object that defines text that can be accepted by this
     * PrimitiveItem.
     */
    public final DatabindableDatatype guard;
    
    public Object visitJI( JavaItemVisitor visitor ) {
        return visitor.onPrimitive(this);
    }
}
