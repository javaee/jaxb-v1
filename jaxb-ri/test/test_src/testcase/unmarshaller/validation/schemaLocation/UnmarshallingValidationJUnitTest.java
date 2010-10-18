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

package testcase.unmarshaller.validation.schemaLocation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import util.JUnitTestBase;

import junit.textui.TestRunner;

/**
 * Makes sure that documents with "xsi:***" attributes
 * can be correctly processed with the unmarshalling time validation
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class UnmarshallingValidationJUnitTest extends JUnitTestBase {
    
    public UnmarshallingValidationJUnitTest(String name) {
        super(name);
    }
    
    private Unmarshaller unmarshaller1,unmarshaller2;
    
    public void setUp() throws JAXBException {
        JAXBContext context = createContext();
        
        unmarshaller1 = context.createUnmarshaller();
        unmarshaller1.setValidating(true);
        
        unmarshaller2 = context.createUnmarshaller();
    }
    
    public void tearDown() {
        // release resources
        unmarshaller1 = unmarshaller2 = null;
    }
    
    public void test1() throws JAXBException {go("test1.xml");}
    public void test2() throws JAXBException {go("test2.xml");}
    public void test3() throws JAXBException {go("test3.xml");}
    public void test4() throws JAXBException {go("test4.xml");}
    public void test5() throws JAXBException {go("test5.xml");}
    public void test6() throws JAXBException {go("test6.xml");}
    
    private void go( String testFileName ) throws JAXBException {
        unmarshaller1.unmarshal( this.getClass().getResourceAsStream(testFileName) );
        unmarshaller2.unmarshal( this.getClass().getResourceAsStream(testFileName) );
    }
    
    
    public static void main(String[] args) {
        TestRunner.run(UnmarshallingValidationJUnitTest.class);
    }
}
