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
