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
package com.sun.tools.xjc.grammar;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.Locator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.grammar.xducer.DatabindableXducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * An AGM with binding annotation.
 * 
 * This object keeps track of all binding information added to the grammar
 * including:
 * <ol>
 *  <li> all {@link ClassItem}s/{@link InterfaceItem}s added to the grammar
 *  <li> all {@link JPackage}s used by those classes/interfaces
 * </ol>
 * 
 * <p>
 *  The <code>exp</code> field holds the top-level expression
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class AnnotatedGrammar extends ReferenceExp implements Grammar
{
    private final ExpressionPool pool;
    
    /**
     * Code model object that keeps information about generated classes.
     */
    public final JCodeModel codeModel;
    
    /**
     * Default ID/IDREF symbol space. Any ID/IDREF without explicit
     * reference to a symbol space is assumed to use this default
     * symbol space.
     */
    public final SymbolSpace defaultSymbolSpace;
    
    /** map keyed by symbol space name. */
    private final Map symbolSpaces = new HashMap();
    
    /** all ClassItems in this grammar (from JClass to ClassItem). */
    private final Map classes = new HashMap();
    
    /** all InterfaceItems in this grammar (from JClass to InterfaceItem). */
    private final Map interfaces = new HashMap();
    
    /** all {@link PrimitiveItems} in this grammar. */
    private final Set primitives = new HashSet();
    
    /**
     * The root class of the implementaion classes, if any.
     * Otherwise null to use usual java.lang.Object.
     */
    public JClass rootClass;
    
    /**
     * If this value is non-null, the back-end is expected to produce
     * serializable classes with the specified version UID.
     * 
     * TODO: it's not clear if this functionality should be provided
     * by the back end, the front end, or some intermediate annotation layer.
     * An intermediate layer would be nice because it's modular,
     * but one difficulty is how to handle xducers.
     */
    public Long serialVersionUID = null;
    
    
    /** Creates an empty AnnotatedGrammar. */
    public AnnotatedGrammar( ExpressionPool pool ) {
        this(null,pool,new JCodeModel());
    }
    
    /**
     * creates an instance by copying values from the specified grammar.
     */
    public AnnotatedGrammar( Grammar source, JCodeModel _codeModel ) {
        this( source.getTopLevel(), source.getPool(), _codeModel );
    }
    
    public AnnotatedGrammar( Expression topLevel,
            ExpressionPool pool, JCodeModel _codeModel ) {
        super("");
        this.exp = topLevel;
        this.pool = pool;
        this.codeModel = _codeModel;
        this.defaultSymbolSpace = new SymbolSpace(codeModel);
        defaultSymbolSpace.setType(codeModel.ref(Object.class));
    }
    
    public Expression getTopLevel() { return exp; }
    
    public ExpressionPool getPool() { return pool; }
    
    public PrimitiveItem[] getPrimitives() {
        return (PrimitiveItem[]) primitives.toArray(new PrimitiveItem[primitives.size()]);
    }
    
    public ClassItem[] getClasses() {
        return (ClassItem[])classes.values().toArray( new ClassItem[classes.size()] );
    }
    public Iterator iterateClasses() {
        return classes.values().iterator();
    }
    
    public InterfaceItem[] getInterfaces() {
        return (InterfaceItem[])interfaces.values().toArray( new InterfaceItem[interfaces.size()] );
    }
    public Iterator iterateInterfaces() {
        return interfaces.values().iterator();
    }
    
    public SymbolSpace getSymbolSpace( String name ) {
        SymbolSpace ss = (SymbolSpace)symbolSpaces.get(name);
        if(ss==null)
            symbolSpaces.put(name,ss=new SymbolSpace(codeModel));
        return ss;
    }
    
    
    public PrimitiveItem createPrimitiveItem(
        Transducer _xducer, DatabindableDatatype _guard, Expression _exp, Locator loc ) {
        
        PrimitiveItem pi = new PrimitiveItem(_xducer,_guard,_exp,loc);
        primitives.add(pi);
        return pi;
    }
    public PrimitiveItem createPrimitiveItem(
        JCodeModel writer, DatabindableDatatype dt, Expression exp, Locator loc ) {
        
        return new PrimitiveItem(new DatabindableXducer(writer,dt),dt,exp,loc);
    }
    
    
    
    /** Gets the existing instance of ClassItem associated to a given type. */
    public ClassItem getClassItem( JDefinedClass type ) {
        return (ClassItem)classes.get(type);
    }
    
    /** creates a new ClassItem with a specified public interface class. */
    public ClassItem createClassItem( JDefinedClass type, Expression body, Locator loc ) {
        // type name must be unique.
        if(classes.containsKey(type)) {
            // assertion failure.
            // Or it might be better to make this method throw an exception
            // and force the caller to catch  - KK
            // dump all the generated class names for debugging
            System.err.println("class name "+type.fullName()+" is already defined");
            Iterator itr = classes.keySet().iterator();
            while(itr.hasNext()) {
                JDefinedClass cls = (JDefinedClass)itr.next();
                System.err.println(cls.fullName());
            }
            _assert(false);
        }
        
        ClassItem o = new ClassItem(this,type,body,loc);
        classes.put(type,o);
        return o;
    }
    
    
    
    /** Gets the existing instance of InterfaceItem associated to a given type. */
    public InterfaceItem getInterfaceItem( JDefinedClass type ) {
        return (InterfaceItem)interfaces.get(type);
    }
    
    /** creates a new InterfaceItem. */
    public InterfaceItem createInterfaceItem( JClass type, Expression body, Locator loc ) {
        // type name must be unique.
        _assert(!interfaces.containsKey(type));
        
        InterfaceItem o = new InterfaceItem(type,body,loc);
        interfaces.put(type,o);
        return o;
    }
    
    
    
    
    
    //
    //
    // all used JPackages
    //
    //
    
    /**
     * Returns all <i>used</i> JPackages.
     * 
     * A JPackage is considered as "used" if a ClassItem or
     * a InterfaceItem resides in that package.
     * 
     * This value is dynamically calculated every time because
     * one can freely remove ClassItem/InterfaceItem.
     * 
     * @return
     *         Given the same input, the order of packages in the array
     *         is always the same regardless of the environment.
     */
    public JPackage[] getUsedPackages() {
        Set s = new TreeSet(packageComparator);
        Iterator itr;
        
        itr = iterateClasses();
        while(itr.hasNext())
            s.add( ((ClassItem)itr.next()).getTypeAsDefined()._package() );
        
        itr = iterateInterfaces();
        while(itr.hasNext())
            s.add( ((InterfaceItem)itr.next()).getTypeAsClass()._package() );
        
        return (JPackage[])s.toArray(new JPackage[s.size()]);
    }
    
    
    private static final void _assert( boolean b ) {
        if(!b)    throw new JAXBAssertionError();
    }
    
    /**
     * Compares {@link JPackage} objects by their names.
     */
    private static final Comparator packageComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            JPackage p1 = (JPackage) o1;
            JPackage p2 = (JPackage) o2;
            
            return p1.name().compareTo(p2.name());
        }
    };
}
