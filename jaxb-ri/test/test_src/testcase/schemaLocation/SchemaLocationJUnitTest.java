/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package testcase.schemaLocation;

import java.io.FileOutputStream;

import javax.xml.bind.*;

import util.JUnitTestBase;
import junit.textui.TestRunner;

/**
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.1 $
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