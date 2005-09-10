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
