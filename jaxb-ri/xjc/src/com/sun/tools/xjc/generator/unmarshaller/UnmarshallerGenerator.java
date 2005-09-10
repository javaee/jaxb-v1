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
package com.sun.tools.xjc.generator.unmarshaller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.AnyNameClass;
import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.DifferenceNameClass;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassVisitor;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.msv.grammar.NotNameClass;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.msv.util.StringPair;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.PackageContext;
import com.sun.tools.xjc.generator.StaticMapGenerator;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;

/**
 * Augments the code model by adding unmarshaller codes.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class UnmarshallerGenerator
{
    /**
     * Generates unmarshallers into the code model.
     */
    public static Automaton[] generate( AnnotatedGrammar grammar, GeneratorContext context, Options opt ) {
        return new UnmarshallerGenerator(grammar,context,opt)._generate();
    }
    
    private Automaton[] _generate() {
        
        final ClassItem[] cis = grammar.getClasses();
        final Automaton[] automata = new Automaton[cis.length];
        
        final Map automataDic = new HashMap();
        
        // create empty automata first so that they can reference
        // each other while building themselves.
        for( int i=0; i<cis.length; i++ ) {
            automata[i] = new Automaton(context.getClassContext(cis[i]));
            automataDic.put(cis[i],automata[i]);
        }
        
        // then build them
        for (int i=0; i<automata.length; i++) {
            AutomatonBuilder.build(automata[i],context,automataDic);
        }
        
        if( options.debugMode && options.verbose ) {
            for( int i=0; i<cis.length; i++ ) {
                //AutomatonPrinter.print(ci.automaton,System.err);
                
                System.out.println(cis[i].getType().fullName());
                System.out.println("nullable: " +automata[i].isNullable());
                System.out.println();
            }
        }
        
        // TODO: determinacy check
        
        // generate unmarshaller in each implementation classes.
        for (int i=0; i<automata.length; i++)
            new PerClassGenerator( this, automata[i] ).generate();
        
        // generate GrammarInfo implementation.
        generateGrammarInfoImpl();
        
        return automata;
    }
    
    /**
     * Generates an implementation class for GrammarInfo.
     * This class will provide information about root elements.
     */
    private void generateGrammarInfoImpl() {
        PackageContext pcs[] = context.getAllPackageContexts();
        for( int i=0; i<pcs.length; i++ ) {
            
            RootMapBuilder rmb = new RootMapBuilder(
                pcs[i].rootTagMap, pcs[i].objectFactory );
            
            Map roots = getRootMap( pcs[i]._package );
            
            ClassItem[] classes = (ClassItem[]) roots.keySet().toArray(new ClassItem[roots.size()]);
            NameClass[] nameClasses = (NameClass[]) roots.values().toArray(new NameClass[roots.size()]);
                        
            // build probe points
            ProbePointBuilder ppb = new ProbePointBuilder();
            for( int j=0; j<nameClasses.length; j++ )
                nameClasses[j].visit(ppb);
            
            // generate rootTagMap
            StringPair[] probePoints = ppb.getResult();
            for( int j=0; j<probePoints.length; j++ ) {
                int k;
                for( k=0; k<nameClasses.length; k++ )
                    if( nameClasses[k].accepts(probePoints[j]) ) {
                        rmb.add( probePoints[j], classes[k] );
                        break;
                    }
                if(k==nameClasses.length)
                    rmb.add( probePoints[j], null );
            }
        }
    }
    
    /**
     * Computes map<ClassItem,NameClass> that represents root type items.
     */
    private Map getRootMap( final JPackage currentPackage ) {
        // we'll accumulate root classes that belong to this package.
        final Map roots = new HashMap();
            
        // find out all root classes.
        grammar.getTopLevel().visit(new ExpressionWalker() {
            private NameClass nc;
            private boolean inClass;
                
            public void onElement(ElementExp exp) {
                if(!inClass)
                    // ERROR: it must be wrapped by classitem.
                    // TODO:
                    // this can happen if the user poorly customize the schema.
                    // we should probably detect this error at eariler timing.
                    // or we can report this error here.
                    throw new UnsupportedOperationException();
                    
                if(nc==null)    nc=exp.getNameClass();
                else
                    nc = new ChoiceNameClass(nc,exp.getNameClass());
                    
                return; // do not visit children.
            }
            public void onOther( OtherExp exp ) {
                if( !inClass && exp instanceof ClassItem ) {
                    ClassItem ci = (ClassItem)exp;
                        
                    if( ci.getTypeAsDefined()._package()==currentPackage ) {
                        // this class belongs to the current package
                        inClass = true;
                        nc = null;
                            
                        exp.exp.visit(this);
                            
                        inClass = false;
                        roots.put(exp,nc);
                    }
                } else {
                    super.onOther(exp);
                }
            }
            /* The following BGM is possible:
            <root>
              <class-ref name="foo"/>
            </root>
            <class name="foo">
              <class-ref name="bar"/>
            </class>
            <class name="bar">
              <element name="bar">
                ...
              </element>
            </class>
            */
        });
        
        return roots;
    }
    
    
    final Options           options;
    final AnnotatedGrammar  grammar;
    final JCodeModel        codeModel;
    final GeneratorContext  context;
    
    /**
     * If this flag is on, this class emits trace statements in
     * the unmarshaller.
     */
    final boolean           trace;
    
    UnmarshallerGenerator( AnnotatedGrammar _grammar, GeneratorContext _context, Options _opt ) {
        this.options        = _opt;
        this.trace          = options.traceUnmarshaller;
        this.grammar        = _grammar;
        this.codeModel      = grammar.codeModel;
        this.context        = _context;
    }
    
    /**
     * Generate expression that evaluates true if and only if
     * the variables $uri and $local match the given name class.
     * 
     * @param $uri
     * @param $local
     *      variables that holds URI and local name to be tested.
     *      These strings must be symbolized by the <code>intern</code>
     *      method. This method uses <code>==</code> instead of
     *      <code>equals</code> for performance reason.
     */
    protected JExpression generateNameClassTest(
        NameClass nc, final JVar $uri, final JVar $local ) {
        
        return (JExpression)nc.visit(new NameClassVisitor(){
            public Object onSimple( SimpleNameClass nc ) {
                return JOp.cand(
                    JExpr.lit(nc.localName   ).eq($local),
                    JExpr.lit(nc.namespaceURI).eq($uri));
                // compare local names first, so that the rejection can be
                // done quickly.
            }
            public Object onNsName( NamespaceNameClass nc ) {
                return JExpr.lit(nc.namespaceURI).eq($uri);
            }
            public Object onAnyName( AnyNameClass nc ) {
                return JExpr.TRUE;
            }
            public Object onNot( NotNameClass nc ) {
                return JOp.not((JExpression)nc.child.visit(this));
            }
            public Object onDifference( DifferenceNameClass nc ) {
                return JOp.cand(
                    (JExpression)nc.nc1.visit(this),
                    JOp.not((JExpression)nc.nc2.visit(this)));
            }
            public Object onChoice( ChoiceNameClass nc ) {
                return JOp.cor(
                    (JExpression)nc.nc1.visit(this),
                    (JExpression)nc.nc2.visit(this));
            }
        });
    }
    
    private static class RootMapBuilder extends StaticMapGenerator {

        /** Enclosing ObjectFactory class. */
        private final JDefinedClass objectFactory;
        private final JCodeModel codeModel;
                
        RootMapBuilder( JVar $rootTagMap, JDefinedClass _objectFactory) {
            super( $rootTagMap, _objectFactory.init());
            this.objectFactory = _objectFactory;
            this.codeModel = objectFactory.owner();
        }
        
        protected JMethod createNewMethod(int uniqueId) {
            return objectFactory.method(
                JMod.PRIVATE|JMod.STATIC,
                codeModel.VOID,
                "__initRootMap"+uniqueId);
        }
        
        public void add( StringPair tagName, ClassItem value ) {
            super.add( JExpr._new(codeModel.ref(QName.class))
                .arg(JExpr.lit(tagName.namespaceURI))
                .arg(JExpr.lit(tagName.localName)),
                value!=null
                    ?value.getTypeAsDefined().dotclass()
                    :JExpr._null() );
        }
    }
    
    /**
     * Builds a set of probe point {@link StringPair}s.
     */
    private static class ProbePointBuilder implements NameClassVisitor {
        private final Set probePoints = new HashSet();
        
        public StringPair[] getResult() {
            return (StringPair[]) probePoints.toArray(new StringPair[probePoints.size()]);
        }
        
        public Object onSimple( SimpleNameClass nc ) {
            probePoints.add( nc.toStringPair() );
            return null;
        }
        public Object onNsName( NamespaceNameClass nc ) {
            probePoints.add( new StringPair(nc.namespaceURI,"*") );
            return null;
        }
        public Object onAnyName( AnyNameClass nc ) {
            probePoints.add( new StringPair("*","*") );
            return null;
        }
        public Object onNot( NotNameClass nc ) {
            nc.child.visit(this);
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
    }
}
