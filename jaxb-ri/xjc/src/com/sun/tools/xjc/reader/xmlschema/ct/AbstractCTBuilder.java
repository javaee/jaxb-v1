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
