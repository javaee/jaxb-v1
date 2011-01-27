/*
 * $Id: ParseConversionEvent.java,v 1.1 2002/09/04 15:29:35 ryans Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.xml.bind;

/**
 * This event indicates that a problem was encountered while converting a
 * string from the XML data into a value of the target Java data type.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li><li>Joe Fialli, Sun Microsystems, Inc.</li></ul> 
 * @version $Revision: 1.1 $
 * @see ValidationEvent
 * @see ValidationEventHandler
 * @see Unmarshaller
 * @since JAXB1.0
 */
public interface ParseConversionEvent extends ValidationEvent {

}
