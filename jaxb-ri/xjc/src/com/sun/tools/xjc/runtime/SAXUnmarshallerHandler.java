/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: SAXUnmarshallerHandler.java,v 1.1 2004-06-25 21:15:24 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEvent;

import org.xml.sax.SAXException;

/**
 * Unified event handler that processes
 * both the SAX events and error events.
 * 
 * <p>
 * This interface refines {@link ContentHandler} as follows:
 * <ol>
 *  <li>element names and attribute names must be {@link String#intern()}ed.
 *  <li>namespace prefix and uris must be {@link String#intern()}ed.
 * </ol>
 */
public interface SAXUnmarshallerHandler extends UnmarshallerHandler {
    
    /**
     * Reports an error to the user, and asks if s/he wants
     * to recover. If the canRecover flag is false, regardless
     * of the client instruction, an exception will be thrown.
     * 
     * Only if the flag is true and the user wants to recover from an error,
     * the method returns normally.
     * 
     * The thrown exception will be catched by the unmarshaller.
     */
    void handleEvent( ValidationEvent event, boolean canRecover ) throws SAXException;
}
