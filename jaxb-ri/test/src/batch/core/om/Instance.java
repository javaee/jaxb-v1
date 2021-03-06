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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;

import org.dom4j.Element;

import batch.core.JDomUtil;
import bsh.Interpreter;
import bsh.TargetError;

/**
 * Represents an instance document to be used for testing.
 * An instance contains an XML instance document and a bunch
 * of properties.
 */
public class Instance {
    public final URL document;
    public final Properties properties = new Properties();

    /** Creates a test instance without any property value. */
    public Instance( URL _document ) {
        this.document = _document;
    }

    /** Creates a test instance without any property value. */
    public Instance( File _document ) throws IOException {
        this.document = _document.toURL();
    }
    
    /** Creates a test instance from a spec meta file. */
    public Instance( URL baseUrl, Element instance ) throws IOException {
        this( new URL(baseUrl,instance.attributeValue("href")) );
            
        Iterator itr = instance.elementIterator("property");
        while(itr.hasNext()) {
            Element p = (Element)itr.next();
            properties.put(
                p.attributeValue("name"),
                p.elementText("value"));
        }
    }
    
    public Instance( URL _document, Properties params ) {
        this( _document );
        properties.entrySet().addAll( params.entrySet() );
    }
        
    public String getName() {
        return document.toExternalForm();
    }
    
    /**
     * Unmarshal this document by using the specified JAXBContext
     */
    public Object unmarshal( JAXBContext context ) throws JAXBException {
        return context.createUnmarshaller().unmarshal(document);
    }
    
    /**
     * Compares the content tree object with this instance and
     * return true if they are identical.
     */
    public boolean compare( JAXBContext context, Object obj ) throws Exception {
        // go all the way back to byte sequences.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Marshaller m = context.createMarshaller();
        
        m.marshal(obj,baos);
        
        // take this opportunity to marshal to DOM, just to make sure it's working
        m.marshal(obj,new DOMResult());
        
        return JDomUtil.compare(
            JDomUtil.load(document),
            JDomUtil.load(new ByteArrayInputStream(baos.toByteArray())) );
            
        
        // we have to marshaller code path.
        
        //                                   /---> DOM node  
        //                                  /  
        // Content tree ----> SAX events --+
        //                                  \
        //                                   \---> stream
        
        // so it's important to test both.
    }





    

    /**
     * Executes the specified JavaScript with this instance.
     */
    public void runScript( Interpreter interpreter, Reader script ) throws Exception {
        // run
        try {
            interpreter.eval( script );
        } catch( TargetError e ) {
            Throwable t = e.getTarget();
            if(t instanceof Exception)
                throw (Exception)t;
            if( t instanceof Error)
                throw (Error)t;
            throw e;
        }
    }
}
