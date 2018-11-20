package cn.edu.whu.openmi.floodmodels.GisVolumn;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Date;
import java.sql.JDBCType;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import java.sql.*;

import cn.edu.whu.openmi.floodmodels.Precipitation.PrecipitationEngine;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

public class GisVolumnEngine extends SimpleWrapper{

	//private String dem_input;
	//private Double s_area;
	private String out_file;
	private Double v_water;
	private Double[][] dem_value = null;
	//private double[][] dem_array;
	private DemData dem_data;
	
	private Map<Calendar, Double> outputValues = new LinkedHashMap<Calendar, Double>();
	public GisVolumnEngine(){
		InputStream inConfig = PrecipitationEngine.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/floodmodels/GisVolumn/GisVolumn-Config.xml");
 		this.setVariablesFromConfigFile(inConfig);
	}
	
	@Override
	public void initialize(HashMap properties){
		super.initialize(properties);
		out_file = properties.get("out_file").toString();
		//dem_input = properties.get("dem_input").toString();
		//s_area = Double.parseDouble(properties.get("s_area").toString());
		dem_data = getDemData();
		
		//DEM数据预处理
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
        
		Double result = calWaterElevation(v_water, dem_value, dem_data.elevMin, dem_data.elevMax, Math.abs(dem_data.scaleX * dem_data.scaleY));
		System.out.println("time:"+curr_time.getTime()+"; flood elevation:"+result);
		
		this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{result}));
		outputValues.put(curr_time, result);
		
		return true;
		
	}
	
	@Override
	public void finish(){
		try {
			if (OpenMIUtilities.isEmpty(this.out_file)) {
				this.out_file = OpenMIUtilities.getCurrentWorkingDirectory()+File.separator+new Date().getSeconds()+"_GisVolumnOutPut.txt";
			}
			File file = new File(this.out_file);
			if(!file.exists()){
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			String line ="";
			String timeFormat = "yyyy-MM-dd hh:mm:ss";
			bw.write("The flood elev...");
			bw.newLine();
			Calendar startCalendar =CalendarConverter.modifiedJulian2Gregorian( this.getTimeHorizon().getStart().getModifiedJulianDay());
			 Calendar endCalendar =CalendarConverter.modifiedJulian2Gregorian( this.getTimeHorizon().getEnd().getModifiedJulianDay());
//			 line = "StartDate: "+startCalendar.getTime().toString();
			 line = "StartDate: "+OpenMIUtilities.calendar2Str(startCalendar, timeFormat);
			 bw.write(line);
			 bw.newLine();
//			 line = "EndDate: "+endCalendar.getTime().toString();
			 line = "EndDate: "+OpenMIUtilities.calendar2Str(endCalendar, timeFormat);
			 bw.write(line);
			 bw.newLine();
			 bw.newLine();
			 line = "Time["+timeFormat+"], Flood_Elevation";
			 bw.write(line);
			 bw.newLine();
			 
			 for (Calendar calendar:outputValues.keySet()) {
				String time = OpenMIUtilities.calendar2Str(calendar, timeFormat);
				line = time +","+outputValues.get(calendar);
				bw.write(line);
				bw.newLine();
			}
			 bw.flush();
			 bw.newLine();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.addError(e.getMessage());
		}
		
	}
	
	public Double calWaterElevation(Double volumn, Double[][] dem_value, Double elevMin, Double elevMax, Double areaUnit){
		Double eLow = elevMin;
		Double eHigh = elevMax;
		Double eMid = (eLow + eHigh) / 2.0;
		while (eHigh - eLow > 0.0001) {
		      Double vLow = volumn - calcWaterVolumn(eLow, dem_value, areaUnit);
		      Double vMid = volumn - calcWaterVolumn(eMid, dem_value, areaUnit);
		      if (vLow * vMid <= 0.0) {
		        eHigh = eMid;
		      } else {
		        eLow = eMid;
		      }
		      eMid = (eLow + eHigh) / 2.0;
		    }
		
		return eMid;
	}
	
	public Double calcWaterVolumn(Double elev_water, Double[][] dem_value, Double areaUnit){
		Double V = 0.0;
		for (int i = 0; i < dem_value.length; i++) {
			for (int j = 0; j < dem_value[i].length; j++) {
				if (dem_value[i][j]>-9999) {
					Double diff = elev_water - dem_value[i][j];
					if (diff > 0) {
						V += diff*areaUnit;
					}
				}
			}
		}
		return V;
	}
	
	public DemData getDemData(){
		
		String dbUrl = "jdbc:postgresql://202.114.118.190:5432/waterlogging";
		String dbAcc = "spark";
		String dbPwd = "spark";
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Couldn't find driver class:");
			e.printStackTrace();
		}
		
		
		try {
			Connection connInDriver = DriverManager.getConnection(dbUrl,dbAcc,dbPwd);
			Statement stateInDriver = connInDriver.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			
			/*ResultSet tmp = stateInDriver.executeQuery("SELECT * FROM area INNER JOIN landtype ON area.landtypeid=landtype.ltid WHERE areaid = 3 ");
			tmp.next();
			int cn = tmp.getInt("cn");
			System.out.println(cn);*/
			
			ResultSet dem = stateInDriver.executeQuery("SELECT ST_DumpValues(rast, 1, false) As dem " +
		              "FROM demclip WHERE rid=3");
			dem.next();
			dem_value = (Double[][])dem.getArray("dem").getArray();
	
			ResultSet dem_info = stateInDriver.executeQuery("SELECT (ST_SummaryStats(rast)).min, " +
			        "(ST_SummaryStats(rast)).max, " +
			        "(ST_MetaData(rast)).scalex, " +
			        "(ST_MetaData(rast)).scaley " +
			        "FROM public.dem");
			dem_info.next();
			
			if (!dem_value.equals(null)) {
				
				DemData dem_data = new DemData();
				dem_data.setElevMin(dem_info.getDouble("min"));
				dem_data.setElevMax(dem_info.getDouble("max"));
				dem_data.setScaleX(dem_info.getDouble("scalex"));
				dem_data.setScaleY(dem_info.getDouble("scaley"));
				
				connInDriver.close();
				return dem_data;			
			}else{
				
				connInDriver.close();
				return null;
			}
			/*while(area.next()){
				t_p.put(area.getString("timestamp"), area.getDouble("precipitation"));
				System.out.println(area.getString("timestamp")+"	"+area.getDouble("precipitation"));
			}*/
			//System.out.println(area.getRow());
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		
		
		
		//gdal.AllRegister();
		/*Dataset hDataset = gdal.Open(input_path,gdalconstConstants.GA_ReadOnly);
		if (hDataset == null)  
        {  
            System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());  
            System.err.println(gdal.GetLastErrorMsg());  
            //return null;
        } 
		
		int iXsize = hDataset.getRasterXSize();
		int iYsize = hDataset.getRasterYSize();
		Vector metadata = hDataset.GetMetadata_List();	
		for (int i = 0; i < metadata.size(); i++) {
			System.out.println(metadata.get(i));
		}*/
		/*Map<String, Double> dem_info = new HashMap<String , Double>();
		try {
			is = new FileInputStream(new File(input_path));
			
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line = br.readLine();
			String[] info = line.split(",");
			for (int i = 0; i < info.length; i++) {
				String[] tmp = info[i].split(":");
				dem_info.put(tmp[0], Double.parseDouble(tmp[1]));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
		
	}
	
	public static void main(String[] args){
		GisVolumnEngine engine = new GisVolumnEngine();
		DemData data = engine.getDemData();
		System.out.println(data.elevMax+";"+data.elevMin+";"+data.scaleX+";"+data.scaleY);
		//DemData data = engine.getDemData("D:\\websocket/DEM_Clip.tif");
		/*try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Couldn't find driver class:");
			e.printStackTrace();
		}*/
	}
}
