/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * {@link List} with modification detection capability.
 * 
 * <p>
 * This wrapper class exposes two additional methods
 * <code>setModified</code> and <code>isModified</code>
 * to check whether the contents of a list is modified.
 * 
 * <p>
 * I originally thought the modCount field of AbstractList
 * is suffice to implement this capability, but a close look
 * at the source code reveals that modCount is not updated
 * when value is modified without a structural change.
 * This includes modifying a value of a list, for example.
 * 
 * <p>
 * Thus unfortunately we need to trap the calls to all
 * the mutation methods of List.
 * 
 * <p>
 * Note that Iterator implementation of AbstractList
 * modifies list directly without using a public method
 * of List. Thus we also need to wrap Iterators.
 * 
 * @since JAXB1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ProxyListImpl implements List, java.io.Serializable {
    final static long serialVersionUID=1L;
    
    /** The actual storage. */
    protected final List core;
    
    public ProxyListImpl() { this(new LinkedList()); }
    public ProxyListImpl(List c) { core=c; }

    public abstract void setModified( boolean f );
    
    public void add(int index, Object element) {
        setModified(true);
        core.add(index, element);
    }
    public boolean add(Object o) {
        setModified(true);
        return core.add(o);
    }
    public boolean addAll(Collection c) {
        setModified(true);
        return core.addAll(c);
    }
    public boolean addAll(int index, Collection c) {
        setModified(true);
        return core.addAll(index, c);
    }
    public Object set(int index, Object element) {
        setModified(true);
        return core.set(index,element);
    }
//    public void addFirst(Object o) {
//        isModified = true;
//        core.addFirst(o);
//    }
//    public void addLast(Object o) {
//        isModified = true;
//        core.addLast(o);
//    }
    public void clear() {
        setModified(true);
        core.clear();
    }
    public Object remove(int index) {
        setModified(true);
        return core.remove(index);
    }
    public boolean remove(Object o) {
        setModified(true);
        return core.remove(o);
    }
//    public Object removeFirst() {
//        isModified = true;
//        return core.removeFirst();
//    }
//    public Object removeLast() {
//        isModified = true;
//        return core.removeLast();
//    }
    public Iterator iterator() {
        setModified(true);
        return core.iterator();
    }
    public ListIterator listIterator(int index) {
        setModified(true);
        return core.listIterator(index);
    }
    public ListIterator listIterator() {
        setModified(true);
        return core.listIterator();
    }
    public boolean removeAll(Collection c) {
        setModified(true);
        return core.removeAll(c);
    }
    public boolean retainAll(Collection c) {
        setModified(true);
        return core.retainAll(c);
    }
    
    /**
     * Iterator wrapper
     */
    class Itr implements Iterator
    {
        private Iterator base;
        Itr( Iterator base ) { this.base=base; }
        public boolean hasNext() {
            return base.hasNext();
        }
        public Object next() {
            return base.next();
        }
        public void remove() {
            base.remove();
            setModified(true);
        }
    }
    
    class ListItr extends Itr implements ListIterator
    {
        private ListIterator itr;
        ListItr( ListIterator base ) { super(base);this.itr=base; }
        public void add(Object o) {
        }
        public boolean hasPrevious() {
            return itr.hasPrevious();
        }
        public int nextIndex() {
            return itr.nextIndex();
        }
        public Object previous() {
            return itr.previous();
        }
        public int previousIndex() {
            return itr.previousIndex();
        }
        public void set(Object o) {
            itr.set(o);
            setModified(true);
        }

    }
    
    // const methods
    
    public boolean contains(Object o) {
        return core.contains(o);
    }
    public boolean containsAll(Collection c) {
        return core.containsAll(c);
    }
    public Object get(int index) {
        return core.get(index);
    }
    public int indexOf(Object o) {
        return core.indexOf(o);
    }
    public boolean isEmpty() {
        return core.isEmpty();
    }
    public int lastIndexOf(Object o) {
        return core.lastIndexOf(o);
    }
    public int size() {
        return core.size();
    }
    public List subList(int fromIndex, int toIndex) {
        final ProxyListImpl externalThis = this;
        
        return new ProxyListImpl(core.subList(fromIndex,toIndex)) {
            public void setModified(boolean f) {
                externalThis.setModified(true);
            }
        };
    }
    public Object[] toArray() {
        return core.toArray();
    }
    public Object[] toArray(Object[] a) {
        return core.toArray(a);
    }

    public boolean equals( Object o ) {
        return core.equals(o);
    }

    public int hashCode() {
        return core.hashCode();
    }
}
