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
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Used to generate the getValueObject/setValueObject fields.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XsiTypeFieldRenderer extends SingleFieldRenderer {

    /**
     * FieldRendererFactory implementation that returns
     * XsiNilFieldRenderer.
     */
    public final static class Factory implements FieldRendererFactory {
        private final ClassItem defaultObjectType;
        public Factory( ClassItem _defaultObjectType ) {
            this.defaultObjectType = _defaultObjectType;
        }
        public FieldRenderer create(ClassContext context, FieldUse fu) {
            return new XsiTypeFieldRenderer(context,fu,
                context.parent.getClassContext(defaultObjectType).implRef);
        }
    }

    
    private final JClass defaultObject;
    
    public XsiTypeFieldRenderer( ClassContext context, FieldUse fu, JClass _defaultObject ) {
        super(context,fu);
        this.defaultObject = _defaultObject;
    }

    protected JFieldVar generateField() {
        return context.implClass.field( JMod.PROTECTED, fu.type, "_"+fu.name,
            JExpr._new(defaultObject));
    }
    
    public JExpression ifCountEqual( int i ) {
        if(i==1)    return JExpr.TRUE;
        else        return JExpr.FALSE;
    }
    public JExpression ifCountGte( int i ) {
        if(i<=1)    return JExpr.TRUE;
        else        return JExpr.FALSE;
    }
    
    public JExpression ifCountLte( int i ) {
        if(i==0)    return JExpr.FALSE;
        else        return JExpr.TRUE;
    }

    public JExpression count() {
        return JExpr.lit(1);
    }
    
    
    public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId) {
        return new FieldMarshallerGenerator() {
            public JExpression hasMore() {
                // hasMore() is true when there is an object
                return JExpr.TRUE;
            }
            public JExpression peek(boolean increment) {
                return ref();
            }
            public void increment(BlockReference block) {}
            public FieldMarshallerGenerator clone( JBlock block, String uniqueId ) {
                // since this iterator has only one item (or zero),
                // there is no need to clone it.
                return this;
            }
            public FieldRenderer owner() { return XsiTypeFieldRenderer.this; }
        };
    }

}
