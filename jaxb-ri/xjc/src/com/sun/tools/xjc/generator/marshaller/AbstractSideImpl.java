/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
