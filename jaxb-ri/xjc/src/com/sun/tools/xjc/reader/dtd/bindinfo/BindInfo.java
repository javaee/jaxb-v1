/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.msv.reader.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Root of the binding information.
 */
public class BindInfo
{
    /** Controller object that can be used to report errors. */
    protected final ErrorReceiver errorReceiver;
    
    private final Options options;
    
    /**
     * The -p option that should control the default Java package that
     * will contain the generated code. Null if unspecified. This takes
     * precedence over the value specified in the binding file.
     */
    private final String defaultPackage;
    
    public BindInfo( InputSource source, ErrorReceiver _errorReceiver,
        JCodeModel _codeModel, Options opts ) throws AbortException {
        
        this( parse(source,_errorReceiver), _errorReceiver, _codeModel, opts );
    }
    
    public BindInfo( Document _dom, ErrorReceiver _errorReceiver, JCodeModel _codeModel, Options opts ) {
        this.dom = _dom.getRootElement();
        this.codeModel = _codeModel;
        this.options = opts;
        this.errorReceiver = _errorReceiver;
        this.classFactory = new CodeModelClassFactory(_errorReceiver);
        // TODO: decide name converter from the binding file
        this.nameConverter = NameConverter.standard;
        
        this.defaultPackage = opts.defaultPackage;
        
        Iterator itr;
        
        // process element declarations
        itr = dom.elementIterator("element");
        while( itr.hasNext() ) {
            BIElement e = new BIElement(this,(Element)itr.next());
            elements.put(e.name(),e);
        }

        // add built-in conversions
        BIUserConversion.addBuiltinConversions(this,conversions);
        
        // process conversion declarations
        itr = dom.elementIterator("conversion");
        while( itr.hasNext() ) {
            BIConversion c = new BIUserConversion(this,(Element)itr.next());
            conversions.put(c.name(),c);
        }
        itr = dom.elementIterator("enumeration");
        while( itr.hasNext() ) {
            BIConversion c = BIEnumeration.create( (Element)itr.next(), this );
            conversions.put(c.name(),c);
        }
        // TODO: check the uniquness of conversion name
        
        
        // process interface definitions
        itr = dom.elementIterator("interface");
        while( itr.hasNext() ) {
            BIInterface c = new BIInterface( (Element)itr.next() );
            interfaces.put(c.name(),c);
        }
        
        options.generateMarshallingCode =
            dom.element(new QName("noMarshaller",XJC_NS))==null;
        options.generateUnmarshallingCode =
            dom.element(new QName("noUnmarshaller",XJC_NS))==null;
        options.generateValidationCode =
            dom.element(new QName("noValidator",XJC_NS))==null;
        options.generateValidatingUnmarshallingCode =
            dom.element(new QName("noValidatingUnmarshaller",XJC_NS))==null;
        if( !options.generateUnmarshallingCode )
            options.generateValidatingUnmarshallingCode = false;
    }
    
    
    /** CodeModel object that is used by this binding file. */
    final JCodeModel codeModel;
    
    /** Wrap the codeModel object and automate error reporting. */
    final CodeModelClassFactory classFactory;
    
    /** Used to convert XML names to Java names. */
    final NameConverter nameConverter;
    
    /** DOM tree that represents binding info. */
    private final Element dom;

    /** Conversion declarations. */
    private final Map conversions = new java.util.HashMap();

    /** Element declarations. */
    private final Map elements = new java.util.HashMap();
    
    /** interface declarations. */
    private final Map interfaces = new java.util.HashMap();
  
    
    /** XJC extension namespace. */
    private static final Namespace XJC_NS = Namespace.get(Const.XJC_EXTENSION_URI);
    
//
//
//    Exposed public methods
//
//
    /** Gets the serialVersionUID if it's turned on. */
    public Long getSerialVersionUID() {
        Element serial = dom.element(new QName("serializable",XJC_NS));
        if(serial==null)    return null;
        
        return new Long(serial.attributeValue("uid","1"));
    }
    
