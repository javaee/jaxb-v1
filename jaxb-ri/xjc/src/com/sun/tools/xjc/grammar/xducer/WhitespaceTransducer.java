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

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.msv.datatype.xsd.WhiteSpaceProcessor;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;

/**
 * Decorates another transducer by adding whitespace normalization.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class WhitespaceTransducer extends TransducerDecorator {
    
    private final JCodeModel codeModel;
    private final WhitespaceNormalizer ws;
    
    private WhitespaceTransducer(Transducer _core,JCodeModel _codeModel,WhitespaceNormalizer _ws) {
        super(_core);
        this.codeModel = _codeModel;
        this.ws = _ws;
    }
    
    // factory methods
    public static Transducer create(Transducer _core,JCodeModel _codeModel,WhitespaceNormalizer _ws) {
        if( _ws==WhitespaceNormalizer.PRESERVE )
            return _core;
        else
            return new WhitespaceTransducer(_core,_codeModel,_ws);
    }
    /**
     * Creates a new transducer that does the right whitespace normalization
     * required by the given whitespace facet object.
     */
    public static Transducer create(Transducer _core,JCodeModel _codeModel,WhiteSpaceProcessor wsf) {
        return create(_core,_codeModel,getNormalizer(wsf));
    }
    /**
     * Creates a new transducer that does the right whitespace normalization
     * required by the given simple type.
     */
    public static Transducer create(Transducer _core,JCodeModel _codeModel,XSSimpleType t) {
        XSFacet f = t.getFacet(XSFacet.FACET_WHITESPACE);
        if(f==null)     // this happens for anySimpleType
            return _core;   // preserve
        else
            return create(_core,_codeModel,WhitespaceNormalizer.parse(f.getValue()));
    }

    
    public boolean isBuiltin() {
        // whitespace transducer doesn't change the builtin-ness.
        return core.isBuiltin();
    }



    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        return super.generateDeserializer(
            ws.generate(codeModel,literal), context);
    }


    private static WhitespaceNormalizer getNormalizer( WhiteSpaceProcessor proc ) {
        if( proc==WhiteSpaceProcessor.theCollapse )
            return WhitespaceNormalizer.COLLAPSE;
        
        if( proc==WhiteSpaceProcessor.theReplace )
            return WhitespaceNormalizer.REPLACE;
        
        if( proc==WhiteSpaceProcessor.thePreserve )
            return WhitespaceNormalizer.PRESERVE;
            
        throw new JAXBAssertionError();  // there are only three instances
    };
}
