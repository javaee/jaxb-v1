/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.validator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Writes a byte stream into a string without changing the bit image.
 */
public class StringOutputStream extends OutputStream
{
    public StringOutputStream( Writer _writer ) {
        this.writer = _writer;
    }
    
    private final Writer writer;
    
    public void write( int ch ) throws IOException {
        writer.write(ch);
    }
    
    public void write( byte[] data ) throws IOException {
        write(data,0,data.length);
    }
    
    public void write( byte[] data, int start, int len ) throws IOException {
        char[] buf = new char[len];
        
        for( int i=0; i<len; i++ )
            buf[i] = (char)(((int)data[i+start])&0xFF);
        writer.write(buf);
    }
    
    public void close() throws IOException {
        writer.close();
    }
    
    public void flush() throws IOException {
        writer.flush();
    }
}
