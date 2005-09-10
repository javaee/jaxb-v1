/*
 * @(#)$Id: MetaVariable.java,v 1.2 2005-09-10 18:20:44 kohsuke Exp $
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
package com.sun.tools.xjc.runtime;

/**
 * 
 * <p>
 * Depending on the compiler settings, we want to generate
 * slightly different code. Doing this requires the
 * code-generation control that goes finer than the class-level
 * generation control. For example, in some case one method in
 * a class needs to be different depending on the setting.
 * 
 * <p>
 * We also want to make sure that the runtime code can be compiled
 * as a part of the XJC build.
 * 
 * <p>
 * To do this, the runtime code has a "meta-level" conditional statement
 * marked by the comment "// META-IF".
 * These conditional statements refer to static variables defined in this
 * class, but they are actually evaluated by XJC and it will not
 * generated parts of the code that won't be used.
 * 
 * <p>
 * For example, the following code in the runtime:
 * <tt>
 * if( MetaVariable.marshaller ) {// META-IF
 *    (A)
 * } else {// META-IF
 *    (B)
 * }
 * </tt>
 * <p>
 * ... will be generated as <tt>(A)</tt> normally, but it will be
 * generated as <tt>(B)</tt> if the user asks not to generate the
 * marshaller.
 * 
 * <p>
 * This class won't be generated as a part of the runtime.
 * It is necessary only to make the XJC compile. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class MetaVariable {
    /** Marshaller/ */
    static final boolean M = (System.getProperty("jaxb.runtime.M")==null);
    /** Unmarshaller. */
    static final boolean U = (System.getProperty("jaxb.runtime.U")==null);
    /** Validator. */
    static final boolean V = (System.getProperty("jaxb.runtime.V")==null);
    /** Unmarshalling Validator. Why W? see, UV->VV->W! */
    static final boolean W = (System.getProperty("jaxb.runtime.W")==null);
}
