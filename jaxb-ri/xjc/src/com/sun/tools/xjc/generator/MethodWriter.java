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

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc.generator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * The back-end may or may not generate the content interface
 * separately from the implementation class. If so, a method
 * needs to be declared on both the interface and the implementation class.
 * <p>
 * This class hides those details and allow callers to declare
 * methods just once.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class MethodWriter {
    protected final JCodeModel codeModel;
    
    protected MethodWriter(ClassContext context) {
        this.codeModel = context.parent.getCodeModel();
    }

    /**
     * Declares a method in both the interface and the implementation.
     * 
     * @return
     *      JMethod object that represents a newly declared method
     *      on the implementation class.
     */
    public abstract JMethod declareMethod( JType returnType, String methodName );
    
    public final JMethod declareMethod( Class returnType, String methodName ) {
        return declareMethod( codeModel.ref(returnType), methodName );
    }
    
    /**
     * To generate javadoc for the previously declared method, use this method
     * to obtain a {@link JDocComment} object. This may return a value
     * different from declareMethod().javadoc().
     */
    public abstract JDocComment javadoc();

            
    /**
     * Adds a parameter to the previously declared method.
     * 
     * @return
     *      JVar object that represents a newly added parameter
     *      on the implementation class.
     */
    public abstract JVar addParameter( JType type, String name );
    
    public final JVar addParameter( Class type, String name ) {
        return addParameter( codeModel.ref(type), name );
    }
}
