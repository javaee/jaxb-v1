/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package batch.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.sun.xml.bind.JAXBAssertionError;
import com.werken.forehead.Forehead;
import com.werken.forehead.ForeheadClassLoader;

/**
 * Misc utilitiy methods.
 */
public class Util
{
    
    /** Recursively deletes all the files/folders inside a folder. */
    public static void recursiveDelete( File f ) {
        if(f.isDirectory()) {
            String[] files = f.list();
            for( int i=0; i<files.length; i++ )
                recursiveDelete( new File(f,files[i]) );
        } else {
            f.delete();
        }
    }
    
    /**
     * Reads an input stream and copies them into System.out.
     */
    private static class ProcessReader implements Runnable {
        ProcessReader( InputStream is ) {
            reader = new BufferedReader(new InputStreamReader(is));
        }
        
        private final BufferedReader reader;
        
        public void run() {
            try {
                while(true) {
                    String s = reader.readLine();
                    if(s==null) {
                        reader.close();
                        return;
                    }
                    System.out.println(s);
                }
            } catch( Exception e ) {
                e.printStackTrace();
                throw new Error();
            }
        }
    }
    
    
    
    /**
     * Alias name to the full name. Used to assign unique names to
     * Forehead class loaders.
     */
    private static final Properties foreheadAlias = new Properties();
    
    /**
     * If Forehead is in use, this will be non-null, otherwise null.
     */
    private static final Forehead forehead = initForehead();
    
    private static Forehead initForehead() {
        Forehead f = Forehead.getInstance();
        if( f.getClassLoader("root")==null ) {
            System.err.println("Forehead is not in use. Run in a single ClassLoader");
            return null;
        } else {
            System.err.println("Forehead is in use.");
            InputStream alias = Util.class.getResourceAsStream("/forehead.alias");
            if(alias!=null) {
                System.err.println("loading Forehead alias table");
                try {
                    foreheadAlias.load(alias);
                } catch (IOException e) {
                    throw new JAXBAssertionError(e);
                }
            } else
                System.err.println("no Forehead alias table");
            return f;
        }
    }
    
    /**
     * Locates the specified class loader from Forehead.
     * If Forehead is not used when launching the program,
     * uses the default ClassLoader.
     * 
     * @param name
     *      The name of the class loader as defined in Forehead.
     */
    public static ClassLoader getForeheadClassLoader( String name ) {
        if( forehead!=null) {
            // look up alias
            if(foreheadAlias.containsKey(name))
                name = (String)foreheadAlias.get(name);
            
            ClassLoader cl = forehead.getClassLoader(name);
            if(cl==null)
                throw new JAXBAssertionError("No such class loader in Forehead:" +name);
            return cl;
        } else {
            return Util.class.getClassLoader();
        }
    }
    
    
    
    /**
     * Javac's compile method.
     */
    private static final Method javac = initJavac();
    
    private static Method initJavac() {
        ClassLoader foreheadLoader = getForeheadClassLoader("xjc");
        try {
            // try to load javac from the current class loader.
            return foreheadLoader
                .loadClass("com.sun.tools.javac.Main")
                .getMethod("compile",new Class[]{String[].class});
        } catch( Throwable e ) {
            ;
        }
        
        try {
            // if it fails, try to locate javac from java.home
            File jreHome = new File(System.getProperty("java.home"));
            File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );
            
            ClassLoader loader = new URLClassLoader(
                    new URL[]{ toolsJar.toURL() }, foreheadLoader );
            
            Method m = loader
                .loadClass("com.sun.tools.javac.Main")
                .getMethod("compile",new Class[]{String[].class});
            System.out.println("Using javac from "+toolsJar);
            return m;
        } catch( Throwable e ) {
            e.printStackTrace();
            throw new JAXBAssertionError("Unable to find javac in the same VM. Have you set JAVA_HOME?");
        }

    }
    
    /**
     * Compiles source files in the specified directory
     * and returns a ClassLoader that can be used to load classes from there.
     * 
     * @return
     *      if the compilation fails, return null.
     */
    public static ClassLoader compile( ClassLoader parent, File dir ) throws IOException {
        ArrayList args = new ArrayList();
        args.add("-d");
        args.add(dir.getPath());
        args.add("-g");
        Util.appendJavaFiles(dir,args);
        
        // javac doesn't load classes from a ClassLoader.
        // instead, we have to list up the locations and specify them
        // via the -classpath parameter
        {
            String path="";
            ClassLoader cl = getForeheadClassLoader("xjc");
            while(cl instanceof ForeheadClassLoader) {
                URLClassLoader ucl = (ForeheadClassLoader)cl;
                URL[] urls = ucl.getURLs();
                for(int i=0; i<urls.length; i++ ) {
                    if(path!="")    path += File.pathSeparatorChar;
                    path += urlToFileName(urls[i]);
                }
                cl = cl.getParent();
            }
            if(path!="") {
                args.add("-classpath");
                args.add(path);
            }
        }
        
        try {
            javac.invoke(null,new Object[]{args.toArray(new String[0])});
        } catch( InvocationTargetException e ) {
            // javac is expected to return gracefully.
            throw new JAXBAssertionError(e.getTargetException());
        } catch( IllegalAccessException e ) {
            throw new JAXBAssertionError(e); // REVISIT: is this handling correct?
        }
            
        return createClassLoader(parent,dir);
    }
    
    /**
     * Waits for the given process to complete, and return its exit code.
     */
    public static int execProcess( Process proc ) throws IOException, InterruptedException {
        // is this a correct handling?
        proc.getOutputStream().close();
        new Thread(new ProcessReader(proc.getInputStream())).start();
        new Thread(new ProcessReader(proc.getErrorStream())).start();
            
        return proc.waitFor();
    }
    
    /**
     * Returns a class loader that loads classes from the specified directory.
     */
    public static ClassLoader createClassLoader( ClassLoader parent, File dir ) throws IOException {
        return URLClassLoader.newInstance(
            new URL[]{dir.getCanonicalFile().toURL()},parent);
    }
    
    /**
     * Collects Java files recursively from the specified directory
     */
    public static void appendJavaFiles( File dir, ArrayList buf ) {
        String[] files = dir.list();
        for( int i=0; i<files.length; i++ ) {
            File f = new File(dir,files[i]);
        
            if( files[i].endsWith(".java") ) {
                buf.add(f.getPath());
            }
            if( f.isDirectory() )
                appendJavaFiles(f,buf);
        }
    }
    
        
    /** Parses an XML file into a dom4j tree. */
    public static Document loadXML( File file ) throws Exception {
        // for this to work in Microsoft VM, this much is necessary...
        return new SAXReader(new org.apache.xerces.parsers.SAXParser())
            .read(file);
    }
    
    
    private static String urlToFileName(URL url) {
        String u = url.toExternalForm();
        if (null != u) {
            if (u.startsWith("file:////")) {
                u = u.substring(7);
            } else if (u.startsWith("file:///")) {
                u = u.substring(6);
            } else if (u.startsWith("file://")) {
                u = u.substring(5); // absolute?
            } else if (u.startsWith("file:/")) {
                u = u.substring(5);
            } else if (u.startsWith("file:")) {
                u = u.substring(4);
            }
        }
        return u;
    }

    
}
