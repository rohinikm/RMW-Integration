package com.agile.agilepharma.util.xml;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Document;

public class NamespaceContextResolver
  implements NamespaceContext
{
  private Document sourceDocument;

  public NamespaceContextResolver(Document document)
  {
    this.sourceDocument = document;
  }

  public String getNamespaceURI(String prefix)
  {
    if (prefix == null)
      throw new IllegalArgumentException("No prefix provided!");
    if (prefix.equals(""))
      return "http://www.oracle.com/Agile/AgilePharmaMapping";
    if (prefix.equals("agilpharma")) {
      return "http://www.oracle.com/Agile/AgilePharmaMapping";
    }
    return "";
  }

  public String getPrefix(String namespaceURI)
  {
    if (namespaceURI == null)
      throw new IllegalArgumentException("No Namespace URI provided");
    if (namespaceURI.equalsIgnoreCase("http://www.oracle.com/Agile/AgilePharmaMapping")) {
      return "";
    }
    return "";
  }

  public Iterator getPrefixes(String namespaceURI)
  {
    return null;
  }
}