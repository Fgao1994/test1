package cn.edu.whu.openmi.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.Dimension;
import nl.alterra.openmi.sdk.backbone.ElementSet;
import nl.alterra.openmi.sdk.backbone.InputExchangeItem;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;
import nl.alterra.openmi.sdk.backbone.Quantity;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.backbone.Unit;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.dom4j.Element;
import org.openmi.standard.IDimension.DimensionBase;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IQuantity.ValueType;

public class OpenMIUtilities {
	private static int i = 0;
	public static double str2JulianDate(String datestr,String format){
		SimpleDateFormat sdf  =   new  SimpleDateFormat( format );  
		Calendar calendar = Calendar.getInstance();
		Date date=null;
		try {
			date = sdf.parse(datestr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calendar.setTime(date);
		return CalendarConverter.gregorian2ModifiedJulian(calendar);
	}
	
	
	public static String timeStamp2Str(TimeStamp timeStamp){
		Calendar calendar = CalendarConverter.modifiedJulian2Gregorian(timeStamp.getModifiedJulianDay());
		return OpenMIUtilities.calendar2Str(calendar, "yyyy-MM-dd HH:mm:ss");
	}
	/**
	 * 
	 * @param time the string of time
	 * @param format, time format, yyyy-MM-dd HH:mm:ss for example
	 * @return
	 */
	public static Date str2Date(String time,String format){
		SimpleDateFormat sdf  =   new  SimpleDateFormat( format );  
		Date date=null;
		try {
			date = sdf.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public static double date2MJulianDate(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return CalendarConverter.gregorian2ModifiedJulian(calendar);
	}
	
	public static OutputExchangeItem xml2OutputExchangeItem(Element outputItemXml,ILinkableComponent linkableComponent){
		Element quantityXml = outputItemXml.element("Quantity");
		Element elementSetXml = outputItemXml.element("ElementSet");
		String id = outputItemXml.elementText("ID");
		if (isEmpty(id)) {
			id = "OutputExchangeItem"+(i++);
		}
		OutputExchangeItem outputExchangeItem = new OutputExchangeItem(linkableComponent, id);
		String des = outputItemXml.elementText("Description");
		if (!isEmpty(des)) {
			outputExchangeItem.setDescription(des);
		}
		if (quantityXml!=null) {
			Quantity quantity = xml2Quantity(quantityXml);
			outputExchangeItem.setQuantity(quantity);
		}
		if (elementSetXml!=null) {
			ElementSet elementSet = xml2ElementSet(elementSetXml);
			outputExchangeItem.setElementSet(elementSet);
		}
		
		return outputExchangeItem;
	}
	
	public static InputExchangeItem xml2InputExchangeItem(Element inputItemXml,ILinkableComponent linkableComponent){
		Element quantityXml = inputItemXml.element("Quantity");
		Element elementSetXml = inputItemXml.element("ElementSet");
		String id = inputItemXml.elementText("ID");
		if (isEmpty(id)) {
			id = "OutputExchangeItem"+(i++);
		}
		InputExchangeItem inputExchangeItem = new InputExchangeItem(linkableComponent, id);
		String des = inputItemXml.elementText("Description");
		if (!isEmpty(des)) {
			inputExchangeItem.setDescription(des);
		}
		if (quantityXml!=null) {
			Quantity quantity = xml2Quantity(quantityXml);
			inputExchangeItem.setQuantity(quantity);
		}
		if (elementSetXml!=null) {
			ElementSet elementSet = xml2ElementSet(elementSetXml);
			inputExchangeItem.setElementSet(elementSet);
		}
		
		return inputExchangeItem;
	}
	// not handle the "ShapefilePath"
	public static ElementSet xml2ElementSet(Element elementSetXml){
		String id = elementSetXml.elementText("ID");
		ElementSet elementSet = new ElementSet();
		if (!isEmpty(id)) {
			elementSet.setID(id);
		}
		String des = elementSetXml.elementText("Description");
		if (!isEmpty(des)) {
			elementSet.setDescription(des);
		}
		return elementSet;
	}
	
	public static Quantity xml2Quantity(Element quantityXml){
		Quantity quantity = new Quantity();
		//id
		String id = quantityXml.elementText("ID");
		if (!isEmpty(id)) {
			quantity.setID(id);
		}
		//description
		String des = quantityXml.elementText("Description");
		if (!isEmpty(des)) {
			quantity.setDescription(des);
		}
		//ValueType
		String valueType = quantityXml.elementText("ValueType");
		if (valueType!=null && !(valueType.trim().equals(""))) {
			if (valueType.equalsIgnoreCase("Scalar")) {
				quantity.setValueType(ValueType.Scalar);
			}else if (valueType.equalsIgnoreCase("Vector")) {
				quantity.setValueType(ValueType.Vector);
			}
		}
		
		//unit
		Element unitEle = quantityXml.element("Unit");
		if (unitEle !=null) {
			Unit unit = new Unit();
			String unitId = unitEle.elementText("ID");
			if (!isEmpty(unitId)) {
				unit.setID(unitId);
			}
			String unitDes = unitEle.elementText("Description");
			if (!isEmpty(unitDes)) {
				unit.setDescription(unitDes);
			}
			
			String factor = unitEle.elementText("ConversionFactorToSI");
			if (!isEmpty(factor)) {
				unit.setConversionFactorToSI(Double.parseDouble(factor));
			}
			
			String offset = unitEle.elementText("OffSetToSI");
			if (!isEmpty(offset)) {
				unit.setOffSetToSI(Double.parseDouble(offset));
			}
			
			//Dimensions
			Element dimensionElement = quantityXml.element("Dimensions");
			if (dimensionElement != null) {
				List<Element> dimentionList = dimensionElement.elements("Dimension");
				Dimension dimension = new Dimension();
				for(Element dimentionEle:dimentionList){
					String base = dimentionEle.elementText("Base");
					String power = dimentionEle.elementText("Power");
					if (!isEmpty(base) && isEmpty(power)) {
						if (base.trim().equalsIgnoreCase("Length")) {
							dimension.setPower(DimensionBase.Length, Integer.parseInt(power));
						}else if (base.trim().equalsIgnoreCase("Time")) {
							dimension.setPower(DimensionBase.Time, Integer.parseInt(power));
						}else if (base.trim().equalsIgnoreCase("AmountOfSubstance")) {
							dimension.setPower(DimensionBase.AmountOfSubstance, Integer.parseInt(power));
						}else if (base.trim().equalsIgnoreCase("Currency")) {
							dimension.setPower(DimensionBase.Currency, Integer.parseInt(power));
						}else if (base.trim().equalsIgnoreCase("ElectricCurrent")) {
							dimension.setPower(DimensionBase.ElectricCurrent, Integer.parseInt(power));
						}else if (base.trim().equalsIgnoreCase("LuminousIntensity")) {
							dimension.setPower(DimensionBase.LuminousIntensity, Integer.parseInt(power));
						}else if (base.trim().equalsIgnoreCase("Mass")) {
							dimension.setPower(DimensionBase.Mass, Integer.parseInt(power));
						}else if (base.trim().equalsIgnoreCase("Temperature")) {
							dimension.setPower(DimensionBase.Temperature, Integer.parseInt(power));
						}
					}
				}
				quantity.setDimension(dimension);
			}
			
			quantity.setUnit(unit);
		}
		
		
		return quantity;
	}
	
	public static boolean isEmpty(String value){
		if (value !=null && !value.trim().equals("")) {
			return false;
		}
		return true;
	}

	
	public static double round(double d,int num){
		double multi = Math.pow(10, num);
		return (Math.round(d*multi)/multi);
	}
	
	public static double sum(double[] values){
		double res = 0.0;
		for(double val:values){
			res+= val;
		}
		return res;
	}
	
	public static String getCurrentWorkingDirectory() {
		String dir = System.getProperty("user.dir");
		return (dir != null) ? dir : ".";
		}
	
	public static String calendar2Str(Calendar calendar,String format){
		return (new SimpleDateFormat(format)).format(calendar.getTime()); 
	}
	public static void main(String[] args){
//		System.err.println(str2JulianDate("2012-01-21 12:21","yyyy-MM-dd HH:mm"));
//		System.out.println(round(12.23243673233, 5));
//		System.out.println(getCurrentWorkingDirectory());
	System.out.println(str2JulianDate("2000/6/14 0:00", "yyyy/MM/dd HH:mm"));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		System.out.println(calendar.getTime().toString());
		//MM必须大写,否则有问题
		System.out.println(calendar2Str(calendar,"yyyy-MM-dd hh:mm:ss"));
		System.out.println(str2JulianDate("2005-01-01 00:00", "yyyy-MM-dd HH:mm"));
		System.out.println(str2JulianDate("2005-01-01 03:00", "yyyy-MM-dd HH:mm"));
		System.out.println(str2JulianDate("2005-01-01 24:00", "yyyy-MM-dd HH:mm"));
		
	}
}
