/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JExpression;

/**
 * Helper class to generate code like
 * 
 * if(...) {
 *     ...
 * } else
 * if(...) {
 *     ...
 * }
 * ...
 * } else {
 *     ... (*1)
 * }
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class NestedIfBlockProvider {
    NestedIfBlockProvider( Context _context ) {
        this.context = _context;
    }
    
    private final Context context;
    
    private int nestLevel = 0;
    private IfThenElseBlockReference previous = null;
    
    /** call this method to start a new if block. */
    public void startBlock( JExpression testExp ) {
        startElse();
            
        nestLevel++;
        previous = new IfThenElseBlockReference(context,testExp);
        context.pushNewBlock(previous.createThenProvider());
    }
    
    /**
     * Usually called immediately before the end method
     * to generate some code in the (*1) block.
     */
    public void startElse() {
        if(previous!=null) {
            context.popBlock();
            context.pushNewBlock(previous.createElseProvider());
        }
    }
    
    /** finally call this method to wrap up everything. */
    public void end() {
        while(nestLevel-- >0)
            context.popBlock();
    }
}
