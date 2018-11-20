package cn.edu.whu.openmi.models.sosreader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.dom4j.Document;
import org.openmi.standard.ITime;

import cn.edu.whu.openmi.models.data.PrecipitationEngine;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;
import cn.edu.whu.openmi.util.RequestMethodStore;
import cn.edu.whu.openmi.util.XMLOperation;

public class SOSReaderEngine extends SimpleWrapper{

	//能不用静态变量的就不要用。
	private  String ADDRESS = "Address",VERSION="Version",
			PROCEDURE="Procedure",OFFERING="Offering",
			OBSERVEDPROPERTY="ObservedProperty";
	private  Document REQ_DOC = null;
	private  String sosAddress = "",sosVersion="";
	
	//output
	private String outputFile;

	private double latitude;
	
	private Map<Calendar, Double> outValues = new LinkedHashMap<Calendar, Double>();
	public SOSReaderEngine(){
		InputStream inConfig = PrecipitationEngine.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/models/sosreader/SOSReader-config.xml");
		this.setVariablesFromConfigFile(inConfig);
	}
	//构建好请求的Request，在请求的时候，仅仅更改时间。
	@Override
	public void initialize(HashMap properties){
		super.initialize(properties);
		//获取参数
		sosAddress = properties.get(ADDRESS).toString();
		sosVersion = properties.get(VERSION).toString();
		String offering = properties.get(OFFERING).toString();
		String procedure = properties.get(PROCEDURE).toString();
		String observedProperty= properties.get(OBSERVEDPROPERTY).toString();
		REQ_DOC = XMLOperation.createBaseReq100(offering, procedure, observedProperty);
		if (REQ_DOC == null || XMLOperation.isNull(sosAddress)) {
			System.err.println("Failed to build the request.");
			return;
		}
		
		setInitialized(true);
	}
	@Override
	public boolean performTimeStep() {
		// TODO Auto-generated method stub
		ITime time = this.getCurrentTime();
		
		if(!(time instanceof TimeStamp))
			return false;
		
		double timeStep = getTimeStep();
		double timeStamp = ((TimeStamp)time).getModifiedJulianDay();
		
		//起始时间为上一步时间点，但不包括该时间点。
		String startTime = formatTime(timeStamp-timeStep+Double.MIN_VALUE);
		//终止时间即为请求的时间点。对于结果只取最近的观测值。
		String endTime = formatTime(timeStamp);
		
		Document reqDoc = XMLOperation.changeReqTime(startTime, endTime, REQ_DOC);
		Document respDoc = RequestMethodStore.POSTDoc(reqDoc.asXML(), sosAddress);
		Double[] results = XMLOperation.parseSOSResp(respDoc);
		
		//获取不到值的时候直接跳过。
		if (results!=null && results.length!=0) {
			this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(results));
		}
	  
		return true;
	}
	private String formatTime(double time){
		Calendar calendar = CalendarConverter.modifiedJulian2Gregorian(time);
		String formatTime = OpenMIUtilities.calendar2Str(calendar, "yyyy-MM-dd HH:mm:ss");
		formatTime = formatTime.replace(" ", "T");
		return formatTime;
	}
	private double calculatePET(double minTemp,double maxTemp,double averTemp,double latitude){
		TimeStamp ts = (TimeStamp)this.getCurrentTime();
        Calendar calendar = CalendarConverter.modifiedJulian2Gregorian(ts.getModifiedJulianDay());
//        int j = dt.DayOfYear;
        int j = calendar.get(Calendar.DAY_OF_YEAR);
        double dr = 1 + 0.033 * Math.cos((2 * Math.PI * j) / 365);

        //---- calculate the solar declination
        double d = 0.4093 * Math.sin((2 * Math.PI * j) / 365 - 1.405);

        //---- calculate the sunset hour angle
        //-- get latitude in degrees
    /*    ElementSet es = (ElementSet)this.GetInputExchangeItem(0).ElementSet;
        Element e = es.GetElement(eid);*/
//        double p = e.GetVertex(0).y * Math.PI / 180;
        double p = this.latitude * Math.PI / 180;
        //-- calc ws
        double ws = Math.acos(-1 * Math.tan(p) * Math.tan(d));

        //---- calculate the total incoming extra terrestrial solar radiation (tested against http://www.engr.scu.edu/~emaurer/tools/calc_solar_cgi.pl)
        double Ra = 15.392 * dr * (ws * Math.sin(p) * Math.sin(d) + Math.cos(p) * Math.cos(d) * Math.sin(ws));

        //---- calculate PET (From Hargreaves and Samani 1985)
        //-- calculate latent heat of vaporization (from Water Resources Engineering, David A. Chin)
        double L = 2.501 - 0.002361 *averTemp;
        double PET = (0.0023 * Ra * Math.sqrt(maxTemp- minTemp) * (averTemp+ 17.8)) / L;

		return PET;
	}
	@Override
	public void finish() {
		// TODO Auto-generated method stub
//		new ElementSet().getElement(0).getVertex(0).getY();

		// TODO Auto-generated method stub
		try {
			if (OpenMIUtilities.isEmpty(this.outputFile)) {
				this.outputFile = OpenMIUtilities.getCurrentWorkingDirectory()+File.separator+new Random().toString()+"_TopModelOutPut.txt";
			}
			 File file = new File(this.outputFile);
			 if (!file.exists()) {
				file.createNewFile();
			}
			 BufferedWriter bw = new BufferedWriter(new FileWriter(file));
	         //System.IO.Directory.CreateDirectory("wateroutput");
			 String line = "";
			 String timeFormat = "yyyy-MM-dd hh:mm:ss";
			 bw.write("Daily evaporation....");
			 bw.newLine();
			 Calendar startCalendar =CalendarConverter.modifiedJulian2Gregorian( this.getTimeHorizon().getStart().getModifiedJulianDay());
			 Calendar endCalendar =CalendarConverter.modifiedJulian2Gregorian( this.getTimeHorizon().getEnd().getModifiedJulianDay());
			 line = "StartDate: "+OpenMIUtilities.calendar2Str(startCalendar, timeFormat);
//			 line = "StartDate: "+startCalendar.getTime().toString();
			 bw.write(line);
			 bw.newLine();
			 line = "EndDate: "+OpenMIUtilities.calendar2Str(endCalendar, timeFormat);
//			 line = "EndDate: "+endCalendar.getTime().toString();
			 bw.write(line);
			 bw.newLine();
			 bw.newLine();
			 line = "Time["+timeFormat+"], PET";
			 bw.write(line);
			 bw.newLine();

			 for(Calendar calendar:outValues.keySet()){
//				 String time = calendar.getTime().toString();
				 String time = OpenMIUtilities.calendar2Str(calendar, timeFormat);
				 double runoff = outValues.get(calendar);
				 line = time+", "+runoff;
				 bw.write(line);
				 bw.newLine();
			 }
	        bw.flush();
	        bw.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.addError(e.getMessage());
		}
		 
	}

	@Override
	public String getComponentID() {
		// TODO Auto-generated method stub
		return "SOSReader";
	}
}
