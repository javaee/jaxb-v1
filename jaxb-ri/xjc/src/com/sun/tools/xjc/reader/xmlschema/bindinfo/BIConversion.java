/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.reader.Const;

/**
 * Conversion declaration.
 * 
 * <p>
 * A conversion declaration specifies how an XML type gets mapped
 * to a Java type.
 * 
 * <p>
 * This customization is acknowledged by the ConversionFinder.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIConversion extends AbstractDeclarationImpl {
    public BIConversion( Locator loc, Transducer transducer ) {
        super(loc);
        this.transducer = transducer;
    }
    
    private final Transducer transducer;
    
    /**
     * Gets a transducer that encapsulates the code generation
     * aspect of the conversion.
     * 
     * @return
     *      always return a non-null valid transducer.
     */
    public Transducer getTransducer() {
        return transducer;
    }
    
    public final QName getName() { return NAME; }
    
    /** Name of the conversion declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "conversion" );
}

