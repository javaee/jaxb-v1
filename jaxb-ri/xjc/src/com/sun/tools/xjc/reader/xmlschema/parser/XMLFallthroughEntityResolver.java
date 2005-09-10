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
