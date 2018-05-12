package com.github.freeacs.web.app.util;

import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;


/**
 * Subclass of {@link FreeMarkerViewResolver} that supports.
 *
 * {@link DecoratingFreeMarkerView}, i.e. FreeMarker templates that can be
 * decorated with a layout template.
 * @author Stefan Haberl
 * @since 1.0
 */
public class DecoratingFreeMarkerViewResolver extends FreeMarkerViewResolver {

  /** The decorating template. */
  private String decoratingTemplate;

  /**
   * JavaBean constructor.
   */
  public DecoratingFreeMarkerViewResolver() {
    setViewClass(requiredViewClass());
    setExposeSpringMacroHelpers(true);
  }

  /**
   * Sets the name of the layout template which will be used to decorate the
   * single Freemarker pages.
   * 
   * @param decoratingTemplate
   *          The name of the decorating template
   */
  public void setDecoratingTemplate(String decoratingTemplate) {
    this.decoratingTemplate = decoratingTemplate;
  }

  /**
   * Requires {@link DecoratingFreeMarkerView}.
   *
   * @return the class
   */
  @Override
  protected Class<?> requiredViewClass() {
    return DecoratingFreeMarkerView.class;
  }

  /* (non-Javadoc)
   * @see org.springframework.web.servlet.view.AbstractTemplateViewResolver#buildView(java.lang.String)
   */
  @Override
  protected AbstractUrlBasedView buildView(String viewName) throws Exception {
    DecoratingFreeMarkerView view = (DecoratingFreeMarkerView) super.buildView(viewName);
    view.setDecoratingTemplate(this.decoratingTemplate);
    return view;
  }

}
