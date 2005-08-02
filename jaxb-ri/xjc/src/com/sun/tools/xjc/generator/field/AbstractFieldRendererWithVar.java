/*
 * @(#)$Id: AbstractFieldRendererWithVar.java,v 1.2 2005-08-02 18:11:46 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * {@link AbstractFieldRenderer} that uses one field variable
 * to store the value.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class AbstractFieldRendererWithVar extends AbstractFieldRenderer {
    
    /**
     * Field declaration of the actual list object that we use
     * to store data.
     */
    private JFieldVar field;
    
    
    protected AbstractFieldRendererWithVar(ClassContext _context, FieldUse _fu) {
        super(_context, _fu);
    }
    
    public final void generate() {
        this.field=generateField();
        generateAccessors();
    }
    
    public JFieldVar ref() { return field; }

    
    
    /** Generates a field definition on the implementation. */
    protected JFieldVar generateField() {
        return generateField(getValueType());
    }
    
    /** Generates accessor methods. */
    public abstract void generateAccessors();
}
