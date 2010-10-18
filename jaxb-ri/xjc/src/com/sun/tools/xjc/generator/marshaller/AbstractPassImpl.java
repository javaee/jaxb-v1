/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
