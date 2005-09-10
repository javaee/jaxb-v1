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
 * Use is subject to the license terms.
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
