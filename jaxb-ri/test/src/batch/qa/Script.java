/*
 * @(#)$Id: Script.java,v 1.1 2004-06-25 21:13:09 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
