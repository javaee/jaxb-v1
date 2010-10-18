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

package batch.core.om;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.iso_relax.jaxp.ValidatingSAXParserFactory;

import batch.core.Util;
import batch.core.VersionProcessor;

import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;
import com.sun.xml.bind.JAXBAssertionError;
import com.thaiopensource.relaxng.jarv.RelaxNgCompactSyntaxVerifierFactory;

/**
 * In-memory representation of <code>testspec.meta</code>.
 * 
 * <p>
 * This object is immutable once created.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TestDescriptor {
    
    /**
     * URL to the <tt>testspec.meta</tt> file.
     * This field is null if the object is created programatically
     * and not from a description file.
     */
    public final URL testSpecUrl;
    
    /** temporary directory to be used to generate Java sources/binaries. */
    public final File outDir;
    
    public final VersionProcessor ver;    
    
    /**
     * Information about the schema to be compiled.
     */
    public final Schema schema;
    
    /**
     * Instance documents in this test.
     */
    public final Instance[] instances;

    public TestDescriptor(File _outDir, Schema _schema, Instance[] _instances) {
        this.outDir = _outDir;
        this.schema = _schema;
        this.instances = _instances;
        
        this.testSpecUrl = null;
        this.ver = new VersionProcessor();
    } 
    
    /**
     * Parses the <tt>testspec&#x2E;meta</tt> file and build a
     * {@link TestDescriptor} object.
     */
    public TestDescriptor( File metaFile ) throws Exception { 
        File testDir = new File(metaFile.getParent());
        outDir = new File(testDir,"gen");
        
        // get the test spec document
        SAXParserFactory factory = ValidatingSAXParserFactory.newInstance(descriptorSchema);
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        Document doc = new SAXReader(factory.newSAXParser().getXMLReader()).read(metaFile);
        Element testSpec = doc.getRootElement();
        testSpecUrl = metaFile.toURL();
        
        ver = new VersionProcessor(doc);
        
        // parse external binding files
        List bindings = new ArrayList();
        Iterator itr = testSpec.elementIterator("bindings");
        while( itr.hasNext() ) {
            Element e = (Element)itr.next();
            bindings.add( new URL(testSpecUrl,e.attributeValue("href")) );
        }

        // parse the schema info
        Element schema = testSpec.element("schema");
        
        ClassLoader parent = null;
        if(schema.attribute("classLoader")!=null)
            parent = Util.getForeheadClassLoader(schema.attributeValue("classLoader"));

        this.schema = new Schema(
            new URL(testSpecUrl,schema.attributeValue("href")),
            (URL[]) bindings.toArray(new URL[bindings.size()]),
            outDir,
            testSpec.elementTextTrim("package"),
            "true".equals(schema.attributeValue("strict")),
            "true".equals(schema.attributeValue("fail")),
            parent,
            tokenize(schema.attributeValue("xjcopts","")));
        
        // parse instances
        List instances = new ArrayList();
        itr = testSpec.elementIterator("instance");
        while(itr.hasNext()) {
            instances.add( new Instance( testSpecUrl, (Element)itr.next() ) );
        }
        this.instances = (Instance[]) instances.toArray(new Instance[instances.size()]);

        if( this.schema.isNegativeTest && this.instances.length!=0 )
            throw new IllegalArgumentException( testSpecUrl + " is a negative compiler test but it has instance documents");
    }
    
    /**
     * Splits the given string by whitespace and returns it
     * as a list.
     */
    private List tokenize(String s) {
        s=s.trim();
        if(s.length()==0)   return Collections.EMPTY_LIST;
        return Arrays.asList(s.split(" +"));
    }

    /**
     * Gets the human readable name.
     */
    public String getName() {
        return testSpecUrl.toString();
    }
    
    public boolean isApplicable(VersionNumber v) {
        return ver.isApplicable(v);
    }
    
    
    public static final org.iso_relax.verifier.Schema descriptorSchema;
    
    static {
        URL url = TestDescriptor.class.getResource("/metadataFormat.rnc");
        if(url==null)
            throw new JAXBAssertionError("unable to find metadataFormat.rnc");
        try {
            descriptorSchema = new RelaxNgCompactSyntaxVerifierFactory().compileSchema(url.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
            throw new JAXBAssertionError("unable to parse metadataFormat.rnc");
        }
    }
}
