/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.marshaller;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;

/**
 * BGM will be scanned three times, each with different
 * {@link Pass} object to generate a different kind of marshaller.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
interface Pass {
    
    /**
     * Switches to this pass and then call the build
     * method of the {@link Context} class.
     */
    void build( Expression exp );
    
    /**
     * Gets the name of the pass. Used as the method name
     * in the generated code.
     */
    String getName();
    
    void onElement( ElementExp exp );
    
    void onExternal( ExternalItem item );
    
    void onAttribute( AttributeExp exp );
    
    void onPrimitive( PrimitiveItem exp );

    void onValue( ValueExp exp );

}
