/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package testcase.wildcard1;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.Validator;

import org.xml.sax.InputSource;

import util.JUnitTestBase;


import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Tests that wildcard can work with facade JAXBContext.
 * 
 * Compile schema A and B individually, merge them together,
 * then see if the wildcard in A can correctly find elements
 * of B.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ContextPathCompositionJUTest extends JUnitTestBase {

    public ContextPathCompositionJUTest(String name) {super(name);}

    public static void main( String[] args ) {
        TestRunner.run(ContextPathCompositionJUTest.class);
    }
    



    /**
     * Try both "a" and "b" at the same time and see if
     * the unmarshaller can switch from "a" to "b" automatically.
     */
    public void testComposition() throws Exception {
        
        String image = roundTrip( JAXBContext.newInstance(
            getPackageName()+".schema1:"+getPackageName()+".schema2"),
            "<a:root xmlns:a='a' xmlns:b='b'><b:foo/><b:bar/></a:root>" );
            
        // make sure that they are properly marshaled
        assertTrue( image.indexOf("foo")!=-1 );
        assertTrue( image.indexOf("bar")!=-1 );
    }
    
    /**
     * Try just "a" and check that elements in "b" are not
     * unmarshalled.
     */
    public void testIndividual() throws Exception {

        // try a only.
        String image = roundTrip( JAXBContext.newInstance(
            getPackageName()+".schema1"),
            
            "<a:root xmlns:a='a' xmlns:b='b'><b:foo/><b:bar/></a:root>" );
            
        // make sure that those are ignored
        assertTrue( image.indexOf("foo")==-1 );
        assertTrue( image.indexOf("bar")==-1 );
    }
    
    /**
     * Performs a round-trip by using a given context. */
    private String roundTrip( JAXBContext context, String xml ) throws Exception {
        
        System.out.println("JAXBContext is "+ context.getClass().getName() );

        // unmarshal
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        Object o = unmarshaller.unmarshal( new InputSource(
            new StringReader(xml)));
        
        // validate it
        Validator validator = context.createValidator();
        validator.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(ValidationEvent event) {
                // show the stack trace
                try {
                    throw new Exception();
                } catch( Exception e ) {
                    e.printStackTrace();
                }
                System.out.println(event);
                return false;
            }
        });
        assertTrue( validator.validate(o) );
        assertTrue( validator.validateRoot(o) );
        
        // marshal it back
        StringWriter sw = new StringWriter();
        Marshaller marshaller = context.createMarshaller();
        
        marshaller.marshal(o,sw);
        return sw.getBuffer().toString();
        
    }
}
