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
