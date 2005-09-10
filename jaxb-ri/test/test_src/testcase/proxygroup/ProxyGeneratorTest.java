package testcase.proxygroup;

import java.io.File;
import java.math.BigInteger;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationException;

import util.JUnitTestBase;

import com.sun.xml.bind.ProxyGroup;

import junit.framework.TestCase;
import junit.textui.TestRunner;

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

/**
 * 
 * ProxyGenerator tests.
 * 
 * Some of these tests directly test ProxyGenerator API's, others indirectly
 * test the code via specific runtime scenarios.
 * 
 * The indirect tests hinge on the fact that the proxy code is running when
 * there are multiple packages on the context path.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.2 $
 */
public class ProxyGeneratorTest extends JUnitTestBase {

    public ProxyGeneratorTest(String name) {
        super(name);
    }

    public void testWrap_NullNeighbors() throws Exception {
        try {
            // this call should throw an InternalError because the neighbor array is null
            ProxyGroup.wrap("foo", String.class, null);
            fail( "expecting IllegalArgumentException" );
        } catch (IllegalArgumentException ie) {
            // passed
        }
    }

    public void testWrap_NullFace() throws Exception {
        // o should be null because the face param is null - the other params don't 
        // matter for this testcase
        Object o = ProxyGroup.wrap( null, String.class,
                                    new Class[] { Collection.class, Collection.class } );
        assertNull( o );
    }
    
    public void testBlindWrap_NullObj() throws Exception {
        // o should be null because the bind obj param is null - the other params 
        // don't matter for this testcase
        Object o = ProxyGroup.blindWrap( null, String.class,
                                         new Class[] { Collection.class, Collection.class } );
        assertNull( o );
    }

    public void testBlindWrap_MissingFace() throws Exception {
        // o should be null because the supplied face hasn't been proxied - the other 
        // params don't matter for this testcase
        Object o = ProxyGroup.blindWrap( "someString", Integer.class,
                                         new Class[] { Collection.class, Collection.class } );
        assertNull( o );
    }

    public void testBlindWrap_SameFaceAndObjClass() throws Exception {
        // o should be a String because the supplied face and obj classes are both 
        // String.class - the other params don't matter for this testcase
        Object o = ProxyGroup.blindWrap( "someString", String.class,
                                         new Class[] { Collection.class, Collection.class } );
        assertTrue( o instanceof String );
    }

    public void testCheckedException_wrap() throws Exception {
        // proxy an object, call a proxied method that throws a checked exception, 
        // see what happens
        testcase.proxygroup.foo.Blarg fooBlarg = new testcase.proxygroup.foo.BlargImpl();
        testcase.proxygroup.bar.Blarg barBlarg = proxyFooBlarg(fooBlarg);
                             
        try {
            // this call should be proxied over to fooBlarg where a NumberFormatException 
            // will be thrown.
            barBlarg.throwACheckedException();
        } catch( NumberFormatException nfe ) {
            return; //pass
        } catch( Throwable t ) {
            fail( "Expecting to catch a NumberFormatException - caught: " + t );
        }

        fail( "Expecting to catch a NumberFormatException - caught nothing");
    }

    public void testUnCheckedException_wrap() throws Exception {
        // proxy an object, call a proxied method that throws an unchecked exception, 
        // see what happens
        testcase.proxygroup.foo.Blarg fooBlarg = new testcase.proxygroup.foo.BlargImpl();
        testcase.proxygroup.bar.Blarg barBlarg = proxyFooBlarg(fooBlarg);
                             
        try {
            // this call should be proxied over to fooBlarg where a NumberFormatException 
            // will be thrown.
            barBlarg.throwAnUncheckedException();
        } catch( NullPointerException npe ) {
            return; //pass
        } catch( Throwable t ) {
            fail( "Expecting to catch a NullPointerException - caught: " + t );
        }

        fail( "Expecting to catch a NullPointerException - caught nothing");
    }

