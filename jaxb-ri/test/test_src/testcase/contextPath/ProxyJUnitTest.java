/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package testcase.contextPath;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import junit.textui.TestRunner;
import util.JUnitTestBase;

/**
 * Tests dynamically composed multiple schema paths and see the
 * proxies really work.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.1 $
 * @since JAXB1.0
 */
public class ProxyJUnitTest extends JUnitTestBase {

    public ProxyJUnitTest( String name ) {
        super( name );
    }

    public static void main(String[] args) {
        TestRunner.run( ProxyJUnitTest.class );
    }

    /*
     * The two schemas are setup such that there is a global element name
     * collision.  Creating the context with both schemas on the context path
     * should trigger an exception.
     * 
     * In this case, neither schema uses namespace.
     */     
    public void testRoundtrip() throws Exception {
        JAXBContext context = JAXBContext.newInstance(
            getPackageName()+".schema1:"+
            getPackageName()+".schema2:"+
            getPackageName()+".schema3:"+
            getPackageName()+".schema4"
        );

        cycle(context, getXMLFile("testcase/contextPath/test1.xml"),true);
        cycle(context, getXMLFile("testcase/contextPath/test2.xml"),true);
        cycle(context, getXMLFile("testcase/contextPath/test3.xml"),true);
        
        // negative test. see error propagation
        try {
            cycle(context, getXMLFile("testcase/contextPath/test4.xml"),false);
            fail("should report an error");
        } catch( MyError e ) {
            ;
//            e.printStackTrace();  // the stack trace is too long to display.
        }
    }

    private void cycle(JAXBContext context, File instance, boolean validate) throws Exception {
        Unmarshaller u = context.createUnmarshaller();
        u.setValidating(validate);
        u.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(ValidationEvent e) {
                throw new MyError("unmarshalling error:"+e.getMessage());
            }
        });
        Object o = u.unmarshal(instance);
        
        assertTrue(context.createValidator().validate(o));
        
        Marshaller m = context.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.marshal( o, System.out );
    }
    
    class MyError extends Error {
        public MyError(String s) { super(s); }
    }
}
