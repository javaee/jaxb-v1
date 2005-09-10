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

