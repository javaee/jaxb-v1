/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: InterleaveDispatcher.java,v 1.1 2004-06-25 21:15:22 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import java.util.Iterator;

import javax.xml.bind.ValidationEvent;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.unmarshaller.Tracer;

/**
 * Splits the unmarshalling events to bracnhes to support
 * XML Schema's &lt;all> and RELAX NG's &lt;interleave>
 * 
 * <p>
 * This class will be extended by the generated code.
 * 
 * @optionalRuntime
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class InterleaveDispatcher implements UnmarshallingEventHandler {
    
    /** Parent context. */
    private final UnmarshallingContext parent;
    
    /** Branches of an interleave. */
    protected final Site[] sites;
    
    /** Set to true while all the branches are joining. */
    private boolean isJoining;
    
    /** Counts the depth of the element nesting. */
    private int nestLevel = 0;
    
    /** When nestLevel>0, all the events shall be sent to this branch. */
    private Site currentSite;

    
    protected InterleaveDispatcher( UnmarshallingContext context, int size ) {
        this.parent = context;
        sites = new Site[size];
        for( int i=0; i<size; i++ )
            sites[i] = new Site();
    }
    
    protected void init( UnmarshallingEventHandler[] handlers ) {
        for( int i=0; i<handlers.length; i++ )
            sites[i].pushContentHandler(handlers[i],0);
    }
    

