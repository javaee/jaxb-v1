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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.generator.util.BlockReference;

/**
 * Generate if(...) { ... } else { ... } block lazily.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class IfThenElseBlockReference {
    
    IfThenElseBlockReference( Context _context, JExpression exp ) {
        this.testExp = exp;
        parent = _context.getCurrentBlock();
    }
    
    /** the test expression will be created from these values. */
    private final JExpression testExp;
    /** parent block provider. Captures the current block when the object is created. */
    private final BlockReference parent;
    
    /** Once a conditional block is created, it will be set here. */
    private JConditional cond;
    
    /**
     * To avoid creating expressions like if(...){}else{ ... };
     * We switch the "then" block and the "else" block if necessary.
     * This flag will be set to true if we do that.
     */
    private boolean swapped = false;
    
    /** Creates a BlockProvider which returns the "then" block. */
    public BlockReference createThenProvider() {
        return new BlockReference() {
            public JBlock get(boolean create) {
                if(!create && cond==null)       return null;
                if(cond==null)
                    cond = parent.get(true)._if(testExp);
                if(!swapped)    return cond._then();
                else            return cond._else();
            }
        };
    }
    
    /** Creates a BlockProvider which returns the "else" block. */
    public BlockReference createElseProvider() {
        return new BlockReference() {
            public JBlock get(boolean create) {
                if(!create && cond==null)       return null;
                if(cond==null) {
                    // the else clause is requested first. Swap the then block
                    // and the else block.
                    cond = parent.get(true)._if(testExp.not());
                    swapped = true;
                }
                if(!swapped)    return cond._else();
                else            return cond._then();
            }
        };
    }
}
