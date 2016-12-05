/**
 * OSTFALIA CONFIDENTIAL
 *
 * 2010 - 2013 Ostfalia University of Applied Sciences All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Ostfalia University of Applied Sciences and its suppliers, if any. The
 * intellectual and technical concepts contained herein are proprietary to
 * Ostfalia University of Applied Sciences and its suppliers and may be covered
 * by U.S. and Foreign Patents, patents in process, and are protected by trade
 * secret or copyright law. Dissemination of this information or reproduction of
 * this material is strictly forbidden unless prior written permission is
 * obtained from Ostfalia University of Applied Sciences.
 */



package sonia.portlet.dbexport;

//~--- non-JDK imports --------------------------------------------------------

import sonia.commons.manifest.Manifest;
import sonia.commons.manifest.ManifestException;
import sonia.commons.manifest.ManifestSection;
import sonia.commons.manifest.Manifests;

//~--- JDK imports ------------------------------------------------------------

//import sonia.portal.useradm.util.Faces;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
@ManagedBean
@RequestScoped
public class ManifestBean
{

  /** Field description */
  private static final String ATRRIBUTE_NAME = "Sonia-Name";

  /** Field description */
  private static final String ATTRIBUTE_BUILD_DATE = "Sonia-Build-Timestamp";

  /** Field description */
  private static final String ATTRIBUTE_BUILD_NUMBER = "Sonia-Build-Number";

  /** Field description */
  private static final String ATTRIBUTE_REVISION = "Sonia-Revision";

  /** Field description */
  private static final String ATTRIBUTE_VERSION = "Sonia-Version";

  /** Field description */
  private static final String RESOURCE_PATH =
    "/".concat(Manifest.RESOURCE_PATH);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @throws IOException
   */
  public void init(HttpServletRequest request) throws IOException
  {

    System.out.println(ManifestBean.class.getResource(RESOURCE_PATH));

    /*
     * InputStream inputStream = this.getClass().getResourceAsStream(RESOURCE_PATH);
     *
     *
     * int c;
     * while ((c=inputStream.read())>=0)
     * {
     * System.err.write(c);
     * }
     *
     * inputStream.close();
     */

    // manifest = Manifests.readFromStream(inputStream).getMainSection();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Date getBuildDate()
  {
    return getManifest().getAttributeMavenDateValue(ATTRIBUTE_BUILD_DATE);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Integer getBuildNumber()
  {
    return getManifest().getAttributeIntegerValue(ATTRIBUTE_BUILD_NUMBER);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBuildTimestamp()
  {
    return getManifest().getAttributeValue(ATTRIBUTE_BUILD_DATE);
  }

  /**
   * Method description
   *
   *
   *
   * @return
   */
  public ManifestSection getManifest()
  {
    if (manifest == null)
    {
      try
      {
        if (manifestUrl == null)
        {
          manifestUrl = getManifestURL();
        }

        if (manifestUrl != null)
        {
          manifest = Manifests.readFromUrl(manifestUrl).getMainSection();
        }
        else
        {
          throw new ManifestException("could not find manifest");
        }
      }
      catch (IOException ex)
      {
        throw new ManifestException("could not read manifest", ex);
      }
    }

    return manifest;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return getManifest().getAttributeValue(ATRRIBUTE_NAME);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRevision()
  {
    return getManifest().getAttributeValue(ATTRIBUTE_REVISION);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getVersion()
  {
    return getManifest().getAttributeValue(ATTRIBUTE_VERSION);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws MalformedURLException
   */

/*  public boolean isPrivileged()
  {
     return Faces.getInstance().getPermissionChecker().isCompanyAdmin();
  }
*/

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws MalformedURLException
   */
  private URL getManifestURL() throws MalformedURLException
  {
    return FacesContext.getCurrentInstance().getExternalContext().getResource(
      RESOURCE_PATH);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ManifestSection manifest;

  /** Field description */
  private URL manifestUrl;
}
