/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.xml.bind.DatatypeConverterImpl;

/**
 * User-supplied transducer.
 * 
 * An user tranducer can be built from a parse method and a print method.
 * 
 * <p>
 * This transducer is primarily used to support &lt;conversion>
 * declaration of DTD binding information.
 * 
 * <p>
 * This class is also useful to support pluggable user-specified
 * serializer/deserializer.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class UserTransducer extends TransducerImpl
{
    /** The target type of this transducer. */
    private final JType type;
    
    /** owner CodeModel object. */
    private final JCodeModel codeModel;
    
    /** Unmarshaller. */
    private final String parseMethod;
    
    /** Marshaller. */
    private final String printMethod;
    
    /** Set to true to pass a namespace context object to the parse/print methods. */
    private final boolean enableNamespaceContext;
    
    
    /**
     * @param    _type
     *        The target type of the conversion. This type will
     *        be returned from the getReturnType method.
     * @param    _parseMethod
     *        The user-specified unmarshalling method.
     *        It must be either "new" (to indicate that the marshalling
     *        will be done by the constructor of the target type),
     *        "[methodName]" (to indicate that the designated
     *        static method of the target type will perform the conversion),
     *        or "[package.className.methodName]"
     *        (to indicate that the specified static method will perform
     *        the conversion.)
     *@param    _printMethod
     *        The user-specified marshalling method.
     *        It must be either
     *        "[methodName]" (to indicate that the designated
     *        method of the target type will perform the conversion),
     *        or "[package.className.methodName]"
     *        (to indicate that the specified static method will perform
     *        the conversion.)
     * 
     * @exception IllegalArgumentException
     *      If the type is a primitive type and print/parse methods are
     *      incorrect, this exception will be thrown.
     */
    public UserTransducer( JType _type,
        String _parseMethod, String _printMethod,
        boolean _enableNamespaceContext ) {
                                                     
        this.type = _type;
        this.codeModel = _type.owner();
        
        this.parseMethod = _parseMethod;
        this.printMethod = _printMethod;
        
        this.enableNamespaceContext = _enableNamespaceContext;
        
        if( type.isPrimitive() ) {
            // when a type is a primitive type,
            // the parse and print method must be an external method
            if( parseMethod.indexOf('.')==-1 )
                throw new IllegalArgumentException(
                    Messages.format(ERR_EXTERNAL_PARSE_METHOD_REQUIRED,_type.name()));
            if( printMethod.indexOf('.')==-1 )
                throw new IllegalArgumentException(
                    Messages.format(ERR_EXTERNAL_PRINT_METHOD_REQUIRED,_type.name()));
        }
    }

    public UserTransducer( JType _type, String _parseMethod, String _printMethod ) {
        this( _type, _parseMethod, _printMethod, false );
    }
    
    
    
    public JType getReturnType() { return type; }

    
    public JExpression generateSerializer( JExpression value, SerializerContext context ) {
        return _generateSerializer(value,context);
    }
    
    private JInvocation _generateSerializer( JExpression value, SerializerContext context ) {
        JInvocation inv;
        
        int idx = printMethod.lastIndexOf('.');
        if(idx<0) {
            // printMethod specifies a method in the target type
            // which performs the serialization.
            
            // RESULT: <value>.<method>()
            inv = value.invoke(printMethod);
        } else {        
            // RESULT: <className>.<method>(<value>)
            try {
                inv = codeModel.ref(printMethod.substring(0,idx)).staticInvoke(
                    printMethod.substring(idx+1)).arg(value);
            } catch( ClassNotFoundException e ) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
        
        if( enableNamespaceContext )
            inv.arg( context.getNamespaceContext() );
        
        return inv;
    }
    
    public JExpression generateDeserializer( JExpression literal, DeserializerContext context ) {
        
        JInvocation inv;
        
        if( parseMethod.equals("new") ) {
            // "new" indicates that the constructor of the target type
            // will do the unmarshalling.
            
            // RESULT: new <type>()
            inv = JExpr._new(type);
        } else {
            int idx = parseMethod.lastIndexOf('.');
            if(idx<0) {
                // parseMethod specifies the static method of the target type
                // which will do the unmarshalling.
                
                // because of an error check at the constructor,
                // we can safely assume that this cast works.
                inv = ((JClass)type).staticInvoke(parseMethod);
            } else {
                try {
                    // parseMethod specifies the class and its static method.
                    inv = codeModel.ref(parseMethod.substring(0,idx)).staticInvoke(
                        parseMethod.substring(idx+1));
                } catch( ClassNotFoundException e ) {
                    throw new NoClassDefFoundError(e.getMessage());
                }
            }
        }
        
        // pass in arguments
        inv.arg(literal);
        if( enableNamespaceContext )
            inv.arg(context.getNamespaceContext());
            
        return inv;
    }

    public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {
        if(enableNamespaceContext)
            // when the context support is turned off, this serializer can't
            // (therefore won't) declare a new prefix.
            body.get(true).add(_generateSerializer(value,context));
    }
    
    public JExpression generateConstant( ValueExp exp ) {
        return generateDeserializer( 
            codeModel.ref(DatatypeConverterImpl.class).staticInvoke("installHook").arg(
                JExpr.lit(obtainString(exp))), null );
    }
    
    
    private static final String ERR_EXTERNAL_PARSE_METHOD_REQUIRED = // arg:1
        "UserTransducer.ExternalParseMethodRequired";
    private static final String ERR_EXTERNAL_PRINT_METHOD_REQUIRED = // arg:1
        "UserTransducer.ExternalPrintMethodRequired";

}
