/*
 * $Id: SampleProcessorDriver.java,v 1.2 2005-09-10 18:19:16 kohsuke Exp $
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
import java.util.ArrayList;
import java.util.Iterator;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.BooleanOption;
import org.kohsuke.args4j.opts.StringOption;

/**
 * This class is responisble for driving the Sample App processing. It
 * maintains a list of Processors that act on a directory containing sample app
 * meta data.
 * 
 * Ryan Shoemaker, Sun Microsystems, Inc.
 * 
 * @version $Revision: 1.2 $
 */
public class SampleProcessorDriver {

    // Processor to run
    private Processor processor;

    // storage for list of directories
    private ArrayList dirs = new ArrayList();

    // command line options for driver
    public StringOption rootDir = new StringOption("-dir");
    public BooleanOption verbose = new BooleanOption("-verbose", false);

    /**
     * Constructor.
     * 
     * @param args
     *                command line args
     */
    SampleProcessorDriver(String[] args) {
        // setup the chain of processors
        processor =
            new ChainProcessor(new ValidatingProcessor(), new AntProcessor());

        // optional processors that run the generated ant build.xml and
        // compare the results
        processor = new ChainProcessor(processor, new ConditionalProcessor("-execute") {
            protected Processor createCoreProcessor() {
                return new ChainProcessor(new AntBuildProcessor(), new GoldenFileProcessor());
            }
        });

        // process cmd line options
        parseCmdLine(args);

        // store the dirs
        this.dirs = findMetaFiles(new File(rootDir.value), "sample.meta");
    }

    private void parseCmdLine(String[] args) {
        // create the parser
        CmdLineParser parser = new CmdLineParser();

        // add driver options
        parser.addOptionClass(this);

        // add processor specific options
        processor.addCmdLineOptions(parser);

        try {
            parser.parse(args);
        } catch (CmdLineException cle) {
            System.err.println(cle.getMessage());
            usage();
        }
    }

    private void usage() {
        System.out.println(
            "usage: java processor.SampleProcessorDriver [-validation] [-ant JWSDP|workspace|RI] -dir <dir>");
        System.exit(-1);
    }

    private void process() {
        System.out.println("Processing sample applications");
        File currentDir;
        boolean result;
        ArrayList failures = new ArrayList();

        for (Iterator i = dirs.iterator(); i.hasNext();) {
            currentDir = new File((String)i.next());
            trace("Processing: " + currentDir.getAbsolutePath());
            result = processor.process(currentDir, verbose.value);
            if (result == false) {
                failures.add(currentDir);
            }
            trace("\n");
        }

        printSummary(failures);
    }

    /**
     * @param failures
     */
    private void printSummary(ArrayList failures) {
        System.out.println("Summary:");
        if (failures.size() == 0) {
            System.out.println("\tALL PASS");
        } else {
            for (int i = 0; i < failures.size(); i++) {
                System.out.println("\tFAILED: " + ((File)failures.get(i)).getName());
            }
        }
        // print out the summary line in the JUnit format
        // so that the tool can count this result with other unit tests.
        System.out.println("Tests run: "+dirs.size()+ 
                     ",  Failures: "+failures.size()+
                     ",  Errors: 0");
    }

    private void trace(String msg) {
        if (verbose.value)
            System.out.println(msg);
    }

    /**
     * Search the specified directory for the specified meta file
     * 
     * @param metaFileName
     *                the name of the meta file to search for
     * @return the meta file or null if not found
     */
    static File getMetaFile(File dir, String metaFileName) {
        if (!dir.isDirectory()) {
            System.out.println(
                "Warning: skipping '"
                    + dir.getAbsolutePath()
                    + "' - not a directory.");
            return null;
        } else {
            return new File(dir, metaFileName);
        }
    }

    /**
     * Recursively search below the specified directory for the specified meta
     * file.
     * 
     * @param dir
     *                the directory to search
     * @param metaFileName
     *                the name of the meta file to search for
     * @return An ArrayList containing all of the dirs that contain meta files
     */
    static ArrayList findMetaFiles(File dir, String metaFileName) {
        ArrayList result = new ArrayList();
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                result.addAll(findMetaFiles(files[i], metaFileName));
            } else if (metaFileName.equals(files[i].getName())) {
                result.add(files[i].getParent());
                break;
            }
        }
        return result;
    }

    /**
     * Iterate over the specified directories and recurse looking for
     * sample.meta files.
     * 
     * @param args
     *                list of directories to process
     */
    public static void main(String[] args) {
        new SampleProcessorDriver(args).process();
    }
}
