/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.xml.sax.SAXException;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.ProgressCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.SkeletonGenerator;
import com.sun.tools.xjc.generator.marshaller.MarshallerGenerator;
import com.sun.tools.xjc.generator.unmarshaller.UnmarshallerGenerator;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.generator.unmarshaller.automaton.AutomatonToGraphViz;
import com.sun.tools.xjc.generator.validator.ValidatorGenerator;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.util.AnnotationRemover;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;
import com.sun.tools.xjc.reader.relaxng.RELAXNGInternalizationLogic;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.tools.xjc.util.NullStream;
import com.sun.tools.xjc.util.Util;
import com.sun.tools.xjc.writer.SignatureWriter;
import com.sun.tools.xjc.writer.Writer;
import com.sun.xml.bind.JAXBAssertionError;


/**
 * CUI of XJC.
 */
public class Driver {
    
    public static void main(final String[] args) throws Exception {
        if( Util.getSystemProperty(Driver.class,"noThreadSwap")!=null )
            _main(args);    // for the ease of debugging
        
        // run all the work in another thread so that the -Xss option
        // will take effect when compiling a large schema. See
        // http://developer.java.sun.com/developer/bugParade/bugs/4362291.html
        final Throwable[] ex = new Throwable[1];
        
        Thread th = new Thread() {
            public void run() {
                try {
                    _main(args);
                } catch( Throwable e ) {
                    ex[0]=e;
                }
            }
        };
        th.start();
        th.join();
        
        if(ex[0]!=null) {
            // re-throw
            if( ex[0] instanceof Exception )
                throw (Exception)ex[0];
            else
                throw (Error)ex[0];
        }
    }
    
    private static void _main( final String[] args ) throws Exception {
        try {
            System.exit(Driver.run( args, System.err, System.out ));
        } catch (BadCommandLineException e) {
            // there was an error in the command line.
            // print usage and abort.
            System.out.println(e.getMessage());
            System.out.println();

            usage( false );
            System.exit(-1);
        }
    }

        



    /**
     * Performs schema compilation and prints the status/error into the
     * specified PrintStream.
     * 
     * <p>
     * This method could be used to trigger XJC from other tools,
     * such as Ant or IDE.
     * 
     * @param    args
     *        specified command line parameters. If there is an error
     *        in the parameters, {@link BadCommandLineException} will
     *        be thrown.
     * @param    status
     *      Status report of the compilation will be sent to this object.
     *        Useful to update users so that they will know something is happening.
     *      Only ignorable messages should be sent to this stream.
     *      
     *      This parameter can be null to suppress messages.
     * 
     * @param    out
     *        Various non-ignorable output (error messages, etc)
     *      will go to this stream.
     * 
     * @return
     *      If the compiler runs successfully, this method returns 0.
     *      All non-zero values indicate an error. The error message
     *      will be sent to the specified PrintStream.
     */
    public static int run(String[] args, PrintStream status, PrintStream out)
        throws Exception {
        
        if(status==null)
            status = new PrintStream(new NullStream());
        
        // recognize those special options before we start parsing options.
        for( int i=0; i<args.length; i++ ) {
             if( args[i].equals( "-help" ) ) {
                 usage( false );
                 return -1;
             }
             if( args[i].equals( "-version" ) ) {
                 status.println(Messages.format(Messages.VERSION));
                 return -1;
             }
             if (args[i].equals( "-private" ) ) {
                 usage( true );
                 return -1;
             }
         }
        
        OptionsEx opt = new OptionsEx();
        opt.setSchemaLanguage(Options.SCHEMA_XMLSCHEMA);  // disable auto-guessing
        opt.parseArguments(args);

        
        // set up the context class loader so that the user-specified classes
        // can be loaded from there
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
            opt.getUserClassLoader(contextClassLoader));

