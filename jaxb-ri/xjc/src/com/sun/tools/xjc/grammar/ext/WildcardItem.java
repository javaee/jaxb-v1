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
package com.sun.tools.xjc.grammar.ext;

import org.xml.sax.Locator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder;
import com.sun.tools.xjc.runtime.MSVValidator;
import com.sun.xml.bind.GrammarImpl;
import com.sun.xml.bind.JAXBObject;
import com.sun.xml.bind.xmlschema.LaxWildcardPlug;
import com.sun.xml.bind.xmlschema.StrictWildcardPlug;
import com.sun.xml.xsom.XSWildcard;

/**
 * Wildcard. An element will be unmarshaled/unmarshaled by
 * another RI-generated class. But the actual class will be
 * determined at runtime in a sort of "lazy-binding" way.
 * 
 * <p>
 * Since we don't know which class to use, this is "external".
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class WildcardItem extends ExternalItem {
    /**
     * The behavior when an element is encountered but
     * nobody can unmarshal it.
     */
    public final boolean errorIfNotFound;
    
    /** Reference to the {@link Object} class. */
    private final JClass refObject;
        
    public WildcardItem( JCodeModel codeModel, NameClass nc, boolean errorIfNotFound, Locator loc) {
        super("wildcard",nc,loc);
        this.refObject = codeModel.ref(Object.class);
        this.errorIfNotFound = errorIfNotFound;
    }

    public WildcardItem( JCodeModel codeModel, XSWildcard wc ) {
        this( codeModel,
            WildcardNameClassBuilder.build(wc),
            wc.getMode()==XSWildcard.STRTICT,
            wc.getLocator() );
    }

    /**
     * Java type of a wildcard is <b>NOT</b> {@link javax.xml.bind.Element}
     * but {@link Object}. See bug #4817737. This really doesn't make sense
     * to me.
     */
    public JType getType() {
        return refObject;
    }
    
    public Expression createAGM(ExpressionPool pool ) {
        // replace by a plug. wildcards require connections after composition
        GrammarImpl.Plug p;
        if( errorIfNotFound )    p = new StrictWildcardPlug(elementName);
        else                     p = new LaxWildcardPlug(elementName);
                
        return p;
    }
    
    public Expression createValidationFragment() {
        return 
            new ElementPattern(
                // allow any interface
                new NamespaceNameClass( MSVValidator.DUMMY_ELEMENT_NS ),
                // as long as its element name is allowed by this wildcard
                new AttributeExp( elementName, Expression.anyString ) );
    }
    
    public void generateMarshaller(
        GeneratorContext context, JBlock block,
        FieldMarshallerGenerator fmg, JExpression $context) {
        
        block.invoke( $context, "childAsBody")
            .arg(
                JExpr.cast(context.getCodeModel().ref(JAXBObject.class),
                fmg.peek(true)))
            .arg( JExpr.lit(fmg.owner().getFieldUse().name));
    }

    public JExpression generateUnmarshaller(GeneratorContext context,
        JExpression $unmarshallingContext, JBlock block, JExpression memento,
        JVar $uri, JVar $local, JVar $qname, JVar $atts ) {
        
        // [RESULT]
        // co = spawnWildcard( memento, ... );
        // if(co!=null)     <set co to the field>();
        JInvocation spawn = JExpr.invoke("spawnWildcard")
            .arg(memento).arg($uri).arg($local).arg($qname).arg($atts);
        
        return block.decl( getType(), "co", spawn );
    }

    public JExpression createRootUnmarshaller( GeneratorContext context, JVar $unmarshallingContext ) {
        // wildcard doesn't start unmarshalling
        return null;
    }
}
