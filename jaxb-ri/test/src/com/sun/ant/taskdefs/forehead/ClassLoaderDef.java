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

package com.sun.ant.taskdefs.forehead;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * &lt;classLoader> tag of {@link ForeheadConfTask}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ClassLoaderDef extends Path {
    
    /** short name of this class loader. */
    private String name;
    /** optional alias name. */
    private String alias;
    /** parent class loader definition */
    private final ClassLoaderDef parent;
    /** List of child {@link ClassLoaderDef}s. */
    private final List children = new ArrayList();
    
    /** Optional main class. */
    private String mainClass;
    
    ClassLoaderDef( ClassLoaderDef _parent, Project project ) {
        super(project);
        this.parent = _parent;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public void setAlias( String name ) {
        this.alias = name;
    }
    
    public void setMainClass( String mainClass ) {
        this.mainClass = mainClass;
    }
    
    /**
     * A new child class loader.
     */
    public ClassLoaderDef createClassLoader() {
        ClassLoaderDef cl = new ClassLoaderDef(this,getProject());
        children.add(cl);
        return cl;
    }
    
    void write( PrintWriter w ) throws BuildException {
        if(name==null)
            throw new BuildException("name is not given for the class loader");
        
        w.println('['+getFullName()+']');
        
        String[] entries = list();
        for( int i=0; i<entries.length; i++ )
            w.println("    "+entries[i]);
        w.println();
        
        for( Iterator itr=children.iterator(); itr.hasNext(); ) {
            ClassLoaderDef child = (ClassLoaderDef)itr.next();
            child.write(w);
        }
        
        if(mainClass!=null) {
            w.println("=["+getFullName()+"] "+mainClass);
            w.println();
        }
    }
    
    public void writeAlias(Properties props) {
        if(alias!=null)
            props.put(alias,getFullName());
        
        for( Iterator itr=children.iterator(); itr.hasNext(); ) {
            ClassLoaderDef child = (ClassLoaderDef)itr.next();
            child.writeAlias(props);
        }
        
    }
    
    private String getFullName() {
        String fullName="";
        if( parent!=null)
            fullName = parent.getFullName()+'.';
        fullName += name;
        return fullName;
    }

}
