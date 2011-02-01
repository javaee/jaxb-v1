/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2011 Oracle and/or its affiliates. All rights reserved.
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

