package com.agile.agilepharma.integration.px;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agile.agilepharma.integration.equipment.EquipmentIntegrationService;
import com.agile.agilepharma.util.AgileAPIConstants;
import com.agile.agilepharma.util.PharmaNameConstants;
import com.agile.agilepharma.util.PropertiesReader;
import com.agile.api.APIException;
import com.agile.api.ChangeConstants;
import com.agile.api.IAgileList;
import com.agile.api.IAgileSession;
import com.agile.api.IChange;
import com.agile.api.IItem;
import com.agile.api.INode;
import com.agile.api.IRow;
import com.agile.api.ITable;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;

public class EquipmentIntegrationPX implements IEventAction {
	private static final Log LOG = LogFactory
			.getLog(EquipmentIntegrationPX.class.getName());
	private static final String CREATE = "create";
	private static final String UPDATE = "update";
	private static final String DELETE = "delete";
	private PropertiesReader propertiesReader;
	private static String strEventType = "";
	private String strResult = "";
	private IAgileSession sdkSession;
	private EquipmentIntegrationService service;

	public EquipmentIntegrationPX() {
		this.propertiesReader = new PropertiesReader();

	}

	public EventActionResult doAction(IAgileSession session, INode node,
			IEventInfo eventInfo) {
		EventActionResult result = null;
		IItem matnum = null;
		String Category = "";
		String oldrev = "";
		String action = "";
		boolean activitycheck = true;
		try {
			this.sdkSession = session;
			this.service = new EquipmentIntegrationService(
					this.propertiesReader);
			IObjectEventInfo objectEventInfo = (IObjectEventInfo) eventInfo;
			IChange chng = (IChange) session.getObject(IChange.OBJECT_TYPE,
					objectEventInfo.getDataObject().toString());
			;
			ITable tab = (ITable) chng
					.getTable(ChangeConstants.TABLE_AFFECTEDITEMS);
			Iterator itr = tab.iterator();
			while (itr.hasNext()) {
				IRow row = (IRow) itr.next();
				matnum = (IItem) row.getReferent();
				if (row.getCell("lifecyclePhase").getValue().toString()
						.equals("Inactive")) {
					activitycheck = false;
				}

				if (chng.getValue("changeType").toString()
						.equals("Approval Process")) {
					oldrev = row.getCell("oldRev").getValue().toString();
					System.out.println("oldrev::" + oldrev);
					if (oldrev.length() == 0) {
						action = "New";
						System.out.println("Action::into iff");
					} else {
						System.out.println("Action::into else");
						action = "Edit";
					}
				} else {
					System.out.println("Action::into iff1");
					action = "New";
				}
			}
			this.strEventType = "create";
			HashMap eqpvalues = getRMattributes(matnum);
			this.service.createMaterial(eqpvalues, action, activitycheck,
					matnum.toString());
			if (action.equals("New")) {
				ArrayList al = new ArrayList();
				al.add("Installation Qualification");
				al.add("Operational Qualification");
				al.add("Performance Qualification");
				al.add("Requalification");
				for (int i = 0; i < al.size(); i++) {
					String type = al.get(i).toString();
					this.service.createQualification(matnum.toString(), type,
							action, activitycheck);
				}
			}
			this.strResult = this.service.getStrResponseMessage();
			String strResponseCode = this.service.getStrResponseCode();

			if ("500".equals(strResponseCode)) {
				this.strResult = processSuccess();
				result = new EventActionResult(eventInfo, new ActionResult(0,
						this.strResult));
			} else {
				this.strResult = processFailiure(this.strResult);
				result = new EventActionResult(eventInfo, new ActionResult(0,
						this.strResult));
			}

		} catch (APIException ex) {
			this.strResult = processAPIException(ex);
			result = new EventActionResult(eventInfo, new ActionResult(-1,
					new Exception(this.strResult)));
		} catch (Exception ex) {
			this.strResult = processException(ex);
			result = new EventActionResult(eventInfo, new ActionResult(-1,
					new Exception(this.strResult)));
		}

		return result;
	}

