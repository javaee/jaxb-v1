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

package testcase.basicMarshalUnmarshal;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import junit.textui.TestRunner;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import util.JUnitTestBase;

public class BasicMandUJUnitTest extends JUnitTestBase {
    
     /** Constants used for JAXP 1.2 */
    static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    static final String JAXP_SCHEMA_LOCATION =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";

    static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";


    public BasicMandUJUnitTest( String name ) {
        super( name );
    }

    public static void main( String[] args ) {
        TestRunner.run( BasicMandUJUnitTest.class );
    }

    /**
     * simple test of the unmarshal( File ) api
     */
    public void testUnmarshalFile() throws Exception {
        JAXBContext jc = createContext();
        Unmarshaller u = jc.createUnmarshaller();
        u.setValidating( true );
        
        ValidationEventCollector vec = new ValidationEventCollector();
        u.setEventHandler( vec );
        
        Object o = unmarshalFile( u, getXMLFile( "testcase/basicMarshalUnmarshal/foo.xml" ) );
        
        // there should not have been any ValidationEvents
        assertTrue( vec.hasEvents() != true );
    }    


    /**
     * simple test of the unmarshal( Node ) api
     */
    public void testUnmarshalNode() throws Exception {
        JAXBContext jc = createContext();
        Unmarshaller u = jc.createUnmarshaller();
        u.setValidating( true );
        
        ValidationEventCollector vec = new ValidationEventCollector();
        u.setEventHandler( vec );
        
        Object o = unmarshalNode( u, getXMLFile( "testcase/basicMarshalUnmarshal/foo.xml" ) );
        
        // there should not have been any ValidationEvents
        assertTrue( vec.hasEvents() != true );
    }    


    /**
     * unmarshal( SAXSource )test that demonstrates how to replace the
     * default RI parser with a validating SAX2.0 parser (xerces).
     */
    public void testUnmarshalSAXSource() throws Exception {
        JAXBContext jc = createContext();
        Unmarshaller u = jc.createUnmarshaller();
        u.setValidating( true );
        
        ValidationEventCollector vec = new ValidationEventCollector();
        u.setEventHandler( vec );
        
        Object o = unmarshalSAXSource( u, 
                                       getXMLFile( "testcase/basicMarshalUnmarshal/foo.xml" ),
                                       getXMLFile( "testcase/basicMarshalUnmarshal/foo.xsd" ) );
        
        // there should not have been any ValidationEvents
        assertTrue( vec.hasEvents() != true );
    }    


    private Object unmarshalFile( Unmarshaller u, File file ) throws Exception {
        Object o = null;

        System.out.println( "\n\nunmarshal( file )" );
        
        o = u.unmarshal( file );
        System.out.println( "got a: " + o.getClass().getName() );
            
        return o;
    }

    
    private Object unmarshalNode( Unmarshaller u, File file ) throws Exception {
        Object o = null;
        
        System.out.println( "\n\nunmarshal( Node )" );

        o = u.unmarshal( getXMLAsNode( file ) );
        System.out.println( "got a: " + o.getClass().getName() );
        
        return o;
    }

    
    private Node getXMLAsNode( File file ) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse( file );
        
