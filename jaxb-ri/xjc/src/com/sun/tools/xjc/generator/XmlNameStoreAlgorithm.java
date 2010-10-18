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

package com.sun.tools.xjc.generator;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.AnyNameClass;
import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.DifferenceNameClass;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.NameClassVisitor;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.msv.grammar.NotNameClass;
import com.sun.msv.grammar.SimpleNameClass;

/**
 * Hides the logic of how namespace URI / local name of
 * an attribute/element is stored inside the generated code.
 * 
 * Logically a name store should be associated with every attribute
 * and element, but 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class XmlNameStoreAlgorithm
{
    
    /** Obtains the stored namespace URI. */
    public abstract JExpression getNamespaceURI();
    
    /** Obtains the stored local name. */
    public abstract JExpression getLocalPart();
    
    /**
     * Obtains the type of the field used to store the name.
     * 
     * @return null if no field is used.
     */
    public abstract JType getType( JCodeModel codeModel );
    
    /**
     * Called when the name is unmarshalled so that they can be stored.
     */
    public abstract void onNameUnmarshalled(
        JCodeModel codeModel, JBlock block, JVar $uri, JVar $localName );
    
    /**
     * Generates the necessary storage and expose the necessary
     * accessor methods to the generated class.
     */
    public abstract void populate( ClassContext target );

    
    /**
     * Convenience method.
     */
    public static XmlNameStoreAlgorithm get( NameClassAndExpression item ) {
        return get(item.getNameClass());
    }
    
    /**
     * Gets the appropriate algorithm from a name class.
     */
    public static XmlNameStoreAlgorithm get( NameClass nc ) {
        if( nc instanceof SimpleNameClass )
            return new Simple((SimpleNameClass)nc);

        final Set namespaces = new HashSet();
        nc.simplify().visit(new NameClassVisitor(){
            public Object onSimple( SimpleNameClass nc ) {
                namespaces.add( nc.namespaceURI );
                return null;
            }
            public Object onNsName( NamespaceNameClass nc ) {
                namespaces.add( nc.namespaceURI );
                return null;
            }
            public Object onAnyName( AnyNameClass nc ) {
                namespaces.add( "*1" );
                namespaces.add( "*2" );
                return null;
            }
            public Object onNot( NotNameClass nc ) {
                namespaces.add( "*1" );
                namespaces.add( "*2" );
                return null;
            }
            public Object onDifference( DifferenceNameClass nc ) {
                nc.nc1.visit(this);
                nc.nc2.visit(this);
                return null;
            }
            public Object onChoice( ChoiceNameClass nc ) {
                nc.nc1.visit(this);
                nc.nc2.visit(this);
                return null;
            }
        });
        
        if( namespaces.size()==1 )
            return new UniqueNamespace( (String)namespaces.iterator().next() );
        
        return Any.theInstance;
    }
    
    
    /** Algorithm used when the entire name is unique. */
    private static class Simple extends XmlNameStoreAlgorithm
    {
        private final SimpleNameClass snc;
        
        private Simple( SimpleNameClass _snc ) {
            this.snc = _snc;
        }
        
        public JExpression getLocalPart() {
            return JExpr.lit(snc.localName);
        }

        public JExpression getNamespaceURI() {
            return JExpr.lit(snc.namespaceURI);
        }

        public void onNameUnmarshalled(
            JCodeModel codeModel, JBlock block, JVar $uri, JVar $localName) {
            
            ; // no need to do anything.
        }

        public void populate( ClassContext target ) {
            // tag name is unique so it won't be exposed.
        }
        
        public JType getType( JCodeModel codeModel ) {
            return null;
        }
    }
    
    /** Algorithm used when only the namespce URI is unique. */
    private static class UniqueNamespace extends XmlNameStoreAlgorithm
    {
        private final String namespaceURI;
        
        private UniqueNamespace( String _nsUri ) {
            this.namespaceURI = _nsUri;
        }
        
        public JExpression getLocalPart() {
            return JExpr.ref("_XmlName");
        }

        public JExpression getNamespaceURI() {
            return JExpr.lit(namespaceURI);
        }

        public void onNameUnmarshalled(
            JCodeModel codeModel, JBlock block, JVar $uri, JVar $localName) {
            
            block.assign(JExpr.ref("_XmlName"),$localName);
        }

        public void populate( ClassContext target ) {
            JDefinedClass impl = target.implClass;            
            JCodeModel codeModel = impl.owner();
            JClass string = codeModel.ref(String.class);
            
            JVar $name = impl.field(JMod.PROTECTED,string,"_XmlName");
            
            MethodWriter writer = target.createMethodWriter();
            
            // getter
            writer.declareMethod(string,"getXmlName").body()._return($name);
            
            // setter
            JMethod setter = writer.declareMethod(codeModel.VOID,"setXmlName");
            JVar $arg = writer.addParameter(string,"newLocalName");
            setter.body().assign($name,$arg);
        }
        
        public JType getType( JCodeModel codeModel ) {
            return codeModel.ref(String.class);
        }
    }
    
    
    /** Algorithm used when only the namespce URI is unique. */
    private static class Any extends XmlNameStoreAlgorithm
    {
        private Any() {}

        private static final Any theInstance = new Any();
        
        public JExpression getLocalPart() {
            return JExpr.ref("_XmlName").invoke("getLocalPart");
        }

        public JExpression getNamespaceURI() {
            return JExpr.ref("_XmlName").invoke("getNamespaceURI");
        }

        public void onNameUnmarshalled(
            JCodeModel codeModel, JBlock block, JVar $uri, JVar $localName) {
            
            block.assign(JExpr.ref("_XmlName"),
                JExpr._new(codeModel.ref(QName.class))
                    .arg($uri).arg($localName));
        }

        public void populate( ClassContext target ) {
            JDefinedClass impl = target.implClass;
            JCodeModel codeModel = impl.owner();
            JClass qname = codeModel.ref(QName.class);
            
            JVar $name = impl.field(JMod.PROTECTED,qname,"_XmlName");
            
            MethodWriter helper = target.createMethodWriter();
            
            // getter
            helper.declareMethod(qname,"getXmlName").body()._return($name);
            
            // setter
            JMethod setter = helper.declareMethod(codeModel.VOID,"setXmlName");
            JVar $arg = helper.addParameter(qname,"newLocalName");
            setter.body().assign($name,$arg);
        }
        
        public JType getType( JCodeModel codeModel ) {
            return codeModel.ref(QName.class);
        }
    }
}
