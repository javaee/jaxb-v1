import java.io.PrintStream;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

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

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class QuietProgressReporter implements TestListener {
    
    private int count;
    private int total;
    
    private final PrintStream out;
    
    public QuietProgressReporter( Test test, PrintStream out ) {
        this.out = out;
        this.total = test.countTestCases();
    }
    
    public void addError(Test test, Throwable t) {
        out.println();
        out.println();
        out.println("ERROR  :"+test.toString());
        t.printStackTrace(out);
        out.println();
    }

    public void addFailure(Test test, AssertionFailedError t) {
        out.println();
        out.println();
        out.println("FAILURE:"+test.toString());
        t.printStackTrace(out);
        out.println();
    }

    public void endTest(Test test) {
    }

    public void startTest(Test test) {
        count++;
        String msg = count+"/"+total+" : "+test.toString();
//        if(msg.length()>79)
//            msg = msg.substring(0,75)+" ...";
//        out.print("\r"+msg);
        out.println(msg);
    }

}
