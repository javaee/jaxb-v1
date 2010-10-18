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

import java.util.ArrayList;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Default implementation of the FieldRendererFactory
 * that faithfully implements the semantics demanded by the JAXB spec.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class DefaultFieldRendererFactory implements FieldRendererFactory {
    
    public DefaultFieldRendererFactory( JCodeModel codeModel) {
        this( new UntypedListFieldRenderer.Factory(
            codeModel.ref(ArrayList.class)) );
    }

    public DefaultFieldRendererFactory( FieldRendererFactory defaultCollectionFieldRenderer ) {
        this.defaultCollectionFieldRenderer = defaultCollectionFieldRenderer;
    }
    
    private FieldRendererFactory defaultCollectionFieldRenderer;

    public FieldRenderer create(ClassContext context, FieldUse fu) {
        if(fu.multiplicity.isAtMostOnce()) {
            // non-collection field
            
            // if the field item is at most one (hence not a collection) and
            // its type is a boxed type (a type that wraps a primitive type),
            // for example "java.lang.Integer", use one of the
            // derived clases of UnboxedFieldImpl
            //
            // TODO: check for bidning info for optionalPrimitiveType=boxed or
            // noHasMethod=false and noDeletedMethod=false
            if(fu.isUnboxable())
                return new OptionalUnboxedFieldRenderer(context,fu);
            else
                // otherwise use the default non-collection field
                return new SingleFieldRenderer(context,fu);
        } else {
            // this field is a collection field.
            // use untyped list as the default. This is consistent
            // to the JAXB spec.
            return defaultCollectionFieldRenderer.create(context,fu);
        }
    }

}
