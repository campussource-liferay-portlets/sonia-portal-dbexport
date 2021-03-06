package sonia.portlet.dbexport;

//~--- non-JDK imports --------------------------------------------------------

import com.liferay.faces.bridge.GenericLiferayFacesPortlet;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

/**
 *
 * @author th
 */

public class DbExportPortlet extends GenericLiferayFacesPortlet
{

  /** Field description */
  private static final Log LOGGER =
    LogFactoryUtil.getLog(DbExportPortlet.class);

  //~--- methods --------------------------------------------------------------

  @Override
  public void serveResource(ResourceRequest request,
    ResourceResponse response) throws PortletException, IOException
  {
    LOGGER.debug("db export serve resource");

    String doExport = request.getParameter("doExport");

    if ("true".equals(doExport))
    {

      PortletSession session = request.getPortletSession();

      DbExportPreferencesBean preferences =
        (DbExportPreferencesBean) session.getAttribute("preferences");

      if (preferences != null)
      {

        LOGGER.debug("preferences uuid = " + preferences.getPreferencesUuid());

        response.setContentType("text/csv;charset="
          + preferences.getExportCharset());
        response.setProperty("Content-Disposition",
          "filename=" + preferences.getFormattedExportFilename());

        try (OutputStream output = response.getPortletOutputStream())
        {
          preferences.csvExport(output);
        }
      }
      else
      {
        LOGGER.error("DBEXPORT_PREFERENCES not set");
      }
    }
    else
    {
      super.serveResource(request, response);
    }
  }
}
