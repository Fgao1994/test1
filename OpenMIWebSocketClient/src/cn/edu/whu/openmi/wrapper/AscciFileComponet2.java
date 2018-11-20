package cn.edu.whu.openmi.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Element;
import nl.alterra.openmi.sdk.backbone.ElementSet;
import nl.alterra.openmi.sdk.backbone.InputExchangeItem;
import nl.alterra.openmi.sdk.backbone.LinkableComponent;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;
import nl.alterra.openmi.sdk.backbone.Quantity;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.IArgument;
import org.openmi.standard.ILink;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;


public class AscciFileComponet2 extends LinkableComponent {
	private static String ARGU = "fileconfig";
	private List<Double> timestamps = new ArrayList<Double>();
	private List<Double> timeValues = new ArrayList<Double>();
	private double _delta = 0.0000000001; 
	private double total = 0;
	public AscciFileComponet2(String id) {
		super(id);
	}

	public AscciFileComponet2() {
		super("AscciFile-OpenMI-Compliant-Component-2");

	}

	// 在LinkableComponent实现中，initialize方法，定义为final，其中调用的是initializeHook，
	// 所以直接实现initializeHook方法即可。
	@Override
	public void initializeHook(IArgument[] properties) {
		// TODO Auto-generated method stub
		String path = null;
		boolean quantityRead = false;
	    boolean elementsRead = false;
	    Quantity quantity = null;
	    ElementSet elementSet = null;
	    
	    InputExchangeItem inputExchangeItem = new InputExchangeItem(this, "AscciFileComponent2-input1");
	    inputExchangeItem.setQuantity(new Quantity("Flow"));
	    ElementSet inElementSet = new ElementSet();
	    inElementSet.setID("AscciFileComponent-2-inElementSet");
	    inElementSet.addElement(new Element("Loc1"));
	    inputExchangeItem.setElementSet(inElementSet);
	    this.inputExchangeItems.add(inputExchangeItem);
	    
//	    inputExchangeItem.setElementSet(elementSet)
	    OutputExchangeItem outputExchangeItem = new OutputExchangeItem(this, "OutputExchangeItem-1");
		for (IArgument argument : properties) {
			if (argument.getKey().equals(ARGU)) {
				path = argument.getValue();
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
					elementSet.setID("File Contents-2");
					elementSet.addElement(new Element(elements[1].trim()));
					/*for(String element:elements){
						elementSet.addElement(new Element(element.trim()));
					}*/
					outputExchangeItem.setElementSet(elementSet);
					this.outputExchangeItems.add(outputExchangeItem);
					continue;
				}
				
				String[] values = line.split(";");
				SimpleDateFormat sdf  =   new  SimpleDateFormat( " yyyy-MM-dd HH:mm " );  
				Calendar calendar = Calendar.getInstance();
				Date date=null;
				try {
					date = sdf.parse(values[0]);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				calendar.setTime(date);
				timestamps.add(CalendarConverter.gregorian2ModifiedJulian(calendar));
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
	public IValueSet getValuesHook(ITime time, ILink link)  {
		// TODO Auto-generated method stub
//		return super.getValuesHook(time, link);
		
		if (time instanceof TimeStamp) {
			TimeStamp timestamp = (TimeStamp)time;
			 double results;
		        for (int i = 0; i < timestamps.size(); i++)
		        {
		            if ( timestamps.get(i) + _delta > timestamp.getTime())
		            {
		                results =  timeValues.get(i);
		                return new ScalarSet(new double[]{results});
		            }
		        }
		}
		return new ScalarSet(new double[]{-1});
	}
	
	public static void main(String[] args) {
		Argument argument = new Argument(ARGU,
				"/cn/edu/whu/wrapper/AsciiReaderData.txt", true, "");
		AscciFileComponet2 ascciFileComponet = new AscciFileComponet2();
		ascciFileComponet.initialize(new Argument[] { argument });
		
	}

}
