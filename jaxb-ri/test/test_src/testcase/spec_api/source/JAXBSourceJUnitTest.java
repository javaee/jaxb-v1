/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package testcase.spec_api.source;

import util.JUnitTestBase;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.*;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import junit.textui.TestRunner;

import org.jdom.Document;
import org.jdom.transform.JDOMResult;

import batch.core.JDomUtil;

/**
 * Test {@link JAXBSource} an {@link JAXBResult} classes.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JAXBSourceJUnitTest extends JUnitTestBase {
    public JAXBSourceJUnitTest(String name) {
        super(name);
    }
    
    public static void main( String[] args ) {
        TestRunner.run(JAXBSourceJUnitTest.class);
    }
    
    /** XSLT engine with identity transformation. */
    private Transformer noopTransformer;
    
    public void setUp() throws Exception {
        // set up XSLT engine
//        InputStream is = this.getClass().getResourceAsStream("noop.xsl");
//        assertNotNull("unable to load noop.xsl",is);
        noopTransformer = TransformerFactory.newInstance().newTransformer();
//            new StreamSource(is));
         
    }
    public void tearDown() {
        noopTransformer = null; // allow GC to wipe out XSLT engine from memory
    }

    /** Tests JAXBSource by using XSLT. */
    public void testSource() throws Exception {
        // unmarshal an object
        JAXBContext context = createContext();
        Folder f = (Folder)context.createUnmarshaller().unmarshal(
            this.getClass().getResourceAsStream("test.xml"));
        
        // put it in JAXBSource
        JAXBSource s = new JAXBSource(context,f);
        
        // see if Xalan likes it. This is for human eyes
        noopTransformer.transform(s,new StreamResult(System.out));
        
        // set up XSLT chain so that it will marshal into JDOM.
        JDOMResult jdomr = new JDOMResult();
        noopTransformer.transform(s,jdomr);
        Document doc1 = jdomr.getDocument();
        
        // marshal the same object to JDOM directly
        jdomr = new JDOMResult();
        context.createMarshaller().marshal(f,jdomr);
        Document doc2 = jdomr.getDocument();
        
        // compare those two.
        assertTrue( "two objects should be identical", JDomUtil.compare(doc1,doc2) );
    }


// exclude the test. this doesn't really work well.
    /** Tests if we can build JAXM message from JAXB. */
//    public void testJAXM() throws Exception {
//        // unmarshal an object
//        JAXBContext context = createContext();
//        Folder f = (Folder)context.createUnmarshaller().unmarshal(
//            this.getClass().getResourceAsStream("test.xml"));
//        
//        // build a JAXM message by marshalling this object
//        SOAPMessage msg = MessageFactory.newInstance().createMessage();
//        msg.getSOAPPart().setContent(new JAXBSource(context,f));
//        msg.saveChanges();
//        
//        // for human eyes
//        msg.writeTo(System.out);
//        
//        // TODO: is there any way to easily verify that the above output is correct?
//    }
    
    /** Tests JAXBResult by using XSLT */
    public void testResult() throws Exception {
        JAXBContext context = createContext();
        // XML->XSLT->JAXB
        JAXBResult result = new JAXBResult(context);
        
        // run transformation and unmarshal
        noopTransformer.transform(
            new StreamSource(
                this.getClass().getResourceAsStream("test.xml")),
                result);
        
        // marshal it into JDOM
        JDOMResult jdomr = new JDOMResult();
        Object o1 = result.getResult();
        context.createMarshaller().marshal(o1,jdomr);
        
        assertTrue( "two objects are suppoed to be equal",
            JDomUtil.compare(
                JDomUtil.load(this.getClass().getResourceAsStream("test.xml")),
                jdomr.getDocument() ));
    }
}
