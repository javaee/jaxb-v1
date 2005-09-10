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

/*
 * @(#)$Id: DefaultJAXBContextImpl.java,v 1.3 2005-09-10 18:20:42 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import com.sun.xml.bind.Messages;
import com.sun.xml.bind.DatatypeConverterImpl;

/**
 * This class provides the default implementation of JAXBContext.  It
 * also creates the GrammarInfoFacade that unifies all of the grammar
 * info from packages on the contextPath.
 *
 * @version $Revision: 1.3 $
 */
public class DefaultJAXBContextImpl extends JAXBContext {
    
    /**
     * This object keeps information about the grammar.
     * 
     * When more than one package are specified,
     * GrammarInfoFacade is used.
     */
    private GrammarInfo gi = null;

    /**
     * This is the constructor used by javax.xml.bind.FactoryFinder which
     * bootstraps the RI.  It causes the construction of a JAXBContext that
     * contains a GrammarInfoFacade which is the union of all the generated
     * JAXBContextImpl objects on the contextPath.
     */
    public DefaultJAXBContextImpl( String contextPath, ClassLoader classLoader ) 
        throws JAXBException {
            
        this( GrammarInfoFacade.createGrammarInfoFacade( contextPath, classLoader ) );

        // initialize datatype converter with ours
        DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
    }
    
    /**
     * This constructor is used by the default no-arg constructor in the
     * generated JAXBContextImpl objects.  It is also used by the 
     * bootstrapping constructor in this class.
     */
    public DefaultJAXBContextImpl( GrammarInfo gi ) {
        this.gi = gi;
    }
        
    public GrammarInfo getGrammarInfo() { 
        return gi;
    }
    
    
    
// META-IF(W)
    /**
     * Once we load a grammar, we will cache the value here.
     */
    private com.sun.msv.grammar.Grammar grammar = null;
    
    /**
     * Loads a grammar object for the unmarshal-time validation.
     * 
     * <p>
     * getGrammar is normally very expensive, so it's worth
     * synchronizing to avoid unnecessary invocation.
     */
    public synchronized com.sun.msv.grammar.Grammar getGrammar() throws JAXBException {
        if( grammar==null )
            grammar = gi.getGrammar();
        return grammar;
    }
// META-ENDIF
    
    
    /**
     * Create a <CODE>Marshaller</CODE> object that can be used to convert a
     * java content-tree into XML data.
     *
     * @return a <CODE>Marshaller</CODE> object
     * @throws JAXBException if an error was encountered while creating the
     *                      <code>Marshaller</code> object
     */
    public Marshaller createMarshaller() throws JAXBException {
        if( MetaVariable.M ) { // META-IF(M)
            return new MarshallerImpl( this );
        } else { // META-ELSE
            throw new UnsupportedOperationException(
                "When generating this code, the compiler option was specified not to generate the marshaller");
        } // META-ENDIF
    }
       
    /**
     * Create an <CODE>Unmarshaller</CODE> object that can be used to convert XML
     * data into a java content-tree.
     *
     * @return an <CODE>Unmarshaller</CODE> object
     * @throws JAXBException if an error was encountered while creating the
     *                      <code>Unmarshaller</code> object
     */
    public Unmarshaller createUnmarshaller() throws JAXBException {
        if( MetaVariable.U ) { // META-IF(U)
            return new UnmarshallerImpl( this, gi );
        } else { // META-ELSE
            throw new UnsupportedOperationException(
                "When generating this code, the compiler option was specified not to generate the unmarshaller");
        } // META-ENDIF
    }    
        
    /**
     * Create a <CODE>Validator</CODE> object that can be used to validate a
     * java content-tree.
     *
     * @return an <CODE>Unmarshaller</CODE> object
     * @throws JAXBException if an error was encountered while creating the
     *                      <code>Validator</code> object
     */
    public Validator createValidator() throws JAXBException {
        if( MetaVariable.V ) { // META-IF(V)
            return new ValidatorImpl( this );
        } else { // META-ELSE
            throw new UnsupportedOperationException(
                "When generating this code, the compiler option was specified not to generate the validator");
        } // META-ENDIF
    }
    

    
    /**
     * Create an instance of the specified Java content interface.  
     *
     * @param javaContentInterface the Class object 
     * @return an instance of the Java content interface
     * @exception JAXBException
     */
    public Object newInstance( Class javaContentInterface ) 
        throws JAXBException {

        if( javaContentInterface == null ) {
            throw new JAXBException( Messages.format( Messages.CI_NOT_NULL ) );
        }

        try {
            Class c = gi.getDefaultImplementation( javaContentInterface );
            if(c==null)
                throw new JAXBException(
                    Messages.format( Messages.MISSING_INTERFACE, javaContentInterface ));
            
            return c.newInstance();
        } catch( Exception e ) {
            throw new JAXBException( e );
        } 
    }
    
    /**
     * There are no required properties, so simply throw an exception.  Other
     * providers may have support for properties on Validator, but the RI doesn't
     */
    public void setProperty( String name, Object value )
        throws PropertyException {
        
        throw new PropertyException(name, value);
    }
    
    /**
     * There are no required properties, so simply throw an exception.  Other
     * providers may have support for properties on Validator, but the RI doesn't
     */
    public Object getProperty( String name )
        throws PropertyException {
            
        throw new PropertyException(name);
    }
    
    
}
