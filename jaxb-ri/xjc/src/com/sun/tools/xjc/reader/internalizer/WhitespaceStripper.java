/*
 * @(#)$Id: WhitespaceStripper.java,v 1.1 2004-06-25 21:15:04 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.internalizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import com.sun.xml.util.XmlChars;

/**
 * Strips ignorable whitespace from SAX event stream.
 * 
 * <p>
 * This filter works only when the event stream doesn't
 * contain any mixed content.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class WhitespaceStripper extends XMLFilterImpl {

    private int state = 0;
    
    private char[] buf = new char[1024];
    private int bufLen = 0;
    
    private static final int AFTER_START_ELEMENT = 1;
    private static final int AFTER_END_ELEMENT = 2;

    public WhitespaceStripper(XMLReader reader) {
        setParent(reader);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        switch(state) {
        case AFTER_START_ELEMENT:
            // we have to store the characters here, even if it consists entirely
            // of whitespaces. This is because successive characters event might
            // include non-whitespace char, in which case all the whitespaces in
            // this event may suddenly become significant.
            if( bufLen+length>buf.length ) {
                // reallocate buffer
                char[] newBuf = new char[Math.max(bufLen+length,buf.length*2)];
                System.arraycopy(buf,0,newBuf,0,bufLen);
                buf = newBuf;
            }
            System.arraycopy(ch,start,buf,bufLen,length);
            bufLen += length;
            break;
        case AFTER_END_ELEMENT:
            // check if this is ignorable.
            int len = start+length;
            for( int i=start; i<len; i++ )
                if( !XmlChars.isSpace(ch[i]) ) {
                    super.characters(ch, start, length);
                    return;
                }
            // if it's entirely whitespace, ignore it.
            break;
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        processPendingText();
        super.startElement(uri, localName, qName, atts);
        state = AFTER_START_ELEMENT;
        bufLen = 0;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        processPendingText();
        super.endElement(uri, localName, qName);
        state = AFTER_END_ELEMENT;
    }
    
    /**
     * Forwars the buffered characters if it contains any non-whitespace
     * character.
     */
    private void processPendingText() throws SAXException {
        if(state==AFTER_START_ELEMENT) {
            for( int i=bufLen-1; i>=0; i-- )
                if( !XmlChars.isSpace(buf[i]) ) {
                    super.characters(buf, 0, bufLen);
                    return;
               }
        }
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // ignore completely.
    }
}
