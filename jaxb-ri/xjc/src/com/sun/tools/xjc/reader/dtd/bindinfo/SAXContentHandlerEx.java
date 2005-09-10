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
package com.sun.tools.xjc.reader.dtd.bindinfo;

import org.dom4j.DocumentFactory;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Locator;

/**
 * dom4j SAXReader with an extension that
 * adds source location information as attributes.
 * 
 * <p>
 * Added location information can be retrieved by calling
 * using {@link DOM4JLocator#getLocationInfo()}.
 */
class SAXContentHandlerEx extends SAXContentHandler
{
    /**
     * Call this method to create a new instance of SAXContentHandlerEx.
     * Because of the restriction of dom4j,
     * we need a bit complicated boot strapping.
     */
    public static SAXContentHandlerEx create() {
        return new SAXContentHandlerEx(new Locator[1]);
    }
    
    private final Locator[] loc;
    
    private SAXContentHandlerEx( Locator[] loc ) {
        super( DocumentFactory.getInstance(), new MyElementHandler(loc) );
        this.loc = loc;
    }
    public void setDocumentLocator( Locator _loc ) {
        loc[0] = _loc;
        super.setDocumentLocator(_loc);
    }

    
    static class MyElementHandler implements ElementHandler {
        private final Locator[] loc;
        
        MyElementHandler( Locator[] loc ) {
            this.loc = loc;
        }
        
        public void onStart( ElementPath path ) {
            // sets location information to elements
            DOM4JLocator.setLocationInfo( path.getCurrent(), loc[0] );
        }
        public void onEnd( ElementPath path ) {}
    }
}
