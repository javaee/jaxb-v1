/*
 * $Id: RegExpHelper.java,v 1.1 2004-06-25 21:12:52 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package processor.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * This helper class is used to process the output from a sample app and help
 * generate a multi-line regular expression that will be used during automated
 * runs of the sample apps.
 * 
 * @author <ul>
 *         <li>Ryan Shoemaker, Sun Microsystems, Inc.</li>
 *         </ul>
 * @version $Revision: 1.1 $
 */
public class RegExpHelper {

    private File infile;

    /**
     * @param infile
     *      the name of the input file
     */
    public RegExpHelper(String infile) {
        this.infile = new File(infile);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println(
                "usage: java processor.util.RegExpHelper </path/to/build.out>");
            System.exit(-1);
        }

        RegExpHelper regEx = new RegExpHelper(args[0]);
        regEx.escapeFile();

        exitReminder();
    }

    /**
     *  
     */
    private void escapeFile() throws IOException {
        File outFile = new File(infile.getParentFile(), "build.golden.regexp");

        BufferedReader br = new BufferedReader(new FileReader(infile));
        BufferedWriter bw =
            new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile)));

        CharacterEscapeHandler escHandler = RegExpEscapeHandler.theInstance;

        System.out.println("Generating: " + outFile.getPath());

        String line = br.readLine();
        while (line != null) {
            char[] chars = line.toCharArray();
            escHandler.escape(chars, 0, chars.length, bw);
            bw.newLine();
            line = br.readLine();
        }

        System.out.println("done.\n\n");
        br.close();
        bw.close();
    }

    /**
     *  
     */
    private static void exitReminder() {
        System.out.println(
            "Please edit the generated output and replace any dynamic\n"
                + "content with the appropriate regular expression.  For\n"
                + "example:\n"
                + "replace \"D\\:\\files\\jaxb\\ws\\jaxb-ri\\samples\\work\\unmarshal-read\\gen-src\\\" with \".*\"\n"
                + "replace \"Total time\\: 11 seconds\" with \"Total time\\: .* seconds\"\n"
                + "Don't forget to check build.regexp into the workspace.");
    }

}