/*
 * @(#)$Id: ClassLoaderDef.java,v 1.1 2004-06-25 21:13:10 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
