/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package batch.core;

import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.Marshaller;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.input.SAXHandler;
import org.jdom.output.SAXOutputter;
import org.jdom.transform.JDOMResult;
import org.vmguys.vmtools.utils.CostOps;
import org.vmguys.vmtools.utils.DifferenceFinder2;
import org.vmguys.vmtools.utils.DomFactory;
import org.xml.sax.InputSource;

/**
 * Various utility methods for JDOM.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class JDomUtil {
    
    /** Loads an XML document. */
    public static Document load( InputSource is ) throws Exception {
        return new SAXBuilder().build(is);
    }
    
    /** Loads an XML document. */
    public static Document load( URL source ) throws Exception {
        return new SAXBuilder().build(source);
    }
    
    /** Loads an XML document. */
    public static Document load( InputStream source ) throws Exception {
        return new SAXBuilder().build(source);
    }
    
    /** Marshals a JAXB content tree into JDOM. */
    public static Document marshal( Object obj, Marshaller m ) throws Exception {
        JDOMResult r = new JDOMResult();
        m.marshal(obj,r);
        return r.getDocument();
    }
    
    /**
     * Compares two Document objects by using VMTools.
     * 
     * @return true
     *      if two documents are judged identical.
     */
    public static boolean compare( Document a, Document b ) throws Exception {
        
        // due to the changes in VMTools 0.5, we have to recreate trees
        SAXHandler handler = new SAXHandler(new DomFactory());
        SAXOutputter writer = new SAXOutputter(handler);
        writer.setLexicalHandler(handler);
        
        writer.output(a);
        a = handler.getDocument();
        
        writer.output(b);
        b = handler.getDocument();
        
        DifferenceFinder2 df = new DifferenceFinder2();
        CostOps ops = df.findDifferences(
            a.getRootElement(),b.getRootElement());
        return ops.getCost()==0;
    }
}
