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

package testcase.schemaLocation;

import java.io.FileOutputStream;

import javax.xml.bind.*;

import util.JUnitTestBase;
import junit.textui.TestRunner;

/**
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.3 $
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
