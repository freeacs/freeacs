package com.github.freeacs.web.help;

import com.github.freeacs.web.Page;
import freemarker.template.TemplateException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlets responsibility is to maintain help/tool dialog state and to serve the help
 * contents.
 *
 * <p>It can be called in four ways:
 *
 * <ul>
 *   <li>help?page=[id] (ex. "help?page=search")
 *   <li>help?cmd=getactivedialog
 *   <li>help?cmd=setactivedialog
 *   <li>help?cmd=setactivetool
 * </ul>
 *
 * @author Jarl Andre Hubenthal
 */
public class HelpServlet extends HttpServlet {
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Do get.
   *
   * @param request the request
   * @param response the response
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if ("getactivedialog".equalsIgnoreCase(request.getParameter("cmd"))) {
      String helpDialog = (String) request.getSession().getAttribute("activehelpdialog");
      response.setContentType("text/plain");
      if (helpDialog != null && !helpDialog.trim().isEmpty()) {
        response.getWriter().print(helpDialog);
        response.getWriter().close();
      }
      return;
    }

    if ("setactivedialog".equalsIgnoreCase(request.getParameter("cmd"))) {
      String dialogTitle = request.getParameter("title");
      if (dialogTitle == null || dialogTitle.trim().isEmpty()) {
        request.getSession().removeAttribute("activehelpdialog");
      } else {
        request.getSession().setAttribute("activehelpdialog", dialogTitle);
      }
      response.setContentType("text/plain");
      response.getWriter().println("ok");
      response.getWriter().close();
      return;
    } else if ("setactivetool".equalsIgnoreCase(request.getParameter("cmd"))) {
      String dialogTitle = request.getParameter("title");
      if (dialogTitle == null || dialogTitle.trim().isEmpty()) {
        request.getSession().removeAttribute("activetooldialog");
      } else {
        request.getSession().setAttribute("activetooldialog", dialogTitle);
      }
      response.setContentType("text/plain");
      response.getWriter().println("ok");
      response.getWriter().close();
      return;
    }
    String page = request.getParameter("page");
    String html = null;
    if (page != null) {
      try {
        Page p = Page.getById(page);
        if (p != Page.NONE) {
          html = HelpPage.getHTMLForPageByClass(p);
        } else {
          html = HelpPage.getHTMLForPageByResourcePath("pages/" + page + "/" + page);
        }
      } catch (TemplateException e1) {
        throw new ServletException("Could not retrieve help file", e1);
      }
    } else {
      html = "The [page] parameter was not supplied in the url";
    }
    if (html != null) {
      try {
        response.getWriter().println(html);
        response.getWriter().close();
      } catch (Throwable e) {
        throw new ServletException("Could not parse template", e);
      }
    }
  }

  /**
   * Do post.
   *
   * @param request the request
   * @param response the response
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }
}
