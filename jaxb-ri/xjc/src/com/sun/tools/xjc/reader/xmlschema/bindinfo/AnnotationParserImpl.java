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

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.io.IOException;

import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.impl.ForkContentHandler;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JCodeModel;
import com.sun.msv.grammar.relaxng.datatype.BuiltinDatatypeLibrary;
import com.sun.msv.verifier.jarv.RELAXNGFactoryImpl;
import com.sun.relaxng.javadt.DatatypeLibraryImpl;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.parser.AnnotationState;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;

/**
 * Implementation of {@link AnnotationParser} of XSOM that
 * parses JAXB customization declarations.
 * 
 * <p>
 * This object returns a Hashtable as a parsed result of annotation.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
public class AnnotationParserImpl extends AnnotationParser {
    public AnnotationParserImpl( JCodeModel cm, Options opts ) {
        this.codeModel=cm;
        this.options=opts;
    }

    private AnnotationState parser = null;
    private final JCodeModel codeModel;
    private final Options options;
    
    public ContentHandler getContentHandler(
        AnnotationContext context, String parentElementName,
        ErrorHandler errorHandler, EntityResolver entityResolver ) {
        
        // return a ContentHandler that validates the customization and also
        // parses them into the internal structure.
        try {
            if(parser!=null)
                // interface contract violation.
                // this method will be called only once.
                throw new JAXBAssertionError();
            
            // set up the actual parser.
            NGCCRuntimeEx runtime = new NGCCRuntimeEx(codeModel,options,errorHandler);
            parser = new AnnotationState(runtime);
            runtime.setRootHandler(parser);
            
            // set up validator
            VerifierFactory factory = new RELAXNGFactoryImpl(); // we need to use a private property exposed.
            factory.setProperty("datatypeLibraryFactory",new DatatypeLibraryFactoryImpl());
            Verifier v = factory.newVerifier(this.getClass().getClassLoader().getResourceAsStream(
                "com/sun/tools/xjc/reader/xmlschema/bindinfo/binding.purified.rng"));
            v.setErrorHandler(errorHandler);

            // the validator will receive events first, then the parser.
            return new ForkContentHandler( v.getVerifierHandler(), runtime );
        } catch( VerifierConfigurationException e ) {
            // there must be something wrong with the deployment.
            e.printStackTrace();
            throw new InternalError();
        } catch( SAXException e ) {
            e.printStackTrace();
            throw new InternalError();
        } catch( IOException e ) {
            e.printStackTrace();
            throw new InternalError();
        }
        
    }

    public Object getResult( Object existing ) {
        if(parser==null)
            // interface contract violation.
            // the getContentHandler method must have been called.
            throw new JAXBAssertionError();
        
        if(existing!=null) {
            BindInfo bie = (BindInfo)existing;
            bie.absorb(parser.bi);
            return bie;
        } else
            return parser.bi;
    }
    
    private static class DatatypeLibraryFactoryImpl implements DatatypeLibraryFactory {
        public DatatypeLibrary createDatatypeLibrary(String namespaceURI) {
            if( namespaceURI.equals("http://www.w3.org/2001/XMLSchema-datatypes") )
                return new com.sun.msv.datatype.xsd.ngimpl.DataTypeLibraryImpl();
            if( namespaceURI.equals("") )
                return BuiltinDatatypeLibrary.theInstance;
            if( namespaceURI.equals("http://java.sun.com/xml/ns/relaxng/java-datatypes") )
                return new DatatypeLibraryImpl();
            return null;
        }
    }
}

