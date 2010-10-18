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
