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
package splitter;

import java.io.File;

import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.DTMNodeProxy;

/**
 * Used from the .ssuite splitter to determine
 * the base directory.
 */
public class SplitterUtil {
    public static String getBaseDir( ExpressionContext context ) {
        // get the base URI of the current document.
        DTM dtm = ((DTMNodeProxy)context.getContextNode()).dtm;
        String uri = dtm.getDocumentBaseURI();

        String path = uri.substring(5); // file:
        
        int idx = path.lastIndexOf('.');
        path = path.substring(0,idx);
        
        if (File.separatorChar == '\\') {
            path = path.replace('\\', '/');
        }
        
        idx = path.lastIndexOf('/');
        return path.substring(idx+1);
    }
}