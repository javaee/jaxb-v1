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
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.generator.util.LazyBlockReference;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * <p>
 * This renderer is used to realize the "nil" special property,
 * which is used for the xsi:nil support.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XsiNilFieldRenderer extends AbstractFieldRendererWithVar {
    
    /**
     * FieldRendererFactory implementation that returns
     * XsiNilFieldRenderer.
     */
    public final static FieldRendererFactory theFactory =
        new FieldRendererFactory() {
            public FieldRenderer create(ClassContext context, FieldUse fu) {
                return new XsiNilFieldRenderer(context,fu);
            }
        };
    
    /**
     * Code fragment that gets executed when the set method
     * is called. IOW, this is an event handler of the "onSet" event.
     */
    private BlockReference onSetEvent;
    
    
    
    public XsiNilFieldRenderer( ClassContext context, FieldUse fu ) {
        super(context,fu);
    }
    
    
    
    protected JFieldVar generateField() {
        return generateField(codeModel.BOOLEAN);
    }

    public void generateAccessors() {
        JMethod $get = writer.declareMethod( codeModel.BOOLEAN, "is"+fu.name );
        writer.javadoc().appendComment(fu.getJavadoc());
                
        $get.body()._return(ref());
                
        // [RESULT]
        // void setNil( boolean v ) {
        //     value=v;
        //     if(v) {
        //         /*onSetEventHandler*/
        //         this part is created on demand
        //     }
        JMethod $set = writer.declareMethod( codeModel.VOID, "set"+fu.name );
        final JVar $value = writer.addParameter( codeModel.BOOLEAN, "value" );
        final JBlock body = $set.body();
        body.assign(ref(),$value);
        
        writer.javadoc().appendComment(
            "Passing <code>true</code> will generate xsi:nil in the XML output"
        );
        writer.javadoc().appendComment(fu.getJavadoc());
        
        
        onSetEvent = new LazyBlockReference() {
            protected JBlock create() {
                return body._if($value)._then();
            }
        };
    }

    public JBlock getOnSetEventHandler() {
        return onSetEvent.get(true);
    }
    
    public void setter( JBlock block, JExpression newValue ) {
        block.assign(ref(), newValue);
    }
    
    public void toArray( JBlock block, JExpression $array ) {
        block.assign( $array.component(JExpr.lit(0)), ref() );
    }
    
    // since XsiNilFieldRenderer is used internally, this will
    // never be requested to produce those two methods.
    public void unsetValues( JBlock body ) {
        throw new JAXBAssertionError();
    }
    public JExpression hasSetValue() {
        return null;
    }
    public JExpression getValue() {
        return codeModel.BOOLEAN.wrap(ref());
    }
    public JClass getValueType() {
        return codeModel.BOOLEAN.getWrapperClass();
    }
    
    
    public JExpression ifCountEqual( int i ) {
        switch(i) {
        case 0:     return ref().not();
        case 1:     return ref();
        default:    return JExpr.FALSE;
        }
    }
    
    public JExpression ifCountGte( int i ) {
        if(i==1)    return ref();
        else        return JExpr.FALSE;
    }
    
    public JExpression ifCountLte( int i ) {
        if(i==0)    return ref().not();
        else        return JExpr.TRUE;
    }
    
    public JExpression count() {
        return JOp.cond(ref(),JExpr.lit(1),JExpr.lit(0));
    }
    
    public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId) {
        return new FieldMarshallerGenerator() {
            public JExpression hasMore() {
                // the xsi:nil is effective only if the value is true.
                return ref();
            }
            public JExpression peek(boolean increment) {
                return ref();
            }
            public void increment(BlockReference block) {}
            public FieldMarshallerGenerator clone( JBlock block, String uniqueId ) {
                // since this iterator has only one item,
                // there is no need to clone it.
                return this;
            }
            public FieldRenderer owner() { return XsiNilFieldRenderer.this; }
        };
    }
}
