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

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Implemented by the driver of the compiler engine to handle
 * errors found during the compiliation.
 * 
 * <p>
 * This class implements {@link ErrorHandler} so it can be
 * passed to anywhere where {@link ErrorHandler} is expected.
 * 
 * <p>
 * However, to make the error handling easy (and make it work
 * with visitor patterns nicely),
 * none of the methods on thi class throws {@link SAXException}.
 * Instead, when the compilation needs to be aborted,
 * it throws {@link AbortException}, which is unchecked.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ErrorReceiver  implements ErrorHandler {

//
//
// convenience methods for callers
//    
//
    /**
     * @param loc
     *      can be null if the location is unknown
     */
    public final void error( Locator loc, String msg ) {
        error( new SAXParseException(msg,loc) );
    }
    
    /**
     * @param loc
     *      can be null if the location is unknown
     */
    public final void warning( Locator loc, String msg ) {
        warning( new SAXParseException(msg,loc) );
    }
    
//
//
// ErrorHandler implementation, but can't throw SAXException
//
//
    public abstract void error(SAXParseException exception) throws AbortException;
    public abstract void fatalError(SAXParseException exception) throws AbortException;
    public abstract void warning(SAXParseException exception) throws AbortException;
    
    /**
     * Reports verbose messages to users.
     * 
     * This method can be used to report additional non-essential
     * messages. The implementation usually discards them
     * unless some specific debug option is turned on.
     */
    public abstract void info(SAXParseException exception) /*REVISIT:throws AbortException*/;

    /**
     * Reports a debug message to users.
     * 
     * @see #info(SAXParseException)
     */
    public final void debug( String msg ) {
        info( new SAXParseException(msg,null) );
    }

//
//
// convenience methods for derived classes
//
//
    
  /**
   * Returns the human readable string representation of the 
   * {@link org.xml.sax.Locator} part of the specified
   * {@link SAXParseException}.
   * 
   * @return  non-null valid object.
   */
  protected final String getLocationString( SAXParseException e ) {
      if(e.getLineNumber()!=-1 || e.getSystemId()!=null) {
          int line = e.getLineNumber();
          return Messages.format( Messages.LINE_X_OF_Y,
              line==-1?"?":Integer.toString( line ),
              getShortName( e.getSystemId() ) );
      } else {        
          return Messages.format( Messages.UNKNOWN_LOCATION );
      }
  }
    
  /** Computes a short name of a given URL for display. */
  private String getShortName( String url ) {
      if(url==null)  
          return Messages.format( Messages.UNKNOWN_FILE );
        
      int idx;
        
      // system Id can be URL, so we can't use File.separator
      idx = url.lastIndexOf('/');
      if(idx!=-1)     return url.substring(idx+1);
      idx = url.lastIndexOf('\\');
      if(idx!=-1)     return url.substring(idx+1);
        
      return url;
  }

}

