/*
 * @(#)$Id: AbstractFieldRendererWithVar.java,v 1.3 2005-09-10 18:20:05 kohsuke Exp $
 */

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
