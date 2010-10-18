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

package testcase.runtimeValidation.xsiNil;

import javax.xml.bind.*;
import junit.textui.TestRunner;
import testcase.runtimeValidation.xsiNil.impl.*;

import util.JUnitTestBase;


/*
 * This is an isolated test to see for 
 * that the document produced after marshalling
 * is invalid
 */
public class XsiNilJUnitTest extends JUnitTestBase {
    
    public XsiNilJUnitTest( String name ) { super(name); }
    
    public static void main(String args[]) {
        TestRunner.run(XsiNilJUnitTest.class);
    }
    
    public void test() throws Exception {
        JAXBContext jc = createContext();
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setValidating(true);


        Validator validator = jc.createValidator();

        Collection inputColl= (Collection) unmarshaller.unmarshal(
            this.getClass().getResourceAsStream("booksInfo.xml"));

        boolean valid1 = validator.validateRoot(inputColl);
        assertTrue(valid1);
        
        marshal(inputColl, System.out);

        Collection coll= new ObjectFactory().createCollection();

        CollectionType.BooksType booksTypeImpl= new CollectionTypeImpl.BooksTypeImpl();
        MagazineTypeImpl magTypeImpl =new MagazineTypeImpl() ;

        PriceTypeImpl priceImpl = new PriceTypeImpl();
        priceImpl.setUnits("INR");
        priceImpl.setCurrency("Indian rupees");

        magTypeImpl.setPrice(priceImpl);
        // magTypeImpl.setMagazineName("Times");  // leave it empty to produce nillable
        booksTypeImpl.getMagazine().add(magTypeImpl);
        coll.setBooks(booksTypeImpl);

        boolean valid = validator.validateRoot(coll);
        assertTrue(valid);
        
        Marshaller m = jc.createMarshaller();           
        m.marshal(coll, System.out);
   }
}

