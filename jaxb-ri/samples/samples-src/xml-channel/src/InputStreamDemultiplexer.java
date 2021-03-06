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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * De-multiplex one {@link InputStream} to multiple
 * sub-{@link InputStream}s. This class should be used
 * in pair with {@link OutputStreamMultiplexer}.
 * 
 * <p>
 * To use this class, do as follows:
 * <pre>
 * InputStream is = getInputStreamThatReceiveDataFromOutputStreamMultiplexer();
 * InputStreamDemultiplexer isd = new InputStreamDemultiplexer(is);
 * 
 * InputStream subStream;
 * while( (subStream=isd.openNextStream())!=null ) {
 *   // read from this sub-stream
 *   subStream.read(...);
 *   
 *   // it's important to close a sub-stream
 *   subStream.close();
 * }
 * isd.close();
 * </pre>
 * 
 * <p>
 * Note that because of the way the multiplexing works, the close method
 * of the sub-stream may block when you close a sub-stream before it
 * reaches the end-of-stream.
 * 
 * 
 * @see OutputStreamMultiplexer
 *      
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class InputStreamDemultiplexer {
    /** Data will be sent to this object. */
    private final DataInputStream underlyingStream;
    
    /** Currently active sub stream. */
    private SubStream subStream;
    
    private final byte[] buffer = new byte[512];
    private int bufPtr = 0;
    private int dataLength = 0;
    private boolean lastBlock = false;
    
    /**
     * Creates a new instance.
     * 
     * @param underlyingStream
     *      All the data will be read from this stream.
     */
    public InputStreamDemultiplexer( InputStream underlyingStream ) {
        this.underlyingStream = new DataInputStream(underlyingStream);
    }
    
    /**
     * Waits for the sender to send a next stream, then return it.
     * 
     * @return
     *      null if the sender closes the underlying stream. Otherwise
     *      non-null valid object.
     * 
     * @exception IOException
     *      If other unexpected errors happen.
     * 
     * @return IllegalStateException
     *      If a sub-stream is still open.
     */
    public InputStream openNextStream() throws IOException {
        if(subStream!=null)
            throw new IllegalStateException("previous sub-stream is still open");
        try {
            subStream = new SubStream();
            return subStream;
        } catch( EOFException e ) {
            return null;
        }
    }
    
    /**
     * Closes the underlying input stream.
     * 
     * @return IllegalStateException
     *      If a sub-stream is still open.
     */
    public void close() throws IOException {
        if(subStream!=null)
            throw new IllegalStateException("previous sub-stream is still open");
        underlyingStream.close();
    }
    
    private class SubStream extends InputStream
    {
        private SubStream() throws IOException {
            readNextBlock();
        }
        
        public void close() throws IOException {
            if( subStream!=this )
                return;     // this stream is already closed. ignore.
            
            // discard all the data till EoS
            while(!lastBlock)
                // read the next block until we hit the last block
                readNextBlock();
            
            // tell the parent that we are done.
            subStream = null;
            bufPtr = 0;
            dataLength = 0;
            lastBlock = false;
        }
        
        private void readNextBlock() throws IOException {
            short header = underlyingStream.readShort();
            lastBlock = (header&0x8000)!=0;
            dataLength = (header&0x7FFF);
            
            // read "dataLength" bytes
            underlyingStream.readFully(buffer,0,dataLength);
            
            bufPtr = 0;
        }
        
        public int read() throws IOException {
            if( subStream!=this )
                throw new IOException("trying to read from a closed stream");
            
            while(true) {
                if( bufPtr!=dataLength )
                    return buffer[bufPtr++];
                
                if( lastBlock )
                    return -1;  // EoS
                
                // always bufPtr==dataLength
                readNextBlock();
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if( subStream!=this )
                throw new IOException("trying to read from a closed stream");
            
            final int originalLen = len;
            
            while(len>0) {
                if( bufPtr!=dataLength ) {
                    // read from our buffer
                    int size = Math.min( len, dataLength-bufPtr );
                    System.arraycopy(buffer,bufPtr,b,off,size);
                    off += size;
                    len -= size;
                    bufPtr += size;
                }
                
                if( bufPtr==dataLength ) {
                    if( lastBlock ) { // EoS
                        if( len==originalLen )      return -1;  // no bytes read
                        else                        return originalLen-len;
                    }
                    
                    readNextBlock();
                }
            }
            
            return originalLen-len;
        }

        public int available() throws IOException {
            return dataLength-bufPtr;
        }
    }
}