        // parse a grammar file
        //-----------------------------------------
        try {
            if( !opt.quiet ) {
                status.println(Messages.format(Messages.PARSING_SCHEMA));
            }
    
            ErrorReceiver receiver = new ConsoleErrorReporter(out,!opt.debugMode,opt.quiet);
            
            if( opt.mode==MODE_FOREST ) {
                // dump DOM forest and quit
                GrammarLoader loader  = new GrammarLoader( opt, receiver );
                DOMForest forest = loader.buildDOMForest(
                    (opt.getSchemaLanguage()==Options.SCHEMA_RELAXNG)
                    ?(InternalizationLogic)new RELAXNGInternalizationLogic()
                    :(InternalizationLogic)new XMLSchemaInternalizationLogic()
                );
                forest.dump(System.out);
                return 0;
            }
            
            
            AnnotatedGrammar grammar;
            try {
                grammar = GrammarLoader.load( opt, receiver );
    
                if (grammar == null) {
                    out.println(Messages.format(Messages.PARSE_FAILED));
                    return -1;
                }
            } catch (SAXException e) {
                // other generic SAXException. Likely to be a bug of a program
                if (e.getException() != null)
                    e.getException().printStackTrace(out);
                throw e;
            }
            
            if( !opt.quiet ) {
                status.println(Messages.format(Messages.COMPILING_SCHEMA));
            }

            switch (opt.mode) {
                case MODE_BGM :
                    // dump BGM
                    //-----------------------------------------
                    Writer.writeToConsole(opt.noNS, false, grammar);
                    break;
    
                case MODE_SIGNATURE :
                    SignatureWriter.write(grammar, new OutputStreamWriter(out));
                    break;
    
                case MODE_SERIALIZE :
                    // serialize AGM
                    ObjectOutputStream stream = new ObjectOutputStream(out);
                    stream.writeObject(AnnotationRemover.remove(grammar));
                    stream.close();
                    break;
                
                case MODE_CODE :
                case MODE_DRYRUN :
                    {
                        // generate actual code
                        GeneratorContext context = generateCode(grammar, opt, receiver);
                        if (context==null) {
                            out.println(
                                Messages.format(Messages.FAILED_TO_GENERATE_CODE));
                            return -1;
                        }

                        if( opt.mode == MODE_DRYRUN )
                            break;  // enough
    
                        // then print them out
                        CodeWriter cw = createCodeWriter(opt.targetDir,opt.readOnly);
                        if( !opt.quiet ) {
                            cw = new ProgressCodeWriter(cw, status);
                        }
                        grammar.codeModel.build(cw);
                        
                        break;
                    }
                case MODE_AUTOMATA :
                    { // dump automata
    
                        GeneratorContext context =
                            SkeletonGenerator.generate(grammar, opt, receiver );
                        if(context==null)
                            return -1;
                        Automaton[] automata =
                            UnmarshallerGenerator.generate(grammar, context, opt);
    
                        for (int i = 0; i < automata.length; i++) {
    
                            AutomatonToGraphViz.convert(
                                automata[i],
                                new File(
                                    opt.targetDir,
                                    automata[i].getOwner().ref.name()
                                        + ".gif"));
                        }
    
                        break;
                    }
                default :
                    throw new JAXBAssertionError();
            }
    
            return 0;
        } catch( StackOverflowError e ) {
            if(opt.debugMode)
                // in the debug mode, propagate the error so that
                // the full stack trace will be dumped to the screen.
                throw e;
            else {
                // otherwise just print a suggested workaround and
                // quit without filling the user's screen
                out.println(Messages.format(Messages.STACK_OVERFLOW));
                return -1;
            }
        }
    }

    public static String getBuildID() {
        return Messages.format(Messages.BUILD_ID);
    }
    
    
    private static final int MODE_BGM = 0;
    private static final int MODE_SIGNATURE = 1;
    private static final int MODE_SERIALIZE = 2;
    private static final int MODE_CODE = 3;
    private static final int MODE_AUTOMATA = 4;
    private static final int MODE_FOREST = 5;
    private static final int MODE_DRYRUN = 6;
        
    
    /**
     * Command-line arguments processor.
     * 
     * <p>
     * This class contains options that only make sense
     * for the command line interface.
     */
    static class OptionsEx extends Options
    {
        /** Operation mode. */
        protected int mode = MODE_CODE;
        
        /** A switch that determines the behavior in the BGM mode. */
        public boolean noNS = false;
        
        /** Parse XJC-specific options. */
        protected int parseArgument(String[] args, int i) throws BadCommandLineException, IOException {
            if (args[i].equals("-noNS")) {
                noNS = true;
                return 1;
            }
            if (args[i].equals("-mode")) {
                i++;
                if (i == args.length)
                    throw new BadCommandLineException(
                        Messages.format(Messages.MISSING_MODE_OPERAND));

                if (args[i].equals("bgm")) {
                    mode = MODE_BGM;
                    return 2;
                }
                if (args[i].equals("serial")) {
                    mode = MODE_SERIALIZE;
                    return 2;
                }
                if (args[i].equals("code")) {
                    mode = MODE_CODE;
                    return 2;
                }
                if (args[i].equals("sig")) {
                    mode = MODE_SIGNATURE;
                    return 2;
                }
                if (args[i].equals("automata")) {
                    mode = MODE_AUTOMATA;
                    return 2;
                }
                if (args[i].equals("forest")) {
                    mode = MODE_FOREST;
                    return 2;
                }
                if (args[i].equals("dryrun")) {
                    mode = MODE_DRYRUN;
                    return 2;
                }
                        

                throw new BadCommandLineException(
                    Messages.format(Messages.UNRECOGNIZED_MODE, args[i]));
            }
            
            return super.parseArgument(args, i);
        }
    }



    /**
     * Fully populate the code into CodeModel.
     * 
     * @return
     *      the object that provides navigation on the generated code,
     *      if the operation was successful, null if there was
     *      any error. Any error should have been sent to the specified
     *      error handler.
     */
    public static GeneratorContext generateCode(
        AnnotatedGrammar grammar,
        Options opt,
        ErrorReceiver errorReceiver ) {
        
        errorReceiver.debug("generating code");

        ErrorReceiverFilter ehFilter = new ErrorReceiverFilter(errorReceiver);

        GeneratorContext context =
            SkeletonGenerator.generate(grammar, opt, ehFilter);
        if(context==null)   return null;
        
        if( opt.generateUnmarshallingCode ) 
            UnmarshallerGenerator.generate(grammar, context, opt);
        if( opt.generateValidationCode || opt.generateMarshallingCode )
            // the on-demand validation code relys on the marshaller.
            // so if either switch is false, we need the marshaller
            MarshallerGenerator.generate(grammar, context, opt);
        if( opt.generateValidationCode )
            ValidatorGenerator.generate(grammar, context, opt);
        
        if (ehFilter.hadError())
            return null;

        // run extensions
        Iterator itr = opt.enabledModelAugmentors.iterator();
        while(itr.hasNext()) {
            CodeAugmenter ma = (CodeAugmenter)itr.next();
            ma.run(grammar,context,opt,errorReceiver);
        }
        
        return context;
    }


    /**
     * Prints the usage screen and exits the process.
     */
    protected static void usage( boolean privateUsage ) {
        if( privateUsage ) {
            System.out.println(Messages.format(Messages.DRIVER_PRIVATE_USAGE));
        } else {
            System.out.println(Messages.format(Messages.DRIVER_PUBLIC_USAGE));
        }
        
        if( Options.codeAugmenters.length!=0 ) {
            System.out.println(Messages.format(Messages.ADDON_USAGE));
            for( int i=0; i<Options.codeAugmenters.length; i++ ) {
                System.out.println(((CodeAugmenter)Options.codeAugmenters[i]).getUsage());
            }
        }
    }
    
    
    /**
     * Creates a configured CodeWriter that produces files into the specified directory.
     */
    public static CodeWriter createCodeWriter(File targetDir, boolean readonly ) throws IOException {
        return createCodeWriter(new FileCodeWriter( targetDir, readonly ));
    }

    /**
     * Creates a configured CodeWriter that produces files into the specified directory.
     */
    public static CodeWriter createCodeWriter( CodeWriter core ) throws IOException {

        // generate format syntax: <date> 'at' <time>
        String format =
            Messages.format(Messages.DATE_FORMAT)
                + " '"
                + Messages.format(Messages.AT)
                + "' "
                + Messages.format(Messages.TIME_FORMAT);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    
        return new PrologCodeWriter( core,
                Messages.format(
                    Messages.FILE_PROLOG_COMMENT,
                    dateFormat.format(new Date())) );
    }
}
