/*
 * @(#)$Id: DataSetBuilder.java,v 1.1 2004-06-25 21:13:07 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance.visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;

/**
 * Builds a {@link CategoryDataset} from data points.
 * 
 * <p>
 * This code is necessary because {@link DefaultCategoryDataset}
 * is senstive to the order the datapoints were added, and that
 * breaks our model. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class DataSetBuilder {
    
    private List lst = new ArrayList();
    
    public void add( Number value, Comparable rowKey, Comparable columnKey ) {
        lst.add(value);
        lst.add(rowKey);
        lst.add(columnKey);
    }
    
    public CategoryDataset build() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        
        TreeSet rows = new TreeSet();
        TreeSet cols = new TreeSet();
        for( int i=0; i<lst.size(); i+=3 ) {
            rows.add( lst.get(i+1) );
            cols.add( lst.get(i+2) );
        }
        
        Comparable[] r = (Comparable[]) rows.toArray(new Comparable[rows.size()]);
        Comparable[] c = (Comparable[]) cols.toArray(new Comparable[cols.size()]);
        
        // insert rows and columns in the right order
        for( int i=0; i<r.length; i++ )
            ds.setValue(null,r[i],c[0]);
        for( int i=0; i<c.length; i++ )
            ds.setValue(null,r[0],c[i]);
        
        for( int i=0; i<lst.size(); i+=3 )
            ds.addValue( (Number)lst.get(i+0), (Comparable)lst.get(i+1), (Comparable)lst.get(i+2) );
        return ds;
    }
}
