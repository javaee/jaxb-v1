/*
 * @(#)$Id: InterningUnmarshallerHandler.java,v 1.1 2004-06-25 21:15:22 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.runtime;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;

import org.xml.sax.SAXException;

import com.sun.xml.bind.unmarshaller.InterningXMLReader;

/**
 * Filter {@link SAXUnmarshallerHandler} that interns all the Strings
 * in the SAX events. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class InterningUnmarshallerHandler extends InterningXMLReader implements SAXUnmarshallerHandler {
    
    private final SAXUnmarshallerHandler core;
    
    InterningUnmarshallerHandler( SAXUnmarshallerHandler core ) {
        super();
        setContentHandler(core);
        this.core = core;
    }
    
    public void handleEvent(ValidationEvent event, boolean canRecover) throws SAXException {
        core.handleEvent(event,canRecover);
    }

    public Object getResult() throws JAXBException, IllegalStateException {
        return core.getResult();
    }

}
