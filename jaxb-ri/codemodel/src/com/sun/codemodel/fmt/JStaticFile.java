/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel.fmt;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.codemodel.JResourceFile;

/**
 * Allows an application to copy a resource file to the output. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class JStaticFile extends JResourceFile {
    
    private final ClassLoader classLoader;
    private final String resourceName;
    
    public JStaticFile(String _resourceName) {
        this( JStaticFile.class.getClassLoader(), _resourceName );
    }
    
    public JStaticFile(ClassLoader _classLoader, String _resourceName) {
        super(_resourceName.substring(_resourceName.lastIndexOf('/')+1));
        this.classLoader = _classLoader;
        this.resourceName = _resourceName;
    }
    
    protected void build(OutputStream os) throws IOException {
        DataInputStream dis = new DataInputStream(classLoader.getResourceAsStream(resourceName));
        
        byte[] buf = new byte[256];
        int sz;
        while( (sz=dis.read(buf))>0 )
            os.write(buf,0,sz);
        
        dis.close();
    }
    
}