        return doc;
    }
    

    private Object unmarshalSAXSource( Unmarshaller u, File file, File schema ) throws Exception {
        Object o = null;
        
        System.out.println( "\n\nunmarshal( SAXSource )" );

        // disable the default JAXB validator
        u.setValidating( false );
        SAXSource source = getSAXSource( file, schema );
        o = u.unmarshal( source );
        System.out.println( "got a: " + o.getClass().getName() );
        
        return o;
    }


    private SAXSource getSAXSource( File file, File schema ) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware(true);

        spf.setValidating(true);

        SAXParser saxParser = spf.newSAXParser();
        
        System.out.println( saxParser.getClass().toString() );

        try {
            java.net.URL schemaURL = schema.toURL();
            saxParser.setProperty( JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA );
            saxParser.setProperty( JAXP_SCHEMA_LOCATION, 
                                   schemaURL.getProtocol() + "://" + schemaURL.getFile() );
               
               // not sure why schema.toURL().toString() didn't work, but it
               // created a url like this "file:/d:/...", but xerces chokes 
               // unless you give it "file:///d:/..."
               // "file:///d:/jaxb/jaxb-ri/test/work/testcase/basicMarshalUnmarshal/foo.xsd" );
        } catch (SAXNotRecognizedException x) {
            // This can happen if the parser does not support JAXP 1.2
            System.err.println(
                "Error: JAXP SAXParser property not recognized:\n\t"
                + x.getMessage() );
            System.err.println(
                "Check to see if parser conforms to JAXP 1.2 spec.");
            assertTrue( false );
        }

        XMLReader xmlReader = saxParser.getXMLReader();

        return new SAXSource( xmlReader, new InputSource( new FileInputStream( file ) ) );
    }
    

















    /**
     * simple test that marshals to an OutputStream
     */
    public void testMarshalToOutputStream() throws Exception {
        marshalToOutputStream( getContent() );
    }

        
    /**
     * simple test that marshals to a
     */
    public void testMarshalToWriter() throws Exception {
        marshalToWriter( getContent() );
    }
    
    
    /**
     * simple test that marshals to a
     */
    public void testMarshalToContentHandler() throws Exception {
        marshalToContentHandler( getContent() );
    }
    
    
    /**
     * simple test that marshals to a
     */
    public void testMarshalToNode( Object o ) throws Exception {
        marshalToNode( getContent() );
    }


    /**
     * simple test that marshals to a
     */
    public void testMarshalToDOMResult() throws Exception {
        marshalToDOMResult( getContent() );
    }
    
    
    /**
     * simple test that marshals to a
     */
    public void testMarshalToSAXResult() throws Exception {
        marshalToSAXResult( getContent() );
    }
    
    
    /**
     * simple test that marshals to a
     */
    public void testMarshalToStreamResult() throws Exception {
        marshalToStreamResult( getContent() );
    }

    
    // unmarshal the file once and share it with all of the marshal tests
    private static Object content = null;
    
    private static Object getContent() throws Exception {
        if( content == null ) {
            JAXBContext jc = JAXBContext.newInstance( "testcase.basicMarshalUnmarshal" );
            Unmarshaller u = jc.createUnmarshaller();
            u.setValidating( true );
            
            ValidationEventCollector vec = new ValidationEventCollector();
            u.setEventHandler( vec );
            
            content = u.unmarshal( getXMLFile( "testcase/basicMarshalUnmarshal/foo.xml" ) );
            
            // there should not have been any ValidationEvents
            assertTrue( vec.hasEvents() != true );
        }
        assertTrue( content != null );
        return content;
    }
    
    
    private void marshalToOutputStream( Object o ) throws Exception {
        System.out.println( "\n\nMarshalToOutputStream" );
        JAXBContext jc = createContext();
        Marshaller m = jc.createMarshaller();

        // marshal the entire tree to a file
        if( o != null ) {
            m.marshal( o, System.out );
        } else {
            System.out.println( "o was null, skipping marshal operation" );
        }        
    }


    private void marshalToWriter( Object o ) throws Exception {
        System.out.println( "\n\nMarshalToWriter" );
        JAXBContext jc = createContext();
        Marshaller m = jc.createMarshaller();

        // marshal the entire tree to a file
        if( o != null ) {
            m.marshal( o, new PrintWriter( System.out ) );
        } else {
            System.out.println( "o was null, skipping marshal operation" );
        }        
    }


    private void marshalToContentHandler( Object o ) throws Exception {
        System.out.println( "\n\nMarshalToContentHandler" );
        JAXBContext jc = createContext();
        Marshaller m = jc.createMarshaller();

        // marshal the entire tree to a file
        if( o != null ) {
            m.marshal( o, new MyContentHandler() );
        } else {
            System.out.println( "o was null, skipping marshal operation" );
        }        
    }


    private void marshalToNode( Object o ) throws Exception {
        System.out.println( "\n\nMarshalToNode" );
        JAXBContext jc = createContext();
        Marshaller m = jc.createMarshaller();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        // marshal the entire tree to a file
        if( o != null ) {
            m.marshal( o, doc );
            (new XMLSerializer(System.out,new OutputFormat("XML","UTF-8",true))).serialize( doc );
        } else {
            System.out.println( "o was null, skipping marshal operation" );
        }        
    }


    private void marshalToDOMResult( Object o ) throws Exception {
        System.out.println( "\n\nMarshalToDOMResult" );
        JAXBContext jc = createContext();
        Marshaller m = jc.createMarshaller();

        DOMResult result = new DOMResult();
        
        // marshal the entire tree to a file
        if( o != null ) {
            m.marshal( o, result );
            if( result.getNode() instanceof Document ) 
                (new XMLSerializer(System.out,new OutputFormat("XML","UTF-8",true))).serialize( (Document)(result.getNode()) );
        } else {
            System.out.println( "o was null, skipping marshal operation" );
        }        
    }


    private void marshalToSAXResult( Object o ) throws Exception {
        System.out.println( "\n\nMarshalToSAXResult" );
        JAXBContext jc = createContext();
        Marshaller m = jc.createMarshaller();

        SAXResult result = new SAXResult( new MyContentHandler() );
        
        // marshal the entire tree to a file
        if( o != null ) {
            m.marshal( o, result );
        } else {
            System.out.println( "o was null, skipping marshal operation" );
        }        
    }


    private void marshalToStreamResult( Object o ) throws Exception {
        System.out.println( "\n\nMarshalToStreamResult" );
        JAXBContext jc = createContext();
        Marshaller m = jc.createMarshaller();

        StreamResult result = new StreamResult( System.out );
        
        // marshal the entire tree to a file
        if( o != null ) {
            m.marshal( o, result );
        } else {
            System.out.println( "o was null, skipping marshal operation" );
        }        
    }

}

class MyContentHandler extends XMLFilterImpl {
    public void startElement( String uri, String lname, 
                               String qname, Attributes atts ) {
        System.out.println( "\t" + qname );
                                
    }
}
 