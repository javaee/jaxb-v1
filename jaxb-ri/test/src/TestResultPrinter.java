import java.io.PrintStream;

import junit.framework.TestResult;

/**
 * {@link ResultPrinter} that always print the result in the same format.
 * 
 * This helps the grep command to summarize the result quite easily.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class TestResultPrinter extends junit.textui.ResultPrinter {
    public TestResultPrinter(PrintStream out) {
        super(out);
    }

    protected void printFooter(TestResult result) {
        getWriter().println();
        getWriter().println("Tests run: "+result.runCount()+ 
                     ",  Failures: "+result.failureCount()+
                     ",  Errors: "+result.errorCount());
        getWriter().println();
    }
}
