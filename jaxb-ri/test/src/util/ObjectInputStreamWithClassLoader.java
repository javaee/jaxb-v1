/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package util;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * {@link ObjectInputStream} with an ability to load classes
 * from arbitrary class loader.
 */
public final class ObjectInputStreamWithClassLoader extends ObjectInputStream {
    private final ClassLoader classLoader;
    
    public ObjectInputStreamWithClassLoader(InputStream in, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
    }
    
    protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        return classLoader.loadClass(desc.getName());
    }
}