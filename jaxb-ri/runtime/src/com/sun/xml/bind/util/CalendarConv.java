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
package com.sun.xml.bind.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Converts Calendar formatter as a string into a Calendar object.
 * 
 * This method is used when xs:date/xs:time/xs:dateTime are used
 * as constant properties.
 * 
 * @since JAXB1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class CalendarConv {
    /**
     * DateFormat used to convert a string into a Calendar object.
     */
    public static final DateFormat formatter =
        DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL, Locale.ENGLISH );

    public static Calendar createCalendar( String formattedDate ) {
        try {
            Calendar c = new GregorianCalendar();
            c.setTime(formatter.parse(formattedDate));
            return c;
        } catch( ParseException e ) {
            // since "formattedDate" is generated by the compiler,
            // we don't expect this error to happen
            throw new Error(e.getMessage());
        }
    }
}
