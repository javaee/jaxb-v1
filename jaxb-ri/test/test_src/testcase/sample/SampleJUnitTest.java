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
