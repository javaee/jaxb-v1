/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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