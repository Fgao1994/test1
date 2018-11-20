package cn.edu.whu.openmi.smw.examples.simpleadding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.apache.log4j.Logger;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;

import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class RunOffEngineC extends SimpleWrapper {
	private static String CONFIGFILE = "ConfigFile";
	private String outData = "F:/workspace/RunOffEngineCProvenance1.txt";
	// private File provFile = null;
	// private static String INPUTDATA = "InputData";
	private BufferedWriter provOut = null;
	private static String OUTPUTDATA = "OutputData";
	private Logger logger = Logger.getLogger(RunOffEngineC.class); 
	@Override
	public void initialize(HashMap properties) {
		// TODO Auto-generated method stub

		String config = properties.get(CONFIGFILE).toString();
		this.outData = properties.get(OUTPUTDATA).toString();
		// String inData = properties.get(INPUTDATA).toString();
		this.setVariablesFromConfigFile(config);

		String firstQuantity = this.getInputExchangeItem(0).getQuantity().getID();
		String firstElement = this.getInputExchangeItem(0).getElementSet().getID();
		String secondQuantity = this.getInputExchangeItem(1).getQuantity().getID();
		String secondElement = this.getInputExchangeItem(1).getElementSet().getID();
		
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
			provOut.write("TimeStamp,RunOffC,"+firstElement+"_"+firstQuantity+","+secondElement+"_"+secondQuantity);
			provOut.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean performTimeStep() {
		// TODO Auto-generated method stub
		// ITime time = this.getCurrentTime();
		logger.info("(modelC)---(modelC)---(modelC)---(modelC)---(modelC)---(modelC)");
		logger.info("执行ModelC的PerformTime，计算下一时刻的值，当前时间："+OpenMIUtilities.timeStamp2Str((TimeStamp)(this.getCurrentTime())));
		double result = 0;
		IValueSet firstValue = this.getValues(this.getInputExchangeItem(0)
				.getQuantity().getID(), this.getInputExchangeItem(0)
				.getElementSet().getID());
		IValueSet secondValue = this.getValues(this.getInputExchangeItem(1)
				.getQuantity().getID(), this.getInputExchangeItem(1)
				.getElementSet().getID());
		double firstResult = ((ScalarSet) firstValue).get(0);
		double secondResult = ((ScalarSet) secondValue).get(0);
		result = firstResult + secondResult;
		this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(),
				this.getOutputExchangeItem(0).getElementSet().getID(),
				new ScalarSet(new double[] { result }));
		logger.info("输入参数：(ModelA,"+firstResult+") (ModelB,"+secondResult+")");
		this.advanceTime();
		logger.info("ModelC in "+OpenMIUtilities.timeStamp2Str((TimeStamp)this.getCurrentTime())+" is "+result);
		String time = "";
		if (this.getCurrentTime() instanceof TimeStamp) {
			Calendar calendar = CalendarConverter.modifiedJulian2Gregorian(((TimeStamp)this.getCurrentTime()).getModifiedJulianDay());
			time = OpenMIUtilities.calendar2Str(calendar, "yyyy-MM-dd HH:mm:ss");
		}else {
			time = this.getCurrentTime().toString();
		}
		try {
			provOut.write(time+","+result+","+firstResult+","+secondResult);
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
