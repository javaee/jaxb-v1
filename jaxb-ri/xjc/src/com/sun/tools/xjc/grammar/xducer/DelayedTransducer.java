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

package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;

/**
 * Wrapper of a Transducer that defers the instanciation of the actual
 * transducer.
 * 
 * <p>
 * To use this transducer, derive a new class from it and implement
 * the create method.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class DelayedTransducer implements Transducer {
    
    private Transducer core = null;
    
    /**
     * Create a transducer object, which will be then used
     * to implement the Transducer semantics.
     * 
     * This method is called once when one of the methods is called
     * from clients.
     * 
     * @return
     *      Always return non-null valid object.
     */
    protected abstract Transducer create();
    
    /**
     * Ensure <code>core!=null</code>.
     */
    private void update() {
        if( core==null )        core = create();
    }
    
    public JType getReturnType() {
        update();
        return core.getReturnType();
    }

    public boolean isBuiltin() {
        return false;
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        update();
        return core.generateSerializer(value,context);
    }

    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        update();
        return core.generateDeserializer(literal,context);
    }

    public JExpression generateConstant(ValueExp exp) {
        update();
        return core.generateConstant(exp);
    }

    public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {
        update();
        core.declareNamespace(body,value,context);
    }

    public boolean needsDelayedDeserialization() {
        update();
        return core.needsDelayedDeserialization();
    }

    public void populate(AnnotatedGrammar grammar, GeneratorContext context) {
        update();
        core.populate(grammar,context);
    }

}
