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
package batch.core.compiler;

import batch.core.JavacException;
import batch.core.XJCException;
import batch.core.om.Schema;

/**
 * JAXB Compiler interface.
 * 
 * This interface is used to allow unit tests to be run
 * against different kinds of compilers in different ways.
 * 
 * (Such as running XJC in memory, or invoking external
 * XJCs)
 *  
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface Compiler {
    /**
     * Compiles the schema under the specified setting and
     * returns the {@link ClassLoader} that can load compiled classes.
     * 
     * @throws XJCException
     *      if the schema compilation fails.
     * @throws JavacException
     *      if the compilation of the generated source code fails.
     * 
     * @return
     *      non-null valid object.
     */
    ClassLoader compile( Schema schema ) throws XJCException, JavacException;
}
