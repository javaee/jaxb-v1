/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
