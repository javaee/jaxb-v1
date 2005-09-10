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
package testcase.unmarshaller.validation.schemaLocation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import util.JUnitTestBase;

import junit.textui.TestRunner;

/**
 * Makes sure that documents with "xsi:***" attributes
 * can be correctly processed with the unmarshalling time validation
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class UnmarshallingValidationJUnitTest extends JUnitTestBase {
    
    public UnmarshallingValidationJUnitTest(String name) {
        super(name);
    }
    
    private Unmarshaller unmarshaller1,unmarshaller2;
    
    public void setUp() throws JAXBException {
        JAXBContext context = createContext();
        
        unmarshaller1 = context.createUnmarshaller();
        unmarshaller1.setValidating(true);
        
        unmarshaller2 = context.createUnmarshaller();
    }
    
    public void tearDown() {
        // release resources
        unmarshaller1 = unmarshaller2 = null;
    }
    
    public void test1() throws JAXBException {go("test1.xml");}
    public void test2() throws JAXBException {go("test2.xml");}
    public void test3() throws JAXBException {go("test3.xml");}
    public void test4() throws JAXBException {go("test4.xml");}
    public void test5() throws JAXBException {go("test5.xml");}
    public void test6() throws JAXBException {go("test6.xml");}
    
    private void go( String testFileName ) throws JAXBException {
        unmarshaller1.unmarshal( this.getClass().getResourceAsStream(testFileName) );
        unmarshaller2.unmarshal( this.getClass().getResourceAsStream(testFileName) );
    }
    
    
    public static void main(String[] args) {
        TestRunner.run(UnmarshallingValidationJUnitTest.class);
    }
}
