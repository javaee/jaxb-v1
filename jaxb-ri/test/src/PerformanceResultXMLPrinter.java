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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import batch.core.om.Schema;
import batch.performance.PerformanceTestListener;
import batch.performance.PerformanceTestDescriptor;
import batch.performance.PerformanceTestDescriptor.Config;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.DumbEscapeHandler;


/**
 * Prints out the performance test result in a machine readable XML format.
 * 
 * <p>
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PerformanceResultXMLPrinter extends PerformanceTestListener {
    
    private final PrintWriter out;
    private final CharacterEscapeHandler escapeHandler;
    
    
    
    public PerformanceResultXMLPrinter( PrintWriter out ) {
        this.out = out;
        escapeHandler = DumbEscapeHandler.theInstance;
    }

    public PerformanceResultXMLPrinter( Writer out ) {
        this( new PrintWriter(out) );
    }
    
    public void startGroup(PerformanceTestDescriptor descriptor) {
        out.print("<group name=\"");
        escape(descriptor.getName());
        out.println("\">");
    }


    public void startCompiler(Schema schema) {
    }

    public void endCompiler(Schema schema, long nanoSeconds) {
        out.print("  <result mode=\"speed\" scenario=\"compile\" instance=\"");
        escape(schema.schema.toExternalForm());
        out.println("\">");
        out.println("    "+Long.toString(nanoSeconds));
        out.println("  </result>");
    }

    public void startTest(Config config) {
    }

    public void endTest(Config config, long result) {
        out.print("  <result scenario=\"");
        escape(config.scenario.getName());
        if( config.instance!=null ) {
            out.print("\" instance=\"");
            escape(config.instance.getName());
        }
        out.print("\" mode=\"");
        escape(config.profiler.name());
        out.println("\">");
        
        if(result==-1)  out.println("    N/A");
        else            out.println("    "+Long.toString(result));
        out.println("  </result>");
    }

    public void endGroup(PerformanceTestDescriptor descriptor) {
        out.println("</group>");
        out.flush();
    }

    private void escape( String s ) {
        try {
            escapeHandler.escape(s.toCharArray(),0,s.length(),true,out);
        } catch (IOException e) {
            ; // ignore it, this is how PrintWriter behaves anyway.  
        }
    }
}
