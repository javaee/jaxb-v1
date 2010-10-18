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
