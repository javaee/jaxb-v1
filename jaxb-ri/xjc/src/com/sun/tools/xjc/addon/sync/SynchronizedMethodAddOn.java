/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.addon.sync;

import java.io.IOException;
import java.util.Iterator;

import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.CodeAugmenter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;

/**
 * Generates synchronized methods.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SynchronizedMethodAddOn implements CodeAugmenter {

    public String getOptionName() {
        return "Xsync-methods";
    }

    public String getUsage() {
        return "  -Xsync-methods     :  generate accessor methods with the 'synchronized' keyword";
    }

    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {
        return 0;   // no option recognized
    }

    public boolean run(
        AnnotatedGrammar grammar,
        GeneratorContext context,
        Options opt,
        ErrorHandler errorHandler ) {
        
        ClassItem[] cis = grammar.getClasses();
        for( int i=0; i<cis.length; i++ )
            augument( context.getClassContext(cis[i]) );
        
        return true;
    }
    
    /**
     * Adds "synchoronized" to all the methods.
     */
    private void augument(ClassContext cc) {
        for( Iterator itr=cc.implClass.methods(); itr.hasNext(); ) {
            JMethod m = (JMethod)itr.next();
            m.getMods().setSynchronized(true);
        }
    }

}
