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

package processor;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.BooleanOption;

/**
 * {@link Processor} that uses a boolean switch to
 * control whether it performs a task or not.
 * 
 * <p>
 * This could be used to conditionally include a processing
 * depending on the command line setting.
 * 
 * The actual processing is done by another {@link Processor} object.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ConditionalProcessor implements Processor  {
    
    private Processor core;
    private final BooleanOption controlSwitch;
    
    /**
     * 
     * @param switchName
     *      This switch will be registered as a boolean command-line
     *      switch to turn on/off the processing specified by the 
     *      core parameter. 
     */
    public ConditionalProcessor( String switchName ) {
        this.controlSwitch = new BooleanOption(switchName) {
            public int parseArguments(CmdLineParser parser, Parameters params) throws CmdLineException {
                if(isOn()) {
                    // if the control switch is turned on,
                    // instanciate the actual processor.
                    core = createCoreProcessor();
                    core.addCmdLineOptions(parser);
                }
                return super.parseArguments(parser,params);
            }
        };
    }
    
    /**
     * Implemented by the derived class to create the actual
     * processor. This method is called only when the processor
     * needs to run.
     */
    protected abstract Processor createCoreProcessor();
    
    public boolean process(File dir, boolean verbose) {
        if( controlSwitch.isOn() )
            return core.process(dir,verbose);
        else // don't run
            return true;
    }

    public void addCmdLineOptions(CmdLineParser parser) {
        parser.addOption(controlSwitch);
    }
}
