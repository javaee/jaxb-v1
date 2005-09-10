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