// abstract methods
    /**
     * Returns the branch number that consumes the given element,
     * or -1 if the name is not recognized. 
     */
    protected abstract int getBranchForElement( String uri, String local );
    
    /**
     * Returns the branch number that consumes the given attribute,
     * or -1 if the name is not recognized. 
     */
    protected abstract int getBranchForAttribute( String uri, String local );
    
    /**
     * Returns the branch number that consumes the text events,
     * or -1 if no branch is expected to consume it. 
     */
    protected abstract int getBranchForText();
    
    
    
    
    
    public Object owner() {
        if( nestLevel>0 )
            return currentSite.getCurrentHandler().owner();
        else
            throw new JAXBAssertionError();
    }

    public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        if( nestLevel++==0 ) {
            int idx = getBranchForElement(uri, local);
            if(idx==-1) {
                // unknown element. revert to parent.
                joinByEnterElement(null, uri, local, qname, atts);
                return;
            }
            currentSite = sites[idx];
        }
            
        currentSite.getCurrentHandler().enterElement(uri, local, qname, atts);
    }
    private void joinByEnterElement( Site source, String uri, String local, String qname, Attributes atts ) throws SAXException {
        if(isJoining)       return; // during a join, child branches send us tokens we sent them. ignore.
        isJoining = true;
        
        // send the token to all the other branches.
        // since they don't recognize this token, it should try to move
        // to the final state (or report an error)
        for( int i=0; i<sites.length; i++ )
            if( sites[i]!=source )
                sites[i].getCurrentHandler().enterElement(uri, local, qname, atts);
        
        // revert to the parent
        parent.popContentHandler();
        parent.getCurrentHandler().enterElement(uri,local,qname,atts);
    }


    public void leaveElement(String uri, String local, String qname) throws SAXException {
        if( nestLevel==0 )
            joinByLeaveElement(null,uri,local,qname);
        else {
            currentSite.getCurrentHandler().leaveElement(uri,local,qname);
            // leaveElement invocation might cause some unprocessed attributes
            // to be handled. Therefore, while the execution is in the leaveElement,
            // we need to let the branch maintain the control.
            nestLevel--;
        }
    }
    private void joinByLeaveElement(Site source,String uri, String local, String qname) throws SAXException {
        if(isJoining)       return; // during a join, child branches send us tokens we sent them. ignore.
        isJoining = true;
        
        // send the token to all the other branches.
        // since they don't recognize this token, it should try to move
        // to the final state (or report an error)
        for( int i=0; i<sites.length; i++ )
            if( sites[i]!=source )
                sites[i].getCurrentHandler().leaveElement(uri,local,qname);
        
        // revert to the parent
        parent.popContentHandler();
        parent.getCurrentHandler().leaveElement(uri,local,qname);
    }


    public void text(String s) throws SAXException {
        if( nestLevel==0 ) {
            int idx = getBranchForText();
            if(idx==-1) {
                if( s.trim().length()==0 ) {
                    // if ignorable, just ignore.
                } else {
                    joinByText(null,s);
                }
                return;
            }
            currentSite = sites[idx];
        }
        
        currentSite.getCurrentHandler().text(s);
    }
    private void joinByText(Site source, String s) throws SAXException {
        if(isJoining)       return; // during a join, child branches send us tokens we sent them. ignore.
        isJoining = true;
        
        // send the token to all the other branches.
        // since they don't recognize this token, it should try to move
        // to the final state (or report an error)
        for( int i=0; i<sites.length; i++ )
            if( sites[i]!=source )
                sites[i].getCurrentHandler().text(s);
        
        // revert to the parent
        parent.popContentHandler();
        parent.getCurrentHandler().text(s);
    }


    public void enterAttribute(String uri, String local, String qname) throws SAXException {
        if( nestLevel++==0 ) {
            int idx = getBranchForAttribute(uri, local);
            if(idx==-1) {
                // unknown element. revert to parent.
                joinByEnterAttribute(null, uri, local, qname);
                return;
            }
            currentSite = sites[idx];
        }
            
        currentSite.getCurrentHandler().enterAttribute(uri, local, qname);
    }
    private void joinByEnterAttribute( Site source, String uri, String local, String qname ) throws SAXException {
        if(isJoining)       return; // during a join, child branches send us tokens we sent them. ignore.
        isJoining = true;
        
        // send the token to all the other branches.
        // since they don't recognize this token, it should try to move
        // to the final state (or report an error)
        for( int i=0; i<sites.length; i++ )
            if( sites[i]!=source )
                sites[i].getCurrentHandler().enterAttribute(uri, local, qname);
        
        // revert to the parent
        parent.popContentHandler();
        parent.getCurrentHandler().enterAttribute(uri,local,qname);
    }


    public void leaveAttribute(String uri, String local, String qname) throws SAXException {
        if( nestLevel==0 )
            joinByLeaveAttribute(null,uri,local,qname);
        else {
            nestLevel--;
            currentSite.getCurrentHandler().leaveAttribute(uri,local,qname);
        }
    }
    private void joinByLeaveAttribute(Site source,String uri, String local, String qname) throws SAXException {
        if(isJoining)       return; // during a join, child branches send us tokens we sent them. ignore.
        isJoining = true;
        
        // send the token to all the other branches.
        // since they don't recognize this token, it should try to move
        // to the final state (or report an error)
        for( int i=0; i<sites.length; i++ )
            if( sites[i]!=source )
                sites[i].getCurrentHandler().leaveAttribute(uri,local,qname);
        
        // revert to the parent
        parent.popContentHandler();
        parent.getCurrentHandler().leaveAttribute(uri,local,qname);
    }

    public void leaveChild(int nextState) throws SAXException {
        // assertion failed. since we don't launch any child
        // this method shall never be called.
        throw new JAXBAssertionError();
    }
    
    
