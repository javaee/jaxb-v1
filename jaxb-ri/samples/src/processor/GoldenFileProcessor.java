/*
 * $Id: GoldenFileProcessor.java,v 1.2 2005-09-10 18:19:16 kohsuke Exp $
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineParser;

/**
 * @author <ul>
 *         <li>Ryan Shoemaker, Sun Microsystems, Inc.</li>
 *         </ul>
 * @version $Revision: 1.2 $
 */
public class GoldenFileProcessor implements Processor {

    /*
     * (non-Javadoc)
     * 
     * @see processor.Processor#process(java.io.File)
     */
    public boolean process(File dir, boolean verbose) {
        File buildDotOut = new File(dir, "build.out");
        File buildDotGoldenRegexp = new File(dir, "build.golden.regexp");
        
        if(!buildDotGoldenRegexp.exists()) {
            return false;
        }
        
        boolean match = match(buildDotOut, buildDotGoldenRegexp);
        if(match) {
            trace("output matches golden file", verbose);
            return true;
        } else {
            trace("output differs from golden file", verbose);
            return false;
        }
    }

    /**
     * return true if the contents of file matches the multi-line
     * regular expression in template.
     * 
     * @param file source file
     * @param template mnulti-line regular expression template
     * @return true if the file matches the regular expression, false otherwise
     */
    boolean match(File file, File template) {
//        return Pattern
//            .compile(getFileAsString(template), Pattern.MULTILINE)
//            .matcher(getFileAsString(file))
//            .matches();
        
        // braindead line-by-line pattern match to narrow down the
        // cause of unit test failures
        try {
            BufferedReader f = new BufferedReader(new FileReader(file));
            BufferedReader t = new BufferedReader(new FileReader(template));

            String fLine;
            String tLine;
            int line = 0;
            boolean matches = true;
            do {
                fLine = f.readLine();
                tLine = t.readLine();
                if (fLine == null || tLine == null)
                    break;
                line++;
                try {
                    matches = Pattern.compile(tLine).matcher(fLine).matches();
                } catch (Exception e) {
                    return false;
                }
            } while (matches && fLine != null && tLine != null);
            if (!matches) {
                System.out.println("error: line " + line + " doesn't match");
                System.out.println("regexp: " + stripBackslashes(tLine));
                System.out.println("xmlout: " + fLine);
            }

            return matches;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return true;
    }

    private String stripBackslashes(String line) {
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if(c != '\\')
                s.append(c);
        }
        return s.toString();
    }

    /**
     * return the contents of the file as a String
     * 
     * @param f source file
     * @return contents of f as a String
     */
    String getFileAsString(File f) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see processor.Processor#addCmdLineOptions(org.kohsuke.args4j.CmdLineParser)
     */
    public void addCmdLineOptions(CmdLineParser parser) {
        // no-op
    }

    private void trace(String msg, boolean verbose) {
        if(verbose) {
            System.out.println("GoldenFileProcessor: " + msg);
        }
        
    }

}
