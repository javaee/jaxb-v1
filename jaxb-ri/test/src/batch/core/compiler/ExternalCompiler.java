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

package batch.core.compiler;

import java.util.Collection;

import batch.core.Util;
import batch.core.XJCException;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ExternalCompiler extends AbstractXJCCompilerImpl {
    
    /**
     * Location to the executable file.
     */
    private final String executable;
    
    public ExternalCompiler( String _executable, Collection xjcParams ) {
        super(xjcParams);
        this.executable = _executable;
    }

    public ExternalCompiler( String _executable ) {
        super();
        this.executable = _executable;
    }

    protected void invoke( String[] args ) throws XJCException {
        String[] commandLine = new String[args.length+1];
        commandLine[0] = executable;
        System.arraycopy(args,0,commandLine,1,args.length);
        
        try {
            int r = Util.execProcess( Runtime.getRuntime().exec(commandLine) );
            if(r!=0)
                throw new XJCException("XJC exited normally but with error code "+r);
        } catch (Exception e) {
            throw new XJCException(e);
        }
    }
}
