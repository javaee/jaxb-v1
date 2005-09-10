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
package testcase.enum.ncname;

import junit.textui.TestRunner;

import util.JUnitTestBase;

/**
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NCNameJUnitTest extends JUnitTestBase {

    public NCNameJUnitTest(String name) { super(name); }
    
    public void testParse() {
        assertSame( Size.fromString("large"), Size.LARGE );
        assertSame( Size.fromString("medium"), Size.MEDIUM );
        assertSame( Size.fromString("small"), Size.SMALL );
        
        assertTrue( Size.fromString("large")!=Size.SMALL );
    }
    
    public void testPrint() {
        assertEquals( Size.LARGE.toString(), "large" );
        assertEquals( Size.MEDIUM.toString(), "medium" );
        assertEquals( Size.SMALL.toString(), "small" );
    }

    public static void main(String[] args) {
        TestRunner.run(NCNameJUnitTest.class);
    }
}
