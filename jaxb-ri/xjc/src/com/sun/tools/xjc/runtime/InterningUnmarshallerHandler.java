/*
 * @(#)$Id: InterningUnmarshallerHandler.java,v 1.2 2005-09-10 18:20:43 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
