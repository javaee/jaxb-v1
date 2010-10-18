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
import com.sun.tools.xjc.grammar.id.SymbolSpace;

/**
 * Filter implementation of a {@link Transducer} that passes
 * through every method call to another Transducer.
 * 
 * The derived class is expected to override methods and add
 * some meaningful semantics.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class TransducerDecorator implements Transducer {
    
    /** Next transducer in the chain. */
    protected final Transducer core;
    
    protected TransducerDecorator( Transducer _core ) {
        this.core = _core;
    }
    
    public JType getReturnType() {
        return core.getReturnType();
    }

    public boolean isBuiltin() {
        // decorating another transducer will usually render the conversion non-standard.
        return false;
    }

    public void populate(AnnotatedGrammar grammar, GeneratorContext context) {
        core.populate(grammar,context);
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        return core.generateSerializer(value,context);
    }

    public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {
        core.declareNamespace(body,value,context);
    }

    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        return core.generateDeserializer(literal,context);
    }

    public boolean needsDelayedDeserialization() {
        return core.needsDelayedDeserialization();
    }
    
    public boolean isID() {
        return core.isID();
    }
    public SymbolSpace getIDSymbolSpace() {
        return core.getIDSymbolSpace();
    }

    public JExpression generateConstant(ValueExp exp) {
        return core.generateConstant(exp);
    }

}
