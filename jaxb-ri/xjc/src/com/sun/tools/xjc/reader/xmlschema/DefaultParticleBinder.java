/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermVisitor;

/**
 * Performs the default binding on {@link XSParticle}.
 * 
 * <p>
 * Note that the terminology "infoset" has almost nothing to do with
 * XML "infoset". Just consider it as a coincidence.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class DefaultParticleBinder extends ParticleBinder {
    DefaultParticleBinder( BGMBuilder builder ) {
        super(builder);
    }
    
    // inherited
    public Expression build( XSParticle p, ClassItem superClass ) {
        // scan the tree by a checker.
        Checker checker = new Checker();
        
        if(superClass!=null)
            checker.readSuperClass(superClass);
        checker.particle(p);
        
        if(checker.hasNameCollision()) {
            // TODO: we also need to check whether this particle has
            // any name collision with that of the base class.
            FieldItem fi = new FieldItem(
                superClass==null?"Content":"Rest",
                builder.typeBuilder.build(p),
                p.getLocator());
            // specify the multiplicity explicitly so that
            // this will always become a list property.
            fi.multiplicity = Multiplicity.star;
            fi.collisionExpected = true;
            
            fi.javadoc = Messages.format( Messages.MSG_FALLBACK_JAVADOC,
                    checker.getCollisionInfo().toString() );
                        
            return fi;
        } else {
            return new Builder(checker.markedParticles).build(p);
        }
    }

    public boolean checkFallback( XSParticle p, ClassItem superClass ) {
        // scan the tree by a checker.
        Checker checker = new Checker();
        
        if(superClass!=null)
            checker.readSuperClass(superClass);
        checker.particle(p);
        
        return checker.hasNameCollision();
    }
    
    
    
    
    
    
    
    
    
    /**
     * Checks whether the infoset binding is possible or not.
     * 
     * It also marks particles that need to be mapped to properties,
     * by reading customization info.
     */
    private class Checker implements XSTermVisitor {
        
        boolean hasNameCollision() {
            return collisionInfo!=null;
        }
        
        CollisionInfo getCollisionInfo() {
            return collisionInfo;
        }
        
        /**
         * If a collision is found, this field will be non-null.
         */
        private CollisionInfo collisionInfo = null;
        
        /** Used to check name collision. */
        private final NameCollisionChecker cchecker
            = new NameCollisionChecker();
        
        public void particle( XSParticle p ) {
            
            BIProperty cust = getLocalPropCustomization(p);
            if(cust!=null) {
                // if a property customization is specfied,
                // check that value and turn around.
                check(p);
                mark(p);
                return;
            }
            
            XSTerm t = p.getTerm();
            
            if(p.getMaxOccurs()!=1
            &&(t.isModelGroup() || t.isModelGroupDecl())) {
                // this particle gets its own property
                mark(p);
                return;
            }
            
            outerParticle = p;    
            t.visit(this);
        }
        
        /**
         * This field points to the parent XSParticle.
         * The value is only valid when we are processing XSTerm.
         */
        private XSParticle outerParticle;
        
        public void elementDecl(XSElementDecl decl) {
            check(outerParticle);
            mark(outerParticle);
        }

        public void modelGroup(XSModelGroup mg) {

            if(builder.selector.bindToType(mg)!=null) {
                mark(outerParticle);
            } else {
                int sz = mg.getSize();
                
                if(mg.getCompositor()==XSModelGroup.CHOICE) {
                    for( int i=0; i<sz; i++ )
                        particle(mg.getChild(i));
                } else {
                    // for other cases, we need to check name collision
                    Range cookie = cchecker.start();
                    for( int i=0; i<sz; i++ ) {
                        particle(mg.getChild(i));
                        cchecker.update(cookie);
                    }
                    cchecker.end();
                }
            }
        }

        public void modelGroupDecl(XSModelGroupDecl decl) {
            if(builder.selector.bindToType(decl)!=null) {
                mark(outerParticle);
            } else {
                modelGroup(decl.getModelGroup());
            }
        }

        public void wildcard(XSWildcard wc) {
            mark(outerParticle);
        }
        
        void readSuperClass( ClassItem ci ) {
            cchecker.readSuperClass(ci);
        }
        
        
        
        
        /**
         * Checks the name collision of a newly found particle.
         */
        private void check( XSParticle p ) {
            if( collisionInfo==null )
                collisionInfo = cchecker.check(p);
        }
        
        /**
         * Marks a particle that it's going to be mapped to a property.
         */
        private void mark( XSParticle p ) {
            markedParticles.put(p,computeLabel(p));
        }
        
        /** 
         * Marked particles.
         * 
         * A map from XSParticle to its label.
         */
        public final Map markedParticles = new Hashtable();


        /** Represents a range [s,e) */
        final class Range {
            Range( int s, int e ) { start=s; end=e; }
            int start,end;
        }
        
    
        /**
         * Checks name collisions among particles that belong to sequences.
         */
        private final class NameCollisionChecker {
            /**
             * Marks the start of a new &lt;sequence>.
             * @return cookie to be specified for the next call of "update".
             */
            Range start() {
                int l = len();
                Range r = new Range(l,l);
                ranges.push(r);
                return r;
            }
            
            /**
             * This method shall be called whenever one child of a &lt;sequence>
             * gets processed.
             * @param cookie
             *      The return value from the start method.
             */
            void update( Range cookie ) {
                // assert r==ranges.peek();
                cookie.end = len();   // update the range
            }
            
            /**
             * Marks the end of a &lt;sequence>.
             */
            void end() {
                // assert r==ranges.peek();
                ranges.pop();
            }
            
            /**
             * Checks the label conflict of a particle.
             * This method shall be called for each marked particle.
             * 
             * @return
             *      a description of a collision if a name collision is
             *      found. Otherwise null.
             */
            CollisionInfo check( XSParticle p ) {
                // this can be used for particles with a property customization,
                // which may not have element declaration as its term.
//                // we only check particles with element declarations.
//                _assert( p.getTerm().isElementDecl() );
                
                String label = computeLabel(p);
                if( occupiedLabels.containsKey(label) ) {
                    // collide with occupied labels
                    return new CollisionInfo(label,p.getLocator(),
                            ((FieldItem)occupiedLabels.get(label)).locator);
                }
                
                for( int i=ranges.size()-1; i>=0; i-- ) {
                    Range r = (Range)ranges.get(i);
                    for( int j=r.start; j<r.end; j++ ) {
                        XSParticle jp = (XSParticle)particles.get(j);
                        if(!check( p, jp )) {
                            // problem was found. no need to check further
                            return new CollisionInfo( label, p.getLocator(), jp.getLocator() );
                        }
                    }
                }
                particles.add(p);
                return null;
            }
            
            private int len() { return particles.size(); }
            
            /** List of particles reported through the check method. */
            private final ArrayList particles = new ArrayList();
            
            /**
             * Label names already used in the base type.
             * <p>
             * The map is keyed by names to one of its {@link FieldItem}s.
             */
            private final Map occupiedLabels = new HashMap();
            
            /**
             * List of Range objects that indicate ranges of index
             * we need to check.
             */
            private final Stack ranges = new Stack();
            
            /**
             * Checks the conflict of two particles.
             * @return
             *      true if the check was successful.
             */
            private boolean check( XSParticle p1, XSParticle p2 ) {
                return !computeLabel(p1).equals(computeLabel(p2));
            }
            
            /**
             * Reads fields of the super class and includes them
             * to name collision tests.
             */
            void readSuperClass( ClassItem ci ) {
                ci.exp.visit(new ExpressionWalker() {
                    public void onOther(OtherExp exp) {
                        if(exp instanceof FieldItem) {
                            occupiedLabels.put(((FieldItem)exp).name,exp);
                            return;
                        }
                        if(exp instanceof IgnoreItem) {
                            return;
                        }
                        exp.exp.visit(this);
                    }
                });
            }
        }
    
        
        
        
        
        /** Keep the computed label names for particles. */
        private final Map labelCache = new Hashtable();
        
        /**
         * Hides the computeLabel method of the outer class
         * and adds caching.
         */
        private String computeLabel( XSParticle p ) {
            String label = (String)labelCache.get(p);
            if(label==null)
                labelCache.put( p, label=DefaultParticleBinder.this.computeLabel(p) );
            return label;
        }
    }
    

    
    
    
    
    


    


    /**
     * Builds a expression by using the result computed by Checker
     */
    private final class Builder implements XSTermFunction, BGMBuilder.ParticleHandler {
        Builder( Map markedParticles ) {
            this.markedParticles = markedParticles;
        }
        
        /** All marked particles. A map from XSParticle to its label. */
        private final Map markedParticles;
        
        
        /** Typed wrapper method. */
        Expression build( XSTerm sc ) {
            return (Expression)sc.apply(this);
        }
        Expression build( XSParticle p ) {
            return (Expression)particle(p);
        }
        
        /** Returns true if a particle is marked. */
        private boolean marked( XSParticle p ) {
            return markedParticles.containsKey(p);
        }
        /** Gets a label of a particle. */
        private String getLabel( XSParticle p ) {
            return (String)markedParticles.get(p);
        }
        
        private boolean isLocalElementDecl( XSTerm t ) {
            XSElementDecl e = t.asElementDecl();
            return e!=null && e.isLocal();
        }
        
        public Object particle( XSParticle p ) {
            XSTerm t = p.getTerm();
            
            if(marked(p)) {
                Expression exp;
                
                if( isLocalElementDecl(t) && builder.selector.bindToType(t)==null) {
                    // type builder will always map an element to a class (which is
                    // a necessary behavior when we are processing a part of
                    // a content model by it), but this behavior is not appropriate
                    // in this special case. 
                    exp = builder.fieldBuilder.build(t);
                } else {
                    Expression typeExp;
                    if( needSkip(t) ) {
                        XSElementDecl e = t.asElementDecl();
                        // skip the class bound to the element and directly
                        // bind to the type
                        
                        // UGLY CODE
                        // remember that this particle is skipping the corresponding
                        // global element class so that AGMFragmentBuilder can correctly
                        // generate the fragment
                        builder.particlesWithGlobalElementSkip.add(p);
                        ElementPattern eexp = builder.typeBuilder.elementDeclFlat(e);
                        
                        if( e.isAbstract() ) {
                            typeExp = Expression.nullSet;
                        } else
                        if( needSkippableElement(e) )
                            // (<foo>FooType</foo>)|Foo)
                            typeExp = pool.createChoice( builder.selector.bindToType(e), eexp );
                        else
                            typeExp = eexp;
                            
                        // include substitution groups
                        typeExp = pool.createChoice( builder.getSubstitionGroupList(e), typeExp );
                            
                    } else {
                        typeExp = builder.typeBuilder.build(t);
                    }
                    exp = builder.fieldBuilder.createFieldItem(
                        getLabel(p), false, typeExp, p );
                }
                
                return builder.processMinMax( exp, p );
            } else {
                // this is an unmarked particle
                return builder.processMinMax(
                    build(t), p);
            }
        }
        
        public Object elementDecl( XSElementDecl e ) {
            // because the corresponding particle must be marked.
            _assert(false); return null;
        }
        
        public Object wildcard( XSWildcard wc ) {
            // because the corresponding particle must be marked.
            _assert(false); return null;
        }
        
        public Object modelGroupDecl( XSModelGroupDecl decl ) {
            // push a new JClassFactory so that the new classes will be prefixed by
            // the model group name
            builder.selector.pushClassFactory(
                new PrefixedJClassFactoryImpl( builder, decl ) );
            
            Object r = modelGroup(decl.getModelGroup());
            
            builder.selector.popClassFactory();
            
            return r;
        }
        
        public Object modelGroup( XSModelGroup mg ) {
            return builder.applyRecursively(mg,this);
        }
    }
}
