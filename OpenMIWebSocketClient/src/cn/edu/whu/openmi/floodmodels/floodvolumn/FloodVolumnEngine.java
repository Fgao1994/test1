package cn.edu.whu.openmi.floodmodels.floodvolumn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openmi.standard.ITime;

import cn.edu.whu.openmi.floodmodels.Precipitation.PrecipitationEngine;

import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;
import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

public class FloodVolumnEngine extends SimpleWrapper{

	//String s_area = "s_area";
	private Double runoff;
	private Double s_area ; 
	private static String INPUTDATA = "input_file";
	private static String OUTPUTDATA = "out_file";
	private String out_file;
	private List<Double> timestamps = new ArrayList<Double>();
	private List<Double> timeValues = new ArrayList<Double>();
	private double _delta = 0.0000000001;
	private Map<Calendar, Double> outValues = new LinkedHashMap<Calendar, Double>();
	
	public FloodVolumnEngine(){
		InputStream inConfig = PrecipitationEngine.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/floodmodels/floodvolumn/FloodVolumn-config.xml");
		this.setVariablesFromConfigFile(inConfig);
	}
	@Override
	public void initialize(HashMap properties){
		super.initialize(properties);
		out_file = properties.get(OUTPUTDATA).toString();
		s_area = Double.parseDouble(properties.get("s_area").toString());
		String inData = properties.get(INPUTDATA).toString();
		InputStream is = null;
		try{
			is = new FileInputStream(new File(inData));
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try{
			for (String line = br.readLine();line!=null;line = br.readLine()) {
				//System.out.println(line);
				if(line.startsWith("//")){
					continue;
				}
				String[] values = line.split("\t");
				System.out.println(values[0]+","+values[1]);
				timestamps.add(OpenMIUtilities.str2JulianDate(values[0].trim(), "yyyy-MM-dd HH:mm:ss"));
				timeValues.add(Double.parseDouble(values[1]));
			}
		} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try{
				br.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
									
	}
	
	@Override
	public boolean performTimeStep() {
		
		ScalarSet input_runoff = (ScalarSet)this.getValues(this.getInputExchangeItem(0).getQuantity().getID(), this.getInputExchangeItem(0).getElementSet().getID());
		
		for (int i = 0; i < input_runoff.getCount(); i++) {
			System.out.println(input_runoff.get(i));
			//前i次累积降雨量
			runoff = input_runoff.get(i);			
		}
		
		ITime time = this.getCurrentTime();
		//TimeStamp time_tmp = (TimeStamp)time;
		//Calendar curr_time = CalendarConverter.modifiedJulian2Gregorian(time_tmp.getModifiedJulianDay());
        
		if (time instanceof TimeStamp) {
			TimeStamp timestamp = (TimeStamp)time;
			Calendar curr_time = CalendarConverter.modifiedJulian2Gregorian(timestamp.getModifiedJulianDay());
	        
			//get the outlet
			 Double outlet = null;
		        for (int i = 0; i < timestamps.size(); i++)
		        {
		            if ( timestamps.get(i) + _delta > timestamp.getTime())
		            {
		            	outlet =  timeValues.get(i);
		                break;
		            }
		        }
		        
		        double result = runoff/1000 * s_area-outlet;
		        if (result < 0) {
					result = 0.0;
				}
		        System.out.println("time:"+curr_time.getTime()+"; flood volumn:"+result);
		        
		        this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{result}));
			    outValues.put(curr_time, result);
		}
		return true;
	}
	
	@Override
	public void finish(){
		try {
			if (OpenMIUtilities.isEmpty(this.out_file)) {
				this.out_file = OpenMIUtilities.getCurrentWorkingDirectory()+File.separator+new Random().toString()+"_FloodVolumnOutPut.txt";
			}
			 File file = new File(this.out_file);
			 if (!file.exists()) {
				file.createNewFile();
			}
			 BufferedWriter bw = new BufferedWriter(new FileWriter(file));
	         //System.IO.Directory.CreateDirectory("wateroutput");
			 String line = "";
			 String timeFormat = "yyyy-MM-dd hh:mm:ss";
			 bw.write("The flood volumn....");
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
			 line = "Time["+timeFormat+"], FloodVolumn";
			 bw.write(line);
			 bw.newLine();
			 for(Calendar calendar:outValues.keySet()){
//				 String time = calendar.getTime().toString();
				 String time = OpenMIUtilities.calendar2Str(calendar, timeFormat);
				 Double flood_Volumn = outValues.get(calendar);
				 line = time+", "+flood_Volumn;
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

	public static void main(String[] args){
		
		String Local_Dir = "D:/websocket/flood/";
		Argument floodVolumnInputArgument = new Argument("input_file",Local_Dir+"FloodVolumnInput.txt",true);
		Argument floodVolumnAreaArgument = new Argument("s_area","7288.5214",true);
		Argument floodVolumnOutputArgument = new Argument("out_file",Local_Dir+"FloodVolumnOutput.txt",true);
		FloodVolumnModel floodVolumnModel = new FloodVolumnModel();
		floodVolumnModel.initialize(new Argument[]{floodVolumnAreaArgument,floodVolumnInputArgument,floodVolumnOutputArgument});
		
		FloodVolumnEngine engine = new FloodVolumnEngine();
		System.out.println(engine);
		
		//表查询数据
		String dbUrl = "jdbc:postgresql://202.114.118.190:5432/waterlogging";
		String dbAcc = "spark";
		String dbPwd = "spark";
		Map<String, Double> t_p = new HashMap<String,Double>();
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Couldn't find driver class:");
			e.printStackTrace();
		}
		
		
		try {
			Connection connInDriver = DriverManager.getConnection(dbUrl, dbAcc, dbPwd);
			Statement stateInDriver = connInDriver.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet area = stateInDriver
					.executeQuery("SELECT * FROM waterlogging WHERE areaid = '3' order by timestamp ");
			// String tmp = area.getString("precipitation");

			/*while (area.next()) {
				t_p.put(area.getString("timestamp"), area.getDouble("displacement"));
				System.out.println(area.getString("timestamp") + "	" + area.getDouble("displacement"));
			}*/
			ResultSet tmp = stateInDriver.executeQuery("SELECT * FROM area INNER JOIN landtype ON area.landtypeid=landtype.ltid WHERE areaid = 3 ");
			tmp.next();
			Double s = tmp.getDouble("area");
			System.out.println(s);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
}