	private static HashMap getRMattributes(IItem matObject) throws APIException {
		String ClassID = matObject.getAgileClass().getSuperClass().getId()
				.toString();
		String ObjectID = matObject.getObjectId().toString();
		String URL = "http://"
				+ AgileAPIConstants.hostname
				+ "/Strides/PCMServlet?fromPCClient=true&module=ItemHandler&requestUrl=module%3DItemHandler%26opcode%3DdisplayObject%26classid%3D"
				+ ClassID + "%26objid%3D" + ObjectID + "%26tabid%3D13%26";
		HashMap values = new HashMap();
		values.put(PharmaNameConstants.Equipment_Name,
				matObject.getValue(AgileAPIConstants.Equipment_Name));
		values.put(PharmaNameConstants.ManufacturerSerialNumber,
				matObject.getValue(AgileAPIConstants.Equipment_Name));
		values.put(PharmaNameConstants.ManufacturerModelNumber,
				matObject.getValue(AgileAPIConstants.ManufacturerModelNumber));
		values.put(PharmaNameConstants.ProductQualityImpact,
				(IAgileList) matObject
						.getValue(AgileAPIConstants.ProductQualityImpact));
		values.put(PharmaNameConstants.IsCalibrationNeeded,
				(IAgileList) matObject
						.getValue(AgileAPIConstants.IsCalibrationNeeded));
		values.put(PharmaNameConstants.CalibrationFrequencyInDays, matObject
				.getValue(AgileAPIConstants.CalibrationFrequencyInDays));
		values.put(PharmaNameConstants.EquipmentTagNumber,
				matObject.getValue(AgileAPIConstants.EquipmentTagNumber));
		values.put(PharmaNameConstants.Agile_PC_URL, URL);
		return values;
	}

	private String processAPIException(APIException ex) {
		String message = ex.getRootCause() != null ? ex.getRootCause()
				.getMessage() : ex.getMessage();
		message = processFailiure(message);

		if (LOG.isErrorEnabled()) {
			LOG.error("****");
			LOG.error("Stack Trace:", ex);
			if (ex.getRootCause() != null) {
				LOG.error("Root cause:", ex.getRootCause());
			}
			LOG.error("*****");
		}

		return message;
	}

	private String processException(Exception ex) {
		String message = ex.getMessage();
		message = processFailiure(message);

		if (LOG.isErrorEnabled()) {
			LOG.error("*****");
			LOG.error("Stack Trace:", ex);
			LOG.error("*****");
		}

		return message;
	}

	private String processSuccess() {
		String strResult = this.propertiesReader
				.getRMWMessageValue("USER_INTEGRATION_SUCCESS");

		if (LOG.isInfoEnabled()) {
			LOG.info("*****");
			LOG.info("Agile RMW User Integration: " + this.strEventType
					+ " succeded for user : ");

			LOG.info("*****");
		}

		return strResult;
	}

	private String appendFailiure(String strResult) {
		StringBuffer buffer = new StringBuffer();
		if (this.strEventType.equals("create")) {
			buffer.append(this.propertiesReader
					.getRMWMessageValue("CREATE_OPERATION"));
		} else if (this.strEventType.equals("update")) {
			buffer.append(this.propertiesReader
					.getRMWMessageValue("UPDATE_OPERATION"));
		}

		buffer.append(strResult);

		return buffer.toString();
	}

	private String processFailiure(String strResult) {
		if (LOG.isErrorEnabled()) {
			LOG.error("*****");
			LOG.error("Agile RMW User Integration: " + this.strEventType
					+ " failed for user : ");
			LOG.error("Reason: " + strResult);
			LOG.error("*****");
		}

		String formattedMsg = MessageFormat.format(this.propertiesReader
				.getRMWMessageValue("USER_INTEGRATION_FAILIURE"),
				new Object[] { strResult });

		return formattedMsg;
	}
}