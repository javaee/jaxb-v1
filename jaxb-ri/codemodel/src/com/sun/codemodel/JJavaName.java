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

package com.sun.codemodel;

import java.util.HashSet;

/**
 * Utility methods that convert arbitrary strings into Java identifiers.
 */
public class JJavaName {


    /**
     * Checks if a given string is usable as a Java identifier.
     */
    public static boolean isJavaIdentifier(String s) {
        if(s.length()==0)   return false;
        if( reservedKeywords.contains(s) )  return false;
        
        if(!Character.isJavaIdentifierStart(s.charAt(0)))   return false;
        
        for (int i = 1; i < s.length(); i++)
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
                return false;
                
        return true;
    }
    
    /**
     * Checks if the given string is a valid Java package name.
     */
    public static boolean isJavaPackageName(String s) {
        while(s.length()!=0) {
            int idx = s.indexOf('.');
            if(idx==-1) idx=s.length();
            if( !isJavaIdentifier(s.substring(0,idx)) )
                return false;
            
            s = s.substring(idx);
            if(s.length()!=0)    s = s.substring(1);    // remove '.'
        }
        return true;
    }
    
    
    /** All reserved keywords of Java. */
    private static HashSet reservedKeywords = new HashSet();
    
    static {
        // see http://java.sun.com/docs/books/tutorial/java/nutsandbolts/_keywords.html
        String[] words = new String[]{
            "abstract",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "extends",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "strictfp",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "try",
            "void",
            "volatile",
            "while",
            
            // technically these are not reserved words but they cannot be used as identifiers.
            "true",
            "false",
            "null",
            
            // and I believe assert is also a new keyword
            "assert"
            };
        for( int i=0; i<words.length; i++ )
            reservedKeywords.add(words[i]);
    }
}
