/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import com.sun.codemodel.util.EncoderFactory;
import com.sun.codemodel.util.UnicodeEscapeWriter;


/**
 * A Java package.
 */
public final class JPackage implements JDeclaration, JGenerable, JClassContainer {

    /**
     * Name of the package.
     * May be the empty string for the root package.
     */
    private String name;

    private final JCodeModel owner;

    /**
     * List of classes contained within this package keyed by their name.
     */
    private final Map classes = new TreeMap();

    /**
     * List of resources files inside this package.
     */
    private final Set resources = new HashSet();
    
    /**
     * All {@link JClass}s in this package keyed the upper case class name.
     * 
     * This field is non-null only on Windows, to detect
     * "Foo" and "foo" as a collision. 
     */
    private final Map upperCaseClassMap;
    
    /**
     * JPackage constructor
     *
     * @param name
     *        Name of package
     *
     * @param  cw  The code writer being used to create this package
     *
     * @throws IllegalArgumentException
     *         If each part of the package name is not a valid identifier
     */
    JPackage(String name, JCodeModel cw) {
        this.owner = cw;
        if (name.equals(".")) {
            String msg = "JPackage name . is not allowed";
            throw new IllegalArgumentException(msg);
        }
        
        int dots = 1;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '.') {
                dots++;
                continue;
            }
            if (dots > 1) {
                String msg = "JPackage name " + name + " missing identifier";
                throw new IllegalArgumentException(msg);
            } else if (dots == 1 && !Character.isJavaIdentifierStart(c)) {
                String msg =
                    "JPackage name " + name + " contains illegal " + "character for beginning of identifier: " + c;
                throw new IllegalArgumentException(msg);
            } else if (!Character.isJavaIdentifierPart(c)) {
                String msg = "JPackage name " + name + "contains illegal " + "character: " + c;
                throw new IllegalArgumentException(msg);
            }
            dots = 0;
        }
        if (!name.trim().equals("") && dots != 0) {
            String msg = "JPackage name not allowed to end with .";
            throw new IllegalArgumentException(msg);
        }
        
        if(JCodeModel.isCaseSensitiveFileSystem)
            upperCaseClassMap = null;
        else
            upperCaseClassMap = new HashMap();
        
        this.name = name;
    }


    public JClassContainer parentContainer() {
        return parent();
    }
    
    /**
     * Gets the parent package, or null if this class is the root package.
     */
    public JPackage parent() {
        if(name.length()==0)    return null;
        
        int idx = name.lastIndexOf('.');
        return owner._package(name.substring(0,idx));
    }

    /**
     * Add a class to this package.
     *
     * @param mods
     *        Modifiers for this class declaration
     *
     * @param name
     *        Name of class to be added to this package
     *
     * @return Newly generated class
     * 
     * @exception JClassAlreadyExistsException
     *      When the specified class/interface was already created.
     */
    public JDefinedClass _class(int mods, String name) throws JClassAlreadyExistsException {
        return _class(mods,name,false);
    }
    
    public JDefinedClass _class( int mods, String name, boolean isInterface ) throws JClassAlreadyExistsException {
        if(classes.containsKey(name))
            throw new JClassAlreadyExistsException((JDefinedClass)classes.get(name));
        else {
            // XXX problems caught in the NC constructor
            JDefinedClass c = new JPackageMemberClass(this, mods, name, isInterface );
            
            if( upperCaseClassMap!=null ) {
                JDefinedClass dc = (JDefinedClass)upperCaseClassMap.get(name.toUpperCase());
                if(dc!=null)
                    throw new JClassAlreadyExistsException(dc);
                upperCaseClassMap.put(name.toUpperCase(),c);
            }            
            classes.put(name,c);
            return c;
        }
    }

	/**
	 * Adds a public class to this package.
	 */
    public JDefinedClass _class(String name) throws JClassAlreadyExistsException {
		return _class( JMod.PUBLIC, name );
	}

    /**
     * Gets a reference to the already created {@link JDefinedClass}.
     * 
     * @return null
     *      If the class is not yet created.
     */
    public JDefinedClass _getClass(String name) {
        if(classes.containsKey(name))
            return (JDefinedClass)classes.get(name);
        else
            return null;
    }

	
    /**
     * Add an interface to this package.
     *
     * @param mods
     *        Modifiers for this interface declaration
     *
     * @param name
     *        Name of interface to be added to this package
     *
     * @return Newly generated interface
     */
    public JDefinedClass _interface(int mods, String name) throws JClassAlreadyExistsException {
        return _class(mods,name,true);
    }

    /**
     * Adds a public interface to this package.
     */
    public JDefinedClass _interface(String name) throws JClassAlreadyExistsException {
        return _interface(JMod.PUBLIC, name);
    }
	
    /**
     * Adds a new resource file to this package.
     */
    public JResourceFile addResourceFile(JResourceFile rsrc) {
        resources.add(rsrc);
        return rsrc;
    }
    
    /**
     * Checks if a resource of the given name exists.
     */
    public boolean hasResourceFile(String name) {
        for (Iterator itr = resources.iterator(); itr.hasNext();) {
            JResourceFile r = (JResourceFile)itr.next();
            if( r.name().equals(name) )
                return true;
        }
        return false;
    }
    
    /**
     * Iterates all resource files in this package.
     */
    public Iterator propertyFiles() {
        return resources.iterator();
    }
    
    /**
     * Removes a class from this package.
     */
    public void remove(JClass c) {
        if (c._package() != this)
            throw new IllegalArgumentException(
                "the specified class is not a member of this package," + " or it is a referenced class");

        // note that c may not be a member of classes.
        // this happens when someone is trying to remove a non generated class
        classes.remove(c.name());
        if (upperCaseClassMap != null)
            upperCaseClassMap.remove(c.name().toUpperCase());
    }
	
    /**
     * Reference a class within this package.
     */
    public JClass ref(String name) {
        if (name.indexOf('.') >= 0)
            throw new IllegalArgumentException("JClass name contains '.': " + name);

        String n = "";
        if (!isUnnamed())
            n = this.name + ".";
        n += name;

        try {
            return owner.ref(Class.forName(n));
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.toString());
        }
    }
    
    /**
     * Gets a reference to a sub package of this package.
     */
    public JPackage subPackage( String pkg ) {
        if(isUnnamed())     return owner()._package(pkg);
        else                return owner()._package(name+"."+pkg);
    }

    /**
     * Returns an iterator that walks the top-level classes defined in this
     * package.
     */
    public Iterator classes() {
        return classes.values().iterator();
    }
    
    /**
     * Checks if a given name is already defined as a class/interface
     */
    public boolean isDefined(String classLocalName) {
        Iterator itr = classes();
        while (itr.hasNext()) {
            if (((JClass)itr.next()).name().equals(classLocalName))
                return true;
        }

        return false;
    }

    /**
     * Checks if this package is the root, unnamed package.
     */
    public final boolean isUnnamed() { return name.length() == 0; }

    /**
     * Get the name of this package
     *
     * @return
     *		The name of this package, or the empty string if this is the
     *		null package. For example, this method returns strings like
     *		<code>"java.lang"</code>
     */
    public String name() {
        return name;
    }

    /**
     * Return the code model root object being used to create this package.
     */
    public final JCodeModel owner() { return owner; }

    /**
     * Convert the package name to directory path equivalent
     */
    File toPath(File dir) {
        if (name == null) return dir;
        return new File(dir, name.replace('.', File.separatorChar));
    }

    public void declare(JFormatter f ) {
        if (name.length() != 0)
            f.p("package").p(name).p(';').nl();
    }

    public void generate(JFormatter f) {
        f.p(name);
    }


    void build( CodeWriter out ) throws IOException {
        
        for (Iterator i = classes.values().iterator(); i.hasNext();) {
            JPackageMemberClass c = (JPackageMemberClass)(i.next());
            
            if( c.isHidden() )
                continue;   // don't generate this file
            
            Writer bw = new BufferedWriter(
                new OutputStreamWriter(out.open(this,c.name()+".java")));
            
            // create writer
            try {
                bw = new UnicodeEscapeWriter(bw) {
                    // can't change this signature to Encoder because
                    // we can't have Encoder in method signature
                    private final Object encoder = EncoderFactory.createEncoder(System.getProperty("file.encoding"));;
                    protected boolean requireEscaping(int ch) {
                        // control characters
                        if( ch<0x20 && " \t\r\n".indexOf(ch)==-1 )  return true;
                        
                        return !((CharsetEncoder)encoder).canEncode((char)ch);
                    }
                };
            } catch( Throwable t ) {
                bw = new UnicodeEscapeWriter(bw);
            }
            
            JFormatter f = new JFormatter(new PrintWriter(bw));
            c.declare(f);
            f.close();
        }
        
        for( Iterator i = resources.iterator(); i.hasNext();) {
            JResourceFile rsrc = (JResourceFile)i.next();
            
            OutputStream os = new BufferedOutputStream(out.open(this,rsrc.name()));
            rsrc.build(os);
            os.close();
        }    
    }

}
