/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * @version $Revision: 1.1 $
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
