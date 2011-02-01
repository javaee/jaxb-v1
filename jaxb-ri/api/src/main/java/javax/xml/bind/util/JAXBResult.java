/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2011 Oracle and/or its affiliates. All rights reserved.
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

package javax.xml.bind.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.transform.sax.SAXResult;

/**
 * JAXP {@link javax.xml.transform.Result} implementation
 * that unmarshals a JAXB object.
 * 
 * <p>
 * This utility class is useful to combine JAXB with
 * other Java/XML technologies.
 * 
 * <p>
 * The following example shows how to use JAXB to unmarshal a document
 * resulting from an XSLT transformation.
 * 
 * <blockquote>
 *    <pre>
 *       JAXBResult result = new JAXBResult(
 *         JAXBContext.newInstance("org.acme.foo") );
 *       
 *       // set up XSLT transformation
 *       TransformerFactory tf = TransformerFactory.newInstance();
 *       Transformer t = tf.newTransformer(new StreamSource("test.xsl"));
 *       
 *       // run transformation
 *       t.transform(new StreamSource("document.xml"),result);
 * 
 *       // obtain the unmarshalled content tree
 *       Object o = result.getReult();
 *    </pre>
 * </blockquote>
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JAXBResult extends SAXResult {

    /**
     * Creates a new instance that uses the specified
     * JAXBContext to unmarshal.
     * 
     * @param context The JAXBContext that will be used to create the
     * necessary Unmarshaller.  This parameter must not be null.
     * @exception JAXBException if an error is encountered while creating the
     * JAXBResult or if the context parameter is null.
     */
    public JAXBResult( JAXBContext context ) throws JAXBException {
        this( ( context == null ) ? assertionFailed() : context.createUnmarshaller() );
    }
    
    /**
     * Creates a new instance that uses the specified
     * Unmarshaler to unmarshal an object.
     * 
     * <p>
     * This JAXBResult object will use the specified Unmarshaller
     * instance. It is the caller's responsibility not to use the
     * same Unmarshaller for other purposes while it is being
     * used by this object.
     * 
     * <p>
     * The primary purpose of this method is to allow the client
     * to configure Unmarshaller. Unless you know what you are doing,
     * it's easier and safer to pass a JAXBContext.
     * 
     * @param _unmarshaller the unmarshaller.  This parameter must not be null.
     * @throws JAXBException if an error is encountered while creating the
     * JAXBResult or the Unmarshaller parameter is null.
     */
    public JAXBResult( Unmarshaller _unmarshaller ) throws JAXBException {
        if( _unmarshaller == null )
            throw new JAXBException( 
                Messages.format( Messages.RESULT_NULL_UNMARSHALLER ) );
            
        this.unmarshallerHandler = _unmarshaller.getUnmarshallerHandler();
        
        super.setHandler(unmarshallerHandler);
    }
    
    /**
     * Unmarshaller that will be used to unmarshal
     * the input documents.
     */
    private final UnmarshallerHandler unmarshallerHandler;

    /**
     * Gets the unmarshalled object created by the transformation.
     * 
     * @return
     *      Always return a non-null object.
     * 
     * @exception IllegalStateException
     * 	if this method is called before an object is unmarshalled.
     * 
     * @exception JAXBException
     *      if there is any unmarshalling error.
     *      Note that the implementation is allowed to throw SAXException
     *      during the parsing when it finds an error.
     */
    public Object getResult() throws JAXBException {
        return unmarshallerHandler.getResult();
    }
    
    /**
     * Hook to throw exception from the middle of a contructor chained call
     * to this
     */
    private static Unmarshaller assertionFailed() throws JAXBException {
        throw new JAXBException( Messages.format( Messages.RESULT_NULL_CONTEXT ) );
    }
}
