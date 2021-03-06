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

package com.sun.tools.xjc.reader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.InputSource;

import com.sun.codemodel.JJavaName;

public class Util
{
    /**
     * Gets an InputSource from a string, which contains either
     * a file name or an URL.
     */
    public static InputSource getInputSource(String fileOrURL) {
        try {
            // try it as an URL
            try {
                return new InputSource(escapeSpace(new URL(fileOrURL).toExternalForm()));
            } catch (MalformedURLException e) {
                String url = new File(fileOrURL).getCanonicalFile().toURL().toExternalForm();
                return new InputSource(escapeSpace(url));
            }
        } catch (Exception e) {
            // try it as an URL
            return new InputSource(fileOrURL);
        }
    }
    
    private static String escapeSpace( String url ) {
        // URLEncoder didn't work.
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            // TODO: not sure if this is the only character that needs to be escaped.
            if (url.charAt(i) == ' ')
                buf.append("%20");
            else
                buf.append(url.charAt(i));
        }
        return buf.toString();
    }
    
    
    /**
     * Computes a Java package name from a namespace URI,
     * as specified in the spec.
     * 
     * @return
     *      null if it fails to derive a package name.
     */
    public static String getPackageNameFromNamespaceURI( String nsUri, NameConverter nameConv ) {
        // remove scheme and :, if present
        // spec only requires us to remove 'http' and 'urn'...
        int idx = nsUri.indexOf(':');
        if(idx>=0) {
            String scheme = nsUri.substring(0,idx);
            if( scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("urn") )
                nsUri = nsUri.substring(idx+1);
        }
        
        // tokenize string
        ArrayList tokens = tokenize( nsUri, "/: " );
        if( tokens.size() == 0 ) {
            return null;
        }
        
        // remove trailing file type, if necessary
        if( tokens.size() > 1 ) {
            // for uri's like "www.foo.com" and "foo.com", there is no trailing
            // file, so there's no need to look at the last '.' and substring
            // otherwise, we loose the "com" (which would be wrong) 
            String lastToken = (String)tokens.get( tokens.size()-1 );
            idx = lastToken.lastIndexOf( '.' );
            if( idx > 0 ) {
                lastToken = lastToken.substring( 0, idx );
                tokens.set( tokens.size()-1, lastToken );
            }
        }
        
        // tokenize domain name and reverse.  Also remove :port if it exists
        String domain = (String)tokens.get( 0 );
        idx = domain.indexOf(':');
        if( idx >= 0) domain = domain.substring(0, idx);
        ArrayList r = reverse( tokenize( domain, "." ) );
        if( ((String)r.get( r.size()-1 )).equalsIgnoreCase( "www" ) ) {
            // remove leading www
            r.remove( r.size()-1 );
        }
        
        // replace the domain name with tokenized items
        tokens.addAll( 1, r );
        tokens.remove( 0 );            
        
        // iterate through the tokens and apply xml->java name algorithm
        for( int i = 0; i < tokens.size(); i++ ) {
            
            // get the token and remove illegal chars
            String token = (String)tokens.get( i );
            token = removeIllegalIdentifierChars( token );

            // this will check for reserved keywords
            if( !JJavaName.isJavaIdentifier( token ) ) {
                token = new String( "_" + token );
            }

            tokens.set( i, token.toLowerCase() );
        }
        
        // concat all the pieces and return it
        return combine( tokens, '.' );
    }

    private static String removeIllegalIdentifierChars(String token) {
        StringBuffer newToken = new StringBuffer();
        for( int i = 0; i < token.length(); i++ ) {
            char c = token.charAt( i );
            
            if( i ==0 && !Character.isJavaIdentifierStart( c ) ) {
                // prefix an '_' if the first char is illegal
                newToken.append( "_" + c );
            } else if( !Character.isJavaIdentifierPart( c ) ) {
                // replace the char with an '_' if it is illegal
                newToken.append( '_' );
            } else {
                // add the legal char
                newToken.append( c );
            }
        }
        return newToken.toString();
    }

    
    private static ArrayList tokenize( String str, String sep ) {
        StringTokenizer tokens = new StringTokenizer(str,sep);
        ArrayList r = new ArrayList();
        
        while(tokens.hasMoreTokens())
            r.add( tokens.nextToken() );
        
        return r;
    }

    private static ArrayList reverse( List a ) {
        ArrayList r = new ArrayList();
        
        for( int i=a.size()-1; i>=0; i-- )
            r.add( a.get(i) );
        
        return r;
    }
    
    private static String combine( List r, char sep ) {
        StringBuffer buf = new StringBuffer((String)r.get(0));
        
        for( int i=1; i<r.size(); i++ ) {
            buf.append(sep);
            buf.append(r.get(i));   
        }
        
        return buf.toString();
    }
    
}
