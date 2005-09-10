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
