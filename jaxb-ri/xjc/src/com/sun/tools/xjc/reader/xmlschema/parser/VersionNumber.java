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

import java.util.StringTokenizer;

/**
 * Immutable representation of a dot-separated digits (such as "1.0.1").
 * 
 * {@link VersionNumber}s are {@link Comparable}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class VersionNumber implements Comparable {
    
    private final int[] digits;
    
    /**
     * Parses a string like "1.0.2" into the version number.
     * 
     * @throws IllegalArgumentException
     *      if the parsing fails.
     */
    public VersionNumber( String num ) {
        StringTokenizer tokens = new StringTokenizer(num,".");
        digits = new int[tokens.countTokens()];
        if(digits.length<2)
            throw new IllegalArgumentException();
        
        int i=0;
        while( tokens.hasMoreTokens() )
            digits[i++] = Integer.parseInt(tokens.nextToken());
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for( int i=0; i<digits.length; i++ ) {
            if(i!=0)    buf.append('.');
            buf.append( Integer.toString(digits[i]) );
        }
        return buf.toString();
    }
    
    public boolean isOlderThan( VersionNumber rhs ) {
        return compareTo(rhs)<0;
    }

    public boolean isNewerThan( VersionNumber rhs ) {
        return compareTo(rhs)>0;
    }
    
    
    public boolean equals( Object o ) {
        return compareTo(o)==0;
    }
    
    public int hashCode() {
        int x=0;
        for( int i=0; i<digits.length; i++ ) {
            x = (x<<1)|digits[i];
        }
        return x;
    }
    
    public int compareTo(Object o) {
        VersionNumber rhs = (VersionNumber)o;
        
        for( int i=0; ; i++ ) {
            if( i==this.digits.length && i==rhs.digits.length )
                return 0;   // equals
            if( i==this.digits.length )
                return -1;  // rhs is larger
            if( i==rhs.digits.length )
                return 1;
            
            int r = this.digits[i] - rhs.digits[i];
            if(r!=0)    return r;
        }
    }
}
