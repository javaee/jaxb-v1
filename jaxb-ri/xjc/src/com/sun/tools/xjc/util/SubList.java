/*
 * @(#)$Id: SubList.java,v 1.2 2005-09-10 18:20:47 kohsuke Exp $
 */

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
package com.sun.tools.xjc.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Sub-list implementation whose actual data store is backed by
 * another {@link List}.
 * 
 * <p>
 * The default {@link List#subList(int,int)} implementation requires
 * the original list to be unmodified while the sub list is being used,
 * but this implementation doesn't have such a restriction.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SubList extends AbstractList {
    private final List l;
    private final int offset;
    private int size;

    public SubList(List list, int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
        l = list;
        offset = fromIndex;
        size = toIndex - fromIndex;
    }

    public Object set(int index, Object element) {
        rangeCheck(index);
        return l.set(index+offset, element);
    }

    public Object get(int index) {
        rangeCheck(index);
        return l.get(index+offset);
    }

    public int size() {
        return size;
    }

    public void add(int index, Object element) {
        if (index<0 || index>size)
            throw new IndexOutOfBoundsException();
        l.add(index+offset, element);
        size++;
        modCount++;
    }

    public Object remove(int index) {
        rangeCheck(index);
        Object result = l.remove(index+offset);
        size--;
        modCount++;
        return result;
    }

    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    public boolean addAll(int index, Collection c) {
        if (index<0 || index>size)
            throw new IndexOutOfBoundsException(
                    "Index: "+index+", Size: "+size);
        int cSize = c.size();
        if (cSize==0)
            return false;

        l.addAll(offset+index, c);
        size += cSize;
        modCount++;
        return true;
    }

    public Iterator iterator() {
        return listIterator();
    }

    public ListIterator listIterator(final int index) {
        if (index<0 || index>size)
            throw new IndexOutOfBoundsException(
                    "Index: "+index+", Size: "+size);

        return new ListIterator() {
            private ListIterator i = l.listIterator(index+offset);

            public boolean hasNext() {
                return nextIndex() < size;
            }

            public Object next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public Object previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                size--;
                modCount++;
            }

            public void set(Object o) {
                i.set(o);
            }

            public void add(Object o) {
                i.add(o);
                size++;
                modCount++;
            }
        };
    }

    public List subList(int fromIndex, int toIndex) {
        return new SubList(this, fromIndex, toIndex);
    }

    private void rangeCheck(int index) {
        if (index<0 || index>=size)
            throw new IndexOutOfBoundsException("Index: "+index+
                    ",Size: "+size);
    }
}
