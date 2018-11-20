package cn.edu.whu.openmi.floodmodels.Precipitation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;
import org.openmi.standard.ITime;

import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;
import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

public class PrecipitationEngine extends SimpleWrapper{
	private static String CONFIGFILE = "ConfigFile";
	private static String INPUTDATA = "input_file";
	private List<Double> timestamps = new ArrayList<Double>();
	private List<Double> timeValues = new ArrayList<Double>();
	private double _delta = 0.0000000001;
	public PrecipitationEngine(){
		InputStream inconfig = PrecipitationModel.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/floodmodels/Precipitation/Precipitation-config.xml");
		this.setVariablesFromConfigFile(inconfig);
	}
	@Override
	public void initialize(HashMap properties){
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
	public boolean performTimeStep(){
		Double _time = null;
		ITime time = this.getCurrentTime();
		if(time instanceof TimeStamp){
			TimeStamp timestamp = (TimeStamp)time;
			double results = 0;
			for (int i = 0; i < timestamps.size(); i++) {
				if (timestamps.get(i)+_delta > timestamp.getTime()) {
					results = timeValues.get(i);
					_time = timestamps.get(i);
					break;
				}
			}
			Calendar curr_time = CalendarConverter.modifiedJulian2Gregorian(timestamp.getModifiedJulianDay());
			System.out.println("time:"+curr_time.getTime()+"; precipitation:"+results);
			System.out.println("time:"+curr_time.getTimeInMillis()+"; precipitation:"+results);
			this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{results}));
		}
		
		return true;
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}
	
	public static void main(String[] args){
		String Local_Dir = "D:/websocket/flood/";
		Argument precipInputArgument = new Argument("input_file",Local_Dir+"PrecipitationInput.txt",true);
		PrecipitationModel precipitationModel = new PrecipitationModel();
		precipitationModel.initialize(new Argument[]{precipInputArgument});
		
		PrecipitationEngine engine = new PrecipitationEngine();
		System.out.println(engine);
		/*Map<String, Double> t_p = new HashMap<String,Double>(); 
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
			ResultSet area = stateInDriver.executeQuery("SELECT * FROM waterlogging WHERE areaid = '3' order by timestamp ");
			//String tmp = area.getString("precipitation");
			
			while(area.next()){
				t_p.put(area.getString("timestamp"), area.getDouble("precipitation"));
				System.out.println(area.getString("timestamp")+"	"+area.getDouble("precipitation"));
			}
			//System.out.println(area.getRow());
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		*/
	}

}
