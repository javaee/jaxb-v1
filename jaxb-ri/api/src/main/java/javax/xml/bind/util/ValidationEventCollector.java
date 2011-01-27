/*
 * $Id: ValidationEventCollector.java,v 1.9 2002/11/11 18:52:21 ryans Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package javax.xml.bind.util;

import java.util.Vector;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;

/**
 * {@link javax.xml.bind.ValidationEventHandler ValidationEventHandler} 
 * implementation that collects all events.
 * 
 * <p>
 * To use this class, create a new instance and pass it to the setEventHandler
 * method of the Validator, Unmarshaller, Marshaller class.  After the call to 
 * validate or unmarshal completes, call the getEvents method to retrieve all 
 * the reported errors and warnings.
 *
 * @author <ul><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li><li>Ryan Shoemaker, Sun Microsystems, Inc.</li><li>Joe Fialli, Sun Microsystems, Inc.</li></ul> 
 * @version $Revision: 1.9 $
 * @see javax.xml.bind.Validator
 * @see javax.xml.bind.ValidationEventHandler
 * @see javax.xml.bind.ValidationEvent
 * @see javax.xml.bind.ValidationEventLocator
 * @since JAXB1.0
 */
public class ValidationEventCollector implements ValidationEventHandler
{
    private final Vector events = new Vector();
     
    /**
     * Return an array of ValidationEvent objects containing a copy of each of 
     * the collected errors and warnings.
     * 
     * @return
     *      a copy of all the collected errors and warnings or an empty array
     *      if there weren't any
     */
    public ValidationEvent[] getEvents() {
        return (ValidationEvent[])events.toArray(new ValidationEvent[events.size()]);
    }
    
    /**
     * Clear all collected errors and warnings.
     */
    public void reset() {
        events.removeAllElements();
    }
    
    /**
     * Returns true if this event collector contains at least one 
     * ValidationEvent.
     *
     * @return true if this event collector contains at least one 
     *         ValidationEvent, false otherwise
     */
    public boolean hasEvents() {
        return events.size() != 0 ? true : false;
    }
    
    public boolean handleEvent( ValidationEvent event ) {        
        events.add(event);

        boolean retVal = true;
        switch( event.getSeverity() ) {
            case ValidationEvent.WARNING:
                retVal = true; // continue validation
                break;
            case ValidationEvent.ERROR:
                retVal = true; // continue validation
                break;
            case ValidationEvent.FATAL_ERROR:
                retVal = false; // halt validation
                break;
            default:
                _assert( false, 
                         Messages.format( Messages.UNRECOGNIZED_SEVERITY,
                                          new Integer( event.getSeverity() ) ) );
                break;
        }
        
        return retVal;
    }

    private static void _assert( boolean b, String msg ) {
        if( !b ) {
            throw new InternalError( msg );
        }
    }
}
