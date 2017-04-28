package com.agile.agilepharma.integration.equipment;

import com.agile.agilepharma.data.generator.PharmaEqpQualXMLGenerator;
import com.agile.agilepharma.data.generator.PharmaEqpXMLGenerator;
import com.agile.agilepharma.integration.client.jaxws.CFMBOWebService;
import com.agile.agilepharma.integration.client.jaxws.CFMBOWebServiceService;
import com.agile.agilepharma.util.PropertiesReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EquipmentIntegrationService {
	private static final Log LOG = LogFactory
			.getLog(EquipmentIntegrationService.class.getName());
	public static final String RESPONSE_CODE_SUCCESS = "500";
	public static final String RESPONSE_CODE_FAILURE = "300";
	public static final String RETURN_RESPONSE_SUCCESS = "Success";
	public static final String RETURN_RESPONSE_FAILURE = "Failure";
	public static final String ACTION_NEW = "New";
	public static final String ACTION_EDIT = "Edit";
	public static final String INACTIVE = "Inactive";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public String generalInfoStatus;
	public String generalInfoEffStartDate;
	public String generalInfoEffEndDate;
	private PropertiesReader propertiesReader;
	private String strApplicationID = "";
	private String strDBName = "";

	private String strResponseCode = "";
	private String eqpnum = "";
	private String strResponseMessage = "";
	private XPathExpression responseCodeExpr;
	private XPathExpression responseMessageExpr;
	public static final String RMW_HOST = "agilepharma.hostname";
	public static final String RMW_PORT = "agilepharma.portnumber";
	public static final String RMW_WEBSERVICE_VPATH = "rmw.webservice.virtualpath";
	public static final String RMW_PROTOCOL = "agilepharma.protocol";

	public String getStrResponseCode() {
		return this.strResponseCode;
	}

	public String getStrResponseMessage() {
		return this.strResponseMessage;
	}

	public EquipmentIntegrationService(PropertiesReader propertiesReader)
			throws XPathExpressionException {
		this.propertiesReader = propertiesReader;
		
		this.strApplicationID = propertiesReader.getAgilePropertyValue("rmw.appid");
		this.strDBName = propertiesReader.getAgilePropertyValue("rmw.dbname");
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		this.responseCodeExpr = xpath.compile("/cfmXML/Response/ResponseCode");
		this.responseMessageExpr = xpath
				.compile("/cfmXML/Response/ResponseMessage");
	}

	public void createMaterial(HashMap values,String action,boolean activitycheck,String num)
			throws Exception {
		this.strResponseCode = "";
		this.strResponseMessage = "";
		this.eqpnum=num;
		String matCFMXML = new PharmaEqpXMLGenerator(values, action, this.eqpnum,
				this.propertiesReader,activitycheck).getRMWXML("Equipment");
		System.out.println(matCFMXML);

		if (LOG.isInfoEnabled()) {
			LOG.info("*****");
			LOG.info("*****CFMXML*****");
			LOG.info(matCFMXML);
			LOG.info("*****");
		}

		CFMBOWebService service = getBOWebservice(this.propertiesReader);
		try {
			CFMBOWebServiceService port = service.getCFMBOWebService();
			if(activitycheck==true)
			{
			String strResponse = port.processBO(matCFMXML,this.strApplicationID, this.strDBName);
			parseResponse(strResponse);
			}
			else
			{
				String strResponse="";
				strResponse = port.processBO(matCFMXML,this.strApplicationID, this.strDBName);
				Thread.sleep(25);
				strResponse = port.processBO(matCFMXML,this.strApplicationID, this.strDBName);
				parseResponse(strResponse);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}
	public void createQualification(String num,String type,String action,boolean activitycheck)
			throws Exception {
		this.strResponseCode = "";
		this.strResponseMessage = "";
		this.eqpnum=num;
		String matCFMXML = new PharmaEqpQualXMLGenerator(action, this.eqpnum,type,
				this.propertiesReader,activitycheck).getRMWXML("Equipment");
		System.out.println(matCFMXML);

		if (LOG.isInfoEnabled()) {
			LOG.info("*****");
			LOG.info("*****CFMXML*****");
			LOG.info(matCFMXML);
			LOG.info("*****");
		}

		CFMBOWebService service = getBOWebservice(this.propertiesReader);
		try {
			CFMBOWebServiceService port = service.getCFMBOWebService();
			String strResponse = port.processBO(matCFMXML,this.strApplicationID, this.strDBName);
			parseResponse(strResponse);
		} catch (Exception ex) {
			throw ex;
		}
	}


	private CFMBOWebService getBOWebservice(PropertiesReader propertiesReader)
			throws Exception {
		CFMBOWebServiceService service = null;
		String strPharmaHost = propertiesReader
				.getAgilePropertyValue("agilepharma.hostname");
		String strPharmaPort = propertiesReader
				.getAgilePropertyValue("agilepharma.portnumber");
		String strPharmaWebServiceVpath = propertiesReader
				.getAgilePropertyValue("rmw.webservice.virtualpath");

		String strPharmaProtocol = propertiesReader
				.getAgilePropertyValue("agilepharma.protocol");

		String url = strPharmaProtocol + "://" + strPharmaHost + ":"
				+ strPharmaPort + "/" + strPharmaWebServiceVpath
				+ "/services/CFMBOWebService?wsdl";

		URL endpoint = new URL(CFMBOWebService.class.getResource("."), url);
		CFMBOWebService webService = new CFMBOWebService(endpoint);
		return webService;
	}

	private String parseResponse(String strResponse)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();

		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		ByteArrayInputStream baisResponse = new ByteArrayInputStream(
				strResponse.getBytes("UTF-8"));

		Document doc = builder.parse(baisResponse);

		NodeList nodes = (NodeList) this.responseCodeExpr.evaluate(doc,
				XPathConstants.NODESET);

		if ((nodes != null) && (nodes.getLength() > 0)) {
			if ("500".equals(nodes.item(0).getTextContent())) {
				this.strResponseCode = "500";
			} else if ("300".equals(nodes.item(0).getTextContent())) {
				this.strResponseCode = "300";
			}

			nodes = (NodeList) this.responseMessageExpr.evaluate(doc,
					XPathConstants.NODESET);

			if ((nodes != null) && (nodes.getLength() > 0))
				this.strResponseMessage = nodes.item(0).getTextContent();
		} else {
			this.strResponseCode = "300";
		}

		return this.strResponseCode;
	}
}