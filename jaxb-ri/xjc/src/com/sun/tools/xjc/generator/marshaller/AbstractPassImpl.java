/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.marshaller;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.msv.datatype.xsd.QnameType;
import com.sun.msv.datatype.xsd.QnameValueType;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ValueExp;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * A partial implementation of the {@link Pass} interface.
 * 
 * This class provides the utility methods for derived classes.
 * 
 * @author Kohsuke KAWAGUCHI
 */
abstract class AbstractPassImpl implements Pass
{
    /**
     * Method of the marshaller to be called when
     * a child object or a reference to the super class is found.
     */
    private final String name;

    protected final Context context;
    
    
    AbstractPassImpl( Context _context, String _name ) {
        this.context = _context;
        this.name = _name;
    }
    
    
    public final String getName() {
        return name;
    }

    public final void build(Expression exp) {
        Pass old = context.currentPass;
        context.currentPass = this;
        context.build(exp);
        context.currentPass = old;
    }

    /**
     * Writes a string for the given ValueExp so that the marshalled
     * document satisifies constraints imposed by the schema.
     */
    protected final void marshalValue( ValueExp exp ) {
        
        // the problem is, we have the value to be marshalled at
        // the compilation time, but we need to produce that
        // text at the runtime.
        
        if( !exp.dt.isContextDependent() ) {
            // if the datatype is not context dependent,
            // we can obtain the serialized form now and
            // generate a code so that that value will be sent
            // at the runtime.
            
            // there is no generalized interface for the object->string
            // conversion, so we test it in an ad-hoc way.
            String literal = null;
            if( exp.dt instanceof XSDatatype ) {
                // this datatype supports conversion to the lexical form.
                literal = ((XSDatatype)exp.dt)
                    .convertToLexicalValue( exp.value, null );
            }
            
            if(literal==null) {
                // we don't know how to convert this value into a lexical form.
                // abort.
                
                // TODO: we need to establish an error reporting scheme
                // from marshaller/unmarshaller generator
                throw new JAXBAssertionError();
            }
            
            // generate a code to produce this string
            // [RESULT]
            // $context.text( <serializer>( fieldIterator.next(), $context ) );
        
            getBlock(true).invoke( context.$serializer, "text" )
                .arg( JExpr.lit(literal) )
                .arg( JExpr._null() );
        } else {
            // TODO:
            // context-dependent datatypes need even more complex treatment,
            // so just treat QName for now and leave others unsupported.
            
            if( exp.dt instanceof QnameType ) {
                
                QnameValueType qn = (QnameValueType)exp.value;
                
                getBlock(true).invoke( context.$serializer, "text" ).arg(
                    context.codeModel.ref(DatatypeConverter.class).staticInvoke("printQName")
                        .arg( JExpr._new(context.codeModel.ref(QName.class))
                            .arg(JExpr.lit(qn.namespaceURI))
                            .arg(JExpr.lit(qn.localPart)) )
                        .arg( context.$serializer.invoke("getNamespaceContext") ) )
                    .arg( JExpr._null() );
            } else {
                throw new JAXBAssertionError("unsupported datatype "+exp.name);
            }
        }
    }

    /**
     * Code should be generated into the returned block.  
     */
    protected final JBlock getBlock(boolean create) {
        return context.getCurrentBlock().get(create);
    }
}