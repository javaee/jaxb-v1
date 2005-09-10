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
