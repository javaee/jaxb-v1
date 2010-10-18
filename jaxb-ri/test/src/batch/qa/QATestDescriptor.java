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

package batch.qa;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import batch.core.Util;
import batch.core.om.Instance;
import batch.core.om.Schema;
import batch.core.om.TestDescriptor;

/**
 * {@link TestDescriptor} with additional information for
 * ECMAScript based tests.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class QATestDescriptor extends TestDescriptor {
    
    /**
     * Scripts that are run against schemas.
     */
    public final Script[] runOnceScripts;
    
    /**
     * Scripts that are run against instances.
     */
    public final Script[] perInstanceScripts;
    
    
    public QATestDescriptor(File _outDir, Schema _schema, Instance[] _instances, Script[] _runOnceScripts, Script[] _perInstanceScripts ) {
        super(_outDir, _schema, _instances);
        this.runOnceScripts = _runOnceScripts;
        this.perInstanceScripts = _perInstanceScripts;
    }

    public QATestDescriptor(File metaFile) throws Exception {
        super(metaFile);

        Document doc = Util.loadXML(metaFile);
        Element testSpec = doc.getRootElement();
        
        List runOnce = new ArrayList();
        List normal = new ArrayList();
        
        {// run sciprts with "run='once'" attribute.
            Iterator jtr = testSpec.elementIterator("script");
            while(jtr.hasNext()) {
                Element script = (Element)jtr.next();
                URL url = new URL( testSpecUrl, script.attributeValue("href") );
                Script s = new Script(url);
                
                if( "once".equals(script.attributeValue("run")) ) {
                    runOnce.add(s);
                } else {
                    normal.add(s);
                }
            }
        }
        
        this.runOnceScripts = (Script[]) runOnce.toArray(new Script[runOnce.size()]);
        this.perInstanceScripts = (Script[]) normal.toArray(new Script[normal.size()]);
        
        if( perInstanceScripts.length!=0 && instances.length==0 )
            throw new IllegalArgumentException(metaFile+" has per-instance scripts but no instance");

        // this might simply mean that the test is written for something other than QA.
//        if( perInstanceScripts.length==0 && instances.length!=0 )
//            throw new IllegalArgumentException(metaFile+" has instances but no per-instance script");
    }

}
