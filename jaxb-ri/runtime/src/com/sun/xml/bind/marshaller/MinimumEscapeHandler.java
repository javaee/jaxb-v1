/*
 * @(#)$Id: MinimumEscapeHandler.java,v 1.1 2004-06-25 21:11:29 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.Writer;

/**
 * Performs no character escaping. Usable only when the output encoding
 * is UTF, but this handler gives the maximum performance.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class MinimumEscapeHandler implements CharacterEscapeHandler {
    
    private MinimumEscapeHandler() {}  // no instanciation please
    
    public static final CharacterEscapeHandler theInstance = new MinimumEscapeHandler(); 
    
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        // avoid calling the Writerwrite method too much by assuming
        // that the escaping occurs rarely.
        // profiling revealed that this is faster than the naive code.
        int limit = start+length;
        for (int i = start; i < limit; i++) {
            char c = ch[i];
            if( c=='&' || c=='<' || c=='>' || (c=='\"' && isAttVal) ) {
                if(i!=start)
                    out.write(ch,start,i-start);
                start = i+1;
                switch (ch[i]) {
                case '&' :
                    out.write("&amp;");
                    break;
                case '<' :
                    out.write("&lt;");
                    break;
                case '>' :
                    out.write("&gt;");
                    break;
                case '\"' :
                    out.write("&quot;");
                    break;
                }
            }
        }
        
        if( start!=limit )
            out.write(ch,start,limit-start);
    }

}
