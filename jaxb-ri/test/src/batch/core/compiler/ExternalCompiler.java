/*
 * @(#)$Id: ExternalCompiler.java,v 1.1 2004-06-25 21:13:01 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
