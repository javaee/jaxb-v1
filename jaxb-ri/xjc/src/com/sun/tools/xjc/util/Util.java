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
