/*
 * @(#)$Id: TestDescriptor.java,v 1.1 2004-06-25 21:13:02 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
