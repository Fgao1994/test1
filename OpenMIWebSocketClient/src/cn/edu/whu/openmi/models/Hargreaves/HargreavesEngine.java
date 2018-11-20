package cn.edu.whu.openmi.models.Hargreaves;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.buffer.SmartBuffer;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.ITime;

import cn.edu.whu.openmi.models.data.PrecipitationEngine;
import cn.edu.whu.openmi.provenance.ProvUtils;
import cn.edu.whu.openmi.provenance.SmartBufferInfo;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class HargreavesEngine extends SimpleWrapper{
	private static String INPUT_N = "InputData",CONFIGFILE_N="ConfigFile",OUTPUT_N="OutputData",PROV_N="ProvOut";
	
	//input temperature,format is "yyyy-MM-dd HH:mm:ss;MinTemp;MaxTemp;AverTemp
	private String inputTemp ;
	
	//configuration file for the model
	private String configFile;
	
	//output
	private String outputFile;
	
	//Provenance
	private String provFile;
	
	//add by zmd,20160114, 记录溯源
//	protected Map<ITime, SmartBuffer> bufferInfos = null; 
	protected List<SmartBufferInfo> bufferInfos = null;
	
	//variables used in this model.
	private List<Double> timestamps = new ArrayList<Double>();
	private List<Double[]> timeValues = new ArrayList<Double[]>();
	private double _delta = 0.0000000001; 
	private double latitude,noValue;
	private String split = "\t";
	
	private Map<Calendar, Double> outValues = new LinkedHashMap<Calendar, Double>();
	public HargreavesEngine(){
		InputStream inConfig = PrecipitationEngine.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/models/Hargreaves/Hargreaves-config.xml");
		this.setVariablesFromConfigFile(inConfig);
	}
	@Override
	public void initialize(HashMap properties){
		super.initialize(properties);
		Object inputObj = properties.get(INPUT_N);
//		Object configObj = properties.get(CONFIGFILE_N);
		Object outputObj = properties.get(OUTPUT_N);
		Object provObj = properties.get(PROV_N);
		if (inputObj == null) {
			this.addError("The parameter named InputData is needed");
			setInitialized(false);
			return;
		}else{
			this.inputTemp = inputObj.toString();
		}
		/*if (configObj == null) {
			this.addError("The parameter named ConfigFile is needed");
			setInitialized(false);
			return;
		}else{
			this.configFile = configObj.toString();
		}*/
		
		if (outputObj == null) {
			this.outputFile = OpenMIUtilities.getCurrentWorkingDirectory()+File.separator+new Date().getSeconds()+"_HargreavesOutput.txt";
		}else{
			this.outputFile = outputObj.toString();
		}
		
		if (provObj != null) {
			this.provFile = provObj.toString();
		}
		/*if (!this.setVariablesFromConfigFile(this.configFile)) {
			this.addError("Error occured when setting Variables using ConfigFile");
			setInitialized(false);
			return;
		}*/
		
		InputStream is=null;
		try {
			is = new FileInputStream(new File(this.inputTemp));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			this.addError(e1.getMessage());
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			int i = 0;
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
//				System.out.println(line);
				if (line.startsWith("//") || line.trim().equals("")) {
					continue;
				}
				//get the latitude in the first line.
				if (i==0) {
					String[] meta = line.split(";");
					latitude = Double.parseDouble(meta[0]);
					noValue = Double.parseDouble(meta[2]);
					i=1;
					continue;
				}
				String[] values = line.split(split);
				// yyyy-MM-dd HH:mm:ss,2000/6/27 0:00
				timestamps.add(OpenMIUtilities.str2JulianDate(values[0].trim(),"yyyy/MM/dd HH:mm"));
				Double[] temps = new Double[]{Double.parseDouble(values[1]),Double.parseDouble(values[2]),Double.parseDouble(values[3])};
				timeValues.add(temps);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		setInitialized(true);
	}
	@Override
	public boolean performTimeStep() {
		// TODO Auto-generated method stub
				
		ITime time = this.getCurrentTime();
		if (time instanceof TimeStamp) {
			TimeStamp timestamp = (TimeStamp)time;
			
			//get the temperatures
			 Double[] temps = null;
		        for (int i = 0; i < timestamps.size(); i++)
		        {
		            if ( timestamps.get(i) + _delta > timestamp.getTime())
		            {
		                temps =  timeValues.get(i);
		                break;
		            }
		        }
		        double minTemp = temps[0];
		        double maxTemp = temps[1];
		        double averTemp = temps[2];
		        double result = calculatePET(minTemp, maxTemp, averTemp, latitude);
		        this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{result}));
		        this.outValues.put(CalendarConverter.modifiedJulian2Gregorian(timestamp.getModifiedJulianDay()), result);
		}
	    //modified by zmd,20151026
		//this.advanceTime();
		return true;
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
		 
		if(!OpenMIUtilities.isEmpty(this.provFile) ){
			ProvUtils.saveProv2File(provFile, bufferInfos);
		}
	}

	@Override
	public String getComponentID() {
		// TODO Auto-generated method stub
		return "Hargreaves";
	}
	
	public void setBufferInfos(List<SmartBufferInfo> bufferInfos){
		this.bufferInfos = bufferInfos;
	}
}
