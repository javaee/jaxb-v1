/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package batch.qa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.sun.xml.bind.JAXBAssertionError;

import util.ObjectInputStreamWithClassLoader;
import batch.core.JDomUtil;

/**
 * This object will be exposed to scripts so that they
 * can call helper methods.
 */
public class ScriptHelper {
    /**
     * Returns true if the given object has a method of a specified name.
     */
    public boolean hasMethod( Object o, String methodName ) {
        Method[] methods = o.getClass().getMethods();
        for( int i=0; i<methods.length; i++ ) {
            if(methods[i].getName().equals(methodName))
                return true;
        }
        return false;
    }
    
    /**
     * Compares the equality of two Java content trees
     * by marshalling.
     */
    public boolean compareTwoTrees( JAXBContext context, Object o1, Object o2 ) throws Exception {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        
        Marshaller m = context.createMarshaller();
        
        m.marshal(o1,baos1);
        m.marshal(o2,baos2);
        
        return JDomUtil.compare(
            JDomUtil.load(new ByteArrayInputStream(baos1.toByteArray())),
            JDomUtil.load(new ByteArrayInputStream(baos2.toByteArray())) );
    }
    
    /**
     * Serializes a given object and then de-serialize it
     * and then return it.
     */
    public Object roundtripSerialize( Object o, final ClassLoader classLoader ) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        
        ObjectInputStream ois = new ObjectInputStreamWithClassLoader(
            new ByteArrayInputStream(baos.toByteArray()), classLoader);
        o = ois.readObject();
        ois.close();
        
        return o;
    }
    
    /**
     * Called when the assertion fails in the test script.
     * This is a good place to set a break point.
     */
    public void fail(String msg) throws Exception {
        throw new JAXBAssertionError(msg);
    }
}