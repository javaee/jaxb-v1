package util;

import java.io.File;

import junit.framework.TestCase;
import batch.core.JAXBTest;

import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;

/**
 * A special {@link JAXBTest} implementation that just
 * throws an {@link Exception} when the test is run.
 * 
 * <p>
 * This is useful when an error is found in the configuration
 * of a test. Inserting this test into the test process would
 * allow other tests to run smoothly (as opposed to throw an
 * exception during the test building phase, which will halt
 * the entire test process at once.)
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ConfigFailureTest extends TestCase implements JAXBTest {
    
//    private final File testSpec;
    private final Exception e;
    
    public ConfigFailureTest( File _testSpec, Exception _exception ) {
        super(_testSpec.toString());
//        this.testSpec = _testSpec;
        this.e = _exception;
    }
    
    public boolean isApplicable(VersionNumber v) {
        return true;
    }

    protected void runTest() throws Exception {
        throw e;
    }

}
