/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
