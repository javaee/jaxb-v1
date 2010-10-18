/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
