/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
