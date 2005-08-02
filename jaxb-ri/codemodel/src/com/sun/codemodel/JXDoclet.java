/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * XDoclet.
 * 
 * @author Aleksei Valikov
 */
public class JXDoclet implements JGenerable {

  /**
   * Name of the XDoclet.
   */
  private final String name;

  /**
   * Parameters of the XDoclet.
   */
  private final Map params = new HashMap();

  /**
   * Constructs a new xdoclet with given name.
   * @param name name of the XDoclet.
   */
  JXDoclet(final String name) {
    this.name = name;
  }

  /**
   * @return Returns the params.
   */
  public Map getParams() {
    return params;
  }

  public JXDoclet addParam(final String param, final String comment) {
    params.put(param, comment);
    return this;
  }

  /**
   * Generates the XDoclet comment.
   */
  public void generate(final JFormatter f) {
    f.p(" * @" + name).nl();
    for (final Iterator iterator = params.entrySet().iterator(); iterator.hasNext();) {
      final Map.Entry param = (Map.Entry) iterator.next();
      f.p(" *     " + param.getKey() + "=\"" + param.getValue() + "\"").nl();
    }
  }
}
