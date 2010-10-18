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
