package sonia.portlet.dbexport;

//~--- non-JDK imports --------------------------------------------------------

import com.liferay.faces.util.portal.WebKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.expando.model.ExpandoBridge;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import sonia.annotation.portlet.SoniaPortletPreference;
import sonia.annotation.portlet.SoniaPortletPreferencesHandler;

import sonia.portal.system.UserCredentials;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.PostConstruct;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

/**
 *
 * @author th
 */

@ManagedBean(name = "preferences")
@SessionScoped
public class DbExportPreferencesBean implements Serializable
{

  /** Field description */
  private static final Log LOGGER =
    LogFactoryUtil.getLog(DbExportPreferencesBean.class);

  /** Field description */
  private static final long serialVersionUID = 9019397898375027512L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param outputStream
   */
  public void csvExport(OutputStream outputStream)
  {
    try (PrintStream out = new PrintStream(outputStream, true, exportCharset))
    {
      Connection connection = null;

      try
      {
        connection = getDatabaseConnection();

        try (Statement statement = connection.createStatement();
          final ResultSet resultSet = statement.executeQuery(dbSqlStatement))
        {
          try
          {
            LOGGER.debug("export csv");

            try (CSVPrinter printer = CSVFormat.EXCEL.withDelimiter(
              ';').withHeader(resultSet).print(out))
            {
              printer.printRecords(resultSet);
              printer.flush();
            }
          }
          catch (IOException ex)
          {
            LOGGER.error("DB Export error during export");
          }
        }
      }
      catch (SQLException ex)
      {
        LOGGER.error("DB Export error", ex);
      }
      finally
      {
        if (connection != null)
        {
          try
          {
            if (!connection.isClosed())
            {
              connection.close();
            }
          }
          catch (SQLException ex)
          {
            LOGGER.error("DB Export error: closing connection", ex);
          }
        }
      }
    }
    catch (UnsupportedEncodingException ex)
    {
      LOGGER.error("Unsupported encoding", ex);
    }
  }

