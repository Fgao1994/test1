package cn.edu.whu.openmi.models.hargreaves2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.ITime;

import cn.edu.whu.openmi.models.data.PrecipitationEngine;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class HargreavesEngine extends SimpleWrapper{
	private static String OUTPUT_N="OutputData";
		
	//output
	private String outputFile;

	private double latitude;
	
	private Map<Calendar, Double> outValues = new LinkedHashMap<Calendar, Double>();
	public HargreavesEngine(){
		InputStream inConfig = PrecipitationEngine.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/models/hargreaves2/Hargreaves-config.xml");
		this.setVariablesFromConfigFile(inConfig);
	}
	@Override
	public void initialize(HashMap properties){
		super.initialize(properties);
		Object outputObj = properties.get(OUTPUT_N);
		if (outputObj == null) {
			this.outputFile = OpenMIUtilities.getCurrentWorkingDirectory()+File.separator+new Date().getSeconds()+"_HargreavesOutput.txt";
		}else{
			this.outputFile = outputObj.toString();
		}

		setInitialized(true);
	}
	@Override
	public boolean performTimeStep() {
		// TODO Auto-generated method stub
		ScalarSet temperatureSet = (ScalarSet)this.getValues(this.getInputExchangeItem(0).getQuantity().getID(),
				this.getInputExchangeItem(0).getElementSet().getID());	
		ITime time = this.getCurrentTime();
		if (time instanceof TimeStamp) {
			TimeStamp timestamp = (TimeStamp)time;
			if (temperatureSet.getCount()<3) 
				return false;
			
			double minTemp = temperatureSet.get(0);
	        double maxTemp = temperatureSet.get(1);
	        double averTemp = temperatureSet.get(2);
	        double[] sortTemp = doBubbleSort(new double[]{minTemp,maxTemp,averTemp});
// 	        double result = calculatePET(minTemp, maxTemp, averTemp, latitude);
 	        double result = calculatePET(sortTemp[0], sortTemp[2], sortTemp[1], latitude);
	        this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{result}));
	        this.outValues.put(CalendarConverter.modifiedJulian2Gregorian(timestamp.getModifiedJulianDay()), result);
		}
		return true;
	}
	
	//对获取的温度值进行排序，最低温度、平均温度、最高温度
	private double[] doBubbleSort(double[] src)
    {
       int len=src.length;
       for(int i=0;i<len;i++)
       {
           for(int j=i+1;j<len;j++)
           {
              double temp;
              if(src[i]>src[j])
              {
                  temp=src[j];
                  src[j]=src[i];
                  src[i]=temp;
              }            
           }
       }   
       return src;
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
		return "Hargreaves";
	}
}
