/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.sun.org.apache.xerces.internal.impl.Version;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.sun.codemodel.JCodeModel;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.reader.util.ForkContentHandler;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.ExtensionBindingChecker;
import com.sun.tools.xjc.reader.dtd.TDTDReader;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.DOMForestScanner;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;
import com.sun.tools.xjc.reader.relaxng.CustomizationConverter;
import com.sun.tools.xjc.reader.relaxng.RELAXNGInternalizationLogic;
import com.sun.tools.xjc.reader.relaxng.TRELAXNGReader;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.AnnotationParserFactoryImpl;
import com.sun.tools.xjc.reader.xmlschema.parser.CustomizationContextChecker;
import com.sun.tools.xjc.reader.xmlschema.parser.IncorrectNamespaceURIChecker;
import com.sun.tools.xjc.reader.xmlschema.parser.ProhibitedFeaturesFilter;
import com.sun.tools.xjc.reader.xmlschema.parser.SchemaConstraintChecker;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.util.Which;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.parser.SAXParserFactoryAdaptor;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * Builds a {@link AnnotatedGrammar} object.
 * 
 * This is an utility class that makes it easy to load a grammar object
 * from various sources.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class GrammarLoader {
    
    private final Options opt;
    private final ErrorReceiverFilter errorReceiver;


    /**
     * A convenience method to load schemas into a BGM.
     */
    public static AnnotatedGrammar load( Options opt, ErrorReceiver er )
        throws SAXException, IOException {

        return new GrammarLoader(opt,er).load();
    }
    
    
    public GrammarLoader(Options _opt, ErrorReceiver er) {
        this.opt = _opt;
        this.errorReceiver = new ErrorReceiverFilter(er);
    }

    private AnnotatedGrammar load() throws IOException {
        AnnotatedGrammar grammar;

        if(!sanityCheck())
            return null;
        
        
        try {
            JCodeModel codeModel = new JCodeModel();
            
            switch (opt.getSchemaLanguage()) {
            case Options.SCHEMA_DTD :
                // TODO: make sure that bindFiles,size()<=1
                InputSource bindFile = null;
                if (opt.getBindFiles().length > 0)
                    bindFile = opt.getBindFiles()[0];
                // if there is no binding file, make a dummy one.
                if (bindFile == null) {
                    // if no binding information is specified, provide a default
                    bindFile =
                        new InputSource(
                            new StringReader(
                                "<?xml version='1.0'?><xml-java-binding-schema><options package='"
                                    + (opt.defaultPackage==null?"generated":opt.defaultPackage)
                                    + "'/></xml-java-binding-schema>"));
                }

                checkTooManySchemaErrors();
                grammar = loadDTD(opt.getGrammars()[0], bindFile );
                break;

            case Options.SCHEMA_RELAXNG :
                checkTooManySchemaErrors();
                grammar = loadRELAXNG();
                break;
            
            case Options.SCHEMA_WSDL:
                checkTooManySchemaErrors();
                grammar = annotateXMLSchema( loadWSDL(codeModel), codeModel );
                break;

            case Options.SCHEMA_XMLSCHEMA :
                grammar = annotateXMLSchema( loadXMLSchema(codeModel), codeModel );
                break;
            
            default :
                throw new JAXBAssertionError(); // assertion failed
            }

            if (errorReceiver.hadError())
                grammar = null;
            return grammar;

        } catch (SAXException e) {
            // parsing error in the input document.
            // this error must have been reported to the user vis error handler
            // so don't print it again.
            if (opt.debugMode) {
                // however, a bug in XJC might throw unexpected SAXException.
                // thus when one is debugging, it is useful to print what went
                // wrong.
                if (e.getException() != null)
                    e.getException().printStackTrace();
                else
                    e.printStackTrace();
            }
            return null;
        }
    }



    /**
     * Do some extra checking and return false if the compilation
     * should abort.
     */
    private boolean sanityCheck() {
        if( opt.getSchemaLanguage()==Options.SCHEMA_DTD ) {
            // DTD compilation requires dom4j. dom4j is not a part of JWSDP,
            // so let's check the existance of it and if not, ask the user
            // to download it manually
            try {
                new org.dom4j.DocumentFactory();
            } catch( NoClassDefFoundError e ) {
                errorReceiver.error(null,Messages.format(Messages.MISSING_DOM4J));
                return false;
            }
        }
        if( opt.getSchemaLanguage()==Options.SCHEMA_XMLSCHEMA ) {
            int guess = opt.guessSchemaLanguage();
            
            String[] msg = null;
            switch(guess) {
            case Options.SCHEMA_DTD:
                msg = new String[]{"DTD","-dtd"};
                break;
            case Options.SCHEMA_RELAXNG:
                msg = new String[]{"RELAX NG","-relaxng"};
                break;
            }
            if( msg!=null )
                errorReceiver.warning( null,
                    Messages.format(
                    Messages.EXPERIMENTAL_LANGUAGE_WARNING,
                    msg[0], msg[1] ));
        }
        return true;
    }


    /**
     * {@link XMLParser} implementation that reads from {@link DOMForest}
     * instead of its original source.
     * 
     * <p>
     * This parser will parse a DOM forest as:
     * DOMForestParser -->
     *   ExtensionBindingChecker -->
     *     ProhibitedFeatureFilter -->
     *       XSOMParser
     */
    private class XMLSchemaForestParser implements XMLParser {
        private final XMLParser forestParser;
        
        private XMLSchemaForestParser(DOMForest forest) {
            super();
            forestParser = forest.createParser();
        }
        
        public void parse(InputSource source, ContentHandler handler,
            ErrorHandler errorHandler, EntityResolver entityResolver ) throws SAXException, IOException {
            // set up the chain of handlers.
            handler = wrapBy(
                new ProhibitedFeaturesFilter(errorReceiver, 
                    opt.compatibilityMode == Options.STRICT ? true : false ),
                handler );
            
            handler = wrapBy( new ExtensionBindingChecker(Const.XMLSchemaNSURI, opt, errorReceiver), handler );
            handler = wrapBy( new IncorrectNamespaceURIChecker(errorReceiver), handler );
            handler = wrapBy( new CustomizationContextChecker(errorReceiver), handler );
//          handler = wrapBy( new VersionChecker(controller), handler );
            
            forestParser.parse( source, handler, errorHandler, entityResolver );
        }
        /**
         * Wraps the specified content handler by a filter.
         * It is little awkward to use a helper implementation class like XMLFilterImpl
         * as the method parameter, but this simplifies the code.
         */
        private ContentHandler wrapBy( XMLFilterImpl filter, ContentHandler handler ) {
            filter.setContentHandler(handler);
            return filter;
        }
    }
    




    private void checkTooManySchemaErrors() {
        if( opt.getGrammars().length!=1 )
            errorReceiver.error(null,Messages.format(Messages.ERR_TOO_MANY_SCHEMA));
    }
    
    /**
     * Parses a DTD file into an annotated grammar.
     * 
     * @param   source
     *      DTD file
     * @param   bindFile
     *      External binding file.
     */
    private AnnotatedGrammar loadDTD( InputSource source, InputSource bindFile) {

        // parse the schema as a DTD.
        return TDTDReader.parse(
            source,
            bindFile,
            errorReceiver,
            opt,
            new ExpressionPool());
    }

    /**
     * Builds DOMForest and performs the internalization.
     */
    public DOMForest buildDOMForest( InternalizationLogic logic ) 
        throws SAXException, IOException {
    
        // parse into DOM forest
        DOMForest forest;
        
        try {
            forest = new DOMForest(logic);
        } catch( ParserConfigurationException e ) {
            // in practice, this error won't happen
            throw new SAXException(e);
        }
        
        forest.setErrorHandler(errorReceiver);
        if(opt.entityResolver!=null)
        forest.setEntityResolver(opt.entityResolver);
        
        // parse source grammars
        InputSource[] sources = opt.getGrammars();
        for( int i=0; i<sources.length; i++ )
            forest.parse( sources[i] );
        
        // parse external binding files
        InputSource[] externalBindingFiles = opt.getBindFiles();
        for( int i=0; i<externalBindingFiles.length; i++ ) {
            Element root = forest.parse( externalBindingFiles[i] ).getDocumentElement();
            // TODO: it somehow doesn't feel right to do a validation in the Driver class.
            // think about moving it to somewhere else.
            if( !root.getNamespaceURI().equals(Const.JAXB_NSURI)
            ||  !root.getLocalName().equals("bindings") )
                errorReceiver.error(new SAXParseException(
                    Messages.format(Messages.ERR_NOT_A_BINDING_FILE,
                        root.getNamespaceURI(),
                        root.getLocalName()),
                    null,
                    externalBindingFiles[i].getSystemId(),
                    -1, -1
                ));
        }

        forest.transform();
        
        return forest;
    }
    
    /**
     * Parses a set of XML Schema files into an annotated grammar.
     */
    private XSSchemaSet loadXMLSchema( JCodeModel codeModel )
        throws SAXException, IOException {
        
        try {
            errorReceiver.info(new SAXParseException(
                "Using Xerces from "+Which.which(Version.class),null));
        } catch( Throwable t ) {
            ; // we will check the version of Xerces later and errors will be printed at that time.
        }
        
        try {
            if( opt.strictCheck && !SchemaConstraintChecker.check(opt.getGrammars(),errorReceiver,opt.entityResolver))
                // schema error. error should have been reported
                return null;
        } catch( LinkageError e ) {
            // this relies on the internal unpublished API of Xerces,
            // and from the experience we know people often have problems.
            // so be prepared and diagnose the problem for them.
            errorReceiver.warning(new SAXParseException(
                Messages.format(Messages.ERR_INCOMPATIBLE_XERCES,e.toString()),null));

            // print out the location where Xerces is being loaded from.
            // this time it should be a warning so that it can be visible to
            // users
            try {
                errorReceiver.warning(new SAXParseException(
                    "Using Xerces from "+Which.which(Version.class),null));
            } catch( Throwable t ) {
                ;
            }
            
            if(opt.debugMode)
                // let the user to obtain the full stack trace if he wants to.
                throw e;
            // otherwise proceed by assuming that the schema is OK.
        }

        DOMForest forest = buildDOMForest( new XMLSchemaInternalizationLogic() );
        
        // load XML Schema from DOMForest instead of loading from its original source.
        
        XSOMParser xsomParser = createXSOMParser(forest, codeModel);
        
        // re-parse the transformed schemas
        InputSource[] grammars = opt.getGrammars();
        for( int i=0; i<grammars.length; i++ )
            xsomParser.parse( grammars[i] );
        
        return xsomParser.getResult();
    }
    
    /**
     * Parses a set of schemas inside a WSDL file.
     * 
     * A WSDL file may contain multiple &lt;xsd:schema> elements.
     */
    private XSSchemaSet loadWSDL( JCodeModel codeModel )
        throws SAXException, IOException {

        
        // build DOMForest just like we handle XML Schema
        DOMForest forest = buildDOMForest( new XMLSchemaInternalizationLogic() );
        
        DOMForestScanner scanner = new DOMForestScanner(forest);
        
        XSOMParser xsomParser = createXSOMParser( forest, codeModel );
        
        // find <xsd:schema>s and parse them individually
        InputSource[] grammars = opt.getGrammars();
        Document wsdlDom = forest.get( grammars[0].getSystemId() );
        
        NodeList schemas = wsdlDom.getElementsByTagNameNS(Const.XMLSchemaNSURI,"schema");
        for( int i=0; i<schemas.getLength(); i++ )
            scanner.scan( (Element)schemas.item(i), xsomParser.getParserHandler() );
        
        return xsomParser.getResult();
    }
    
    /**
     * Annotates the obtained schema set.
     * 
     * @return
     *      null if an error happens. In that case, the error messages
     *      will be properly reported to the controller by this method.
     */
    public AnnotatedGrammar annotateXMLSchema(XSSchemaSet xs,JCodeModel codeModel) throws SAXException {
        if (xs == null)
            return null;
        
        return BGMBuilder.build(xs, codeModel, errorReceiver, opt.defaultPackage, opt.compatibilityMode==Options.EXTENSION);
    }
    
    /**
     * Creates a properly configured XSOMParser
     * that parses from a given DOMForest.
     */
    public XSOMParser createXSOMParser(DOMForest forest, JCodeModel codeModel) {
        // set up other parameters to XSOMParser
        XSOMParser reader = new XSOMParser(new XMLSchemaForestParser(forest));
        reader.setAnnotationParser(new AnnotationParserFactoryImpl(codeModel,opt));
        reader.setErrorHandler(errorReceiver);
        return reader;
    }
    
    /**
     * Parses a RELAX NG grammar into an annotated grammar.
     */
    private AnnotatedGrammar loadRELAXNG() throws IOException, SAXException {

        // build DOM forest
        final DOMForest forest = buildDOMForest( new RELAXNGInternalizationLogic() );

        // fix up customization
        new CustomizationConverter(opt).fixup(forest);

        // DOMForest -> ExtensionBindingChecker -> 
        XMLParser parser = new XMLParser() {
            private final XMLParser forestParser = forest.createParser();
            
            public void parse(InputSource source, ContentHandler handler,
                ErrorHandler errorHandler, EntityResolver entityResolver ) throws SAXException, IOException {
                
                handler = wrapBy( new ExtensionBindingChecker(Const.RELAXNG_URI, opt, errorReceiver), handler );
                
                if( opt.strictCheck ) {
                    try {
                        Verifier verifier = TRELAXNGReader.getRELAXNGSchema4Schema().newVerifier();
                        verifier.setErrorHandler(errorHandler);
                        verifier.setEntityResolver(entityResolver);
                        handler = new ForkContentHandler( verifier.getVerifierHandler(), handler );
                    } catch( VerifierConfigurationException e ) {
                        // impossible, since we are loading the fixed schema
                        e.printStackTrace();
                        throw new InternalError();
                    }
                }
                
                forestParser.parse( source, handler, errorHandler, entityResolver );
            }
            /**
             * Wraps the specified content handler by a filter.
             * It is little awkward to use a helper implementation class like XMLFilterImpl
             * as the method parameter, but this simplifies the code.
             */
            private ContentHandler wrapBy( XMLFilterImpl filter, ContentHandler handler ) {
                filter.setContentHandler(handler);
                return filter;
            }
        };

        // use JAXP masquerading to validate the input document.
        // DOMForest -> ExtensionBindingChecker -> validation -> RELAXNGReader
        
        SAXParserFactory parserFactory = new SAXParserFactoryAdaptor(parser);
        parserFactory.setNamespaceAware(true);
        
        TRELAXNGReader reader = new TRELAXNGReader(
            errorReceiver,
            opt.entityResolver,
            parserFactory,
//            new com.sun.msv.verifier.jaxp.SAXParserFactoryImpl(
//                new SAXParserFactoryAdaptor(parser),
//                TRELAXNGReader.getRELAXNGSchema4Schema()),
            opt.defaultPackage);
        
        // parse the source grammar
        reader.parse( opt.getGrammars()[0] );
        
        return reader.getAnnotatedResult();
    }
}
