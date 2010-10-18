/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.reader;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.msv.reader.GrammarReaderController;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.util.ErrorReceiverFilter;

/**
 * {@link ErrorReceiver} that also implements {@link com.sun.msv.reader.GrammarReaderController}.
 * <p>
 * JAXB RI uses {@link ErrorReceiver} to report errors, but
 * MSV (which is used by JAXB RI) uses
 * {@link GrammarReaderController} for that purpose.
 * <p>
 * Thus we need an object that can be used for both, which is this class.
 *  
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class GrammarReaderControllerAdaptor extends ErrorReceiverFilter implements GrammarReaderController {
    
    private final EntityResolver entityResolver;
    
    /**
     * 
     * @param _entityResolver
     *      Can be null.
     */
    public GrammarReaderControllerAdaptor(ErrorReceiver core, EntityResolver _entityResolver) {
        super(core);
        this.entityResolver = _entityResolver;
    }

    public void warning(Locator[] locs, String msg) {
        boolean firstTime = true;
        if( locs!=null ) {
            for( int i=0; i<locs.length; i++ ) {
                if( locs[i]!=null ) {
                    if(firstTime)
                        this.warning(locs[i],msg);
                    else
                        this.warning(locs[i],Messages.format(Messages.ERR_RELEVANT_LOCATION));
                    firstTime = false;
                }
            }
        }
        
        if(firstTime)   // no message has been reported yet.
            this.warning((Locator)null,msg);
    }

    public void error(Locator[] locs, String msg, Exception e) {
        boolean firstTime = true;
        if( locs!=null ) {
            for( int i=0; i<locs.length; i++ ) {
                if( locs[i]!=null ) {
                    if(firstTime)
                        this.error(locs[i],msg);
                    else
                        this.error(locs[i],Messages.format(Messages.ERR_RELEVANT_LOCATION));
                    firstTime = false;
                }
            }
        }
        
        if(firstTime)   // no message has been reported yet.
            this.error((Locator)null,msg);
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if( entityResolver==null )  return null;
        else    return entityResolver.resolveEntity(publicId,systemId);
    }

}
