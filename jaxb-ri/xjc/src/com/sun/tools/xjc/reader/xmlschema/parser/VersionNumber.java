/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
