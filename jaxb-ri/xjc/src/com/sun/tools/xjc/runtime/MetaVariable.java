/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
