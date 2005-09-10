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
