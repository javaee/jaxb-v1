/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
