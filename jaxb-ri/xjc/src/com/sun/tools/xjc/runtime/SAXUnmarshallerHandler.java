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

/*
 * @(#)$Id: SAXUnmarshallerHandler.java,v 1.2 2005-09-10 18:20:44 kohsuke Exp $
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
