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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

/**
 * JAXB client test classes should extend this base class.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.3 $
 * @since JAXB1.0
 */
public abstract class JUnitTestBase extends TestCase {
    
    protected JUnitTestBase( String name ) { super(name); }

    /** 
     * open the xml document as a File.
     * 
     * File name should contain testcase package.  For example, 
     * testcase/sample/sample.xml
     * 
     * @param fileName name of the file including the testcase
     *         package.
     */
    public static File getXMLFile( String fileName ) 
        throws Exception {
            
        return findXMLDoc( fileName );
    }
    
    /** 
     * open the xml document as a URL.
     * 
     * File name should contain testcase package.  For example, 
     * testcase/sample/sample.xml
     * 
     * @param fileName name of the file including the testcase
     *         package.
     */
    public static URL getXMLURL( String fileName ) 
        throws Exception {
            
        return findXMLDoc( fileName ).toURL();
    }
    
    /** 
     * open the xml document as an InputStream.
     * 
     * File name should contain testcase package.  For example, 
     * testcase/sample/sample.xml
     * 
     * @param fileName name of the file including the testcase
     *         package.
     */
    public static InputStream getXMLInputStream( String fileName ) 
        throws Exception {
            
        return new FileInputStream( findXMLDoc( fileName ) );
    }
    
    /** 
     * This method actualy does the work.
     * 
     * File name should contain testcase package.  For example, 
     * testcase/sample/sample.xml
     * 
     * @param fileName name of the file including the testcase
     *         package.
     */
    private static File findXMLDoc( String fileName ) 
        throws Exception {
            
        // this key will be set if the test case is being run via ant
        String root = System.getProperty( "testcase.root", "." );
        String fileNamePrime = null;
        
        if( root.equals( "." ) ) {
            // testcase.root lookup failed, so assume that the instance doc
            // is in the current directory and remove the package prefix
            // "testcase/sample"
            int idx = fileName.lastIndexOf( '/' );
            if( idx == -1 ) {
                idx = fileName.lastIndexOf( '\\' );
            }
            fileNamePrime = fileName.substring( idx+1 );
        } else {
            // testcase.root lookup passed, so add the absolute path of the
            // root to the fileName
            fileNamePrime = root + File.separator + fileName;
        }

        System.out.println( "findXMLDoc opening doc: " + fileNamePrime );
        return new File( fileNamePrime );
    }
    
    /**
     * Creates a new JAXBContext with the package of the test case as
     * the context path.
     */
    protected JAXBContext createContext() throws JAXBException {
        return JAXBContext.newInstance(getPackageName());
    }
    
    /**
     * Marshals a content object.
     */
    protected void marshal( Object o, OutputStream out ) throws JAXBException {
        createContext().createMarshaller().marshal(o,out);
    }
    
    /**
     * Marshals a content object to <code>System&#x2E;out</code>.
     */
    protected final void marshal( Object o ) throws JAXBException {
        marshal( o, System.out );
    }
    
    /**
     * Gets the package name of the current test.
     * Since test names are used as package names and
     * test names can change, test code should better use
     * this method to avoid hard-coding package names into
     * their code.
     */
    protected String getPackageName() {
        Class testClass = this.getClass();
        String name = testClass.getName();
        int idx = name.lastIndexOf('.');
        return name.substring(0,idx);
    }
    
}
