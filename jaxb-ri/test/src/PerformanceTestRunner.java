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

import junit.framework.Test;
import junit.framework.TestResult;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.OutputStreamOption;

import com.sun.tools.profiler.Profiler;

import batch.core.TestBuilder;
import batch.performance.PerformanceTestBuilder;
    
/**
 * Runs the QA tests.
 */
public class PerformanceTestRunner extends BatchTestRunner
{
    /**
     * Where to send the XML output?
     */
    private final OutputStreamOption xmlOutput = new OutputStreamOption("-xml");
    
    protected CmdLineParser createCmdLineParser() {
        CmdLineParser parser = super.createCmdLineParser();
        parser.addOption(xmlOutput);
        return parser;
    }


    protected PerformanceTestRunner(TestBuilder _testBuilder) {
        super(_testBuilder);
    }

    public PerformanceTestRunner() {
        super( new PerformanceTestBuilder() );
    }
    
    protected void usage() {
        System.out.println("Measures the performance of tests");
        super.usage();
        System.out.println(
            "-xml <file>:\n" +            "  produce the test result in an XML format to this file\n");
    }

    public static void main( String[] args ) throws Exception {
        // we'll control the profiler by ourselves in SpeedProfiler. 
        Profiler.create().disable();
        
        System.exit(new PerformanceTestRunner().run(args));
    }

    protected void addTestListeners(Test test, TestResult result) {
        try {
            super.addTestListeners(test, result);
            
            if( xmlOutput.value!=null )
                result.addListener(new PerformanceResultXMLPrinter(xmlOutput.createWriter()));
            result.addListener(new PerformanceResultPrinter());
        } catch( IOException e ) {
            // since this is a tool for a developer, dumping stack trace
            // should be OK.
            e.printStackTrace();
            throw new InternalError();
        }
    }
}
