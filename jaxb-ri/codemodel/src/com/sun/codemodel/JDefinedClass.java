/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * A generated Java class.
 */
public abstract class JDefinedClass
    extends JClass
    implements JDeclaration, JClassContainer
{

    /** Name of this class */
    private String name = null;

    // this field cannot be modified.
    private final boolean isInterface;

    /** Modifiers for the class declaration */
    private JMods mods;

    /** Name of the super class of this class. */
    private JClass superClass;

    /** List of interfaces that this class implements */
    private final List interfaces = new ArrayList();

    /** Set of fields that are members of this class */
    private final List fields = new ArrayList();
    
    /** Fields keyed by their names. */
    private final Map fieldsByName = new HashMap();

    /** Static initializer, if this class has one */
    private JBlock init = null;

    /** class javadoc */
    private JDocComment jdoc = null;
    
    /** Set of constructors for this class, if any */
    private final List constructors = new ArrayList();

    /** Set of methods that are members of this class */
    private final List methods = new ArrayList();

    /** Nested classes as a map from name to JDefinedClass */
    private final Map classes = new TreeMap();
    
    /** Null, or a {@link #classes} keyed by the upper-case class name. */
    private final Map upperCaseClassMap;
    
    /**
     * Flag that controls whether this class should be really generated or not.
     * 
     * Sometimes it is useful to generate code that refers to class X,
     * without actually generating the code of X.
     * This flag is used to surpress X.java file in the output.
     */
    private boolean hideFile = false;

    /**
     * Client-app spcific metadata associated with this user-created class.
     */
    public Object metadata;
    
    /**
     * String that will be put directly inside the generated code.
     * Can be null.
     */
    private String directBlock;

    /**
     * JClass constructor
     *
     * @param mods
     *        Modifiers for this class declaration
     *
     * @param name
     *        Name of this class
     *
     * @param isInterface
     *        <tt>true</tt> if, and only if, this is to be an interface
     */
    JDefinedClass(int mods, String name, boolean isInterface, JCodeModel owner ) {
        super(owner);
        
        if( JCodeModel.isCaseSensitiveFileSystem )
            upperCaseClassMap = null;
        else
            upperCaseClassMap = new HashMap();            
        
        if (name.trim().length() == 0)
                throw new IllegalArgumentException("JClass name empty");

        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            String msg = "JClass name " + name + " contains illegal character"
                + " for beginning of identifier: " + name.charAt(0);
            throw new IllegalArgumentException(msg);
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                String msg ="JClass name " + name
                    + " contains illegal character " + name.charAt(i);
                throw new IllegalArgumentException(msg);
            }
        }

        this.mods = (isInterface
                     ? JMods.forInterface(mods)
                     : JMods.forClass(mods));
        this.name = name;
        this.isInterface = isInterface;

        // by default the super class is java.lang.Object
        if(!isInterface)
            this.superClass = owner().ref(Object.class);
    }

    /**
     * This class extends the specifed class.
     *
     * @param superClass
     *        Superclass for this class
     *
     * @return This class
     */
    public JDefinedClass _extends(JClass superClass) {
        if(isInterface())
            throw new IllegalArgumentException(
                "unable to set the super class for an interface");
        if(superClass==null)
            throw new NullPointerException();
        
        this.superClass = superClass;
        return this;
    }
    
    public JDefinedClass _extends(Class superClass) {
        return _extends(owner().ref(superClass));
    }

    /**
     * Returns the class extended by this class,
     * or <tt>null</tt> if this class does not explicitly extend a class.
     */
    public JClass _extends() {
        return superClass;
    }

    /**
     * This class implements the specifed interface.
     *
     * @param iface
     *        Interface that this class implements
     *
     * @return This class
     */
    public JDefinedClass _implements(JClass iface) {
        interfaces.add(iface);
        return this;
    }
    
    public JDefinedClass _implements(Class iface) {
        return _implements(owner().ref(iface));
    }

    /**
     * Returns an iterator that walks the nested classes defined in this
     * class.
     */
    public Iterator _implements() {
        return interfaces.iterator();
    }

    /**
     * JClass name accessor.
     * 
     * <p>
     * For example, for <code>java.util.List</code>, this method
     * returns <code>"List"</code>"
     *
     * @return Name of this class
     */
    public String name() {
        return name;
    }

    public boolean isInterface() {
        return isInterface;
    }

    /**
     * Adds a field to the list of field members of this JDefinedClass.
     *
     * @param mods
     *        Modifiers for this field
     *
     * @param type
     *        JType of this field
     *
     * @param name
     *        Name of this field
     *
     * @return Newly generated field
     */
    public JFieldVar field(int mods, JType type, String name) {
        return field(mods,type,name,null);
    }
    
    public JFieldVar field(int mods, Class type, String name) {
        return field( mods, owner().ref(type), name );
    }

    /**
     * Adds a field to the list of field members of this JDefinedClass.
     *
     * @param mods
     *        Modifiers for this field.
     * @param type
     *        JType of this field.
     * @param name
     *        Name of this field.
     * @param init
     *        Initial value of this field.
     *
     * @return Newly generated field
     */
    public JFieldVar field(int mods, JType type, String name, JExpression init) {
        JFieldVar f = new JFieldVar(JMods.forField(mods), type, name, init);
        fields.add(f);
        
        JFieldVar existing = (JFieldVar)fieldsByName.get(name);
        if( existing!=null )
            fields.remove(existing);
        fieldsByName.put(name,f);
        
        return f;
    }

    
    public JFieldVar field(int mods, Class type, String name, JExpression init) {
        return field( mods, owner().ref(type), name, init );
    }

    /**
     * Returns an iterator that walks the fields defined in this class.
     */
    public Iterator fields() {
        return fields.iterator();
    }

    /**
     * Creates, if necessary, and returns the static initializer
     * for this class.
     *
     * @return JBlock containing initialization statements for this class
     */
    public JBlock init() {
        if (init == null) init = new JBlock();
        return init;
    }

    /**
     * Adds a constructor to this class.
     *
     * @param mods
     *        Modifiers for this constructor
     */
    public JMethod constructor(int mods) {
        JMethod c = new JMethod(mods, this);
        constructors.add(c);
        return c;
    }

    /**
     * Returns an iterator that walks the constructors defined in this class.
     */
    public Iterator constructors() {
        return constructors.iterator();
    }
    
    /**
     * Looks for a method that has the specified method signature
     * and return it.
     * 
     * @return
     *      null if not found.
     */
    public JMethod getConstructor(JType[] argTypes) {
        for (Iterator itr = constructors.iterator(); itr.hasNext();) {
            JMethod m = (JMethod)itr.next();
            
            if( m.hasSignature(argTypes) )
                return m;
        }
        return null;
    }

    /**
     * Add a method to the list of method members of this JDefinedClass instance.
     *
     * @param mods
     *        Modifiers for this method
     *
     * @param type
     *        Return type for this method
     *
     * @param name
     *        Name of the method
     *
     * @return Newly generated JMethod
     */
    public JMethod method(int mods, JType type, String name) {
        // XXX problems caught in M constructor
        JMethod m = new JMethod(this, mods, type, name);
        methods.add(m);
        return m;
    }
    
    public JMethod method(int mods, Class type, String name ) {
        return method( mods, owner().ref(type), name );
    }

    /**
     * Returns an iterator that walks the methods defined in this class.
     */
    public Iterator methods() {
        return methods.iterator();
    }
    
    /**
     * Looks for a method that has the specified method signature
     * and return it.
     * 
     * @return
     *      null if not found.
     */
    public JMethod getMethod(String name, JType[] argTypes) {
        outer:
        for (Iterator itr = methods.iterator(); itr.hasNext();) {
            JMethod m = (JMethod)itr.next();
            if( !m.name().equals(name) )
                continue;
                
            if( m.hasSignature(argTypes) )
                return m;
        }
        return null;
    }

    /**
     * Add a new nested class to this class.
     *
     * @param mods
     *        Modifiers for this class declaration
     *
     * @param name
     *        Name of class to be added to this package
     *
     * @return Newly generated class
     */
    public JDefinedClass _class(int mods, String name) throws JClassAlreadyExistsException {
        return _class(mods,name,false);
    }

    public JDefinedClass _class( int mods, String name, boolean isInterface )
        throws JClassAlreadyExistsException {
            
        if(classes.containsKey(name))
            throw new JClassAlreadyExistsException((JDefinedClass)classes.get(name));
        else {
            // XXX problems caught in the NC constructor
            JDefinedClass c = new JNestedClass(this, mods, name, isInterface );
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
     * Add a new public nested class to this class.
     */
    public JDefinedClass _class(String name) throws JClassAlreadyExistsException {
        return _class(JMod.PUBLIC,name);
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
        return _interface( JMod.PUBLIC, name );
    }
    
    /**
     * Creates, if necessary, and returns the class javadoc for this
     * JDefinedClass
     *
     * @return JDocComment containing javadocs for this class
     */
    public JDocComment javadoc() {
        if( jdoc == null ) 
                jdoc = new JDocComment();
        return jdoc;
    }

    /**
     * Mark this file as hidden, so that this file won't be
     * generated.
     * 
     * <p>
     * This feature could be used to generate code that refers
     * to class X, without actually generating X.java.
     */
    public void hide() {
        hideFile = true;
    }
    
    public boolean isHidden() {
        return hideFile;
    }

    
    /**
     * Returns an iterator that walks the nested classes defined in this
     * class.
     */
    public final Iterator classes() {
        return classes.values().iterator();
    }
    
    /**
     * Returns all the nested classes defined in this class.
     */
    public final JClass[] listClasses() {
        return (JClass[]) classes.values().toArray(new JClass[classes.values().size()]);        
    }

    /**
     * Returns the class in which this class is nested, or <tt>null</tt> if
     * this is a top-level class.
     */
    public JClass outer() {
        return null;
    }



    public void declare(JFormatter f) {
        if( jdoc != null )
            f.nl().g(jdoc);
        
        f.g(mods).p(isInterface ? "interface" : "class").p(name);
        
        if (superClass != null
        && superClass!=owner().ref(Object.class))
            f.nl().i().p("extends").g(superClass).nl().o();
        
        if (!interfaces.isEmpty()) {
            boolean first = true;
            if (superClass == null) f.nl();
            f.i().p(isInterface ? "extends" : "implements");
            for (Iterator i = interfaces.iterator(); i.hasNext();) {
                if (!first) f.p(',');
                    f.g((JClass)(i.next()));
                    first = false;
            }
            f.nl().o();
        }
        declareBody(f);
    }
    
    /**
     * prints the body of a class.
     */
    protected void declareBody(JFormatter f) {
        f.p('{').nl().nl().i();
        for (Iterator i = fields.iterator(); i.hasNext();) {
            f.d((JVar)(i.next()));
        }
        if (init != null)
            f.nl().p("static").s(init);
        for (Iterator i = constructors.iterator(); i.hasNext();) {
            f.nl().d((JMethod)(i.next()));
        }
        for (Iterator i = methods.iterator(); i.hasNext();) {
            f.nl().d((JMethod)(i.next()));
        }
        for (Iterator i = classes.values().iterator(); i.hasNext();) {
            f.nl().d((JDefinedClass)(i.next()));
        }
        if(directBlock!=null)
            f.p(directBlock);
        f.nl().o().p('}').nl();
    }

    public void generate(JFormatter f) {
        f.p(fullName());
    }

    /**
     * Places the given string directly inside the generated class.
     * 
     * This method can be used to add methods/fields that are not
     * generated by CodeModel.
     * This method should be used only as the last resort.
     */
    public void direct(String string) {
        if( directBlock==null )
            directBlock = string;
        else
            directBlock += string;
    }

}
