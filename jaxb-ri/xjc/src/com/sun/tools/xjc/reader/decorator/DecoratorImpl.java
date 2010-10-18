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

package com.sun.tools.xjc.reader.decorator;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JCodeModel;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.reader.ExpressionState;
import com.sun.msv.reader.GrammarReader;
import com.sun.msv.reader.State;
import com.sun.msv.reader.trex.DefineState;
import com.sun.msv.util.StartTagInfo;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Implementation classes of the Decorator interface
 * can derive this class to receive various services.
 */
abstract public class DecoratorImpl implements Decorator
{
    protected DecoratorImpl( GrammarReader _reader, AnnotatedGrammar _grammar,
        NameConverter nc ) {
        this.reader = _reader;
        this.grammar = _grammar;
        this.codeModel = grammar.codeModel;
        this.nameConverter = nc;
    }
    
    /**
     * GrammarReader object which is now parsing the schema.
     * This reader "owns" this decorator.
     */
    protected final GrammarReader reader;
    
    /**
     * Grammar object which is being constructed.
     */
    protected final AnnotatedGrammar grammar;
    
    /**
     * Code Model object which you can use.
     */
    protected final JCodeModel codeModel;
    
    /**
     * This object should be used to convert XML name to Java name.
     */
    protected final NameConverter nameConverter;
    
    
    
    
    /**
     * Gets JAXB annotation attribute.
     * 
     * @return
     *      null if the attribute is not present
     */
    protected final String getAttribute( StartTagInfo tag, String attName ) {
        return tag.getAttribute(Const.JAXB_NSURI,attName);
    }

    protected final String getAttribute( StartTagInfo tag, String attName, String defaultValue ) {
        String r = getAttribute(tag,attName);
        if(r!=null)     return r;
        else            return defaultValue;
    }

    /**
     * compute the name for the item.
     * 
     * @param role
     *        the role of this expression. One of "field","interface", and "class".
     * @param suffix
     *      Suffix is added if the name is defaulted from something else
     */
    protected final String decideName( State state, Expression exp, String role, String suffix, Locator loc ) {
        
        final StartTagInfo tag = state.getStartTag();
    
        String name = getAttribute(tag,"name");
        // if we have t:name attribute, use it.
        if(name==null) {
        
            // if the current tag has the name attribute, use it.
            // this is the case of <define/>,<ref/>, and sometimes
            // <element/> and <attribute/>
            name = tag.getAttribute("name");
            if(name!=null)    name = xmlNameToJavaName(role,name+suffix);
        }
        
        if(name==null) {
            // otherwise, sniff the name.
        
            // if it's element/attribute, then we may be able to use its name.
            if( exp instanceof NameClassAndExpression ) {
                NameClass nc = ((NameClassAndExpression)exp).getNameClass();
                if( nc instanceof SimpleNameClass )
                    name = xmlNameToJavaName(role,((SimpleNameClass)nc).localName+suffix);
                    
                // if it's not a simple type, abort.
            }
        }
        
        if(name==null) {
            if( state.getParentState() instanceof ExpressionState
            ||  state.getParentState() instanceof DefineState )
                // try to obtain a name from the parent.
                return decideName( state.getParentState(), exp, role, suffix, loc );
            
            // we can't generate a proper name. bail out
            reader.controller.error( new SAXParseException(
                Messages.format( Messages.ERR_NAME_NEEDED ), loc ) );
            return "DUMMY";
        }
        
        return name;
    }
    
    /**
     * Converts a token into a Java identifier.
     * @param role
     *        the role of this expression. One of "field","interface", and "class".
     */
    private final String xmlNameToJavaName( String role, String name ) {
        if(role.equals("field") )        return nameConverter.toPropertyName(name);
        if(role.equals("interface"))    return nameConverter.toInterfaceName(name);
        if(role.equals("class"))        return nameConverter.toClassName(name);
        
        throw new JAXBAssertionError(role);
    }
    
    
    
    //
    //
    // Error message formatting
    //
    //
    /*
    protected String localize( String propertyName, Object[] args ) {
        String format = ResourceBundle.getBundle(
                "com.sun.tools.xjc.reader.decorator.Messages").getString(propertyName);
        return MessageFormat.format(format, args );
    }
    
    protected final void reportError( String prop, Object arg1 ) {
        reader.reportError( localize(prop,new Object[]{arg1}) );
    }
    protected final void reportError( String prop ) {
        reader.reportError( localize(prop,new Object[]{}) );
    }

*/    
}
