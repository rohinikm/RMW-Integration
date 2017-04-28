package com.agile.agilepharma.util.xml;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseXMLConfigUtil
{
  private Document doc;
  private XPath xpath;

  public ParseXMLConfigUtil()
    throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

    domFactory.setNamespaceAware(true);
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    InputStream configXML = Thread.currentThread().getContextClassLoader().getResourceAsStream("AgilePharmaMapping.xml");

    this.doc = builder.parse(configXML);

    XPathFactory factory = XPathFactory.newInstance();
    this.xpath = factory.newXPath();
    this.xpath.setNamespaceContext(new NamespaceContextResolver(this.doc));
  }

  public NodeList getAttributeMaps(String strXPathExpr)
    throws ParserConfigurationException, SAXException, IOException, XPathExpressionException
  {
    XPathExpression expr = this.xpath.compile(strXPathExpr);
    NodeList nodes = (NodeList)expr.evaluate(this.doc, XPathConstants.NODESET);

    return nodes;
  }
}