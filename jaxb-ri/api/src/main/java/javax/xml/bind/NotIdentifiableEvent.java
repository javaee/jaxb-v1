/*
 * $Id: NotIdentifiableEvent.java,v 1.1 2002/09/04 17:45:07 ryans Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.xml.bind;

/**
 * This event indicates that a problem was encountered resolving an ID/IDREF.
 * 
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li><li>Joe Fialli, Sun Microsystems, Inc.</li></ul> 
 * @version $Revision: 1.1 $
 * @see Validator
 * @see ValidationEventHandler
 * @since JAXB1.0
 */
public interface NotIdentifiableEvent extends ValidationEvent {

}
