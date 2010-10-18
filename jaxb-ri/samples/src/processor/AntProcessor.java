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

/*
 * $Id: AntProcessor.java,v 1.3 2010-10-18 14:21:24 snajper Exp $
 */

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
package processor;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.StringOption;

import com.sun.tools.xmlpp.PrettyPrintResult;

/**
 * Generates a build script for a sample app.
 * 
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Revision: 1.3 $
 */
public class AntProcessor implements Processor {

    // command-line options for ant processing
    public StringOption antTarget = new StringOption("-ant");

    // xsl transform file name
    private static final String XSLT_FILE_NAME = "build-script.xsl";

    // the name of the meta file we're searching for
    private static final String META_FILE_NAME = "sample.meta";

    private final Transformer transformer;

    AntProcessor() {
        TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(StreamSource.FEATURE)) {
            throw new TransformerFactoryConfigurationError(
                "Error: "
                    + tf.getClass().getName()
                    + " doesn't support StreamSource");
        }
        InputStream xsltFile =
            AntProcessor.class.getResourceAsStream(XSLT_FILE_NAME);
        try {
            transformer = tf.newTransformer(new StreamSource(xsltFile));
            System.out.println("Transformer is: "+transformer.getClass().getName());
        } catch (TransformerConfigurationException e) {
            // must be a bug of the stylesheet
            throw new TransformerFactoryConfigurationError(e);
        }
    }

    /*
     * Look for sample.meta in the specified directory and then apply a
     * transform that generates an Ant build.xml for the sample.
     * 
     * @see processor.Processor#process(java.util.List)
     */
    public boolean process(File dir, boolean verbose) {
        boolean continueProcessing = true;

        // find the meta data file in dir
        // this can't be null - the driver only passes in directories
        // containing sample.meta files
        File metaFile = SampleProcessorDriver.getMetaFile(dir, META_FILE_NAME);

        // process it
        try {
            // trace info
            trace("generating build.xml", verbose);

            // setup source and result
            StreamSource source = new StreamSource(metaFile);
            PrettyPrintResult result =
                new PrettyPrintResult(
                    new FileWriter(
                        metaFile.getParent()
                            + File.separatorChar
                            + "build.xml"));
            
            transformer.setParameter("target",antTarget.value);
            
            // run the transform
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
            continueProcessing = false;
        }

        return continueProcessing;
    }

    private void trace(String msg, boolean verbose) {
        if(verbose)
            System.out.println("AntProcessor: " + msg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see processor.Processor#addCmdLineOptions(org.kohsuke.args4j.CmdLineParser)
     */
    public void addCmdLineOptions(CmdLineParser parser) {
        parser.addOption(antTarget);
    }

}
