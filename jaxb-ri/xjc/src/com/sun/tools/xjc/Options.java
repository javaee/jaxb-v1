/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.sun.tools.xjc.reader.Util;

/**
 * Global options.
 * 
 * <p>
 * This class stores invocation configuration for XJC.
 * The configuration in this class shoule be abstract enough so that
 * it could be parsed from both command-line or Ant.
 */
public class Options
{
    /** If "-debug" is specified. */
    public boolean debugMode;
    
    /** If the "-verbose" option is specified. */
    public boolean verbose;
    
    /** If the "-quiet" option is specified. */
    public boolean quiet;
    
    /** If "-trace-unmarshaller" is specfied. */
    public boolean traceUnmarshaller;
    
    /** If the -readOnly option is specified */
    public boolean readOnly;
    
// those four features can be also set by the customization.
    /** If false, XJC will not generate code for the on-demand validation. */
    public boolean generateValidationCode = true;
    
    /** If false, XJC will not generate code for the marshalling. */
    public boolean generateMarshallingCode = true;
    
    /** If false, XJC will not generate code for the unmarshalling. */
    public boolean generateUnmarshallingCode = true;

    /**
     * If false, XJC will not generate code for the validating unmarshaller.
     * If this flag to be true, {@link #generateUnmarshallingCode} has
     * to be true.
     */
    public boolean generateValidatingUnmarshallingCode = true;
    
    /**
     * Check the source schemas with extra scrutiny.
     * The exact meaning depends on the schema language.
     */
    public boolean strictCheck =true;
    
    /**
     * strictly follow the compatibility rules and reject schemas that
     * contain features from App. E.2, use vendor binding extensions
     */
    public static final int STRICT = 1;
    /**
     * loosely follow the compatibility rules and allow the use of vendor
     * binding extensions
     */
    public static final int EXTENSION = 2;
    
    /**
     * this switch determines how carefully the compiler will follow
     * the compatibility rules in the spec. Either <code>STRICT</code>
     * or <code>EXTENSION</code>.
     */
    public int compatibilityMode = STRICT;

    /** Target direcoty when producing files. */
    public File targetDir = new File(".");
    
    /**
     * Actually stores {@link CatalogResolver}, but the field
     * type is made to {@link EntityResolver} so that XJC can be
     * used even if resolver.jar is not available in the classpath.
     */
    public EntityResolver entityResolver = null;
    
    // type of the source schema
    public static final int SCHEMA_DTD = 0;
    public static final int SCHEMA_XMLSCHEMA = 1;
    public static final int SCHEMA_RELAXNG = 2;
    public static final int SCHEMA_WSDL = 3;
    private static final int SCHEMA_AUTODETECT = -1;
    
    /**
     * Type of input schema language. One of the <code>SCHEMA_XXX</code>
     * constants.
     */
    private int schemaLanguage = SCHEMA_AUTODETECT;
    
    /**
     * The -p option that should control the default Java package that
     * will contain the generated code. Null if unspecified.
     */
    public String defaultPackage = null;
    
    /**
     * Input schema files as a list of {@link InputSource}s.
     */
    private final List grammars = new ArrayList();
    
    private final List bindFiles = new ArrayList();
    
    // Proxy setting.
    String proxyHost = null;
    String proxyPort = null;
    
    /**
     * Set to true to avoid generating the runtime.
     * This option is useful in conjunction with the runtimePackage
     * parameter to consolidate the runtime into one when the client
     * compiles a lot of schemas separately.
     */
    public boolean generateRuntime = true;

    /**
     * The package name of the generated runtime. Set the field
     * null to generate it into the default location.
     */
    public String runtimePackage = null;
    
        
    /** {@link ModelAugumentors} that are enabled. */
    public final List enabledModelAugmentors = new ArrayList();

    /** Enabled customization URIs. */ 
    public final Set enabledCustomizationURIs = new HashSet();
    
    
    /**
     * External code augmenter add-ons.
     */
    public static final Object[] codeAugmenters = findServices(CodeAugmenter.class.getName());
    
    
    
    
    
    public int getSchemaLanguage() {
        if( schemaLanguage==SCHEMA_AUTODETECT)
            schemaLanguage = guessSchemaLanguage();
        return schemaLanguage;
    }
    public void setSchemaLanguage(int _schemaLanguage) {
        this.schemaLanguage = _schemaLanguage;
    }
    
    /** Input schema files. */
    public InputSource[] getGrammars() {
        return (InputSource[]) grammars.toArray(new InputSource[grammars.size()]);
    }
    
    /**
     * Adds a new input schema.
     */
    public void addGrammar( InputSource is ) {
        grammars.add(absolutize(is));
    }
    
    
    private InputSource absolutize(InputSource is) {
        // absolutize all the system IDs in the input,
        // so that we can map system IDs to DOM trees.
        try {
            URL baseURL = new File(".").getCanonicalFile().toURL(); 
            is.setSystemId( new URL(baseURL,is.getSystemId()).toExternalForm() );
        } catch( IOException e ) {
            ; // ignore
        }
        return is;
    }

    
    /** Input external binding files. */
    public InputSource[] getBindFiles() {
        return (InputSource[]) bindFiles.toArray(new InputSource[bindFiles.size()]);
    }

