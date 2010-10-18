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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Abstract model of one field in a generated class.
 * 
 * <p>
 * Responsible for "realizing" a Java property by actually generating
 * members(s) to store the property value and a set of methods
 * to manipulate them.
 * 
 * <p>
 * For discussion of the model this interface is representing, see
 * the "field meta model" design document.
 * 
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface FieldRenderer {
    /**
     * Generates accesssors and fields. This method will be called
     * immediately after a FieldRenderer is created.
     */
    public void generate();
    
    /**
     * Get a code block that will be executed when the state of
     * this field changes from a null state to a non-null state.
     * (the unset state to the set state.)
     * 
     * <p>
     * This method can be called only after the generate method is
     * called.
     * 
     * @return
     *      Always return non-null object.
     */
    JBlock getOnSetEventHandler();
    
    
    /** Get the corresponding field use. */
    FieldUse getFieldUse();
        
    /**
     * Generate a code to add the "newValue" to this field
     * and set it to the 'body'.
     */
    void setter( JBlock body, JExpression newValue );
    
    /**
     * Dumps everything in this field into the given array,
     * which is guaranteed to have the enough space to store
     * all the values (that is, the caller is responsible
     * for ensuring the size of the array.)
     * 
     * The type of the array must be the same
     * as <code>getFieldUse().getType()</code>.
     */
    void toArray( JBlock block, JExpression $array );
    
    /**
     * Generates a code fragment to remove any "set" value
     * and move this field to the "unset" state.
     * 
     * @param body
     *      The code will be appended at the end of this block.
     */
    void unsetValues( JBlock body );
    
    /**
     * Return an expression that evaluates to true only when
     * this field has a set value(s).
     * 
     * @return null
     *      if the isSetXXX/unsetXXX method does not make sense 
     *      for the given field.
     */
    JExpression hasSetValue();

    /**
     * Get the "content" of this field in one object.
     * The type of this expression is the type returned by the
     * getValueType method.
     * 
     * <p>
     * This object will be returned from the getContent method
     * of the choice content interface.
     */
    JExpression getValue();
    
    /**
     * Get the type of the object returned from the getValue method.
     * 
     * @return
     *      A JClass object that represents the type. This method
     *      needs to return a JClass, not JPrimitiveType since
     *      the getContent method must be able to return null.
     */
    JClass getValueType();
    
        
    //
    //
    // marshaller-related methods
    //
    //
    
    // TODO: it's ugly to allow those methods to return null.
    // we should rather modify CodeModel to allow easier inspection,
    // so that the appropriate optimization can be done.

    /**
     * Returns an expression that evaluates to true
     * if and only if the number of items in this field
     * is equal to <code>i</code>.
     */
    JExpression ifCountEqual( int i );
    
    /**
     * Returns an expression that evaluates to true
     * if and only if the number of items in this field
     * is greater than or equal to <code>i</code>.
     * 
     * @param   i
     *      a non-zero integer 
     */
    JExpression ifCountGte( int i );


    /**
     * Returns an expression that evaluates to true
     * if and only if the number of items in this field
     * is less than or equal to <code>i</code>.
     */
    JExpression ifCountLte( int i );
    
    /**
     * Returns an expression that evaluates to the number of
     * items in this field.
     */
    JExpression count();

        
    /**
     * Generates the marshaller for this field, if necessary.
     * 
     * @param   block
     *      If any initialization code is necessary, the code
     *      should be added to this block immediately.
     * @param   uniqueId
     *      a string identifier which is unique within the marshal function.
     *      This id can be used to create local variables, etc.
     * @return
     *      Returned object will be used to marshal this field.
     */
    FieldMarshallerGenerator createMarshaller(
        JBlock block, String uniqueId );
}
