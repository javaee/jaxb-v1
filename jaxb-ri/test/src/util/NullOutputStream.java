/*
 * @(#)$Id: NullOutputStream.java,v 1.1 2004-06-25 21:13:11 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} that works like /dev/null. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NullOutputStream extends OutputStream {

    public void close() throws IOException {
    }

    public void flush() throws IOException {
    }

    public void write(byte[] b, int off, int len) throws IOException {
    }

    public void write(byte[] b) throws IOException {
    }

    public void write(int b) throws IOException {
    }

}
