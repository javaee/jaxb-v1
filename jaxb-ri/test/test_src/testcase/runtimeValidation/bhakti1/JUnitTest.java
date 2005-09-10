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
