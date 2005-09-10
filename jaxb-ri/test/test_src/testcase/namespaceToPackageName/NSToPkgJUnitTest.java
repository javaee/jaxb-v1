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

package testcase.namespaceToPackageName;

import util.JUnitTestBase;

import junit.textui.TestRunner;

import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.Util;

/**
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.2 $
 * @since JAXB1.0
 */
public class NSToPkgJUnitTest extends JUnitTestBase {

    // the uri is expected to be converted into the pkg name
    private String[][] uriPkgPairs = { 
        { "foo.org", "org.foo" }, 
        { "foo.org/blarg.xsd", "org.foo.blarg" }, 
        { "foo.org/bar/baz/blarg.xsd", "org.foo.bar.baz.blarg" }, 
        { "http://foo.org", "org.foo" }, 
        { "http://foo.org/blarg.xsd", "org.foo.blarg" }, 
        { "http://foo.org/bar/baz/blarg.xsd", "org.foo.bar.baz.blarg" }, 
        { "http://www.foo.org", "org.foo" }, 
        { "http://www.foo.org/blarg.xsd", "org.foo.blarg" }, 
        { "http://www.foo.org/bar/baz/blarg.xsd", "org.foo.bar.baz.blarg" },

        { "FOO.ORG", "org.foo" }, 
        { "Foo.ORG/blarg.xsd", "org.foo.blarg" }, 
        { "foo.org/BAR/baz/BLARG.xsd", "org.foo.bar.baz.blarg" }, 
        { "http://wWw.fOo.org/bAr/Baz/blarG.xSd", "org.foo.bar.baz.blarg" },

        { "123foo.123org", "_123org._123foo" }, 
        { "boolean.float.true", "_true._float._boolean" },
        
        { "urn://abc:def:/ghi//jkl/", "abc.def.ghi.jkl" },
        { "urn:oasis:names:tc:SAML:1.0:assertion", "oasis.names.tc.saml._1_0.assertion" },
        { "http://www.w3.org/2000/09/xmldsig#", "org.w3._2000._09.xmldsig_" }, 
    };

    public NSToPkgJUnitTest( String name ) {
        super( name );
    }

    public void testConversion() throws Exception {
        NameConverter nc = NameConverter.standard;
        
        for( int i = 0; i < uriPkgPairs.length; i++ ) {
            String uriName = uriPkgPairs[i][0];
            String pkgName = uriPkgPairs[i][1];
            
            String result = Util.getPackageNameFromNamespaceURI( uriName, nc );
            
            System.out.println( "uri: " + uriName + 
                                "\nexp: " + pkgName + 
                                "\ngen: " + result + "\n" );
                                
            assertTrue( result.equals( pkgName ) );
        }
    }
    
    public static void main(String[] args)
        throws Exception {
        
        TestRunner.run( NSToPkgJUnitTest.class );            
    }
}
