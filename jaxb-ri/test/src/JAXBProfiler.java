/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;
import batch.core.compiler.Compiler;
import batch.core.compiler.ExternalCompiler;
import batch.core.compiler.InternalCompiler;
import batch.core.om.Instance;
import batch.core.om.Schema;
import batch.performance.PerformanceTest;
import batch.performance.PerformanceTestDescriptor;


/**
 * A command line driver to the performance measurement framework.
 * 
 * <p>
 * It allows you to effectively create a performance test description
 * on the fly and run it.   
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JAXBProfiler {
    
    // TODO: unify JAXBTester and JAXBProfiler
    // so that at least they can share some common code.
    
    public static void main(String[] args) throws Exception {
        URL schema = null;
        List extBindings = new ArrayList();
        String pkg = "gen";
        List configs = new ArrayList();
        List instances = new ArrayList();
        List additionalParams = new ArrayList();
        boolean skipCompiler = false;
        Compiler compiler;
        String externalXJC = null;
        
        for( int i=0; i<args.length; i++ ) {
            String arg = args[i].intern();
            
            if( arg=="-p" ) {
                pkg = args[++i];
                continue;
            }
            if( arg=="-b" ) {
                extBindings.add( new File(args[++i]).toURL() );
                continue;
            }
            if( arg=="-m" ) {
                // TODO: write code to parse this
                throw new UnsupportedOperationException();
                // continue;
            }
            if( arg=="-xjc" ) {
                additionalParams.add(args[++i]);
                continue;
            }
            if( arg=="-externalxjc" ) {
                externalXJC = args[++i];
                continue;
            }
            if( arg=="-skip" ) {
                skipCompiler = true;
                continue;
            }
            if( arg=="-static-runtime" ) {
                additionalParams.add("-use-runtime");
                additionalParams.add("com.sun.tools.xjc.runtime");
                continue;
            }
            if( arg.charAt(0)=='/' || arg.charAt(0)=='-') {
                usage("unrecognized option: "+arg );
            }
            
            if( schema==null ) {
                schema = new File(arg).toURL();
            } else {
                instances.add( new File(arg) );
            }
        }
        
        if( schema==null )  usage("no schema was specified");
        
        if( configs.isEmpty() ) usage("no performance measurement config was given");

        if( externalXJC==null )
            compiler = new InternalCompiler(additionalParams);
        else
            compiler = new ExternalCompiler(externalXJC,additionalParams);
        
        
        File outDir = new File("./jaxbtester");
        outDir.mkdir();
        
        PerformanceTestDescriptor desc = new PerformanceTestDescriptor(
            outDir,
            new Schema(
                schema,
                (URL[]) extBindings.toArray(new URL[extBindings.size()]),
                outDir,
                pkg, false, false, null, new ArrayList() ),
            (Instance[]) instances.toArray(new Instance[instances.size()]),
            (PerformanceTestDescriptor.Config[]) configs.toArray() );
        
        PerformanceTest stc = new PerformanceTest( desc, compiler );
        if(skipCompiler)    stc.skipCompiler();
        TestRunner.run(stc);
    }

    private static void usage(String msg) {
        System.err.println(msg);
        System.err.println(
            "JAXBProfiler <schema> [<instance1> <instance2> ...]\n" +
            "  compile schema and run script-based tests with it.\n" +
            "\n" +
            "Options:\n" +
            "  -p <pkg>\n" +
            "    The Java package to which the schema is compiled\n" +
            "  -b <externalBinding>\n" +
            "    External binding file for the compiler. More than one\n" +
            "    can be specified\n" +
            "  -m <scenario> [speed|memory]\n" +
            "    Script files to be run. If no instance is given, scripts\n" +
            "    will be run after the schema is compiled, otherwise they will run\n" +
            "    for each instance. At least one has to be given. More than one\n" +
            "    can be specified.\n" +
            "  -static-runtime\n" +
            "    equivalent of the '-use-runtime com.sun.tools.xjc.runtime' XJC option\n"+
            "  -xjc <xjc opt>\n" +
            "    Additional command line options to XJC. More than one can be\n" +
            "    specified.\n"+
            "  -externalxjc <path to xjc.bat or xjc.sh>\n" +
            "    Use external XJC command to compile the schema. Useful to check\n" +
            "    the difference in behaviors across releases."
        );
        System.exit(-1);
    }
}
