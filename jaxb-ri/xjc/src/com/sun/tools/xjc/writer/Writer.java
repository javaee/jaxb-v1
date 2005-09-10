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

package com.sun.tools.xjc.writer;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import com.sun.msv.grammar.AnyNameClass;
import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.DifferenceNameClass;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassVisitor;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.msv.grammar.NotNameClass;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.writer.GrammarWriter;
import com.sun.msv.writer.SAXRuntimeException;
import com.sun.msv.writer.XMLWriter;
import com.sun.msv.writer.relaxng.Context;
import com.sun.msv.writer.relaxng.PatternWriter;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.JavaItemVisitor;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Serializes annotated AGM to XML.
 */
public class Writer implements GrammarWriter, Context {
    
    /**
     * @param    _noNS
     *        if true, the writer will not produce the ns attribute.
     *        This is useful to make the output concise.
     * @param   _sigOnly
     *      If true, this write will only produce interface signature.
     */
    public Writer( boolean _noNS, boolean _sigOnly ) {
        this.noNS = _noNS;
        this.signatureOnly = _sigOnly;
    }

    public static void writeToConsole(
        boolean noNS, Grammar grammar ) {
        
        writeToConsole(noNS,false,grammar);
    }
    
    /**
     * Writes the grammar to the console.
     * This is typically useful for a debugging.
     */
    public static void writeToConsole(
        boolean noNS, boolean signatureOnly, Grammar grammar ) {
        
        try {
            Writer w = new Writer(noNS,signatureOnly);
        
            OutputFormat format = new OutputFormat("xml",null,true);
            format.setIndent(1);
            w.setDocumentHandler(new XMLSerializer(System.out,format));
        
            w.write(grammar);
        } catch( SAXException e ) {
            // this is not possible in this control environment
            // where we'll write to the console
            throw new JAXBAssertionError(e);
        }
    }
    
    private final boolean noNS;
    private final boolean signatureOnly;
    
    /**
     * Remembers the {@link ClassCandidateItem}s that are already written.
     */
    private final Set candidates = new HashSet();
    
    private final XMLWriter writer = new XMLWriter();
    public XMLWriter getWriter() { return writer; }
    
    // we don't use the default target namespace
    public String getTargetNamespace() { return null; }
    
    public void setDocumentHandler( DocumentHandler handler ) {
        writer.setDocumentHandler(handler);
    }
    
    public void write( Grammar g ) throws SAXException {
        write((AnnotatedGrammar)g);
    }
    
    
    /** Generates XML representation of the annotated grammar. */
    public void write( AnnotatedGrammar grammar ) throws SAXException {
        
        try {
            final DocumentHandler handler = writer.getDocumentHandler();
            handler.setDocumentLocator( new LocatorImpl() );
            handler.startDocument();
            
            writer.start("bgm");
            
            writer.start("root");
            grammar.getTopLevel().visit(patternWriter);
            writer.end("root");
            
            
            ClassItem[] cs = grammar.getClasses();
            for( int i=0; i<cs.length; i++ )
                writeClass(cs[i]);
            
            InterfaceItem[] is = grammar.getInterfaces();
            for( int i=0; i<is.length; i++ )
                writeInterface(is[i]);
            
            writer.end("bgm");
            
            handler.endDocument();
        } catch( SAXRuntimeException sre ) {
            throw sre.e;
        }
    }
    
    private void writeClass( ClassItem item ) {
        writer.start("class",new String[]{
            "name",item.getType().fullName()});
        
        if(item.getSuperClass()!=null)
            writer.element("extends",new String[]{
                "name",item.getSuperClass().name});
        
        // print field summary
        writer.start("field-summary");
        FieldUse[] fus = item.getDeclaredFieldUses();
        for( int i=0; i<fus.length; i++ ) {
            FieldUse fu = fus[i];
            
            Vector vec = new Vector();
            vec.add("name");
            vec.add(fu.name);

            vec.add("type");
            vec.add(fu.type.name());
            
            vec.add("occurs");
            vec.add(fu.multiplicity.toString());
            
            if( fu.getRealization()!=null ) {
                vec.add("realization");
                vec.add(fu.getRealization().getClass().getName());
            }
            
            writer.element("field", (String[])vec.toArray(new String[0]));
        }
        writer.end("field-summary");
        
        if(!signatureOnly)
            // print the body
            patternWriter.visitUnary(item.exp);
        writer.end("class");
    }
    
    private void writeInterface( InterfaceItem item ) {
        writer.start("interface",new String[]{
            "name",item.name});
        patternWriter.visitUnary(item.exp);
        writer.end("interface");
    }
    
