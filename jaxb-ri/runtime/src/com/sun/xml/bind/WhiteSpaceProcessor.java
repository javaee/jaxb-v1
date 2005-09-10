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
package com.sun.xml.bind;

/**
 * Processes white space normalization.
 * 
 * @since 1.0
 */
public abstract class WhiteSpaceProcessor {

// benchmarking (see test/src/ReplaceTest.java in the CVS Attic)
// showed that this code is slower than the current code.
//
//    public static String replace(String text) {
//        final int len = text.length();
//        StringBuffer result = new StringBuffer(len);
//
//        for (int i = 0; i < len; i++) {
//            char ch = text.charAt(i);
//            if (isWhiteSpace(ch))
//                result.append(' ');
//            else
//                result.append(ch);
//        }
//
//        return result.toString();
//    }

    public static String replace(String text) {
        int i=text.length()-1;

        // look for the first whitespace char.
        while( i>=0 && !isWhiteSpaceExceptSpace(text.charAt(i)) )
            i--;
        
        if( i<0 )
            // no such whitespace. replace(text)==text.
            return text;

        // we now know that we need to modify the text.
        // allocate a char array to do it.
        char[] buf = text.toCharArray();
        
        buf[i--] = ' ';
        for( ; i>=0; i-- )
            if( isWhiteSpaceExceptSpace(buf[i]))
                buf[i] = ' ';

        return new String(buf);
    }
    
    /**
     * This is usually the biggest processing bottleneck.
     */
    public static String collapse(String text) {
        int len = text.length();
        
        // most of the texts are already in the collapsed form.
        // so look for the first whitespace in the hope that we will
        // never see it.
        int s=0;
        while(s<len) {
            if(isWhiteSpace(text.charAt(s)))
                break;
            s++;
        }
        if(s==len) 
            // the input happens to be already collapsed.
            return text;
        
        // we now know that the input contains spaces.
        // let's sit down and do the collapsing normally. 
        
        StringBuffer result = new StringBuffer(len /*allocate enough size to avoid re-allocation*/ );
        
        if(s!=0) {
            for( int i=0; i<s; i++ )
                result.append(text.charAt(i));
            result.append(' ');
        }
            
        boolean inStripMode = true;
        for (int i = s+1; i < len; i++) {
            char ch = text.charAt(i);
            boolean b = isWhiteSpace(ch);
            if (inStripMode && b)
                continue; // skip this character

            inStripMode = b;
            if (inStripMode)
                result.append(' ');
            else
                result.append(ch);
        }

        // remove trailing whitespaces
        len = result.length();
        if (len > 0 && result.charAt(len - 1) == ' ')
            result.setLength(len - 1);
        // whitespaces are already collapsed,
        // so all we have to do is to remove the last one character
        // if it's a whitespace.

        return result.toString();
    }

    /** returns true if the specified char is a white space character. */
    protected static final boolean isWhiteSpace(char ch) {
        // most of the characters are non-control characters.
        // so check that first to quickly return false for most of the cases.
        if( ch>0x20 )   return false;
        
        // other than we have to do four comparisons.
        return ch == 0x9 || ch == 0xA || ch == 0xD || ch == 0x20;
    }

    /**
     * Returns true if the specified char is a white space character
     * but not 0x20.
     */
    protected static final boolean isWhiteSpaceExceptSpace(char ch) {
        // most of the characters are non-control characters.
        // so check that first to quickly return false for most of the cases.
        if( ch>=0x20 )   return false;
        
        // other than we have to do four comparisons.
        return ch == 0x9 || ch == 0xA || ch == 0xD;
    }
   }

