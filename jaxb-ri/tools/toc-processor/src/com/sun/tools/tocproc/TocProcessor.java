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

package com.sun.tools.tocproc;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;

/**
 * This is a specialized processor that generates a toc navbar in each of the
 * .html files in the jaxb release notes.
 * 
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Revision: 1.3 $
 */
public class TocProcessor {

    /** the directory containing html files to process */
    private File sourceDir;

    /** the destination directory */
    private String destDirName;

    /** xml file describing the toc navigation bar */
    private File tocDotXml;

    /** the name of the toc style sheet */
    private final String tocDotXsl = "toc.xsl";

    public TocProcessor(String[] args) {
        if (args.length != 1)
            usage();

        sourceDir = new File(args[0]);
        tocDotXml = new File(sourceDir, "toc.xml");

        destDirName = args[0];
    }

    private void transform(String fileName, Node node) {
        TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(StreamSource.FEATURE)) {
            throw new TransformerFactoryConfigurationError(
                "Error: "
                    + tf.getClass().getName()
                    + " doesn't support StreamSource");
        }

        InputStream xsltFile =
            TocProcessor.class.getResourceAsStream(tocDotXsl);

        try {
            StreamSource xsltSource = new StreamSource(xsltFile);
            // workaround for xalan bugid: 5008888
            xsltSource.setSystemId("blarg");
            
            Transformer transformer =
                tf.newTransformer(xsltSource);

            // setup source and result
            DOMSource source = new DOMSource(node);
            
            
            StreamResult result =
                new StreamResult(
                    new FileWriter(destDirName + File.separator + fileName));
            
            transformer.setParameter("tocDotXml", tocDotXml.toURL().toExternalForm());

            // run the transform
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            // must be a bug of the stylesheet
            e.printStackTrace();
            throw new TransformerFactoryConfigurationError(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int run() {
        try {
            DOMParser parser = new DOMParser();
            parser.setFeature(
                "http://cyberneko.org/html/features/balance-tags",
                true);

            File[] htmlFiles = sourceDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".html");
                }
            });

            for (int i = 0; i < htmlFiles.length; i++) {
                File file = htmlFiles[i];
                parser.parse(file.getCanonicalPath());
                transform(file.getName(), parser.getDocument());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void usage() {
        System.out.println("java TocProcessor <dir>");
        System.out.println("\t<dir> must contain toc.xml");
        System.exit(-1);
    }

    public static void main(String[] args) {
        (new TocProcessor(args)).run();
    }
}
