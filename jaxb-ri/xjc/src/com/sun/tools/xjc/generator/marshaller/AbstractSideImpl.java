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
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.generator.util.LazyBlockReference;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Partial implementation of {@link Side
 * that cotains utility methods. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractSideImpl implements Side {
    
    protected final Context context;
    
    protected AbstractSideImpl(Context _context) {
        this.context = _context;
    }
    
    /**
     * Code should be generated into the returned block.  
     */
    protected final JBlock getBlock(boolean create) {
        return context.getCurrentBlock().get(create);
    }
    
    /** Creates a while block. */
    protected final BlockReference createWhileBlock(
        final BlockReference parent, final JExpression expr ) {
        
        return new LazyBlockReference() {
            public JBlock create() {
                return parent.get(true)._while(expr).body();
            }
        };
    }

    /**
     * Builds <code>"x instanceof y"</code> expression.
     * 
     * This function properly handles JNullType.
     */
    protected final JExpression instanceOf( JExpression obj, JType type ) {
        if(context.codeModel.NULL==type)
            return obj.eq(JExpr._null());
        else {
            if(type instanceof JPrimitiveType)
                type = ((JPrimitiveType)type).getWrapperClass();
                
            return obj._instanceof(type);
        }
    }
    
    protected static Object _assert(boolean b) {
        if(!b)
            throw new JAXBAssertionError();
        return null;
    }
}
