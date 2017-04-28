package com.agile.agilepharma.data.generator;

import com.agile.agilepharma.util.PropertiesReader;
import com.agile.agilepharma.util.xml.ParseXMLConfigUtil;
import com.agile.agilepharma.util.SDKUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PharmaEqpXMLGenerator implements PharmaObjectXMLGenerator {
	private static final String BO_ROOT = "BORoot";
	private static final String USER_OBJECT = "User";
	private PropertiesReader propertiesReader;
	private HashMap hmAttributeMap = null;
	private HashMap hgenralatrributeMap = null;
	private String EqpNum="";
	private String strAction;
	private boolean activitycheck = true;
	private String strIntUserName;
	private String strIntUserPasswd;
	private String strUserCategoryName = "";
	private String strUserCategoryDBName = "";
	private String strUserCategoryViewName = "";
	private ParseXMLConfigUtil parser;

	public PharmaEqpXMLGenerator(HashMap hmAttributeMap,
			 String strAction,String eqpnum,
			PropertiesReader propertiesReader,boolean activitycheck) throws Exception {
		this.hmAttributeMap = hmAttributeMap;
		this.strAction = strAction;
		this.EqpNum = eqpnum;
		this.activitycheck = activitycheck;
		this.propertiesReader = propertiesReader;
		init();
		handleEscapeSequences();

		this.parser = new ParseXMLConfigUtil();
	}

	private void init() throws Exception {
		this.strIntUserName = this.propertiesReader.getAgilePropertyValue("rmw.intusername");
		this.strIntUserPasswd = this.propertiesReader.getAgilePropertyValue("rmw.intuserpasswd");
	}

	private void handleEscapeSequences() {
		if (this.hmAttributeMap != null) {
			Set stKeys = this.hmAttributeMap.keySet();
			Iterator it = stKeys.iterator();
			while (it.hasNext()) {
				String strKey = (String) it.next();
				try {
					Object strValue = this.hmAttributeMap.get(strKey);
					strValue = escapeSequence(SDKUtil.checknullValue(strValue));
					this.hmAttributeMap.put(strKey, strValue);
				} catch (Exception ex) {
				}
			}
		}
		
	}

	public String getRMWXML(String Category) throws Exception {
		StringBuffer sbCfmXML = new StringBuffer();
		sbCfmXML.append("<cfmXML>");
		sbCfmXML.append(loginInfo());
		sbCfmXML.append(generatePayload(Category));
		sbCfmXML.append("</cfmXML>");
		return sbCfmXML.toString();
	}

	private String loginInfo() {
		return "<LoginInfo><UserName>" + this.strIntUserName
				+ "</UserName><Password>" + this.strIntUserPasswd
				+ "</Password></LoginInfo>";
	}

	private String getBOAction() {
		return "<BOActions><BOAction>" + this.strAction
				+ "</BOAction></BOActions>";
	}

	private String generatePayload(String Category) throws Exception {
		StringBuffer sbPayload = new StringBuffer();
		sbPayload.append("<Payload>");
		sbPayload.append(generateObjectGroup(Category));
		sbPayload.append("</Payload>");
		return sbPayload.toString();
	}

	private String generateObjectGroup(String strObjectGroup) throws Exception {
		StringBuffer sbObjectGroup = new StringBuffer();
		String MatLib = "Equipment Library";
		sbObjectGroup.append("<ObjectGroup isBO=\"yes\" name=\""
				+ MatLib + "\">");
		sbObjectGroup.append(getBOAction());
			sbObjectGroup.append(getObject("BORoot", "Equipment", "A_EQP_EQUIPMENT", true));
		sbObjectGroup.append("</ObjectGroup>");
		return sbObjectGroup.toString();
	}

	private String getObject(String strObjectName, String strCategoryName,
			String strCategoryDBName, boolean blIncludeOperation)
			throws Exception {
		StringBuffer sbObject = new StringBuffer();
		String strOperation = "";
		if (blIncludeOperation) {
			if (this.strAction.equals("New"))
				strOperation = "I";
			else if (this.strAction.equals("Edit"))
				strOperation = "U";
			else if (this.strAction.equals("Inactive"))
				strOperation = "D";
		}
		if (blIncludeOperation) {
			sbObject.append("<Object name=\"" + strObjectName
					+ "\" operation=\"" + strOperation + "\">");
		} else
			sbObject.append("<Object name=\"" + strObjectName + "\">");
		sbObject.append("<CategoryName>" + strCategoryName
				+ "</CategoryName><CategoryDBName>" + strCategoryDBName
				+ "</CategoryDBName>");

		if (blIncludeOperation)
			sbObject.append("<operation>" + strOperation + "</operation>");
		sbObject.append(generateObjectKey());
		sbObject.append(generateObjectDetail(strCategoryName));
		sbObject.append("</Object>");
		return sbObject.toString();
	}

	private String generateObjectKey() throws Exception {
		StringBuffer sbObjectKey = new StringBuffer();
		sbObjectKey.append("<ObjectKey>");
		sbObjectKey.append("<Attribute name=\"Equipment ID\">");
		sbObjectKey.append("<Value dataType=\"Basic Text\">"+this.EqpNum+"</Value>");
		sbObjectKey.append("<UnitOfMeasure></UnitOfMeasure>");
		sbObjectKey.append("</Attribute>");
		sbObjectKey.append("</ObjectKey>");
		return sbObjectKey.toString();
	}

	private String generateObjectDetail(String strCategoryName)
			throws Exception {
		boolean activiytype = this.activitycheck;
		StringBuffer sbObjectDetail = new StringBuffer();
		sbObjectDetail.append("<ObjectDetail>");
		sbObjectDetail.append(" <Attribute name=\"Effective Start Date\">");
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a z");
		Date date = new Date();
		sbObjectDetail.append("<Value GMTDateTime=\""+date.getTime()+"\" dataType=\"Date\">"+dateFormat.format(date) +"</Value>");
		sbObjectDetail.append("<UnitOfMeasure/>");
		sbObjectDetail.append("<Format>MM/dd/yyyy hh:mm:ss a z</Format>");
		sbObjectDetail.append("</Attribute>");
		sbObjectDetail.append("<Attribute name=\"Is Active\">");
		sbObjectDetail.append("<Value dataType=\"Boolean\">"+activiytype+"</Value>");
		sbObjectDetail.append("<UnitOfMeasure></UnitOfMeasure>");
		sbObjectDetail.append("</Attribute>");
   
		sbObjectDetail.append(generateAttributeDetails(this.hmAttributeMap));
		
		sbObjectDetail.append("</ObjectDetail>");
		return sbObjectDetail.toString();
	}

	private String generateAttributeDetails(HashMap CatAttr)
			throws ParseException {
		StringBuffer sbAttributeDetails = new StringBuffer();
				Iterator att = CatAttr.entrySet().iterator();
		while (att.hasNext()) {
			Entry Entry = (Entry) att.next();
			sbAttributeDetails.append("<Attribute name=\"" + Entry.getKey()
					+ "\">");
			if( Entry.getKey().toString().equals("Is Calibration Needed"))
			{
				sbAttributeDetails.append("<Value dataType=\"Boolean\">");
			}else if(Entry.getKey().toString().equals("Calibration Frequency(in days)"))
			{
				sbAttributeDetails.append("<Value dataType=\"Integer\">");
			}
			else if (Entry.getKey().equals("Agile PC URL")) {
				sbAttributeDetails.append("<Value dataType=\"URL\">");
			} 
			else{
			sbAttributeDetails.append("<Value dataType=\"Basic Text\">");
			}
			sbAttributeDetails.append(Entry.getValue());
			sbAttributeDetails.append("</Value>");
			sbAttributeDetails.append("</Attribute>");
		}
		sbAttributeDetails.append(" <Attribute name=\"Cleaning Method\">");
		sbAttributeDetails.append("<Value dataType=\"Basic Text\">N/A</Value>");
		sbAttributeDetails.append("</Attribute>");
		
		sbAttributeDetails.append(generateRelationshipDetails());
		return sbAttributeDetails.toString();
	}
	private String generateRelationshipDetails()
			throws ParseException {
		StringBuffer sbAttributeDetails = new StringBuffer();
		String strPharmaAttributeRelName = "BO Status";
		String strPharmaAttributeRelCategoryName = "BO Status";
		String strPharmaAttributeRelCategoryDBName = "D_BO_STATUS";
		String strPharmaAttributeName = "Status ID";
		String strAgileAttributeValue = "EQPDRAFT";
		sbAttributeDetails.append("<Relationship name=\""
				+ strPharmaAttributeRelName + "\">");
		sbAttributeDetails.append("<CategoryName>"
				+ strPharmaAttributeRelCategoryName + "</CategoryName>");
		sbAttributeDetails.append("<CategoryDBName>"
				+ strPharmaAttributeRelCategoryDBName + "</CategoryDBName>");
		sbAttributeDetails.append("<ObjectKey>");
		sbAttributeDetails.append("<Attribute name=\"" + strPharmaAttributeName
				+ "\">");
		sbAttributeDetails.append("<Value dataType=\"Basic Text\">");
		sbAttributeDetails.append(strAgileAttributeValue);
		sbAttributeDetails.append("</Value>");
		sbAttributeDetails.append("</Attribute>");
		sbAttributeDetails.append("</ObjectKey>");
		sbAttributeDetails.append("</Relationship>");
		sbAttributeDetails.append("<Relationship name=\"Equipment Site\">");
		sbAttributeDetails.append("<CategoryName>Site</CategoryName>");
		sbAttributeDetails.append("<CategoryDBName>D_ORG_SITE</CategoryDBName>");
		sbAttributeDetails.append("<ObjectKey>");
		sbAttributeDetails.append(" <Attribute name=\"Site ID\">");
		sbAttributeDetails.append("<Value dataType=\"Basic Text\">Global</Value>");
		sbAttributeDetails.append("</Attribute>");
		sbAttributeDetails.append("</ObjectKey>");
		sbAttributeDetails.append("</Relationship>");
		sbAttributeDetails.append("<Relationship name=\"Current Site\">");
		sbAttributeDetails.append("<CategoryName>Site</CategoryName>");
		sbAttributeDetails.append("<CategoryDBName>D_ORG_SITE</CategoryDBName>");
		sbAttributeDetails.append("<ObjectKey>");
		sbAttributeDetails.append(" <Attribute name=\"Site ID\">");
		sbAttributeDetails.append("<Value dataType=\"Basic Text\">Global</Value>");
		sbAttributeDetails.append("</Attribute>");
		sbAttributeDetails.append("</ObjectKey>");
		sbAttributeDetails.append("</Relationship>");
		

		return sbAttributeDetails.toString();
	}

	private String escapeSequence(String strValue) {
		StringBuffer strTempValue = new StringBuffer();
		char[] chars = strValue.toCharArray();

		char APOS = '\'';
		char QUOT = '"';
		char AMP = '&';
		char LT = '<';
		char GT = '>';
		if (chars != null) {
			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				switch (ch) {
				case '\'':
					strTempValue.append("&apos;");
					break;
				case '"':
					strTempValue.append("&quot;");
					break;
				case '&':
					strTempValue.append("&amp;");
					break;
				case '<':
					strTempValue.append("&lt;");
					break;
				case '>':
					strTempValue.append("&gt;");
					break;
				default:
					strTempValue.append(ch);
				}
			}
			return strTempValue.toString();
		}
		return strValue;
	}
}