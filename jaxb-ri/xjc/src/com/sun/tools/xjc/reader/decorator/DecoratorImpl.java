/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
