/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package testcase.spec_api.eventhandler;

import java.math.BigInteger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationException;
import javax.xml.bind.Validator;

import util.JUnitTestBase;
import junit.textui.TestRunner;

/**
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.1 $
 * @since JAXB1.0
 */
public class EventHandlerJUnitTest extends JUnitTestBase {

    public EventHandlerJUnitTest( String name ) {
        super( name );
    }

    public static void main(String[] args) {
        TestRunner.run( EventHandlerJUnitTest.class );
    }
    
    /*
     * throwing a RutimeException from the handleEvent method should cause the
     * Unmarshaller to halt with an UnmarshalException
     */
    public void testUnmarshalException() throws Exception {
        JAXBContext jc = createContext();

        Unmarshaller u = jc.createUnmarshaller();
        u.setValidating( true );
        u.setEventHandler( new MyEventHandler() );

        try {
            Object o = u.unmarshal( 
                        this.getClass().getResourceAsStream( "test.xml" ) );
            fail("should throw an UnmarshalException");
        } catch( Exception ue ) {
            System.out.println( "caught UnmarshalException" );
        }
    }
    
    /*
     * throwing a RutimeException from the handleEvent method should cause the
     * Validator to halt with an ValidationException
     */
    public void testValidationException() throws Exception {
        JAXBContext jc = createContext();
        
        APositiveInteger api = new testcase.spec_api.eventhandler.ObjectFactory().createAPositiveInteger();
        api.setValue( new BigInteger( "-5" ) );
        
        Validator v = jc.createValidator();
        v.setEventHandler( new MyEventHandler() );

        boolean caughtException = false;
        try {
            v.validateRoot( api );
        } catch( ValidationException ve ) {
            System.out.println( "caught ValidationException" );
            caughtException = true;
        }
        assertTrue( caughtException );
    }
                
}

class MyEventHandler implements ValidationEventHandler {
    /**
     * @see javax.xml.bind.ValidationEventHandler#handleEvent(ValidationEvent)
     */
    public boolean handleEvent(ValidationEvent event) {
        System.out.println( "in handleEvent" );
        throw new RuntimeException();
    }

}
