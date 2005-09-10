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

