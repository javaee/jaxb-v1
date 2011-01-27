/*
 * $Id: DefaultValidationEventHandler.java,v 1.8 2003/02/28 21:44:25 ryans Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package javax.xml.bind.helpers;

import java.net.URL;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;

import org.w3c.dom.Node;

/**
 * <p>
 * This is the default validation event handler that will be used by the
 * Unmarshaller and Validator if the client application doesn't specify 
 * their own.
 *
 * <p>
 * It will cause the unmarshal and validate operations to fail on the first
 * error or fatal error.
 *
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.8 $
 * @see javax.xml.bind.Unmarshaller
 * @see javax.xml.bind.Validator
 * @see javax.xml.bind.ValidationEventHandler
 * @since JAXB1.0
 */
public class DefaultValidationEventHandler implements ValidationEventHandler {
    
    public boolean handleEvent( ValidationEvent event ) {
        
        if( event == null ) {
            throw new IllegalArgumentException();
        }

        // calculate the severity prefix and return value        
        String severity = null;
        boolean retVal = false;
        switch ( event.getSeverity() ) {
            case ValidationEvent.WARNING:
                severity = Messages.format( Messages.WARNING );
                retVal = true; // continue after warnings
                break;
            case ValidationEvent.ERROR:
                severity = Messages.format( Messages.ERROR );
                retVal = false; // terminate after errors
                break;
            case ValidationEvent.FATAL_ERROR:
                severity = Messages.format( Messages.FATAL_ERROR );
                retVal = false; // terminate after fatal errors
                break;
            default:
                _assert( false, 
                    Messages.format( Messages.UNRECOGNIZED_SEVERITY,
                                     new Integer( event.getSeverity() ) ) );
        }
        
        // calculate the location message
        String location = getLocation( event );
        
        System.out.println( 
            Messages.format( Messages.SEVERITY_MESSAGE,
                             severity,
                             event.getMessage(),
                             location ) );
        
        // fail on the first error or fatal error
        return retVal;
    }

    /**
     * Calculate a location message for the event
     * 
     */
    private String getLocation(ValidationEvent event) {
        StringBuffer msg = new StringBuffer();
        
        ValidationEventLocator locator = event.getLocator();
        
        if( locator != null ) {
            
            URL url = locator.getURL();
            Object obj = locator.getObject();
            Node node = locator.getNode();
            
            if( url != null ) {
                msg.append( "line " + locator.getLineNumber() );
                msg.append( " of " + url.toExternalForm() );
            } else if( obj != null ) {
                msg.append( " obj: " + obj.toString() );
            } else if( node != null ) {
                msg.append( " node: " + node.toString() );
            }
        } else {
            msg.append( Messages.format( Messages.LOCATION_UNAVAILABLE ) );
        } 
        
        return msg.toString();
    }

    
    private static void _assert( boolean b, String msg ) {
        if( !b ) {
            throw new InternalError( msg );
        }
    }
}