    /**
     * Adds a new input schema.
     */
    public void addBindFile( InputSource is ) {
        bindFiles.add(absolutize(is));
    }
    
    public final List classpaths = new ArrayList();
    /**
     * Gets a classLoader that can load classes specified via the
     * -classpath option.
     */
    public URLClassLoader getUserClassLoader( ClassLoader parent ) {
        return new URLClassLoader(
            (URL[])classpaths.toArray(new URL[classpaths.size()]),parent);
    }

    
    /**
     * Parses an option <code>args[i]</code> and return
     * the number of tokens consumed.
     * 
     * @return
     *      0 if the argument is not understood. Returning 0
     *      will let the caller report an error.
     * @exception BadCommandLineException
     *      If the callee wants to provide a custom message for an error.
     */
    protected int parseArgument( String[] args, int i ) throws BadCommandLineException, IOException {
        if (args[i].equals("-classpath") || args[i].equals("-cp")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_CLASSPATH));
            classpaths.add(new File(args[++i]).toURL());
            return 2;
        }
        if (args[i].equals("-d")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_DIR));
            targetDir = new File(args[++i]);
            if( !targetDir.exists() )
                throw new BadCommandLineException(
                    Messages.format(Messages.NON_EXISTENT_DIR,targetDir));
            return 2;
        }
        if (args[i].equals("-readOnly")) {
            readOnly = true;
            return 1;
        }
        if (args[i].equals("-p")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PACKAGENAME));
            defaultPackage = args[++i];
            return 2;
        }
        if (args[i].equals("-debug")) {
            debugMode = true;
            // try to set the verbose flag of catalog resolver
            try {
                Debug.setDebug(10);
            } catch(Throwable _) {
                ;   // ignore, in case catalog resolver isn't in the classpath
            }
            return 1;
        }
        if (args[i].equals("-trace-unmarshaller")) {
            traceUnmarshaller = true;
            return 1;
        }
        if (args[i].equals("-nv")) {
            strictCheck = false;
            return 1;
        }
        if (args[i].equals("-use-runtime")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_RUNTIME_PACKAGENAME));
            generateRuntime = false;
            runtimePackage = args[++i];
            return 2;
        }
        if (args[i].equals("-verbose")) {
            verbose = true;
            return 1;
        }
        if (args[i].equals("-quiet")) {
            quiet = true;
            return 1;
        }
        if (args[i].equals("-b")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_FILENAME));
            if (args[i + 1].startsWith("-")) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_FILENAME));
            }
            addBindFile(Util.getInputSource(args[++i]));
            return 2;
        }
        if (args[i].equals("-dtd")) {
            schemaLanguage = SCHEMA_DTD;
            return 1;
        }
        if (args[i].equals("-relaxng")) {
            schemaLanguage = SCHEMA_RELAXNG;
            return 1;
        }
        if (args[i].equals("-xmlschema")) {
            schemaLanguage = SCHEMA_XMLSCHEMA;
            return 1;
        }
        if (args[i].equals("-wsdl")) {
            schemaLanguage = SCHEMA_WSDL;
            return 1;
        }
        if (args[i].equals("-extension")) {
            compatibilityMode = Options.EXTENSION;
            return 1;
        }
        if (args[i].equals("-host")) {
            if (i == args.length - 1) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYHOST));
            }
            if (args[i + 1].startsWith("-")) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYHOST));
            }
          proxyHost = args[++i];
          return 2;
        }
        if (args[i].equals("-port")) {
            if (i == args.length - 1) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYPORT));
            }
            if (args[i + 1].startsWith("-")) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYPORT));
            }
            proxyPort = args[++i];
            return 2;
        }
        if( args[i].equals("-catalog") ) {
            // use Sun's "XML Entity and URI Resolvers" by Norman Walsh
            // to resolve external entities.
            // http://www.sun.com/xml/developers/resolver/
            if (i == args.length - 1)
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_CATALOG));
            
            addCatalog(new File(args[++i]));
            return 2;
        }
        if( args[i].equals("-source")) {
            // silently ignore the -source option.
            // this is used by the XJC 2.0 to activate the 1.0 compiler. 
            if (i == args.length - 1)
                return 1;
            else
                return 2;
        }
