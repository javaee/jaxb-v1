/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.util.BlockReference;

/**
 * Generates marshalling code for a particular field
 */
public interface FieldMarshallerGenerator {
        
    /** Returns a Field object that created this generator. */
    FieldRenderer owner();
        
    /**
     * Returns an expression that evaluates to the next object
     * to be marshalled.
     * 
     * <p>
     * In general, the type of the returned expression can be
     * more general than the specific context requires.
     * For example, it could be "java.lang.Object", or if the
     * field is union of A and B, it could be the common type
     * between A and B.
     * 
     * <p>
     * The caller is thus responsible for casting the
     * result to a proper sub-type. This is because a field
     * cannot know the exact type that the context requires.
     * (this happens when the field is union.)
     * 
     * @param increment
     *      If true, "the pointer" is incremented to point the
     *      next object in this field.
     *      
     */
    JExpression peek( boolean increment );
    
    /**
     * Just increment the pointer.
     */
    void increment( BlockReference block );
    
    /**
     * Returns an expression that evaulates to the boolean value
     * that indicates this field still has a value to be marshalled.
     */
    JExpression hasMore();
    
    /**
     * Creates a clone of the iterator into the given code block
     * by using a given unique id, then return a FieldMarshallerGenerator
     * that manipulates the cloned iterator.
     */
    FieldMarshallerGenerator clone( JBlock block, String uniqueId );

}
    
    
