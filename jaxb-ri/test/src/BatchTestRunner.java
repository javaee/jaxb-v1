/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOption;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.IllegalOptionParameterException;
import org.kohsuke.args4j.opts.AliasOption;
import org.kohsuke.args4j.opts.BooleanOption;
import org.kohsuke.args4j.opts.IntOption;
import org.kohsuke.args4j.opts.MultiStringOption;
import org.kohsuke.args4j.opts.OutputStreamOption;
import org.kohsuke.args4j.opts.StringOption;

import util.ParallelTest;

import batch.core.JAXBTest;
import batch.core.TestBuilder;
import batch.core.compiler.Compiler;
import batch.core.compiler.ExternalCompiler;
import batch.core.compiler.InternalCompiler;

import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;
    
/**
 * Automated test driver.
 * 
 * See <a href="design_docs/AutomatedTest.html">the document</a> for details.
 * 
 * This class parses the command line options and finds test directories,
 * then ask {@link Builder} to build actual tests to be run.
 */
abstract class BatchTestRunner
{
    /**
     * Responsible for actually building tests from each test case.
     */
    private final TestBuilder testBuilder;

    /**
     * JAXB compiler engine to be used.
     */
    private Compiler compiler;
    
    private VersionOption version = new VersionOption(new VersionNumber("999.0"));   // newest possible
    
    /**
     * Additional command line options to XJC.
     */
    private final List xjcParams = new ArrayList();
    
    /**
     * Process specified target directories recursively?
     */
    private final BooleanOption recursive = new BooleanOption("-r");
    
    /**
     * Location of xjc.sh/xjc.bat to use, or null to use internal compiler.
     */
    private final StringOption externalXJC = new StringOption("-externalxjc");
    
    /**
     * True to send the short progress report to the stderr.
     */
    private final BooleanOption printProgressToStdErr = new BooleanOption("-progress");
    
    /**
     * Where to send detailed output? "-" to console.
     */
    private final OutputStreamOption logOutput = new OutputStreamOption("-o","-");
    
    /**
     * Run tests parallely.
     */
    private final IntOption parallel = new IntOption("-parallel");
    
    protected BatchTestRunner( TestBuilder _testBuilder ) {
        this.testBuilder = _testBuilder;
    }
    
    /**
     * Prints out the usage message.
     * 
     * Should be overridden by the derived class if it's adding more
     * options.
     */
    protected void usage() {
        System.out.println(
            "Usage: "+this.getClass().getName()+" <test spec file/dir>\n"+
            "  If a dir name is specified, \"testspec.meta\" file\n"+
            "  in that dir will be used. If a full file name is specified\n"+
            "  that file will be used.\n"+
            "\n"+
            "Options:\n"+
            "  -r: recursively scan all tests inside the specified directory\n"+
            "  -version <ver>: skip the tests newer than the specified version\n"+
            "  -static-runtime : equivalent of the '-use-runtime com.sun.tools.xjc.runtime' XJC option\n"+
            "  -xjc <opt>: pass the specified parameter to XJC.\n"+
            "  -externalxjc> <path>: set the external XJC compiler\n"+
            "                        (for backward compatibility test)\n"+
            "\n" +            "Outout Control Options:\n" +            "  -progress :\n" +            "    report the concise progress message to stderr. When this option\n" +            "    is used, stdout should be redirected.\n" +
            "  -o <filename>\n" +
            "    send detailed log to a file instead of stdout\n" +            "  -quiet\n" +            "    same as '-o /dev/null -progress'\n" );
    }


    /**
     * Hook for the derived class to register additional listeners.
     * 
     * This method is called right after the {@link TestResult} is created.
     */
    protected void addTestListeners(Test test, TestResult result) {
        if( printProgressToStdErr.isOn() ) {
            // register another ResultPrinter to send the progress to stderr.
            result.addListener(new QuietProgressReporter(test,System.err));
        }
    }
    
