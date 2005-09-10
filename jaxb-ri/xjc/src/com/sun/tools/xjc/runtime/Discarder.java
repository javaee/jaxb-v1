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

/*
 * @(#)$Id: Discarder.java,v 1.2 2005-09-10 18:20:42 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * UnmarshallingEventHandler implementation that discards the whole sub-tree.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class Discarder implements UnmarshallingEventHandler {
    
    private final UnmarshallingContext context;

    // nest level of elements.
    private int depth = 0;
    
    
    public Discarder(UnmarshallingContext _ctxt) {
        this.context = _ctxt;
    }

    public void enterAttribute(String uri, String local, String qname) throws SAXException {
    }

    public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        depth++;
    }

    public void leaveAttribute(String uri, String local, String qname) throws SAXException {
    }

    public void leaveElement(String uri, String local, String qname) throws SAXException {
        depth--;
        if(depth==0)
            context.popContentHandler();
    }

    public Object owner() {
        return null;
    }

    public void text(String s) throws SAXException {
    }

    public void leaveChild(int nextState) throws SAXException {
    }

}
