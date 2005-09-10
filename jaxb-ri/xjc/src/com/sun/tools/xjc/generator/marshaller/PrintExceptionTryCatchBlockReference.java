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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JTryBlock;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.runtime.Util;

/**
 * Block reference that generates <code>try - catch</code> block
 * that cathces an exception and pass it to the
 * {@link ContentHandlerEx.handlePrintConversionException} method.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class PrintExceptionTryCatchBlockReference implements BlockReference {

    private final BlockReference parent;
    private final Context context;
    
    /** Once a try-catch block is created. It will be stored here. */
    private JTryBlock block = null;


    PrintExceptionTryCatchBlockReference( Context _context ) {
        this.context = _context;
        this.parent = context.getCurrentBlock();
    }
    
    public JBlock get(boolean create) {
        if(!create && block==null)  return null;
        
        if(block==null) {
            // create the try-catch block.
            block = parent.get(true)._try();
            
            JCodeModel codeModel = context.codeModel;
            
            JCatchBlock $catch = block._catch(codeModel.ref(Exception.class));
            $catch.body().staticInvoke(
                context.getRuntime(Util.class), "handlePrintConversionException")
                .arg(JExpr._this())
                .arg($catch.param("e"))
                .arg(context.$serializer);
        }
        
        return block.body();
    }

}
