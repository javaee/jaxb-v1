/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package testcase.customization.property2;

import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;

import junit.textui.TestRunner;
import util.JUnitTestBase;

/**
 * Tests collection type customization on list.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ListifiedAttJUnitTest extends JUnitTestBase {

    public ListifiedAttJUnitTest(String name) {
        super(name);
    }

    public void testMethods() throws JAXBException {
        ObjectFactory of = new ObjectFactory();
        
        // just test that the generated code has the expected methods
        Root rt = of.createRoot();
        rt.getFoo().add( new Integer(0) );
        marshal( rt, System.out );

        try {
            rt.getFoo().add( new Integer(1) );
            marshal( rt, System.out );
            // it is OK to marshal this content
        } catch( MarshalException e ) {
            // it is allowed to fail marshalling
            System.out.println("failed but it's OK: "+e.getMessage());
        }
    }
    
    public static void main( String[] args ) {
        TestRunner.run(ListifiedAttJUnitTest.class);
    }
}
