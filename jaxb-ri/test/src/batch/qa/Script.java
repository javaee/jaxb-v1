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

package batch.qa;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Script {
    /**
     * location of the script file. 
     */
    private final URL script;
//    
//    /**
//     * Set of excluded class names.
//     */
//    private final List excludedClasses = new ArrayList();
    
    public Script(URL _script) {
        this.script = _script;
    }
    
    /**
     * Name of this script
     */
    public String getName() {
        return script.toExternalForm();
    }
    
    /**
     * Gets the scripts as a {@link Reader} to read the contents.
     */
    public Reader getScriptReader() throws IOException {
        URLConnection con = script.openConnection();
        
        String encoding = con.getContentEncoding();
        if(encoding==null)  encoding="UTF-8";
        
        return new InputStreamReader(con.getInputStream(),encoding);
    }
    
//    /**
//     * Checks if the given class name should be excluded
//     * from the class loader.
//     */
//    public boolean isExcluded( String className ) {
//        for(int i=0;i<excludedClasses.size();i++) {
//            String entry = (String)excludedClasses.get(i);
//            if(entry.endsWith("*")) {
//                if(className.startsWith(entry.substring(0,entry.length()-1)))
//                    return true;
//            } else {
//                if(entry.equals(className))
//                    return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Hides a set of classes so that
//     * it won't be visible by this script.
//     * 
//     * @param classMask
//     *      Strings like "com.sun.msv.*"
//     */
//    public void addExcludedClasses(String classMask) {
//        excludedClasses.add(classMask);
//    }
//    
//    /**
//     * Wraps a {@link ClassLoader} so that the class loader cannot
//     * find hidden classes.
//     * 
//     * @see #addExcludedClasses(String)
//     */
//    public ClassLoader createProtectedClassLoader( ClassLoader classLoader ) {
//        return new ClassLoader(classLoader) {
//            protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
//                // check the exclusion list first
//                if( isExcluded(name) )
//                    throw new ClassNotFoundException(name+" is hidden for this script");
//                // then hide it.
//                return super.loadClass(name,resolve);
//            }
//        };
//    }
}