// Leonid has a concern with these options regarding compatibilities.
// people should use customization to control those four switches.
//        if( args[i].equals("-no-validation-code") ) {
//            generateValidationCode = false;
//            return 1;
//        }
//        if( args[i].equals("-no-marshalling-code") ) {
//            generateMarshallingCode = false;
//            return 1;
//        }
//        if( args[i].equals("-no-unmarshalling-code") ) {
//            generateUnmarshallingCode = false;
//            generateValidatingUnmarshallingCode = false;
//            return 1;
//        }
//        if( args[i].equals("-no-validating-unmarshaller-code") ) {
//            generateValidatingUnmarshallingCode = false;
//            return 1;
//        }
        
        // see if this is one of the extensions
        for( int j=0; j<codeAugmenters.length; j++ ) {
            CodeAugmenter ma = (CodeAugmenter)codeAugmenters[j];
            if( ("-"+ma.getOptionName()).equals(args[i]) ) {
                enabledModelAugmentors.add(ma);
                if (ma instanceof CodeAugmenterEx) {
                  enabledCustomizationURIs.addAll(((CodeAugmenterEx) ma).getCustomizationURIs());
                }
                return 1;
            }
                    
            int r = ma.parseArgument(this,args,i);
            if(r!=0)    return r;
        }
        
        return 0;   // unrecognized
    }
    
    /**
     * Adds a new catalog file.
     */
    public void addCatalog(File catalogFile) throws IOException {
        if(entityResolver==null) {
            CatalogManager.ignoreMissingProperties(true);
            entityResolver = new CatalogResolver(true);
        }
        ((CatalogResolver)entityResolver).getCatalog().parseCatalog(catalogFile.getPath());
    }
    
    /**
     * Parses arguments and fill fields of this object.
     * 
     * @exception BadCommandLineException
     *      thrown when there's a problem in the command-line arguments
     */
    public void parseArguments( String[] args ) throws BadCommandLineException, IOException {

        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                int j = parseArgument(args,i);
                if(j==0)
                    throw new BadCommandLineException(
                        Messages.format(Messages.UNRECOGNIZED_PARAMETER, args[i]));
                i += (j-1);
            } else
                addGrammar(Util.getInputSource(args[i]));
        }
        
        // configure proxy
        if (proxyHost != null || proxyPort != null) {
            if (proxyHost != null && proxyPort != null) {
                System.setProperty("http.proxyHost", proxyHost);
                System.setProperty("http.proxyPort", proxyPort);
                System.setProperty("https.proxyHost", proxyHost);
                System.setProperty("https.proxyPort", proxyPort);
            } else if (proxyHost == null) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYHOST));
            } else {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYPORT));
            }
        }

        if (grammars.size() == 0)
            throw new BadCommandLineException(
                Messages.format(Messages.MISSING_GRAMMAR));
        
        if( schemaLanguage==SCHEMA_AUTODETECT )
            schemaLanguage = guessSchemaLanguage();
    }
    
    
    /**
     * Guesses the schema language.
     */
    public int guessSchemaLanguage() {
        if (grammars.size() > 1)
            return SCHEMA_XMLSCHEMA;

        // otherwise, use the file extension.
        // not a good solution, but very easy.
        String name = ((InputSource)grammars.get(0)).getSystemId().toLowerCase();

        if (name.endsWith(".rng"))
            return SCHEMA_RELAXNG;
        if (name.endsWith(".dtd"))
            return SCHEMA_DTD;
        if (name.endsWith(".wsdl"))
            return SCHEMA_WSDL;

        // by default, assume XML Schema
        return SCHEMA_XMLSCHEMA;
    }

    
    
    
    
    private static Object[] findServices( String className ) {
        return findServices( className, Driver.class.getClassLoader() );
    }
    
    /**
     * Looks for all "META-INF/services/[className]" files and
     * create one instance for each class name found inside this file.
     */
    private static Object[] findServices( String className, ClassLoader classLoader ) {
        
        // if true, print debug output
        final boolean debug = com.sun.tools.xjc.util.Util.getSystemProperty(Options.class,"findServices")!=null;
        
        String serviceId = "META-INF/services/" + className;

        if(debug) {
            System.out.println("Looking for "+serviceId+" for add-ons");
        }
        
        // try to find services in CLASSPATH
        try {
            Enumeration e = classLoader.getResources(serviceId);
            if(e==null) return new Object[0];
    
            ArrayList a = new ArrayList();            
            while(e.hasMoreElements()) {
                URL url = (URL)e.nextElement();
                BufferedReader reader=null;
                
                if(debug) {
                    System.out.println("Checking "+url+" for an add-on");
                }
                
                try {
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    String impl;
                    while((impl = reader.readLine())!=null ) {
                        // try to instanciate the object
                        impl = impl.trim();
                        if(debug) {
                            System.out.println("Attempting to instanciate "+impl);
                        }
                        Class implClass = classLoader.loadClass(impl);
                        a.add(implClass.newInstance());
                    }
                    reader.close();
                } catch( Exception ex ) {
                    // let it go.
                    if(debug) {
                        ex.printStackTrace(System.out);
                    }
                    if( reader!=null ) {
                        try {
                            reader.close();
                        } catch( IOException ex2 ) {
                        }
                    }
                }
            }
            
            return a.toArray();
        } catch( Throwable e ) {
            // ignore any error
            if(debug) {
                e.printStackTrace(System.out);
            }
            return new Object[0];
        }
    }

}
