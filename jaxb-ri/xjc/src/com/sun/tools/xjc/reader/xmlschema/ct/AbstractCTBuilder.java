/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * helper base class for the {@link ComplexTypeFieldBuilder} 
 * implementation classes.
 * 
 * This class provides quick access to various frequently accessed
 * objects inside BGMBuilder.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractCTBuilder implements CTBuilder {
    
    /** Reference to the parent. */
    protected final ComplexTypeFieldBuilder builder;
    
    /** Reference to the central object of the binding. */
    protected final BGMBuilder bgmBuilder;
    
    protected final ExpressionPool pool;
    
    protected AbstractCTBuilder( ComplexTypeFieldBuilder _builder ) {
        this.builder = _builder;
        this.bgmBuilder = builder.builder;
        this.pool = bgmBuilder.grammar.getPool();
    }

    protected static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
    
}
