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
package util;

import junit.framework.TestResult;

import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;

import batch.core.JAXBTest;

/**
 * {@link JAXBTest} that runs nothing.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NullJAXBTest implements JAXBTest {
    private NullJAXBTest() {}
    
    /** Singleton. */
    public static final JAXBTest theInstance = new NullJAXBTest();

    public boolean isApplicable(VersionNumber v) {
        return true;
    }

    public int countTestCases() {
        return 0;
    }

    public void run(TestResult result) {
    }
}
