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
import nl.alterra.openmi.sdk.backbone.InputExchangeItem;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;
import nl.alterra.openmi.sdk.backbone.Quantity;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.IArgument;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class SimpleWrapper1_2 extends SimpleWrapper{
	private static String ARGU = "fileconfig";
	private List<Double> timestamps = new ArrayList<Double>();
	private List<Double> timeValues = new ArrayList<Double>();
	private double _delta = 0.0000000001; 
	@Override
	public void initialize(HashMap properties) {
		// TODO Auto-generated method stub
		
		//initialize model's parameters
		this._modelDescription = "this is the simplewrapper for model-2";
		this._modelID ="model-2";
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
	    
	    //Create an InputExchangeItem
	    InputExchangeItem inputExchangeItem = new InputExchangeItem(linkableComponent, "InputExchangeItem-1");
	    ElementSet inElementSet = new ElementSet();
	    inElementSet.setID("inLocations");
	    inElementSet.addElement(new Element("loc2"));
	    this._elementSets.put(inElementSet.getID(), inElementSet);
	    inputExchangeItem.setElementSet(inElementSet);
	    Quantity inQuantity = new Quantity("flow");
	    inputExchangeItem.setQuantity(inQuantity);
	    this._inputs.add(inputExchangeItem);
	    
	    
	    OutputExchangeItem outputExchangeItem = new OutputExchangeItem(linkableComponent, "OutputExchangeItem-2");
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
					elementSet.setID("Locations");
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
		        
		        IValueSet firstValue = this.getValues(this.getInputExchangeItem(0).getQuantity().getID(), this.getInputExchangeItem(0).getElementSet().getID());
		        int num = 0;
		        double[] res = null; 
		        if (firstValue instanceof ScalarSet) {
					ScalarSet scalarSet = (ScalarSet)firstValue;
					num = scalarSet.getCount();
					res = new double[num+1];
					for(int i=0;i<num;i++){
						res[i] = scalarSet.get(i);
					}
					res[num] = results;
				}else {
					res = new double[2];
					res[0] = -1;
					res[1] = results;
				}
		        
		        this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(), this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(res));
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
