/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
