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
package com.sun.tools.xjc.reader.xmlschema;

import java.util.StringTokenizer;

import org.relaxng.datatype.ValidationContext;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.ExpressionVisitorExpression;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OneOrMoreExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Function object that is used to apply fixed value constraints to
 * BGM expressions.
 * 
 * <p>
 * In BGM, fixed value constraints are represented by just using
 * &lt;value> pattern of RELAX NG. However, XML Schema works in
 * such a way that a schema is allowed to specify a fixed value when
 * it is used (i.e., attribute use.)
 * 
 * Thus we need to build a BGM fragment that only accepts the value
 * specified by the fixed value constraint by taking a fully built
 * BGM fragment that represents the content model of an attribute
 * and a fixed value.
 * 
 * <p>
 * Also, for the back-end to work, we need to correctly annotate
 * &lt;value> patterns with {@link PrimitiveItem}s.
 * 
 * <p>
 * For example,
 * <pre><xmp>
 * <list>
 *   <zeroOrMore>
 *     <choice>
 *       <primitive xducer1>
 *         <data type="int"/>
 *       </primitive>
 *       <primitive xducer2>
 *         <data type="string"/>
 *       </primitive>
 *     </choice>
 *   </zeroOrMore>
 * </list>
 * </xmp></pre>
 * with a string "abc 15 3f" would produce:
 * <pre><xmp>
 * <list>
 *   <primitive xducer2>
 *     <value type=string>abc</value>
 *   </primitive>
 *   <primitive xducer1>
 *     <value type=int>15</value>
 *   </primitive>
 *   <primitive xducer2>
 *     <value type=string>3f</value>
 *   </primitive>
 * </xmp></pre>
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class FixedExpBuilder implements ExpressionVisitorExpression {
    
    /**
     * The only method exposed to outside. Computes an expression
     * as defined in the class javadoc.
     * 
     * @return Expression.nullSet
     *      If the given token is not accepted by the expression.
     */
    public static Expression build( Expression exp, String token,
        AnnotatedGrammar grammar, ValidationContext context ) {
        
        return exp.visit( new FixedExpBuilder(grammar,token,context) );
    }
    
    private FixedExpBuilder( AnnotatedGrammar _grammar, String _token, ValidationContext _context ) {
        this.grammar = _grammar;
        this.pool = grammar.getPool();
        this.token = _token;
        this.context = _context;
    }
    
    private final AnnotatedGrammar grammar;
    private final ExpressionPool pool;
    private String token;
    /** Contest in which the token should be interpreted. */
    private final ValidationContext context;
    

    public Expression onOther(OtherExp exp) {
        if(exp instanceof PrimitiveItem) {
            PrimitiveItem pi = (PrimitiveItem)exp;
            
            Expression body = exp.exp.visit(this);
            if(body==Expression.nullSet)    return body;
            
            return grammar.createPrimitiveItem( pi.xducer, pi.guard, body, pi.locator );
        }
        return exp.exp.visit(this);
    }

    public Expression onList(ListExp exp) {
        String oldToken = token;
        
        Expression residual = exp.exp;
        Expression result = Expression.epsilon;
        StringTokenizer tokens = new StringTokenizer(token);
        while(tokens.hasMoreTokens()) {
            token = tokens.nextToken();
            result = pool.createSequence( result, residual.visit(this) );
            // residual =   TODO: update residual
            // but this is not quite important because XML Schema's list is
            // always of the form
            // <list><zeroOrMore>...</zeroOrMore></list> and there will
            // be no <group> in it.
        }
        
        // always wrap it in a <list>.
        // this is a sign that says you need to map it to a list.
        result = pool.createList(result);
        
        token = oldToken;
        return result;
    }

    public Expression onRef(ReferenceExp exp) {
        return exp.exp.visit(this);
    }

    private static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }

    public Expression onAnyString() {
        // accepted
        return pool.createValue( StringType.theInstance, token );
    }

    public Expression onChoice(ChoiceExp exp) {
        // The semantics of XML Schema union is that if the first branch
        // matches, the 2nd branch won't be tried.
        Expression r = exp.exp1.visit(this);
        if(r!=Expression.nullSet)   return r;
        return exp.exp2.visit(this);
    }

    public Expression onEpsilon() {
        return Expression.nullSet;
    }

    public Expression onNullSet() {
        return Expression.nullSet;
    }

    public Expression onOneOrMore(OneOrMoreExp exp) {
        return exp.exp.visit(this);
    }

    public Expression onSequence(SequenceExp exp) {
        Expression r = exp.exp1.visit(this);
        // once again, the semantics of XML Schema is that if the 1st branch
        // matches, the 2nd branch won't be tested.
       
        if(r==Expression.nullSet && exp.exp1.isEpsilonReducible())
            r = exp.exp2.visit(this);
            
        return r;
    }

    public Expression onData(DataExp exp) {
        if( exp.dt.isValid(token,context) )
            return pool.createValue(
                exp.dt, exp.name, exp.dt.createValue(token,context) );
        
        // was not accepted
        return Expression.nullSet;
    }

    public Expression onValue(ValueExp exp) {
        if(exp.dt.sameValue( exp.value, exp.dt.createValue(token,context) ))
            return exp;
        else
            return Expression.nullSet;
    }


    // these expressions should never be contained
    public Expression onAttribute(AttributeExp exp) { _assert(false); return null; }
    public Expression onElement(ElementExp exp) { _assert(false); return null; }
    public Expression onConcur(ConcurExp p) { _assert(false); return null; }
    public Expression onInterleave(InterleaveExp p) { _assert(false); return null; }
    public Expression onMixed(MixedExp exp) { _assert(false); return null; }
}