    private final PatternWriter patternWriter = new SmartWriter(this);
    
    private class SmartWriter extends PatternWriter implements JavaItemVisitor {
        
        SmartWriter( Context context ) { super(context); }
        
        public void onRef( ReferenceExp exp ) {
            if(exp.name==null) {
                // ignore unnamed ReferenceExp
                exp.exp.visit(this);
            } else {
                this.writer.start("ref",new String[]{"name",exp.name});
                exp.exp.visit(this);
                this.writer.end("ref");
            }
        }
        
        public void onOther( OtherExp exp ) {
            if( exp instanceof JavaItem ) {
                ((JavaItem)exp).visitJI(this);
                return;
            }
            
            if( exp instanceof ClassCandidateItem ) {
                
                boolean isNew = candidates.add(exp);
                
                this.writer.start("class-candidate",new String[]{
                    isNew?"name":"ref",
                    ((ClassCandidateItem)exp).name});
                if( isNew )
                    exp.exp.visit(this);
                this.writer.end("class-candidate");
                return;
            }
            
            exp.exp.visit(this);    // ignore other OtherExps
        }
        
        public Object onClass( ClassItem item ) {
            this.writer.element("class-ref",new String[]{"name",item.name});
            return null;
        }
        
        public Object onInterface( InterfaceItem item ) {
            this.writer.element("interface-ref",new String[]{"interface",item.name});
            return null;
        }
        
        public Object onExternal( ExternalItem item ) {
            this.writer.start("external",new String[]{"type",item.toString()});
            writeNameClass(item.elementName);
            this.writer.end("external");
            return null;
        }
        
        /** Gets the class name of a given object. */
        private String getClassName(Object o) {
            String name = o.getClass().getName();
            int idx = name.lastIndexOf('.');
            if (idx < 0)
                return name;
            else
                return name.substring(idx + 1);
        }

        public Object onPrimitive(PrimitiveItem item) {
            this.writer.start("primitive", new String[] { "type", item.xducer.toString()});
            visitUnary(item.exp);
            this.writer.end("primitive");
            return null;
        }

        public Object onSuper(SuperClassItem item) {
            this.writer.start("superClass");
            visitUnary(item.exp);
            this.writer.end("superClass");
            return null;
        }

        public Object onIgnore(IgnoreItem item) {
            this.writer.start("ignore");
            //            visitUnary(item.exp);
            this.writer.end("ignore");
            return null;
        }

        public Object onField(FieldItem item) {
            Vector vec = new Vector();
            vec.add("name");
            vec.add(item.name);
            if (item.defaultValues != null) {
                // TODO: print more information about the specified default value.
                vec.add("hasDefaultValue");
                vec.add("true");
            }
            this.writer.start("field", (String[])vec.toArray(new String[vec.size()]));
            visitUnary(item.exp);
            this.writer.end("field");
            return null;
        }
    };
    
    
    
    /**
     * Serializes Name Class into XML.
     */
    public void writeNameClass( NameClass nc ) {
        nc.visit( new NameClassVisitor() {
            public Object onAnyName(AnyNameClass nc) {
                writer.element("anyName");
                return null;
            }
            public Object onNsName(NamespaceNameClass nc) {
                writer.element("nsName",new String[]{"ns",nc.namespaceURI});
                return null;
            }
            public Object onNot(NotNameClass nc) {
                writer.start("not");
                nc.child.visit(this);
                writer.end("not");
                return null;
            }
            public Object onSimple(SimpleNameClass nc) {
                if(noNS)    writer.start("name");
                else        writer.start("name",new String[]{"ns",nc.namespaceURI});
                writer.characters(nc.localName);
                writer.end("name");
                return null;
            }
            public Object onDifference(DifferenceNameClass nc) {
                writer.start("difference");
                nc.nc1.visit(this);
                nc.nc2.visit(this);
                writer.end("difference");
                return null;
            }
            public Object onChoice( ChoiceNameClass nc ) {
                writer.start("choice");
                processChoice(nc);
                writer.end("choice");
                return null;
            }
                    
            private void processChoice( ChoiceNameClass nc ) {
                Stack s = new Stack();
                s.push(nc.nc1);
                s.push(nc.nc2);
                    
                while(!s.empty()) {
                    NameClass n = (NameClass)s.pop();
                    if(n instanceof ChoiceNameClass ) {
                        s.push( ((ChoiceNameClass)n).nc1 );
                        s.push( ((ChoiceNameClass)n).nc2 );
                        continue;
                    }
                        
                    n.visit(this);
                }
            }
        });
    }
}
