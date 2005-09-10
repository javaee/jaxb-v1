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
package util;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

/**
 * Dumps first 5 validation events to the console.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ValidationEventDumper implements ValidationEventHandler {

    private int count=0;
    
    public boolean handleEvent(ValidationEvent v) {
        if( count++ < 5 )   dump(v);
        return true;    // continue validation to see if RI can recover from errors.
    }
    
    public static void dump( ValidationEvent e ) {
        System.out.println("ValidationErr: "+e.getMessage());
        System.out.println("  at :"+e.getLocator().getObject());
        if( e.getLinkedException()!=null ) {
            System.out.println("--linked exception--");
            e.getLinkedException().printStackTrace(System.out);
        }
    }

}