    /** Gets the xjc:superClass customization if it's turned on. */
    public JClass getSuperClass() {
        Element sc = dom.element(new QName("superClass",XJC_NS));
        if(sc==null)        return null;

        JDefinedClass c;
        
        try {
          c = codeModel._class(sc.attributeValue("name","java.lang.Object"));
          c.hide();
        } catch( JClassAlreadyExistsException e ) {
          c = e.getExistingClass();
        }

        return c;
    }
    
    /** Gets the specified package name (options/@package). */
    public JPackage getTargetPackage() {
        String p;
        if( defaultPackage!=null )
            p = defaultPackage;
        else
            p = getOption("package", "");
        return codeModel._package(p);
    }

    /**
     * Gets the conversion declaration from the binding info.
     * 
     * @return
     *        A non-null valid BIConversion object.
     */
    public BIConversion conversion(String name) {
        BIConversion r = (BIConversion)conversions.get(name);
        if (r == null)
            throw new JAXBAssertionError("undefined conversion name: this should be checked by the validator before we read it");
        return r;
    }
    
    /**
     * Gets the element declaration from the binding info.
     * 
     * @return
     *        If there is no declaration with a given name,
     *        this method returns null.
     */
    public BIElement element( String name ) {
        return (BIElement)elements.get(name);
    }
    /** Iterates all {@link BIElement}s. */
    public Iterator elements() {
        return elements.values().iterator();
    }
    
    /** Iterates all {@link BIInterface}s. */
    public Iterator interfaces() {
        return interfaces.values().iterator();
    }
    
    
    
//
//
//    Internal utility methods
//
//
    
    
    /** Gets the value from the option element. */
    private String getOption(String attName, String defaultValue) {
        Element opt = dom.element("options");
        if (opt != null) {
            String s = opt.attributeValue(attName);
            if (s != null)
                return s;
        }
        return defaultValue;
    }

    
    /**
     * Parses an InputSource into dom4j Document.
     * Returns null in case of an exception.
     */
    private static Document parse( InputSource is, ErrorReceiver receiver ) throws AbortException {
        try {
            // validate the bind info file
            VerifierFactory factory = new com.sun.msv.verifier.jarv.RELAXNGFactoryImpl();
            VerifierFilter verifier = factory.newVerifier(
                BindInfo.class.getResourceAsStream("bindingfile.rng")).getVerifierFilter();
            
            // set up the pipe line as :
            //   parser->validator->dom4jbuilder
            SAXParserFactory pf = SAXParserFactory.newInstance();
            pf.setNamespaceAware(true);
            SAXContentHandlerEx builder = SAXContentHandlerEx.create();
            
            ErrorReceiverFilter controller = new ErrorReceiverFilter(receiver);
            verifier.setContentHandler(builder);
            verifier.setErrorHandler(controller);
            verifier.setParent(pf.newSAXParser().getXMLReader());
            verifier.parse(is);
            
            if(controller.hadError())   throw AbortException.theInstance;
            return builder.getDocument();
        } catch( IOException e ) {
            receiver.error( new SAXParseException(e.getMessage(),null,e) );
        } catch( SAXException e ) {
            receiver.error( new SAXParseException(e.getMessage(),null,e) );
        } catch( VerifierConfigurationException ve ) {
            ve.printStackTrace();
        } catch( javax.xml.parsers.ParserConfigurationException e ) {
            receiver.error( new SAXParseException(e.getMessage(),null,e) );
        }
        
        throw AbortException.theInstance;
    }
/*    
    private static void convertException( Throwable e ) throws IOException, SAXException {
        if( e instanceof IOException )
            throw (IOException)e;
        if( e instanceof SAXException )
            throw (SAXException)e;
        if( e instanceof RuntimeException )
            throw (RuntimeException)e;
        if( e instanceof Exception )
            throw new SAXException((Exception)e);
        
        // more serious error.
        e.printStackTrace();
    }
*/
}
