/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package testcase.runtimeValidation.bhakti1;

import javax.xml.bind.Validator;
import javax.xml.bind.util.ValidationEventCollector;

import util.JUnitTestBase;

import junit.textui.TestRunner;

public class JUnitTest extends JUnitTestBase {
    
    public JUnitTest( String name ) {
        super( name );
    }
    
    public void testIt() throws Exception {
       ScreenName screenName = new ObjectFactory().createScreenName();

       screenName.getUserNames().add(new String("Tom"));
       //Not added the userNo
       //screenName.setUserNo(new java.math.BigInteger("9"));

       Validator validator = createContext().createValidator();
       ValidationEventCollector vec = new ValidationEventCollector();
       validator.setEventHandler(vec);
  
       boolean valid = validator.validateRoot(screenName);
       
       assertTrue( "the validator should gracefully detect an error", !valid );
    }
    
    public static void main( String[] args )
        throws Exception {
        
        TestRunner.run( JUnitTest.class );            
    }
}
