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
