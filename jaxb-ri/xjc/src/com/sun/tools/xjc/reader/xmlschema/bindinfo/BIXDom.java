/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ext.DOMItemFactory;
import com.sun.tools.xjc.reader.Const;

/**
 * DOM customization.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIXDom extends AbstractDeclarationImpl {
    private final DOMItemFactory factory;

    public BIXDom( DOMItemFactory _factory, Locator _loc) {
        super(_loc);
        this.factory = _factory;
    }

    /**
     * Builds a BGM fragment necessary to map an element to a DOM.
     */
    public Expression create(NameClass nc, AnnotatedGrammar grammar, Locator loc) {
        markAsAcknowledged();
        return factory.create(nc, grammar, loc);
    }

    public final QName getName() { return NAME; }
    
    /** Name of the conversion declaration. */
    public static final QName NAME = new QName(
        Const.XJC_EXTENSION_URI, "dom" );

}
