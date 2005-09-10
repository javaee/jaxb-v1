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
