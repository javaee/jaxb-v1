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
package batch.core;

/**
 * Signals a failure of XJC execution.
 * 
 * This exception will be thrown if XJC exists with non-zero.
 * This should mean that XJC has detected errors in the schema.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XJCException extends Exception {
    public XJCException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public XJCException(Throwable cause) {
        super(cause);
    }

    public XJCException(String message) {
        super(message);
    }
}
