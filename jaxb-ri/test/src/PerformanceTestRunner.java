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
            "-xml <file>:\n" +
            "  produce the test result in an XML format to this file\n");
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
