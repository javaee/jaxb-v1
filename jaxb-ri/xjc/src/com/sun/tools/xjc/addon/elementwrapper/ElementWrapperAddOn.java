/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.addon.elementwrapper;

import java.io.IOException;
import org.xml.sax.ErrorHandler;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.CodeAugmenter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.runtime.ElementWrapper;

/**
 * Generates {@link ElementWrapper} to the <tt>impl.runtime</tt>
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ElementWrapperAddOn implements CodeAugmenter {

    public String getOptionName() {
        return "Xelement-wrapper";
    }

    public String getUsage() {
        return "  -Xelement-wrapper  :  generates the general purpose element wrapper into impl.runtime";
    }

    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {
        return 0;   // no option recognized
    }

    public boolean run(
        AnnotatedGrammar grammar,
        GeneratorContext context,
        Options opt,
        ErrorHandler errorHandler ) {
        
        context.getRuntime(ElementWrapper.class);
        
        return true;
    }
}
