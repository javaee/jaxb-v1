/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
