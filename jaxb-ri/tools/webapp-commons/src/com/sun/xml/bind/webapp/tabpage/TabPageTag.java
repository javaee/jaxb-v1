/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.webapp.tabpage;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.sun.xml.bind.webapp.AbstractTagImpl;

/**
 * Tab page. Used in conjunction with {@link TabSheetTag}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TabPageTag extends AbstractTagImpl {

    private String name;
    
    private boolean defaultPage;
    
    
    /**
     * JSP "name" attribute.
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * JSP "default" attribute.
     */
    public void setDefault( String pg ) {
        this.defaultPage = true;
    }


    public int startTag() throws IOException {
        
        TabSheetTag parent = (TabSheetTag)this.parent;
        
        parent.incrementPageCount();

        JspWriter w = context.getOut();
        
        if( parent.getPass()==Pass.header ) {
            w.write("<td align=center ");
            if( isActive() )
                w.write("bordercolor=#ffffff>");
            else
                w.write("bgcolor="+parent.getShadowColor()+">");
                
            String url = ((HttpServletRequest)context.getRequest()).getRequestURI();  
            
            w.write("<a href='"+url+"?page="+URLEncoder.encode(name,"UTF-8")+"' style='text-decoration:none'>");
            w.write( "&nbsp;"+name+"&nbsp;" );
            w.write("</a>");
            
            w.write("</td>");
        } else {
            if( isActive() )
                return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    public int endTag() {
        return EVAL_PAGE;
    }

    /**
     * Returns true if the page is active.
     */
    public boolean isActive() {
        String param = context.getRequest().getParameter("page");

        return name.equals(param) || (param==null && defaultPage);
    }


}
