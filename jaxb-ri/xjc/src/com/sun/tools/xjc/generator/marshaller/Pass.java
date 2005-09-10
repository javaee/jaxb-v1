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
