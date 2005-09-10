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
package com.sun.tools.xjc.util;


/**
 * Other miscellaneous utility methods. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class Util {
    private Util() {}   // no instanciation please
    
    /**
     * An easier-to-use version of the System.getProperty method
     * that doesn't throw an exception even if a property cannot be
     * read.
     */
    public static final String getSystemProperty( String name ) {
        try {
            return System.getProperty(name); 
        } catch( SecurityException e ) {
            return null;
        }
    }
    
    /**
     * Calls the other getSystemProperty method with
     * "[clazz]&#x2E;[name].
     */
    public static final String getSystemProperty( Class clazz, String name ) {
        return getSystemProperty( clazz.getName()+'.'+name );
    }
    
    /** 
     * Calculate an appropriate initialCapacity for a HashMap to avoid 
     * rehashing at runtime.
     * @param count the number of expected items in the HashMap
     * @param loadFactor the desired loadFactor of the HahsMap 
     */
    public static int calculateInitialHashMapCapacity(int count, float loadFactor) {
        int initialCapacity = (int)Math.ceil( count / loadFactor ) + 1;
        
        if( initialCapacity < 16 ) {
            return 16; // default hashmap capacity
        } else {
            return initialCapacity; 
        }
    }
    
    
}
