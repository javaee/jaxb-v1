/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.parser;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.impl.Version;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.parsers.XMLGrammarPreparser;
import com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper;
import com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.tools.xjc.ConsoleErrorReporter;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.util.Which;

/**
 * Checks XML Schema XML representation constraints and
 * schema component constraints by using Xerces.
 * 
 * This code relies heavily on internals on Xerces.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SchemaConstraintChecker {
    
    private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    
    /**
     * 
     * @param schemas
     *      Schema files to be checked.
     * @param errorHandler
     *      detected errors will be reported to this handler.
     * @return
     *      true if there was no error, false if there were errors.
     */
    public static boolean check( InputSource[] schemas,
        ErrorReceiver errorHandler, EntityResolver entityResolver ) throws IOException {
        
        checkXercesVersion(errorHandler);
        
        XMLGrammarPreparser preparser = new XMLGrammarPreparser();
        preparser.registerPreparser(XML_SCHEMA,null);
        
        // turn on all the checks on the source schema
        preparser.setFeature(
            "http://apache.org/xml/features/validation/schema-full-checking",true);
        
        ErrorReceiverFilter filter = new ErrorReceiverFilter(errorHandler);
        preparser.setErrorHandler(new ErrorHandlerWrapper(filter));
        if(entityResolver!=null)
            // TODO: a bug in Xerces.
            // Xerces expects an entity resolver to always return something, but
            // that's a wrong assumption. Workaround this problem by using its default
            // entity resolver implementation as the fall-back. 
            preparser.setEntityResolver(
                new XMLFallthroughEntityResolver(
                    new XMLEntityResolverImpl(entityResolver),new XMLEntityManager()));
        // it's not entirely clear to me why I need to set a pool,
        // but unless we have this, apparently Xerces won't try to
        // associate documents that we passed in.
        preparser.setGrammarPool(new XMLGrammarPoolImpl());
        
        try {
            for( int i=0; i<schemas.length; i++ ) {
                // parse all the grammars
                preparser.preparseGrammar(XML_SCHEMA,createXMLInputSource(schemas[i]));
                rewind(schemas[i]);
            }
        } catch( XNIException e ) {
            ; // this error should have been reported. don't report it again.
        }
                
        return !filter.hadError();
    }

    /**
     * Checks the version of Xerces and issue a warning if it's too old.
     */
    private static void checkXercesVersion(ErrorReceiver errorHandler) {
        String version = null;
        try {
            // fVersion field is histroically there first.
            version =(String)Version.class.getField("fVersion").get(null);
        } catch( Throwable t ) {
            // if it fails, try the newer getVersion method.
            try {
                version = Version.getVersion();
            } catch( Throwable tt ) {
                // everything fails.
            }
        }

        if( version!=null ) {
            // try to parse the version number
            StringTokenizer tokens = new StringTokenizer(version);
            while(tokens.hasMoreTokens()) {
                VersionNumber v;
                try {
                    v = new VersionNumber(tokens.nextToken());
                } catch( IllegalArgumentException e ) {
                    continue;   // try the next token
                }
                if( v.isOlderThan(new VersionNumber("2.2")) ) {
                    // we know for sure that 2.0.2 doesn't work.
                    errorHandler.warning( null,
                        Messages.format(Messages.XERCES_TOO_OLD,Which.which(Version.class),version));
                } else {
                    // looks like it's reasonably new.
                    ;
                }
                return;
            }
        }

        errorHandler.warning( null,
            Messages.format(Messages.UNABLE_TO_CHECK_XERCES_VERSION,Which.which(Version.class),version));
        return;
    }
    

    /** constructs an XMLInputSource from SAX InputSource. */
    private static XMLInputSource createXMLInputSource(InputSource is) throws IOException {
        XMLInputSource xis = new XMLInputSource(
            is.getPublicId(),is.getSystemId(),null);
        xis.setByteStream(is.getByteStream());
        xis.setCharacterStream(is.getCharacterStream());
        xis.setEncoding(is.getEncoding());
        return xis;
    }
    
    private static void rewind( InputSource is ) throws IOException {
        if( is.getByteStream()!=null )
            is.getByteStream().reset();
        if( is.getCharacterStream()!=null )
            is.getCharacterStream().reset();
    }
    
    
    // quick test
    public static void main( String[] args ) throws IOException {
        InputSource[] sources = new InputSource[args.length];
        for( int i=0; i<args.length; i++ )
            sources[i] = new InputSource( new File(args[i]).toURL().toExternalForm() );

        check( sources, new ConsoleErrorReporter(), null );
    }
}
