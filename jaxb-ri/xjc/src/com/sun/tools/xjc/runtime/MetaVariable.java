/*
 * @(#)$Id: MetaVariable.java,v 1.1 2004-06-25 21:15:23 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
