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

package com.sun.tools.xjc.reader.xmlschema.parser;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

/**
 * Wraps {@link EntityResolver}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class XMLEntityResolverImpl implements XMLEntityResolver {
    
    private final EntityResolver entityResolver;

    XMLEntityResolverImpl( EntityResolver er ) {
        if(er==null)    throw new NullPointerException();
        this.entityResolver = er;
    }
    
    public XMLInputSource resolveEntity(XMLResourceIdentifier r) throws XNIException, IOException {
        String publicId = r.getPublicId();
        String systemId = r.getExpandedSystemId();
        
        if(publicId==null)
            // perhaps this is for resolving a schema document.
            // I know this is an abuse of the public ID, but I claim
            // it's still better than not providing any entity resolution
            // support at all.
            //
            // this at least allows users to resolve
            // <xs:import> without @schemaLocation
            publicId = r.getNamespace();
        
        try {
            InputSource is = entityResolver.resolveEntity(publicId, systemId);
            if(is==null)        return null;
            
            XMLInputSource xis = new XMLInputSource(
                is.getPublicId(),
                is.getSystemId(),
                r.getBaseSystemId(),
                is.getByteStream(),
                is.getEncoding());
            
            xis.setCharacterStream(is.getCharacterStream());
            return xis;
        } catch (SAXException e) {
            throw new XNIException(e);
        }
        
    }
}
