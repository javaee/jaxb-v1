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

package com.sun.tools.xmlpp;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Surpresses ignorable whitespaces.
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class IgnorableWhitespaceFilter extends XMLFilterImpl {
    
    IgnorableWhitespaceFilter( XMLReader parent ) {
        setParent(parent);
    }

    IgnorableWhitespaceFilter() {
    }

    public void characters(char[] ch, int start, int length)
        throws SAXException {
        
        if( isIgnorable(ch,start,length) )
            ignorableWhitespace(ch,start,length);
        else
            super.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    
    private static boolean isIgnorable( char[] ch, int start, int length ) {
        for (int i = length - 1; i >= 0; i--)
            if (!isWhitespace(ch[i + start]))
                return false;
        return true;
    }
    private static boolean isWhitespace( char ch ) {
        return ch==' ' || ch=='\t' || ch=='\r' || ch=='\n';
    }
}
