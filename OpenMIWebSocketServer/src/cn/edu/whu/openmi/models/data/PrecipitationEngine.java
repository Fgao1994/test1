package cn.edu.whu.openmi.models.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;

import org.openmi.standard.ITime;

import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class PrecipitationEngine extends SimpleWrapper{
	private static String CONFIGFILE = "ConfigFile";
	private static String INPUTDATA = "InputData";
	private List<Double> timestamps = new ArrayList<Double>();
	private List<Double> timeValues = new ArrayList<Double>();
	private double _delta = 0.0000000001; 
	public PrecipitationEngine(){
		InputStream inConfig = PrecipitationModel.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/models/data/Precipitation-config.xml");
		this.setVariablesFromConfigFile(inConfig);
	}
	@Override
	public void initialize(HashMap properties) {
		// TODO Auto-generated method stub
		
//		String config = properties.get(CONFIGFILE).toString();
//		this.setVariablesFromConfigFile(config);
		String inData = properties.get(INPUTDATA).toString();
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
				String[] values = line.split("\t");
				timestamps.add(OpenMIUtilities.str2JulianDate(values[0].trim(),"yyyy/MM/dd H:mm"));
				timeValues.add(Double.parseDouble(values[1]));
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
	
	}

	@Override
	public boolean performTimeStep() {
		// TODO Auto-generated method stub
		
		ITime time = this.getCurrentTime();
		if (time instanceof TimeStamp) {
			TimeStamp timestamp = (TimeStamp)time;
			 double results=0;
		        for (int i = 0; i < timestamps.size(); i++)
		        {
		        	
		            if ( timestamps.get(i) + _delta > timestamp.getTime())
		            {
		                results =  timeValues.get(i);
		                break;
		            }
		        }
		        this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(new double[]{results}));
		        /*if (this.linkableRunEngine!=null) {
					this.linkableRunEngine.update
				}*/
//		        this.advanceTime();
		}
	//	this.advanceTime();
		return true;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}
	
	public static void main(String[] args){
		PrecipitationEngine engine = new PrecipitationEngine();
		System.out.println(engine);
		
	}
}
