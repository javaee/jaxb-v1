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
