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
    
    
