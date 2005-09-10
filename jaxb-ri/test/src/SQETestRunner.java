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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import batch.core.CompileTestCase;
import batch.core.compiler.InternalCompiler;
import batch.core.om.Instance;
import batch.core.om.Schema;
import batch.core.om.TestDescriptor;

/**
 * Runs one SQE test.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SQETestRunner {

    public static void main(String[] args) throws Exception {
        
        // Listing.xml file in SQE test/jaxb/beta/usage/schemas/
        URL listingURL=null;
        boolean validation = false;
        
        Vector testCases = new Vector();
        Vector xjcParams = new Vector();
        
        for( int i=0; i<args.length; i++ ) {
            if(args[i].charAt(0)=='-') {
                // parameters
                if(args[i].equals("-xjc")) {
                    xjcParams.add(args[++i]);
                    continue;
                }
                if(args[i].equals("-v")) {
                    validation = true;
                    continue;
                }
                
                usage("unknown option:" +args[i]);    // unknown option
                return;
            } else {
                // arguments
                if(listingURL==null) {
                    try {
                        listingURL = new URL(args[i]);
                    } catch( MalformedURLException e ) {
                        listingURL = new File(args[i]).toURL();
                    }
                    continue;
                }
                // the rest of the arguments will be considered as test IDs.
                testCases.add(args[i]);
            }
        }
        
        
        if(testCases.size()==0) {
            usage("no test");
            return;
        }
        
        // parse the schema listing file
        Document listingFile = loadDOM(new InputSource(
            listingURL.toExternalForm()));
            
        // build the test
        TestSuite suite = new TestSuite();
        for( int i=0; i<testCases.size(); i++ ) {
            Element testCase = findTestCase(listingFile,
                (String)testCases.get(i));
            TestDescriptor descriptor = buildDescriptor(listingURL,testCase);
            suite.addTest(buildTest(descriptor,validation,xjcParams));
        }
        
        // run the test
        TestRunner.run(suite);
    }
    
    /** Prints the usage screen. */
    private static void usage( String msg ) {
        if(msg!=null)   System.err.println(msg);
        System.err.println(
            "Usage: SQETestRunner <Listing.xml file> <test ID> [<test ID> ....]\n"+
            "\n"+
            "Run a specific test from SQE test suite.\n"+
            "\n"+
            "Options:\n"+
            "  -xjc <XJC opt>\n"+
            "    specified argument will be passed to XJC when a schema is ocmpiled.\n"+
            "  -v\n"+
            "    turn on the unmarshalling validation.\n"
        );
    }
    
    /** Loads an XML file into dom4j. */
    private static Document loadDOM( InputSource source ) throws DocumentException {
        return new SAXReader().read(source);
    }
    
    /** Finds a test case with the specified ID. */
    private static Element findTestCase( Document dom, String id ) {
        Iterator itr = dom.getRootElement().elementIterator("test");
        while(itr.hasNext()) {
            Element e = (Element)itr.next();
            if(id.equals(e.attributeValue("id")))
                return e;
        }
        
        // not found
        throw new NoSuchElementException("unable to find test "+id);
    }
    
    /**
     * Builds a {@link TestDescriptor} from an element in the SQE &lt;test> element.
     */
    private static TestDescriptor buildDescriptor( URL baseURL, Element test ) throws IOException {
        // compute test directory
        URL testDir = new URL(baseURL, test.attributeValue("testDir")+"/");
        
        Element files = test.element("files");
        
        // list up schema files
        List schemas = new ArrayList();  // List<URL>
        for (Iterator itr = files.elementIterator("schemaDTDFile"); itr.hasNext();) {
            Element schema = (Element)itr.next();
            schemas.add( new URL(testDir,schema.attributeValue("fileName")) );
        }

        // list up instance files
        List instances = new ArrayList();
        Iterator itr = files.elementIterator("instanceFile");
        while(itr.hasNext()) {
            Element instance = (Element)itr.next();
            
            String fileName = instance.attributeValue("fileName");
            final URL instURL = new URL(testDir,fileName);
            
            instances.add(new Instance(instURL));
        }
        
        File outDir = new File("sqetemp");    // TODO: is there any better location? 
        
        return new TestDescriptor(
            outDir,
            new Schema(
                (URL)schemas.get(0),    // FIXME: pass all
                new URL[0],             // no ext binding file
                outDir,
                "generated",            // give them a harmless default value
                false, true,            // TODO: which mode SQE tests are run?
                null,
                new ArrayList() ),
            (Instance[]) instances.toArray(new Instance[instances.size()]) );
    }
    
    /** Builds a JUnit Test  */
    private static Test buildTest( TestDescriptor descriptor,
        final boolean validation, Collection xjcParams ) throws Exception {
        
        TestSuite suite = new TestSuite();
        
        // first, the test compiles a schema
        final CompileTestCase ct = new CompileTestCase(
            descriptor.schema, new InternalCompiler(xjcParams) );
        suite.addTest(ct);
        
        // other tests process instance files.
        for( int i=0; i<descriptor.instances.length; i++ ) {
            final Instance inst = descriptor.instances[i];
            
            // each test unmarshal the instance then marshal it back.
            suite.addTest(new TestCase(inst.getName()) {
                protected void runTest() throws Exception {
                    
                    JAXBContext context = ct.getContext();
                    if(context==null) {
                        fail("schema didn't compile");
                        return;
                    }
                    
                    Unmarshaller unm = context.createUnmarshaller();
                    if(validation) unm.setValidating(true);
                    Object o = unm.unmarshal(inst.document);
                    context.createMarshaller().marshal(o,System.out);
                }
            });
        }
        
        return suite;
    }
}
