import java.io.PrintStream;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

/*
 * @(#)$Id: QuietProgressReporter.java,v 1.1 2004-06-25 21:12:58 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
