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
package testcase.customization.property1;

import javax.xml.bind.JAXBException;

import util.JUnitTestBase;

/**
 * Tests property customizations on attribute decl/use.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AttPropertyJUnitTest extends JUnitTestBase {

    public AttPropertyJUnitTest(String name) {
        super(name);
    }

    public void testMethods() throws JAXBException {
        ObjectFactory of = new ObjectFactory();
        // just test that the generated code has the expected methods
        RootType rt = of.createRootType();
        rt.setABc(0);
        rt.setDef("");
        rt.setGhI((short)0);
        
        rt.getABc();
        rt.getDef();
        rt.getGhI();
        
        SubType st = of.createSubType();
        st.setJkL((short)0);
        st.getJkL();
    }
}
