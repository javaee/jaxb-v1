/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import com.sun.tools.xjc.ErrorReceiver;

/**
 * Provides error report capability to other builder components
 * by encapsulating user-specified {@link ErrorHandler}
 * and exposing utlity methods.
 * 
 * <p>
 * This class also wraps SAXException to a RuntimeException
 * so that the exception thrown inside the error handler
 * can abort the process.
 * 
 * <p>
 * At the end of the day, we need to know if there was any error.
 * So it is important that all the error messages go through this
 * object. This is done by hiding the errorHandler from the rest
 * of the components.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ErrorReporter {
    
    /**
     * Error handler to report any binding error to.
     * To report errors, use the error method.
     */
    private ErrorReceiver errorReceiver;
    
    private boolean hadError = false;
    
    
    ErrorReporter( ErrorReceiver handler ) {
        setErrorHandler(handler);
    }
    
    boolean hadError() { return hadError; }
    
    void setErrorHandler( ErrorReceiver h ) {
        this.errorReceiver = h;
    }
    
    
    //
    // helper methods for classes in this package.
    //    properties are localized through the Messages.properties file
    //    in this package
    //
    void error( Locator loc, String prop ) {
        error( loc, prop, new Object[]{} );
    }
    void error( Locator loc, String prop, Object arg1 ) {
        error( loc, prop, new Object[]{arg1} );
    }
    void error( Locator loc, String prop, Object arg1, Object arg2 ) {
        error( loc, prop, new Object[]{arg1,arg2} );
    }
    void error( Locator loc, String prop, Object arg1, Object arg2, Object arg3 ) {
        error( loc, prop, new Object[]{arg1,arg2,arg3} );
    }
    void error( Locator loc, String prop, Object[] args ) {
        errorReceiver.error( loc, Messages.format(prop,args) );
    }
    
    void warning( Locator loc, String prop, Object[] args ) {
        errorReceiver.warning( new SAXParseException(
            Messages.format(prop,args), loc ));
    }
    
    
    
    /*
    private String format( String prop, Object[] args ) {
        // use a bit verbose code to make it portable.
        String className = this.getClass().getName();
        int idx = className.lastIndexOf('.');
        String packageName = className.substring(0,idx);
        
        String fmt = ResourceBundle.getBundle(packageName+".Messages").getString(prop);
        
        return MessageFormat.format(fmt,args);
    }
    */
    
////
////
//// ErrorHandler implementation
////
////
//    public void error(SAXParseException exception) {
//        errorReceiver.error(exception);
//    }
//
//    public void fatalError(SAXParseException exception) {
//        errorReceiver.fatalError(exception);
//    }
//
//    public void warning(SAXParseException exception) {
//        errorReceiver.warning(exception);
//    }

}
