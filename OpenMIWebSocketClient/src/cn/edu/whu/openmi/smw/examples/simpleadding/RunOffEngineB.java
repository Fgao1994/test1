package cn.edu.whu.openmi.smw.examples.simpleadding;

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
import java.util.List;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.apache.log4j.Logger;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.ITime;

import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class RunOffEngineB extends SimpleWrapper{
	private static String CONFIGFILE = "ConfigFile";
	private static String INPUTDATA = "InputData";
	private List<Double> timestamps = new ArrayList<Double>();
	private List<Double> timeValues = new ArrayList<Double>();
	private double _delta = 0.0000000001; 
	private static String OUTPUTDATA = "OutputData";
	private BufferedWriter provOut = null;
	private String outData = null;
	private Logger logger = Logger.getLogger(RunOffEngineB.class);
	@Override
	public void initialize(HashMap properties) {
		// TODO Auto-generated method stub
		
		String config = properties.get(CONFIGFILE).toString();
		String inData = properties.get(INPUTDATA).toString();
		this.outData = properties.get(OUTPUTDATA).toString();
		this.setVariablesFromConfigFile(config);
		InputStream is=null;
		try {
			is = new FileInputStream(new File(inData));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				System.out.println(line);
				if (line.startsWith("//")) {
					continue;
				}
				String[] values = line.split(";");
				timestamps.add(OpenMIUtilities.str2JulianDate(values[0].trim(),"yyyy-MM-dd HH:mm"));
				timeValues.add(Double.parseDouble(values[2]));
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
		
		File provFile = new File(outData);
		if (!provFile.exists()) {
			try {
				provFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			provOut = new BufferedWriter(new FileWriter(provFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			provOut.write("TimeStamp,RunOffB");
			provOut.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	@Override
	public boolean performTimeStep() {
		// TODO Auto-generated method stub
		
		logger.info("(modelB)---(modelB)---(modelB)---(modelB)---(modelB)---(modelB)");
		logger.info("执行ModelB的PerformTime，计算下一时刻的值，当前时间："+OpenMIUtilities.timeStamp2Str((TimeStamp)(this.getCurrentTime())));
		ITime time = this.getCurrentTime();
		double timestep = this.getTimeStep();
		double results=0;
		if (time instanceof TimeStamp) {
			TimeStamp timestamp = (TimeStamp)time;
		        for (int i = 0; i < timestamps.size(); i++)
		        {
		        	
		            if ( timestamps.get(i) + _delta  > timestamp.getTime()+timestep)
		            {
		                results =  timeValues.get(i);
		                break;
		            }
		        }
		        this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{results}));
		        /*if (this.linkableRunEngine!=null) {
					this.linkableRunEngine.update
				}*/
		}
		this.advanceTime();
		logger.info("ModelB in "+OpenMIUtilities.timeStamp2Str((TimeStamp)this.getCurrentTime())+" is "+results);
		try {
			Calendar calendar = CalendarConverter.modifiedJulian2Gregorian(((TimeStamp)this.getCurrentTime()).getModifiedJulianDay());
			String timestr = OpenMIUtilities.calendar2Str(calendar, "yyyy-MM-dd HH:mm:ss");
			provOut.write(timestr+","+results);
			provOut.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		if (provOut != null) {
			try {
				provOut.flush();
				provOut.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
}
