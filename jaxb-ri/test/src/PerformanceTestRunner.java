/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