    public void testError_wrap() throws Exception {
        // proxy an object, call a proxied method that throws an error, 
        // see what happens
        testcase.proxygroup.foo.Blarg fooBlarg = new testcase.proxygroup.foo.BlargImpl();
        testcase.proxygroup.bar.Blarg barBlarg = proxyFooBlarg(fooBlarg);
                             
        try {
            // this call should be proxied over to fooBlarg where an InternalError 
            // will be thrown.
            barBlarg.throwAnError();
        } catch( InternalError ie ) {
            return; //pass
        } catch( Throwable t ) {
            fail( "Expecting to catch a InternalError - caught: " + t );
        }

        fail( "Expecting to catch a InternalError - caught nothing");
    }

    private testcase.proxygroup.bar.Blarg proxyFooBlarg(testcase.proxygroup.foo.Blarg fooBlarg) {
        return (testcase.proxygroup.bar.Blarg)
            ProxyGroup.wrap( fooBlarg, 
                             testcase.proxygroup.foo.Blarg.class,
                             new Class[] { testcase.proxygroup.foo.Blarg.class, testcase.proxygroup.bar.Blarg.class } );
    }
 
    private JAXBContext getJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(
            getPackageName()+".schema1:"+
            getPackageName()+".schema2" );
    }

    private Object unmarshalFile(JAXBContext context, File instance, boolean exceptionExpected) throws Exception {
        Unmarshaller u = context.createUnmarshaller();
        u.setValidating(true);
        Object o = null;
        
        try {
            o = u.unmarshal(instance);
            if( exceptionExpected ) {
                fail("should have caused UnmarshalException");
            }
        } catch( UnmarshalException ue ) {
            if( !exceptionExpected ) {
                fail("should not have caused UnmarshalException");
            }
        }

        return o;
    }
    
    public void testUnmarshalInvalid_Foo1() throws Exception {
        unmarshalFile(getJAXBContext(), getXMLFile("testcase/proxygroup/invalid-foo-1.xml"), true);
    }
    
    public void testUnmarshalInvalid_Foo2() throws Exception {
        unmarshalFile(getJAXBContext(), getXMLFile("testcase/proxygroup/invalid-foo-2.xml"), true);
    }

    public void testUnmarshalInvalid_Foo3() throws Exception {
        unmarshalFile(getJAXBContext(), getXMLFile("testcase/proxygroup/invalid-foo-3.xml"), true);
    }
    
    public void testValidate_InvalidateFooContentTree() throws Exception {
        // unmarshal a valid instance doc, make the content tree invalid, call validate
        // and make sure we catch the exception
        JAXBContext context = getJAXBContext();
        testcase.proxygroup.schema1.Foo foo = (testcase.proxygroup.schema1.Foo)
            unmarshalFile(context, getXMLFile("testcase/proxygroup/valid-foo-1.xml"), false);
        foo.setAPositiveInteger( new BigInteger("-5") );
        try {
            assertTrue(!context.createValidator().validateRoot(foo));
        } catch( ValidationException ve ) {
            return; // pass
        }
        fail( "didn't catch ValidationException" );
    }
    
    public void testMarshal_InvalidFoo1() throws Exception {
        // unmarshal a valid instance doc, make the content tree invalid, call marshal
        // and make sure we catch the exception
        JAXBContext context = getJAXBContext();
        testcase.proxygroup.schema1.Foo foo = (testcase.proxygroup.schema1.Foo)
            unmarshalFile(context, getXMLFile("testcase/proxygroup/valid-foo-1.xml"), false);
        foo.setAPositiveInteger( null );
        try {
            context.createMarshaller().marshal(foo, System.out);        
        } catch( MarshalException me ) {
            return; //pass
        }
        fail( "didn't catch MarshalException" );
    }
    
    public static void main(String[] args) {
        TestRunner.run(ProxyGeneratorTest.class);
    }
}
