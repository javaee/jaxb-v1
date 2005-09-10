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
