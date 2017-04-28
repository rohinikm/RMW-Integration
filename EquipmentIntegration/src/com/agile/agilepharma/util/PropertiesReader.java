package com.agile.agilepharma.util;

//import com.agile.util.sql.AgileUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.agile.util.sql.AgileUtil;

public class PropertiesReader
{
  private static final Log LOG = LogFactory.getLog(PropertiesReader.class.getName());
  private ResourceBundle rmwIntProperties;
  private ResourceBundle agileProperties;
  private ResourceBundle rmwMessageProperties;

  public PropertiesReader()
  {
    try
    {
      InputStream properties = Thread.currentThread().getContextClassLoader().getResourceAsStream("rmw_integration.properties");

      this.rmwIntProperties = new PropertyResourceBundle(properties);

      properties = Thread.currentThread().getContextClassLoader().getResourceAsStream("agile.properties");

      this.agileProperties = new PropertyResourceBundle(properties);

      properties = Thread.currentThread().getContextClassLoader().getResourceAsStream("rmw_integration_messages.properties");

      this.rmwMessageProperties = new PropertyResourceBundle(properties);
    } catch (IOException ex) {
      if (LOG.isErrorEnabled())
        LOG.error(ex.getMessage());
    }
  }

  public String getIntegrationPropertyValue(String key)
  {
    if ((this.rmwIntProperties != null) && (key != null)) {
      return this.rmwIntProperties.getString(key);
    }

    return null;
  }

  public String getAgilePropertyValue(String key) {
    if ((this.agileProperties != null) && (key != null)) {
      return AgileUtil.getProperty(key);
    }

    return null;
  }

  public String getRMWMessageValue(String key) {
    if ((this.rmwMessageProperties != null) && (key != null)) {
      return this.rmwMessageProperties.getString(key);
    }

    return null;
  }

  public Enumeration<String> getIntegrationPropertyKeys() {
    if (this.rmwIntProperties != null) {
      return this.rmwIntProperties.getKeys();
    }
    return null;
  }

  public Enumeration<String> getAgilePropertyKeys() {
    if (this.agileProperties != null) {
      return this.agileProperties.getKeys();
    }
    return null;
  }
}