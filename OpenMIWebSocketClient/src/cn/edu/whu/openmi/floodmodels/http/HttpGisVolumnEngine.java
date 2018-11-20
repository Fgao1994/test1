package cn.edu.whu.openmi.floodmodels.http;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.edu.whu.openmi.floodmodels.GisVolumn.GisVolumnModel;
import cn.edu.whu.openmi.floodmodels.Precipitation.PrecipitationEngine;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.RequestMethodStore;
import cn.edu.whu.websocket.WSModelUtils;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

public class HttpGisVolumnEngine extends SimpleWrapper{
	
	private String httpUrl = "http://192.168.23.131:8080/OpenMIWebSocketServer/http_gis_volumn_model";
	private String httpUuid = "";
	
	private String out_file;
	private Double v_water;
	private Map<Calendar, Double> outputValues = new LinkedHashMap<Calendar, Double>();
	
	
	public HttpGisVolumnEngine(){
		InputStream inConfig = PrecipitationEngine.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/floodmodels/GisVolumn/GisVolumn-Config.xml");
 		this.setVariablesFromConfigFile(inConfig);
		
	}
	
	@Override
	public void initialize(HashMap properties){
		super.initialize(properties);
		out_file = properties.get("out_file").toString();
		//dem_input = properties.get("dem_input").toString();
		//s_area = Double.parseDouble(properties.get("s_area").toString());
		String parameter = "?"+HttpGisVolumnModel.METHODKEY+"="+HttpGisVolumnModel.Method_Initialize;
		parameter +="&out_file="+out_file;
		String uuid = RequestMethodStore.GETStr(httpUrl+parameter);
		uuid = uuid.replaceAll("\r|\n", "");
		
        this.httpUuid = uuid;
        setInitialized(true);
	}
	
	@Override
	public boolean performTimeStep(){
		
		ScalarSet input_flood_volumn = (ScalarSet)this.getValues(this.getInputExchangeItem(0).getQuantity().getID(), this.getInputExchangeItem(0).getElementSet().getID());
		
		for (int i = 0; i < input_flood_volumn.getCount(); i++) {
			v_water = input_flood_volumn.get(i);
		}
		if(v_water<0){
			v_water = 0.0;
		}
			
		
		TimeStamp time = (TimeStamp)this.getCurrentTime();
		Calendar curr_time = CalendarConverter.modifiedJulian2Gregorian(time.getModifiedJulianDay());
	
		String parameter = "?"+HttpGisVolumnModel.METHODKEY+"="+HttpGisVolumnModel.Method_getvalue;
		parameter += "&"+HttpGisVolumnModel.UUIDKEY+"="+this.httpUuid + "&"+HttpGisVolumnModel.Flood_Volumn_KEY+"="+v_water;
		String out_temp = RequestMethodStore.GETStr(WSModelUtils.appendToByte((httpUrl+parameter),900));
		Double result = Double.valueOf(out_temp.split("&")[0]);
		
		System.out.println("time:"+curr_time.getTime()+"; flood elevation:"+result);
		
		this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{result}));
		outputValues.put(curr_time, result);
		return true;
	}
	
	@Override
	public void finish(){
		String parameter = "?"+HttpGisVolumnModel.METHODKEY+"="+HttpGisVolumnModel.Method_Finish+"&"+HttpGisVolumnModel.UUIDKEY+"="+this.httpUuid;
		String out = RequestMethodStore.GETStr(httpUrl+parameter);
		System.out.println(out);
	}
	

}
