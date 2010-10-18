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

package com.sun.tools.xjc;

import java.io.IOException;

import org.xml.sax.ErrorHandler;

import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;

/**
 * Interface implemented by external add-ons that
 * extends the backend.
 * 
 * This add-on will be called after the grammar is completely parsed
 * and the basic skeleton is generated.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface CodeAugmenter {
    
    /**
     * Gets the option name to turn on this add-on.
     * For example, if "abc" is returned, "-abc" will
     * turn on this extension.
     */
    public String getOptionName();
    
    /**
     * Gets the description of this add-on. Used to generate
     * a usage screen.
     * 
     * @return
     *      localized description message. should be terminated by \n.
     */
    public String getUsage();

    /**
     * Parses an option <code>args[i]</code> and augment
     * the <code>opt</code> object appropriately, then return
     * the number of tokens consumed.
     * 
     * <p>
     * The callee doesn't need to recognize the option that the
     * getOptionName method returns.
     * 
     * @return
     *      0 if the argument is not understood.
     * @exception BadCommandLineException
     *      If the option was recognized but there's an error.
     */
    int parseArgument( Options opt, String[] args, int i )
        throws BadCommandLineException, IOException;
    
    
    /**
     * Run the add-on.
     * 
     * @param grammar
     *      The parsed grammar.
     * 
     * @param context
     *      This object allows access to various generated code.
     * 
     * @param errorHandler
     *      Errors should be reported to this handler.
     * 
     * @return
     *      If the add-on executes successfully, return true.
     *      If it detects some errors but those are reported and
     *      recovered gracefully, return false.
     */
    public boolean run( AnnotatedGrammar grammar,
        GeneratorContext context,
        Options opt,
        ErrorHandler errorHandler );
        
}
