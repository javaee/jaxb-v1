/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
