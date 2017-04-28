package com.agile.agilepharma.util;

import java.util.Date;
import java.util.HashMap;

import com.agile.api.APIException;


public class SDKUtil {
public static String checknullValue(Object value)throws APIException
			  {
			    if (value == null)
			    {
			      value = "";
			    }
			    
			    return value.toString();
			  }
}
