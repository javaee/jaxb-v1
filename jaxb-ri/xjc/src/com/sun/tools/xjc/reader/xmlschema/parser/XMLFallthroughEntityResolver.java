/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.parser;

import java.io.IOException;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

/**
 * A workaround for a bug in Xerces XMLGrammarPreparser.
 * See my bug report 
 * http://nagoya.apache.org/bugzilla/show_bug.cgi?id=17691
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XMLFallthroughEntityResolver implements XMLEntityResolver {
    
    private final XMLEntityResolver first;
    private final XMLEntityResolver second;
    
    public XMLFallthroughEntityResolver(XMLEntityResolver _first, XMLEntityResolver _second) {
        this.first = _first;
        this.second = _second;
    }
    /**
     * Tries the first entity resolver, and if it fails, use the second one.
     */
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
        XMLInputSource xis = first.resolveEntity(resourceIdentifier);
        if(xis!=null)   return xis;
        return second.resolveEntity(resourceIdentifier);
    }

}