  /**
   * Method description
   *
   */
  @PostConstruct
  public void initialize()
  {
    LOGGER.debug(getClass().getCanonicalName() + ":initialize");

    PortletRequest request =
      (PortletRequest) FacesContext.getCurrentInstance().getExternalContext()
      .getRequest();

    ThemeDisplay themeDisplay =
      (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

    Group siteGroup = themeDisplay.getSiteGroup();
    ExpandoBridge siteAttributes = siteGroup.getExpandoBridge();

    PortletPreferences preferences = request.getPreferences();

    SoniaPortletPreferencesHandler.load(preferences, this);

    userCredentials = UserCredentials.getUserCredentials(request);

    initialized = true;

    preferencesUuid = UUID.randomUUID().toString();

    LOGGER.debug(getClass().getCanonicalName() + " set Preferences "
      + preferencesUuid);
  }

  /**
   * Method description
   *
   */
  public void save()
  {
    LOGGER.debug("save");

    PortletRequest request =
      (PortletRequest) FacesContext.getCurrentInstance().getExternalContext()
      .getRequest();
    PortletPreferences preferences = request.getPreferences();

    SoniaPortletPreferencesHandler.store(preferences, this);

    initialize();
  }

  /**
   * Method description
   *
   */
  public void testDbConnection()
  {
    LOGGER.debug("testHisDbConnection");

    FacesContext facesContext = FacesContext.getCurrentInstance();

    FacesMessage message;
    String m;

    try
    {
      Class.forName(dbDriverClassName);

      try (Connection connection = getDatabaseConnection())
      {
        DatabaseMetaData dbMetaData = connection.getMetaData();

        m = "Successfully connected to " + dbMetaData.getDatabaseProductName()
          + " (" + dbMetaData.getDatabaseProductVersion() + ")";

        message = new FacesMessage(FacesMessage.SEVERITY_INFO, m, m);

      }
      catch (SQLException ex)
      {
        m = "Could not connect to database (" + ex.getMessage() + ")";
        message = new FacesMessage(FacesMessage.SEVERITY_ERROR, m, m);
        LOGGER.error("could not connect to database.", ex);
      }

    }
    catch (ClassNotFoundException ex)
    {
      m = "Database driver (" + dbDriverClassName + ") not found.";
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, m, m);
      LOGGER.error("db driver not found", ex);
    }

    facesContext.addMessage(null, message);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getColumnSeperator()
  {
    return columnSeperator;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws SQLException
   */
  public Connection getDatabaseConnection() throws SQLException
  {
    try
    {
      Class.forName(dbDriverClassName);

      return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    catch (ClassNotFoundException ex)
    {
      LOGGER.error("Database Driver not found.", ex);
    }

    return null;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDbDriverClassName()
  {
    return dbDriverClassName;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDbPassword()
  {
    return dbPassword;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDbSqlStatement()
  {
    return dbSqlStatement;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDbUrl()
  {
    return dbUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDbUser()
  {
    return dbUser;
  }

  /**
   * Get the value of exportCharset
   *
   * @return the value of exportCharset
   */
  public String getExportCharset()
  {
    return exportCharset;
  }

  /**
   * Get the value of filenameTemplate
   *
   * @return the value of filenameTemplate
   */
  public String getFilenameTemplate()
  {
    return filenameTemplate;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getFormattedExportFilename()
  {

    String filename = filenameTemplate + ".csv";

    try
    {
      User portalUser = userCredentials.getPortalUser();
      TimeZone timeZone = portalUser.getTimeZone();

      LOGGER.debug("users timezone = " + timeZone.getID());

      Calendar calendar = new GregorianCalendar(timeZone);

      filename = filename.replaceAll("%y",
        Integer.toString(calendar.get(Calendar.YEAR)));
      filename = filename.replaceAll("%m",
        format2Digits(calendar.get(Calendar.MONTH) + 1));
      filename = filename.replaceAll("%d",
        format2Digits(calendar.get(Calendar.DAY_OF_MONTH)));

      filename = filename.replaceAll("%H",
        format2Digits(calendar.get(Calendar.HOUR_OF_DAY)));
      filename = filename.replaceAll("%M",
        format2Digits(calendar.get(Calendar.MINUTE)));
      filename = filename.replaceAll("%S",
        format2Digits(calendar.get(Calendar.SECOND)));

    }
    catch (PortalException | SystemException ex)
    {
      LOGGER.error(ex);
    }

    return filename;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPreferencesUuid()
  {
    return preferencesUuid;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getTextDelemiter()
  {
    return textDelemiter;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public UserCredentials getUserCredentials()
  {
    return userCredentials;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUserLocalizedTimestamp()
  {
    return userCredentials.getUserLocalizedTimestamp();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isInitialized()
  {
    return initialized;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isShowColumnHeaders()
  {
    return showColumnHeaders;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param columnSeperator
   */
  public void setColumnSeperator(String columnSeperator)
  {
    this.columnSeperator = columnSeperator;
  }

  /**
   * Method description
   *
   *
   * @param dbDriverClassName
   */
  public void setDbDriverClassName(String dbDriverClassName)
  {
    this.dbDriverClassName = dbDriverClassName;
  }

  /**
   * Method description
   *
   *
   * @param dbPassword
   */
  public void setDbPassword(String dbPassword)
  {
    this.dbPassword = dbPassword;
  }

  /**
   * Method description
   *
   *
   * @param dbSqlStatement
   */
  public void setDbSqlStatement(String dbSqlStatement)
  {
    this.dbSqlStatement = dbSqlStatement;
  }

  /**
   * Method description
   *
   *
   * @param dbUrl
   */
  public void setDbUrl(String dbUrl)
  {
    this.dbUrl = dbUrl;
  }

  /**
   * Method description
   *
   *
   * @param dbUser
   */
  public void setDbUser(String dbUser)
  {
    this.dbUser = dbUser;
  }

  /**
   * Set the value of exportCharset
   *
   * @param exportCharset new value of exportCharset
   */
  public void setExportCharset(String exportCharset)
  {
    this.exportCharset = exportCharset;
  }

  /**
   * Set the value of filenameTemplate
   *
   * @param filenameTemplate new value of filenameTemplate
   */
  public void setFilenameTemplate(String filenameTemplate)
  {
    this.filenameTemplate = filenameTemplate;
  }

  /**
   * Method description
   *
   *
   * @param showColumnHeaders
   */
  public void setShowColumnHeaders(boolean showColumnHeaders)
  {
    LOGGER.debug("setting show column headers");
    this.showColumnHeaders = showColumnHeaders;
  }

  /**
   * Method description
   *
   *
   * @param textDelemiter
   */
  public void setTextDelemiter(String textDelemiter)
  {
    this.textDelemiter = textDelemiter;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param n
   *
   * @return
   */
  private String format2Digits(int n)
  {
    return (n < 10)
      ? "0" + n
      : Integer.toString(n);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @SoniaPortletPreference
  private boolean showColumnHeaders = true;

  /** Field description */
  private String preferencesUuid;

  /** Field description */
  @SoniaPortletPreference(value = "select * from tabelle")
  private String dbSqlStatement;

  /** Field description */
  @SoniaPortletPreference
  private String dbDriverClassName;

  /** Field description */
  @SoniaPortletPreference
  private String dbUrl;

  /** Field description */
  @SoniaPortletPreference
  private String dbUser;

  /** Field description */
  @SoniaPortletPreference
  private String dbPassword;

  /** Field description */
  @SoniaPortletPreference(value = "\"")
  private String textDelemiter;

  /** Field description */
  @SoniaPortletPreference(value = ";")
  private String columnSeperator;

  /** Field description */
  @SoniaPortletPreference(value = "dbexport-%y%m%d%H%M%S")
  private String filenameTemplate;

  /** Field description */
  @SoniaPortletPreference(value = "iso-8859-15")
  private String exportCharset;

  /** Field description */
  private boolean initialized;

  /** Field description */
  private UserCredentials userCredentials;
}
