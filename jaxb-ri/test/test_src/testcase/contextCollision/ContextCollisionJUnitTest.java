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

package testcase.contextCollision;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import util.JUnitTestBase;
import junit.textui.TestRunner;

/**
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.2 $
 * @since JAXB1.0
 */
public class ContextCollisionJUnitTest extends JUnitTestBase {

    public ContextCollisionJUnitTest( String name ) {
        super( name );
    }

    public static void main(String[] args) {
        TestRunner.run( ContextCollisionJUnitTest.class );
    }

    /*
     * The two schemas are setup such that there is a global element name
     * collision.  Creating the context with both schemas on the context path
     * should trigger an exception.
     * 
     * In this case, neither schema uses namespace.
     */     
    public void testCollisionDetectionNoNS() throws Exception {
        doIt( getPackageName() + ".schema1a", 
              getPackageName() + ".schema1b",
              false );
    }

    /*
     * The two schemas are setup such that there is a global element name
     * collision.  Creating the context with both schemas on the context path
     * should trigger an exception.
     * 
     * In this case, both schemas use namespace.
     */     
    public void testCollisionDetectionNS() throws Exception {
        doIt( getPackageName() + ".schema2a", 
              getPackageName() + ".schema2b",
              false );
    }
    
    /*
     * The two schemas are setup such that there are no global element name
     * collisions.  Creating the context should not trigger an exception.
     */
    public void testNoCollision() throws Exception {
        doIt( getPackageName() + ".schema1a", 
              getPackageName() + ".schema2a",
              true );
    }
    
    private void doIt( String pkg1, String pkg2, boolean assertVal ) {
        try {
            JAXBContext.newInstance( pkg1 + ":" + pkg2 );
            assertTrue( assertVal );
        } catch( JAXBException je ) {
            assertTrue( !assertVal );
        }
    }
}
