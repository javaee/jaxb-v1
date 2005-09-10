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
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.tools.xjc.grammar.id.IDREFTransducer;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TransducerDecorator;
import com.sun.tools.xjc.reader.Const;

/**
 * ID symbol space customization.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIXIdSymbolSpace extends AbstractDeclarationImpl {
    
    /** Symbol space name. */
    private final String name;
    
    public BIXIdSymbolSpace(Locator _loc,String _name) {
        super(_loc);
        this.name = _name;
    }
    
    /**
     * Wraps the given trasnducer and apply the specified symbol space
     * semantics.
     */
    public Transducer makeTransducer(Transducer core) {
        markAsAcknowledged();   // this customizaion is used.
        final SymbolSpace ss = getBuilder().grammar.getSymbolSpace(name);
        
        if( core.isID() )
            return new TransducerDecorator(core) {
                public SymbolSpace getIDSymbolSpace() { return ss; }
            };
        else
            return new IDREFTransducer(getBuilder().grammar.codeModel,ss,true);
    }

    public QName getName() { return NAME; }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.XJC_EXTENSION_URI, "idSymbolSpace" );
}
