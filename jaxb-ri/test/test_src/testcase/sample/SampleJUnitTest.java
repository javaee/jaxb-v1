/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package testcase.sample;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import testcase.sample.impl.*;
import util.JUnitTestBase;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class SampleJUnitTest extends JUnitTestBase {
    
    public SampleJUnitTest( String name ) {
        super( name );
    }
    
    public void testIt() throws Exception {
        File xmlFile = getXMLFile( "testcase/sample/sample.xml" );
        JAXBContext jc = JAXBContext.newInstance( "testcase.sample" );
        Boston b = (Boston)( jc.createUnmarshaller().unmarshal( xmlFile ) );
        System.out.println( "Boston is: " + b.getValue() );
        jc.createMarshaller().marshal( b, System.out );
    }
    
    public static void main( String[] args )
        throws Exception {
        
        TestRunner.run( SampleJUnitTest.class );            
    }
}
