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

package testcase.contextPath;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import junit.textui.TestRunner;
import util.JUnitTestBase;

/**
 * Tests dynamically composed multiple schema paths and see the
 * proxies really work.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.3 $
 * @since JAXB1.0
 */
public class ProxyJUnitTest extends JUnitTestBase {

    public ProxyJUnitTest( String name ) {
        super( name );
    }

    public static void main(String[] args) {
        TestRunner.run( ProxyJUnitTest.class );
    }

    /*
     * The two schemas are setup such that there is a global element name
     * collision.  Creating the context with both schemas on the context path
     * should trigger an exception.
     * 
     * In this case, neither schema uses namespace.
     */     
    public void testRoundtrip() throws Exception {
        JAXBContext context = JAXBContext.newInstance(
            getPackageName()+".schema1:"+
            getPackageName()+".schema2:"+
            getPackageName()+".schema3:"+
            getPackageName()+".schema4"
        );

        cycle(context, getXMLFile("testcase/contextPath/test1.xml"),true);
        cycle(context, getXMLFile("testcase/contextPath/test2.xml"),true);
        cycle(context, getXMLFile("testcase/contextPath/test3.xml"),true);
        
        // negative test. see error propagation
        try {
            cycle(context, getXMLFile("testcase/contextPath/test4.xml"),false);
            fail("should report an error");
        } catch( MyError e ) {
            ;
//            e.printStackTrace();  // the stack trace is too long to display.
        }
    }

    private void cycle(JAXBContext context, File instance, boolean validate) throws Exception {
        Unmarshaller u = context.createUnmarshaller();
        u.setValidating(validate);
        u.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(ValidationEvent e) {
                throw new MyError("unmarshalling error:"+e.getMessage());
            }
        });
        Object o = u.unmarshal(instance);
        
        assertTrue(context.createValidator().validate(o));
        
        Marshaller m = context.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.marshal( o, System.out );
    }
    
    class MyError extends Error {
        public MyError(String s) { super(s); }
    }
}
