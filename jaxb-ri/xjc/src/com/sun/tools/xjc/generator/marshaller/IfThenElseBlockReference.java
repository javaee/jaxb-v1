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
