/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.runtime;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.PrintConversionEventImpl;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.SAXException;

import com.sun.xml.bind.Messages;
import com.sun.xml.bind.serializer.AbortSerializationException;
import com.sun.xml.bind.util.ValidationEventLocatorExImpl;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Util {
// META-IF(M|V)
    /**
     * Reports a print conversion error while marshalling.
     */
    public static void handlePrintConversionException(
        Object caller, Exception e, XMLSerializer serializer ) throws SAXException {
        
        if( e instanceof SAXException )
            // assume this exception is not from application.
            // (e.g., when a marshaller aborts the processing, this exception
            //        will be thrown) 
            throw (SAXException)e;
        
        ValidationEvent ve = new PrintConversionEventImpl(
            ValidationEvent.ERROR, e.getMessage(),
            new ValidationEventLocatorImpl(caller), e );
        serializer.reportError(ve);
    }
    
    /**
     * Reports that the type of an object in a property is unexpected.  
     */
    public static void handleTypeMismatchError( XMLSerializer serializer,
            Object parentObject, String fieldName, Object childObject ) throws AbortSerializationException {
        
         ValidationEvent ve = new ValidationEventImpl(
            ValidationEvent.ERROR, // maybe it should be a fatal error.
            Messages.format(Messages.ERR_TYPE_MISMATCH,
                getUserFriendlyTypeName(parentObject),
                fieldName,
                getUserFriendlyTypeName(childObject) ),
            new ValidationEventLocatorExImpl(parentObject,fieldName) );
         
        serializer.reportError(ve);
    }
    
    private static String getUserFriendlyTypeName( Object o ) {
// META-IF(V)
        if( o instanceof ValidatableObject )
            return ((ValidatableObject)o).getPrimaryInterface().getName();
        else
// META-ENDIF
            return o.getClass().getName();
    }
// META-ENDIF
}
