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

public class PharmaEqpQualXMLGenerator implements PharmaObjectXMLGenerator {
	private static final String BO_ROOT = "BORoot";
	private static final String USER_OBJECT = "User";
	private PropertiesReader propertiesReader;
	private HashMap hmAttributeMap = null;
	private HashMap hgenralatrributeMap = null;
	private String EqpNum="";
	private String qualtype="";
	private String strAction;
	private boolean activitycheck = true;
	private String strIntUserName;
	private String strIntUserPasswd;
	private String strUserCategoryName = "";
	private String strUserCategoryDBName = "";
	private String strUserCategoryViewName = "";
	private ParseXMLConfigUtil parser;

	public PharmaEqpQualXMLGenerator(String strAction,String eqpnum,String type,
			PropertiesReader propertiesReader,boolean activitycheck) throws Exception {
		this.strAction = strAction;
		this.EqpNum = eqpnum;
		this.qualtype = type;
		this.activitycheck = activitycheck;
		this.propertiesReader = propertiesReader;
		init();

		this.parser = new ParseXMLConfigUtil();
	}

	private void init() throws Exception {
		this.strIntUserName = this.propertiesReader.getAgilePropertyValue("rmw.intusername");
		this.strIntUserPasswd = this.propertiesReader.getAgilePropertyValue("rmw.intuserpasswd");
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
		String MatLib = "Application";
		sbObjectGroup.append("<ObjectGroup isBO=\"yes\" name=\""
				+ MatLib + "\">");
		sbObjectGroup.append(getBOAction());
			sbObjectGroup.append(getObject("BORoot", "Equipment Applicable Qualification", "A_EQP_APPLICABLE_QUALS", true));
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
		sbObject.append("</Object>");
		return sbObject.toString();
	}

	private String generateObjectKey() throws Exception {
		StringBuffer sbObjectKey = new StringBuffer();
		sbObjectKey.append("<ObjectKey>");
		sbObjectKey.append("<Relationship name=\"Qualification Type\">");
		sbObjectKey.append("<CategoryName>Equipment Qualification Type</CategoryName>");
		sbObjectKey.append("<CategoryDBName>A_EQP_QUAL_TYPE</CategoryDBName>");
		sbObjectKey.append("<ObjectKey>");
		sbObjectKey.append("<Attribute name=\"Qualification Type\">");
		sbObjectKey.append("<Value dataType=\"Basic Text\">"+this.qualtype +"</Value>");
		sbObjectKey.append("</Attribute>");
		sbObjectKey.append("</ObjectKey>");
		sbObjectKey.append("</Relationship>");
		sbObjectKey.append("<Relationship name=\"Equipment\">");
		sbObjectKey.append("<CategoryName>Equipment</CategoryName>");
		sbObjectKey.append("<CategoryDBName>A_EQP_EQUIPMENT</CategoryDBName>");
		sbObjectKey.append("<ObjectKey>");
		sbObjectKey.append("<Attribute name=\"Equipment ID\">"); 
		sbObjectKey.append("<Value dataType=\"Basic Text\">"+this.EqpNum+"</Value>");
		sbObjectKey.append("</Attribute>");
		sbObjectKey.append("</ObjectKey>");
		sbObjectKey.append("</Relationship>");
        sbObjectKey.append("</ObjectKey>");
		return sbObjectKey.toString();
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