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
package com.sun.ant.taskdefs.forehead;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Ant task that generates <tt>forehead.conf</tt>.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ForeheadConfTask extends Task {
    /** config file to be written. */
    private File confFile;

    /** optional class loader alias property file. */
    private File aliasFile;
    
    private ClassLoaderDef rootClassLoader; 
    
    public void setFile( File confFile ) {
        this.confFile = confFile;
    }
    
    public void setAliasFile( File aliasFile ) {
        this.aliasFile = aliasFile;
    }
    
    public ClassLoaderDef createClassLoader() {
        if( rootClassLoader!=null )
            throw new BuildException("The root class loader is already defined.");
        rootClassLoader = new ClassLoaderDef(null,getProject());
        return rootClassLoader;
    }
    
    public void execute() throws BuildException {
        if( rootClassLoader==null )
            throw new BuildException("No class loader is defined.");
        
        log("Writing "+confFile,Project.MSG_INFO);
        
        try {
            PrintWriter w = new PrintWriter(new FileWriter(confFile));
            rootClassLoader.write(w);
            w.close();
        } catch( IOException e ) {
            throw new BuildException(e);
        }
        
        if(aliasFile!=null) {
            log("Writing "+confFile,Project.MSG_INFO);
            
            Properties props = new Properties();
            rootClassLoader.writeAlias(props);
            try {
                FileOutputStream os = new FileOutputStream(aliasFile);
                props.store(os,"Forehad class loader alias file");
                os.close();
            } catch( IOException e ) {
                throw new BuildException(e);
            }
        }
    }
    
}
