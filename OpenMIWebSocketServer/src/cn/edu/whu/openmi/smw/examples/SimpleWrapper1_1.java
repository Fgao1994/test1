package cn.edu.whu.openmi.smw.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.Element;
import nl.alterra.openmi.sdk.backbone.ElementSet;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;
import nl.alterra.openmi.sdk.backbone.Quantity;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.IArgument;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.ITime;

import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class SimpleWrapper1_1 extends SimpleWrapper{
	private static String ARGU = "fileconfig";
	private List<Double> timestamps = new ArrayList<Double>();
	private List<Double> timeValues = new ArrayList<Double>();
	private double _delta = 0.0000000001; 
	@Override
	public void initialize(HashMap properties) {
		// TODO Auto-generated method stub
		
		//initialize model's parameters
		this._modelDescription = "this is the simplewrapper for model-1";
		this._modelID ="model-1";
		this._simulationStartTime = OpenMIUtilities.str2JulianDate("2005-01-01 00:00","yyyy-MM-dd HH:mm");
		this._simulationEndTime = OpenMIUtilities.str2JulianDate("2005-01-01 18:00","yyyy-MM-dd HH:mm");
		this._timeStep = 3.0/24; // the unit of timestep is day
//		this.
		
		// TODO Auto-generated method stub
		String path = null;
		boolean quantityRead = false;
	    boolean elementsRead = false;
	    Quantity quantity = null;
	    ElementSet elementSet = null;
	    
	    OutputExchangeItem outputExchangeItem = new OutputExchangeItem(linkableComponent, "OutputExchangeItem-1");
		for (Object obj:properties.keySet()) {
			if (obj.toString().equals(ARGU)) {
				path = properties.get(obj).toString();
				break;
			}
		}
		InputStream is = this.getClass().getResourceAsStream(path);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				System.out.println(line);
				if (line.startsWith("//")) {
					continue;
				}
				if (!quantityRead) {
					quantity = new Quantity(line.trim());
					quantityRead = true;
					outputExchangeItem.setQuantity(quantity);
					continue;
				}
				if (!elementsRead) {
					String[] elements = line.split(";");
					elementSet = new ElementSet();
					elementSet.setID("File Contents-1");
					elementSet.addElement(new Element(elements[0].trim()));
					this._elementSets.put(elementSet.getID(), elementSet);
					/*for(String element:elements){
						elementSet.addElement(new Element(element.trim()));
					}*/
					outputExchangeItem.setElementSet(elementSet);
					this._outputs.add(outputExchangeItem);
					elementsRead = true;
					continue;
				}
				
				String[] values = line.split(";");
				/*SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd HH:mm" );  
				Calendar calendar = Calendar.getInstance();
				Date date=null;
				try {
					date = sdf.parse(values[0].trim());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				calendar.setTime(date);
				timestamps.add(CalendarConverter.gregorian2ModifiedJulian(calendar));*/
				timestamps.add(OpenMIUtilities.str2JulianDate(values[0].trim(),"yyyy-MM-dd HH:mm"));
				//Double[] locs = new Double[3];
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
		return true;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}
	
}