    /**
     * Creates new {@link CmdLineParser} object to parse the
     * command line options.
     * 
     * This is an opportunity for the derived class to introduce
     * additional options.
     */
    protected CmdLineParser createCmdLineParser() {
        CmdLineParser parser = new CmdLineParser();
        
        parser.addOption(new MultiStringOption("-xjc",xjcParams));
        parser.addOption(recursive);
        parser.addOption(new AliasOption("-static-runtime",
            new String[]{"-xjc","-use-runtime","-xjc","com.sun.tools.xjc.runtime"}));
        parser.addOption(version);
        parser.addOption(externalXJC);
        parser.addOption(printProgressToStdErr);
        parser.addOption(logOutput);
        parser.addOption(parallel);
        parser.addOption(new AliasOption("-quiet",
            new String[]{"-o","/dev/null","-progress"}));
        
        return parser;
    }

    /**
     * Executes the program.
     * 
     * @return
     *      the exit code. 0 if tests run normally (that is,
     *      failures and errors are reported properly.)
     */
    public final int run( String[] args ) throws Exception {
        
        CmdLineParser parser = createCmdLineParser();
        try {
            parser.parse(args);
        } catch( CmdLineException e ) {
            System.out.println(e.getMessage());
            usage();
            return 1;
        }
        
        // set the default output
        System.setOut(new PrintStream(logOutput.createOutputStream()));
        
        List targets=parser.getArguments();
            
        if(targets.isEmpty()) {
            usage();
            return 1;
        }
        
        if( externalXJC.value==null )
            compiler = new InternalCompiler(xjcParams);
        else
            compiler = new ExternalCompiler(externalXJC.value,xjcParams);
        
        // build test cases
        TestSuite ts = parallel.isSet ? new ParallelTest(parallel.value) : new TestSuite();
        
        for( int i=0; i<targets.size(); i++ ) {
            File target = new File((String)targets.get(i));
            
            if(target.isDirectory() && recursive.isOn()) {
                buildRecursive(target,ts);
            } else {
                ts.addTest(build(target));
            }
        }
        
        if( ts.countTestCases()==0 ) {
            System.err.println("no tests to run");
            return -1;
        }
        
        // run the test
        new TestRunnerEx().doRun(ts);
        
        return 0;
    }
    
    /**
     * Builds single test case from a spec file.
     */
    public final Test build( File specFile ) throws Exception {
        
        if(specFile.isDirectory())
            // assume the default test spec file.
            specFile = new File(specFile,"testspec.meta");
        
        JAXBTest t = testBuilder.createTest(specFile,compiler);
        if( !t.isApplicable(version.value) ) {
            System.out.println("test case "+t+" is not applicable to this version");
            return new TestSuite(); // don't run this test
        } else
            return t;
    }
    
    /**
     * Recursively visit sub-directories and runs
     * all "testspec.meta" files.
     */
    private void buildRecursive( File dir, TestSuite result ) throws Exception {
        
        File[] children = dir.listFiles();
        for( int i=0; i<children.length; i++ ) {
            if(children[i].isDirectory())
                buildRecursive(children[i],result);
            else
            if(children[i].getName().equals("testspec.meta"))
                result.addTest(build(children[i]));
        }
    }
    
    /**
     * {@link TestRunner} with a hook to le the {@link BatchTestRunner}
     * customize the run. 
     */
    private class TestRunnerEx extends TestRunner {
        /** {@link Test} to run. */
        private Test test;
        
        private TestRunnerEx() {
            super(new TestResultPrinter(System.out));
        }

        protected TestResult createTestResult() {
            TestResult result = super.createTestResult();
            addTestListeners(test,result);
            return result;
        }
        
        public TestResult doRun( Test ts ) {
            test = ts;
            return super.doRun(ts);
        }
    }
    
    
    /**
     * {@link CmdLineOption} that read a version number.
     */
    private static class VersionOption implements CmdLineOption {
        
        VersionNumber value;
        
        VersionOption( VersionNumber defaultValue ) {
            this.value = defaultValue;
        }
        
        public boolean accepts(String optionName) {
            return optionName.equals("-version");
        }

        public int parseArguments(CmdLineParser parser, Parameters params) throws CmdLineException {
            String token = params.getParameter(0);
            try {
                value = new VersionNumber(token);
                return 1;
            } catch( IllegalArgumentException e ) {
                throw new IllegalOptionParameterException("-version",token);
            }
        }
        
    }
}
