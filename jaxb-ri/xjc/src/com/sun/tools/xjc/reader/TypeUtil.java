/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Type-related utility methods.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class TypeUtil {
    
    
    /**
     * Computes the common base type of two types.
     * 
     * @param types
     *      set of {@link JType} objects.
     */
    public static JType getCommonBaseType( JCodeModel codeModel, Set types ) {
        return getCommonBaseType( codeModel, (JType[])types.toArray(new JType[types.size()]) );
    }
    
    /**
     * Computes the common base type of types.
     * 
     * TODO: this is a very interesting problem. Since one type has possibly
     * multiple base types, it's not an easy problem.
     * The current implementation is very naive.
     * 
     * To make the result deterministic across differente JVMs, we have to
     * use a Set whose ordering is deterministic.
     */
    public static JType getCommonBaseType(JCodeModel codeModel, JType[] t) {
        // first, eliminate duplicates.
        Set uniqueTypes = new TreeSet(typeComparator);
        for (int i = 0; i < t.length; i++)
            uniqueTypes.add(t[i]);

        // if this yields only one type. return now.
        // this is the only case where we can return a primitive type
        // from this method
        if (uniqueTypes.size() == 1)
            return (JType)uniqueTypes.iterator().next();
        if (uniqueTypes.size() == 0)
            // assertion failed. nullType can be used only under a very special circumstance
            throw new JAXBAssertionError();

        // box all the types and compute the intersection of all types
        Set s = null;
        for (Iterator itr = uniqueTypes.iterator(); itr.hasNext();) {
            JType type = (JType)itr.next();
            if (type == codeModel.NULL)
                // the null type doesn't need to be taken into account.
                continue;

            JClass cls = box(codeModel, type);

            if (s == null)
                s = getAssignableTypes(cls);
            else
                s.retainAll(getAssignableTypes(cls));
        }

        // refine 's' by removing "lower" types.
        // for example, if we have both java.lang.Object and
        // java.io.InputStream, then we don't want to use java.lang.Object.

        JClass[] raw = (JClass[])s.toArray(new JClass[s.size()]);

        s.clear();
        for (int i = 0; i < raw.length; i++) { // for each raw[i]
            int j;
            for (j = 0; j < raw.length; j++) { // see if raw[j] "includes" raw[i]
                if (i == j)
                    continue;

                if (raw[i].isAssignableFrom(raw[j]))
                    break; // raw[j] is derived from raw[i], hence j includes i.
            }

            if (j == raw.length)
                // no other type inclueds raw[i]. remember this value.
                s.add(raw[i]);
        }

        // assert(s.size()!=0) since at least java.lang.Object has to be there

        // we may have more than one candidates at this point.
        // any user-defined generated types should have
        // precedence over system-defined existing types.
        //
        // so try to return such a type if any.
        Iterator itr = s.iterator();
        while (itr.hasNext()) {
            JClass c = (JClass)itr.next();
            if (c instanceof JDefinedClass)
                return c;
        }

        // we can do more if we like. for example,
        // we can avoid types in the RI runtime.
        // but for now, just return the first one.
        return (JClass)s.iterator().next();
    }
    
    /**
     * Returns the set of all classes/interfaces that a given type
     * implements/extends, including itself.
     * 
     * For example, if you pass java.io.FilterInputStream, then the returned
     * set will contain java.lang.Object, java.lang.InputStream, and
     * java.lang.FilterInputStream.
     */
    public static Set getAssignableTypes( JClass t ) {
        Set s = new TreeSet(typeComparator);
        
        // any JClass can be casted to Object.
        s.add( t.owner().ref(Object.class));
        
        _getAssignableTypes(t,s);
        return s;
    }
    
    private static void _getAssignableTypes( JClass t, Set s ) {
        if(!s.add(t))   return;
        
        // if this type is added first time,
        // recursively process the super class.
        JClass _super = t._extends();
        if(_super!=null)
            _getAssignableTypes(_super,s);
        
        // recursively process all implemented interfaces
        Iterator itr = t._implements();
        while(itr.hasNext())
            _getAssignableTypes((JClass)itr.next(),s);
    }
    
    /**
     * If the given type can be assigned to java.lang.Object,
     * return itself. Otherwise return the corresponding wrapper
     * class for Java primitive type.
     */
    private static JClass box( JCodeModel codeModel, JType t ) {
        if( t instanceof JClass )
            return (JClass)t;
        else
            return ((JPrimitiveType)t).getWrapperClass();
    }

    /**
     * Obtains a {@link JType} object for the string representation
     * of a type.
     * 
     * Reports an error if the type is not found. In that case,
     * a reference to {@link Object} will be returned.
     */
    public static JType getType( JCodeModel codeModel,
        String typeName, ErrorHandler errorHandler, Locator errorSource ) throws SAXException {
        
        try {
            // first, try as a primitive type
            return JType.parse(codeModel,typeName); 
        } catch( IllegalArgumentException e ) {
            try {
                // then try it as a class
                return codeModel.ref(typeName);
            } catch( ClassNotFoundException ee ) {
                
                errorHandler.error( new SAXParseException(
                    Messages.format(Messages.ERR_CLASS_NOT_FOUND,typeName)
                    ,errorSource));
                    
                // recover by assuming some class.
                return codeModel.ref(Object.class);
            }
        }
    }
    
    /**
     * Compares {@link JType} objects by their names.
     */
    private static final Comparator typeComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            JType t1 = (JType) o1;
            JType t2 = (JType) o2;
            
            return t1.fullName().compareTo(t2.fullName());
        }
    };
}
