/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc;

import java.io.OutputStream;
import java.io.PrintStream;

import org.xml.sax.SAXParseException;

/**
 * {@link ErrorReceiver} that prints to a {@link PrintStream}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ConsoleErrorReporter extends ErrorReceiver {

    /**
     * Errors, warnings are sent to this output.
     */
    private PrintStream output;
    
    /**
     * True if warning messages are supressed
     */
    private boolean supressWarnings;
    
    /**
     * True if the info messaegs are printed.
     */
    private boolean supressInfo;
    
    public ConsoleErrorReporter( PrintStream out, boolean supressInfo, boolean supressWarnings ) {
        this.output = out;
        this.supressInfo = supressInfo;
        this.supressWarnings = supressWarnings;
    }
    public ConsoleErrorReporter( OutputStream out, boolean supressInfo, boolean supressWarnings ) {
        this(new PrintStream(out), supressInfo, supressWarnings);
    }
    public ConsoleErrorReporter() { this(System.out, true, false); }
    
    /**
     * Starts printing verbose 'info' messages.
     */
    public void supressInfoOutput() {
        supressInfo = true;
    }
    
    public void supressWarnings() {
        supressWarnings = true;
    }
    
    public void warning(SAXParseException e) {
        if( supressWarnings ) return; // ignore
        print(Messages.WARNING_MSG,e);
    }
    
    public void error(SAXParseException e) {
        print(Messages.ERROR_MSG,e);
    }
    
    public void fatalError(SAXParseException e) {
        print(Messages.ERROR_MSG,e);
    }
    
    public void info(SAXParseException e) {
        if( supressInfo )   return; // ignore
        print(Messages.INFO_MSG,e);
    }

    private void print( String resource, SAXParseException e ) {
        output.println(Messages.format(resource,e.getMessage()));
        output.println(getLocationString(e));
        output.println();
    }
}
