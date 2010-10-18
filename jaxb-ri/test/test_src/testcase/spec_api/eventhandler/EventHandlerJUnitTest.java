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
 * @version $Revision: 1.3 $
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
