/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Partial implementation of FieldRenderer.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractFieldRenderer implements FieldRenderer {
    
    /** A reference to the code model for use by derived classes. */
    protected final JCodeModel codeModel;
    
    /** information about the class in which this field exists. */
    protected final ClassContext context;
    
    /** FieldUse for which this renderer is working. */
    protected final FieldUse fu;
    
    /** Helper object that shall be used to generate methods. */
    protected final MethodWriter writer;

    
    protected AbstractFieldRenderer( ClassContext _context, FieldUse _fu ) {
        this.context = _context;
        this.fu = _fu;
        this.codeModel = _context.parent.getCodeModel();
        this.writer = context.createMethodWriter();
    }
    
    public final FieldUse getFieldUse() { return fu; }
    


//
//
// utility methods
//
//

    /**
     * Generates the field declaration.
     */
    protected final JFieldVar generateField( JType type ) {
        return context.implClass.field( JMod.PROTECTED, type, "_"+fu.name );
    }
    protected final void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
}
