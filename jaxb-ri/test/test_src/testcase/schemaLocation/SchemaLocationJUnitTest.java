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

package testcase.schemaLocation;

import java.io.FileOutputStream;

import javax.xml.bind.*;

import util.JUnitTestBase;
import junit.textui.TestRunner;

/**
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.2 $
 * @since JAXB1.0
 */
public class SchemaLocationJUnitTest extends JUnitTestBase {

    public SchemaLocationJUnitTest( String name ) {
        super( name );
    }

    public static void main(String[] args) {
        TestRunner.run( SchemaLocationJUnitTest.class );
    }

    public void testInstaceDocWithoutXSINs() throws Exception {
        doIt( "doc1.xml" );
    }
    
    public void testInstaceDocWithXSINs() throws Exception {
        doIt( "doc2.xml" );
    }
    
    public void testInstaceDocWithXSINsWrongURI() throws Exception {
        doIt( "doc3.xml" );
    }
    
    private void doIt( String docName ) throws Exception {
        JAXBContext jc = JAXBContext.newInstance( getPackageName() );

        Unmarshaller u = jc.createUnmarshaller();
        Object o = u.unmarshal( this.getClass().getResourceAsStream( docName ) );

        Marshaller m = jc.createMarshaller();
        
        StringBuffer schemaLocation = new StringBuffer();
        schemaLocation.append( "http://www.foo.org/2002/Blarg " );
        schemaLocation.append( "http://www.foo.org/2002/Blarg.xsd " );
        schemaLocation.append( "http://www.foo.org/2002/Bletch " );
        schemaLocation.append( "http://www.foo.org/2002/Bletch.xsd" );
        m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation.toString() );
 
        StringBuffer noNSSchemaLocation = new StringBuffer();       
        noNSSchemaLocation.append( "http://www.bar.org/2002/Nosferatu.xsd " );
        noNSSchemaLocation.append( "http://www.bar.org/2002/Godzilla.xsd" );
        m.setProperty( Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, noNSSchemaLocation.toString() );
        
        System.out.println( "Marshalling " + docName + ":" );
        m.marshal( o, System.out );
    }
    
}