/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
 * @version $Revision: 1.2 $
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
