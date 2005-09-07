/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.ProgressCodeWriter;


/**
 * Root of the code DOM.
 */
public final class JCodeModel {
    
    /** The packages that this JCodeWriter contains. */
    private HashMap packages = new HashMap();
    
    /** All JReferencedClasses are pooled here. */
    private final HashMap refClasses = new HashMap();

    
    /** Obtains a reference to the special "null" type. */
    public final JNullType NULL = new JNullType(this);
    // primitive types 
    public final JPrimitiveType VOID    = new JPrimitiveType(this,"void",   Void.class);
    public final JPrimitiveType BOOLEAN = new JPrimitiveType(this,"boolean",Boolean.class);
    public final JPrimitiveType BYTE    = new JPrimitiveType(this,"byte",   Byte.class);
    public final JPrimitiveType SHORT   = new JPrimitiveType(this,"short",  Short.class);
    public final JPrimitiveType CHAR    = new JPrimitiveType(this,"char",   Character.class);
    public final JPrimitiveType INT     = new JPrimitiveType(this,"int",    Integer.class);
    public final JPrimitiveType FLOAT   = new JPrimitiveType(this,"float",  Float.class);
    public final JPrimitiveType LONG    = new JPrimitiveType(this,"long",   Long.class);
    public final JPrimitiveType DOUBLE  = new JPrimitiveType(this,"double", Double.class);
    
    /**
     * If the flag is true, we will consider two classes "Foo" and "foo"
     * as a collision.
     */
    protected static final boolean isCaseSensitiveFileSystem = getFileSystemCaseSensitivity();
    
    private static boolean getFileSystemCaseSensitivity() {
        try {
            // let the system property override, in case the user really
            // wants to override.
            if( System.getProperty("com.sun.codemodel.FileSystemCaseSensitive")!=null )
                return true;
        } catch( Exception e ) {}
        
        // on Unix, it's case sensitive.
        return (File.separatorChar == '/');
    }


    public JCodeModel() {}
    
    /**
     * Add a package to the list of packages to be generated
     *
     * @param name
     *        Name of the package. Use "" to indicate the root package.
     *
     * @return Newly generated package
     */
    public JPackage _package(String name) {
        JPackage p = (JPackage)(packages.get(name));
        if (p == null) {
            p = new JPackage(name, this);
            packages.put(name, p);
        }
        return p;
    }
    
    public final JPackage rootPackage() {
        return _package("");
    }

    /**
     * Returns an iterator that walks the packages defined using this code
     * writer.
     */
    public Iterator packages() {
        return packages.values().iterator();
    }
    
    /**
     * Creates a new generated class.
     * 
     * @exception JClassAlreadyExistsException
     *      When the specified class/interface was already created.
     */
    public JDefinedClass _class(String fullyqualifiedName) throws JClassAlreadyExistsException {
        int idx = fullyqualifiedName.lastIndexOf('.');
        if( idx<0 )     return rootPackage()._class(fullyqualifiedName);
        else
            return _package(fullyqualifiedName.substring(0,idx))
                ._class( fullyqualifiedName.substring(idx+1) );
    }
    
    /**
     * Gets a reference to the already created generated class.
     * 
     * @return null
     *      If the class is not yet created.
     */
    public JDefinedClass _getClass(String fullyQualifiedName) {
        int idx = fullyQualifiedName.lastIndexOf('.');
        if( idx<0 )     return rootPackage()._getClass(fullyQualifiedName);
        else
            return _package(fullyQualifiedName.substring(0,idx))
                ._getClass( fullyQualifiedName.substring(idx+1) );
    }

    /**
     * Creates a new anonymous class.
     */
    public JDefinedClass newAnonymousClass(JClass baseType) {
        return new JAnonymousClass(baseType,this);
    }


    /**
     * Generates Java source code.
     * A convenience method that calls {@link #build(CodeWriter)}.
     * 
     * @param	destDir
     *		source files are generated into this directory.
     * @param   status
     *      if non-null, progress indication will be sent to this stream.
     */
    public void build( File destDir, java.io.PrintStream status ) throws IOException {
        CodeWriter out = new FileCodeWriter(destDir);
        if(status!=null)
            out = new ProgressCodeWriter(out, status );
        
        build(out);
    }
    
    /**
     * A convenience method for <code>build(destDir,System.out)</code>.
     */
    public void build( File destDir ) throws IOException {
        build(destDir,System.out);
    }
    
    /**
     * Generates Java source code.
     */
    public void build( CodeWriter out ) throws IOException {
        for (Iterator i = packages.values().iterator(); i.hasNext();)
            ((JPackage)(i.next())).build(out);
        out.close();
    }
    
	
	
    /**
     * Obtains a reference to an existing class from its Class object.
     */
    public JClass ref(Class clazz) {
        JReferencedClass jrc = (JReferencedClass)refClasses.get(clazz);
        if (jrc == null) {
            if (clazz.isArray()) {
                return new JArrayClass(this, ref(clazz.getComponentType()));
            } else {
                jrc = new JReferencedClass(clazz);
                refClasses.put(clazz, jrc);
            }
        }
        return jrc;
    }

