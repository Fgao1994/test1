package cn.edu.whu.openmi.floodmodels.Scscn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.org.apache.xml.internal.security.keys.keyresolver.implementations.PrivateKeyResolver;
import com.sun.swing.internal.plaf.basic.resources.basic_zh_TW;

import cn.edu.whu.openmi.floodmodels.Precipitation.PrecipitationEngine;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

public class ScscnEngine extends SimpleWrapper {
	
	private static String CONFIGFILE_N = "ConfigFile";
	private static String OUTPUT_N = "OutputData";
	private String inputTemp;
	private String configFile;
	private String out_file = null;
	private double P;
	private double PPT_ini = 0;
	private int cn_value;
	private Map<String, Integer> CN = new HashMap<String, Integer>();
	private Map<Calendar, ScscnData> outputValues = new LinkedHashMap<Calendar, ScscnData>();
	//private double _delta = 0.0000000001; 
	
	public ScscnEngine(){
		InputStream inconfig = PrecipitationEngine.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/floodmodels/Scscn/Scscn-Config.xml");
		this.setVariablesFromConfigFile(inconfig);
	}
	
	@Override
	public void initialize(HashMap properties) {
		super.initialize(properties);
		out_file = properties.get("out_file").toString();
		String cnType = properties.get("land_use").toString();
		//CN表，后续继续添加
		/*String dbUrl = "jdbc:postgresql://202.114.118.190:5432/waterlogging";
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
			
			ResultSet tmp = stateInDriver.executeQuery("SELECT * FROM area INNER JOIN landtype ON area.landtypeid=landtype.ltid WHERE areaid = 3 ");
			tmp.next();
			int cn = tmp.getInt("cn");
			System.out.println(cn);
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		CN.put("Cement Land", 49);
		CN.put("Asphalt Road", 76);
		CN.put("Grass Land and Park", 36);
		CN.put("Residential District and Vehicle Lane", 98);
		CN.put("Wood Land", 36);
		
		for(String land_type:CN.keySet()){
			if (land_type.equals(cnType)) {
				this.cn_value = CN.get(land_type);
				break;
			}
		}
		
		setInitialized(true);
	}
	
	@Override
	public boolean performTimeStep(){
		ScalarSet input_precip = (ScalarSet)this.getValues(this.getInputExchangeItem(0).getQuantity().getID(), this.getInputExchangeItem(0).getElementSet().getID());
		
		for (int i = 0; i < input_precip.getCount(); i++) {
			System.out.println(input_precip.get(i));
			//前i次累积降雨量
			P = input_precip.get(i);			
		}
		
		double result = calculateRunoff(P, cn_value);
		TimeStamp time = (TimeStamp)this.getCurrentTime();
		Calendar curr_time = CalendarConverter.modifiedJulian2Gregorian(time.getModifiedJulianDay());
		System.out.println("time:"+curr_time.getTime()+"; runoff:"+result);
		
		ScscnData modelData = new ScscnData();
		modelData.setPrecip(P);
		modelData.setTime(curr_time);
		modelData.setRunoff(result);
		outputValues.put(curr_time, modelData);
		//System.out.println(result);
		this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{result}));
		return true;
	}
	
	@Override
	public void finish(){
		try{
			if(OpenMIUtilities.isEmpty(this.out_file)){
				this.out_file = OpenMIUtilities.getCurrentWorkingDirectory()+File.separator+new Date().getSeconds()+"_ScscnOutput.txt";
				
			}
			File file = new File(this.out_file);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			String line = "";
			String timeFormat = "yyyy-MM-dd hh:mm:ss";
			bw.write("The runoff as time goes on... ");
			bw.newLine();
			Calendar startCalendar = CalendarConverter
					.modifiedJulian2Gregorian(this.getTimeHorizon().getStart().getModifiedJulianDay());
			Calendar endCalendar = CalendarConverter
					.modifiedJulian2Gregorian(this.getTimeHorizon().getEnd().getModifiedJulianDay());
			// line = "StartDate: "+startCalendar.getTime().toString();
			line = "StartDate: " + OpenMIUtilities.calendar2Str(startCalendar, timeFormat);
			bw.write(line);
			bw.newLine();
			// line = "EndDate: "+endCalendar.getTime().toString();
			line = "EndDate: " + OpenMIUtilities.calendar2Str(endCalendar, timeFormat);
			bw.write(line);
			bw.newLine();
			bw.newLine();
			line = "Time["+timeFormat+"], Precipitation, Runoff";
			bw.write(line);
			bw.newLine();
			for(Calendar calendar:outputValues.keySet()){
				String time = OpenMIUtilities.calendar2Str(calendar, timeFormat);
				ScscnData modelData = outputValues.get(calendar);
				line = time +","+modelData.getPrecip()+","+modelData.getRunoff();
				bw.write(line);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
			this.addError(e.getMessage());
		}
		
		
	}
	
	public double calculateRunoff(double P, int cn){

		//int cn = CN.get(landuse_type);
		double S = 25400/cn - 254;
		double I = 0.2*S;
		double runoff = 0.0;
		if(P > I){
			runoff = (P-I)*(P-I)/(P+S-I);
		}
		return runoff;
	}
	
	public static void main(String[] args){
		ScscnEngine engine = new ScscnEngine();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("land_use", "Grass Land and Park");
		map.put("out_file", "D:\\websocket\\flood/ScscnOutput.txt");
		engine.initialize(map);
		engine.performTimeStep();
	}
	 

}