//    /** triggers join. */    
//    private final UnmarshallingEventHandler nullHandler = new UnmarshallingEventHandler() {
//        public Object owner() {
//            return null;
//        }
//
//        public void enterElement(String uri, String local, String qname, Attributes atts) throws UnreportedException {
//            join
//        }
//        public void leaveElement(String uri, String local, String qname) throws UnreportedException {
//        }
//        public void text(String s) throws UnreportedException {
//        }
//        public void enterAttribute(String uri, String local, String qname) throws UnreportedException {
//        }
//        public void leaveAttribute(String uri, String local, String qname) throws UnreportedException {
//        }
//        public void leaveChild(int nextState) throws UnreportedException {
//        }
//    };
    
    
    /**
     * This implementation will be passed to branches as
     * the {@link UnmarshallingContext} implementation.
     * 
     * Used to maintain separate handler stacks for each branch.
     * 
     * As an {@link UnmarshallingEventHandler}, this object
     * triggers join.
     */
    private class Site implements UnmarshallingContext, UnmarshallingEventHandler {
        
        // handler stack implemented as an array        
        private UnmarshallingEventHandler[] handlers = new UnmarshallingEventHandler[8];
        private int[] mementos = new int[8];
        private int handlerLen=0;
        
        private Site() {
            pushContentHandler(this, 0);
        }
    
        public void pushContentHandler( UnmarshallingEventHandler handler, int memento ) {
            if(handlerLen==handlers.length) {
                // expand buffer
                UnmarshallingEventHandler[] h = new UnmarshallingEventHandler[handlerLen*2];
                int[] m = new int[handlerLen*2];
                System.arraycopy(handlers,0,h,0,handlerLen);
                System.arraycopy(mementos,0,m,0,handlerLen);
                handlers = h;
                mementos = m;
            }
            handlers[handlerLen] = handler;
            mementos[handlerLen] = memento;
            handlerLen++;
        }
    
        public void popContentHandler() throws SAXException {
            handlerLen--;
            handlers[handlerLen]=null;  // this handler is removed
            getCurrentHandler().leaveChild(mementos[handlerLen]);
        }

        public UnmarshallingEventHandler getCurrentHandler() {
            return handlers[handlerLen-1];
        }

        
        // UnmarshallingEventHandler impl. triggers the join operation
        public Object owner() { return null; }
        public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
            joinByEnterElement(this,uri,local,qname,atts);
        }
        public void leaveElement(String uri, String local, String qname) throws SAXException {
            joinByLeaveElement(this,uri,local,qname);
        }
        public void enterAttribute(String uri, String local, String qname) throws SAXException {
            joinByEnterAttribute(this,uri,local,qname);
        }
        public void leaveAttribute(String uri, String local, String qname) throws SAXException {
            joinByLeaveAttribute(this,uri,local,qname);
        }
        public void text(String s) throws SAXException {
            joinByText(this,s);
        }
        public void leaveChild(int nextState) throws SAXException {
        }
        
        
        // the rest of the methods are just delegations for UnmarshallingContext
        
        public void addPatcher(Runnable job) {
            parent.addPatcher(job);
        }

        public String addToIdTable(String id) {
            return parent.addToIdTable(id);
        }

        public void consumeAttribute(int idx) throws SAXException {
            parent.consumeAttribute(idx);
        }

        public String eatAttribute(int idx) throws SAXException {
            return parent.eatAttribute(idx);
        }

        public int getAttribute(String uri, String name) {
            return parent.getAttribute(uri, name);
        }

        public String getBaseUri() {
            return parent.getBaseUri();
        }

        public GrammarInfo getGrammarInfo() {
            return parent.getGrammarInfo();
        }

        public Locator getLocator() {
            return parent.getLocator();
        }

        public String getNamespaceURI(String prefix) {
            return parent.getNamespaceURI(prefix);
        }

        public Object getObjectFromId(String id) {
            return parent.getObjectFromId(id);
        }

        public String getPrefix(String namespaceURI) {
            return parent.getPrefix(namespaceURI);
        }

        public Iterator getPrefixes(String namespaceURI) {
            return parent.getPrefixes(namespaceURI);
        }

        public Tracer getTracer() {
            return parent.getTracer();
        }

        public Attributes getUnconsumedAttributes() {
            return parent.getUnconsumedAttributes();
        }

        public void handleEvent(ValidationEvent event, boolean canRecover) throws SAXException {
            parent.handleEvent(event,canRecover);
        }

        public boolean isNotation(String arg0) {
            return parent.isNotation(arg0);
        }

        public boolean isUnparsedEntity(String arg0) {
            return parent.isUnparsedEntity(arg0);
        }

        public void popAttributes() {
            parent.popAttributes();
        }

        public void pushAttributes(Attributes atts,boolean collectTextFlag) {
            parent.pushAttributes(atts,collectTextFlag);
        }

        public String resolveNamespacePrefix(String prefix) {
            return parent.resolveNamespacePrefix(prefix);
        }

        public String[] getNewlyDeclaredPrefixes() {
            return parent.getNewlyDeclaredPrefixes();
        }

        public String[] getAllDeclaredPrefixes() {
            return parent.getAllDeclaredPrefixes();
        }

    }
}
