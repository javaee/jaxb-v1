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

package util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import batch.core.compiler.InternalCompiler;
import batch.core.om.Schema;

/**
 * Recursively descend each directory and compile the schema file
 * and java sources.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.3 $
 * @since JAXB1.0
 */
public class TestBuilder {

    /**
     * This method should be passed a list of directories that contain 
     * schema files and java sources.  It will recursively traverse the
     * directories and compile all the schemas and sources.
     */
    public static void main(String[] args) 
        throws Exception {
       
        // first build all the compilation tasks into JUnit tests
        // so that errors in one directory won't affect other directories
        // and so that we can print nicely formatted error list.
        TestSuite tsuite = new TestSuite(); 
           
        System.out.println( "in TestBuilder.main.." );
        if( args.length < 2 ) {
            System.out.println( "usage: java TestBuilder <outDir> <inDir1> <inDir2> ... <inDirN>" );
            System.out.println( "\tTestBuilder will recursively descend each directory" );
            System.out.println( "\tand compile the xsd and java sources." );
        } else {
            for( int i=1; i<args.length; i++ ) {
                System.out.println( "Processing Directory: " + args[i] );
                tsuite.addTest(
                    buildSchemaRecursive( new File( args[i] ), new File(args[0]), "" ));
            }
        }
        
        // then run them all.
        junit.textui.TestRunner.run(tsuite);
    }


    /**
     * Takes a directory name and recursively descends into it looking
     * for schema files to compile.
     * 
     * @param dir
     *      starting directory
     * @param outPath
     *      The directory to put generated files
     * @param pkgName
     *      Package name
     */
    private static Test buildSchemaRecursive( File dir, File outPath, String pkgName ) 
        throws Exception {

        TestSuite tsuite = new TestSuite();
        File[] children = dir.listFiles();
        
        for( int i=0; i<children.length; i++ ) {
            if( children[i].isDirectory() ) {
                String childPackage = pkgName;
                if( childPackage.length()!=0 )  childPackage += '.';
                childPackage += children[i].getName();
                
                tsuite.addTest(buildSchemaRecursive( children[i], outPath, childPackage ));
            }
            else if( children[i].getName().endsWith(".xsd") ) {
                tsuite.addTest(buildSchema( children[i], outPath, pkgName ));
            }
        }
        
        return tsuite;
    }
    
    /**
     * compile the given schema file with xjc into a package name computed
     * from the relative name of the directory containing the schema file.
     * For example, if the schema is contained in a relative directory named
     * com/acme/foo, then XJC will generate java source into the com.acme.foo
     * package.
     * 
     * @param schema the schema file to compile
     * @param outPath
     *      The directory to put generated files
     * @param pkgName
     *      Package name
     */
    private static Test buildSchema( final File schema, final File outPath, final String pkgName ) 
        throws Exception {
        
        // perform the up-to-date check
        File objectFactory = new File(outPath, pkgName.replace('.','/')+"/ObjectFactory.java");
        if( objectFactory.exists()
        &&  objectFactory.lastModified() > schema.lastModified() ) {
            System.out.println(schema+" is up to date");
            return new TestSuite(); // return an empty dummy test case
        }
        
        return new TestCase(schema.toString()) {
            public void runTest() throws Exception {
                // generate a pkg name to pass on to xjc from the dirName 
                // containing the schema file
                // String dirName = schema.getParent();        
                System.out.println(pkgName);                                    

                System.out.println( "Compiling schema: " + schema.getAbsolutePath() );
                                    
                // compile the schema
                Schema s = new Schema(
                    schema.toURL(),
                    new URL[0],
                    outPath,
                    pkgName,
                    true, false, null,
                    new ArrayList() );
                
                new InternalCompiler().compile(s);
            }
        };
    }
    
}
