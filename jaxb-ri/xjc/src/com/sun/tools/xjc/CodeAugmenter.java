/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