    /**
     * Obtains a reference to an existing class from its fully-qualified
     * class name.
     * 
     * @exception ClassNotFoundException
     *      If the specified class is not available in the current class path.
     */
    public JClass ref(String fullyQualifiedClassName) throws ClassNotFoundException {
        try {
            // try the context class loader first
            return ref(Thread.currentThread().getContextClassLoader().loadClass(fullyQualifiedClassName));
        } catch (ClassNotFoundException e) {
            // then the default mechanism.
            return ref(Class.forName(fullyQualifiedClassName));
        }
    }

    /**
     * References to existing classes.
     * 
     * <p>
     * JReferencedClass is kept in a pool so that they are shared.
     * There is one pool for each JCodeModel object.
     * 
     * <p>
     * It is impossible to cache JReferencedClass globally only because
     * there is the _package() method, which obtains the owner JPackage
     * object, which is scoped to JCodeModel.
     */
    private class JReferencedClass extends JClass implements JDeclaration {
        private final Class _class;

        JReferencedClass(Class _clazz) {
            super(JCodeModel.this);
            this._class = _clazz;

            if (_class.isArray())
                throw new InternalError("assertion failure: class cannot be an array");
        }

        /**
         * Converts encoded array type name (e.g., "[[B") into
         * a normal type name (e.g., "byte[][]").
         */
        private String getArrayName(String name) {
            // this reference class actually points to an array
            // I don't know if it's a good idea to keep it as JClass
            // --- you can well argue that since it's array, we should
            // use JType. I think, since we allow JCodeModel.ref method
            // to take a Class object, it's really hard (and not application
            // friendly) to prohibit those arrays.
            // so let's just make it work.
            int i = 1;
            while (name.charAt(i) == '[')
                i++;

            String[] primitives =
                new String[] { "Bbyte", "Cchar", "Ddouble", "Ffloat", "Iint", "Jlong", "Sshort", "2boolean" };

            String r = null;

            for (int j = 0; j < primitives.length; j++) {
                if (primitives[j].charAt(0) == name.charAt(i))
                    r = primitives[j].substring(1);
            }
            if (r == null) {
                // ASSERT: it must be Lclassname;
                if (name.charAt(i) != 'L')
                    throw new InternalError();

                r = name.substring(i + 1, name.length() - 1);
            }

            for (; i > 0; i--)
                r += "[]";
            return r;
        }

        public String name() {
            String name = _class.getName();
            if (name.charAt(0) == '[')
                return getArrayName(name);

            int idx = name.lastIndexOf('.');
            if (idx < 0)
                return name;
            else
                return name.substring(idx + 1);
        }

        public String fullName() {
            // TODO: this isn't accurate. this computes a binary name, not a full name.
            String name = _class.getName();
            if (name.charAt(0) == '[')
                return getArrayName(name);
            else
                return name;
        }

        public String binaryName() {
            String name = _class.getName();
            if (name.charAt(0) == '[')
                return getArrayName(name);
            else
                return name;
        }
        
        public JPackage _package() {
            String name = fullName();

            // this type is array
            if (name.indexOf('[') != -1)
                return JCodeModel.this._package("");

            // other normal case
            int idx = name.lastIndexOf('.');
            if (idx < 0)
                return JCodeModel.this._package("");
            else
                return JCodeModel.this._package(name.substring(0, idx));
        }

        public JClass _extends() {
            Class sp = _class.getSuperclass();
            if (sp == null)
                return null;
            else
                return JCodeModel.this.ref(sp);
        }

        public Iterator _implements() {
            final Class[] interfaces = _class.getInterfaces();
            return new Iterator() {
                private int idx = 0;
                public boolean hasNext() {
                    return idx < interfaces.length;
                }
                public Object next() {
                    return JCodeModel.this.ref(interfaces[idx++]);
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public boolean isInterface() {
            return _class.isInterface();
        }

        public JPrimitiveType getPrimitiveType() {
            if (_class == Boolean.class)
                return BOOLEAN;
            if (_class == Byte.class)
                return BYTE;
            if (_class == Character.class)
                return CHAR;
            if (_class == Double.class)
                return DOUBLE;
            if (_class == Float.class)
                return FLOAT;
            if (_class == Integer.class)
                return INT;
            if (_class == Long.class)
                return LONG;
            if (_class == Short.class)
                return SHORT;
            if (_class == Void.class)
                return VOID;
            return null;
        }

        public boolean isArray() {
            return _class.isArray();
        }

        public void generate(JFormatter f) {
            // TODO: imported parameter should be propagated from its parent.
            // we cannot have that information in this class since there is
            // one-to-one correspondence between a java.lang.Class object
            // and JReferenecedClass class.

            f.p(fullName());
        }

        public void declare(JFormatter f) {
        }

    }
}
