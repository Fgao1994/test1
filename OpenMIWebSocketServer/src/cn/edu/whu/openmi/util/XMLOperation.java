package cn.edu.whu.openmi.util;


import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XMLOperation {
	private static Namespace sosNS = new Namespace("sos",
			"http://www.opengis.net/sos/1.0"), owsNs = new Namespace("ows",
			"http://www.opengis.net/ows/1.1"), gmlNS = new Namespace("gml",
			"http://www.opengis.net/gml"), omns = new Namespace("om",
			"http://www.opengis.net/om/1.0"), swens = new Namespace("swe",
			"http://www.opengis.net/swe/1.0.1"), smlNs = new Namespace("sml",
			"http://www.opengis.net/sensorML/1.0.1");
	private static Map<String, String> nsMap = new HashMap<String, String>();
	public static String STARTTIMEID="starttime",ENDTIMEID="endtime",TIMEZONE="+00:00";
	static{
		nsMap.put(sosNS.getPrefix(), sosNS.getURI());
		nsMap.put(owsNs.getPrefix(), owsNs.getURI());
		nsMap.put(omns.getPrefix(), omns.getURI());
		nsMap.put(swens.getPrefix(), swens.getURI());
		nsMap.put(smlNs.getPrefix(), smlNs.getURI());
		nsMap.put(gmlNS.getPrefix(), gmlNS.getURI());
	}
	
	public static Document createBaseReq100(String offering,
			String procedure,String observedProperty){
		StringBuffer req = new StringBuffer();
		req.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<GetObservation xmlns=\"http://www.opengis.net/sos/1.0\" "
				+ " xmlns:ows=\"http://www.opengis.net/ows/1.1\"  "
				+ "xmlns:gml=\"http://www.opengis.net/gml\"  "
				+ "xmlns:ogc=\"http://www.opengis.net/ogc\"  "
				+ "xmlns:om=\"http://www.opengis.net/om/1.0\"  "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  "
				+ "xsi:schemaLocation=\"http://www.opengis.net/sos/1.0  "
				+ "http://schemas.opengis.net/sos/1.0.0/sosGetObservation.xsd\"  "
				+ "service=\"SOS\" version=\"1.0.0\" >");
		//offering
		if (!isNull(offering)) {
			String offerings[] = offering.split(";");
			for(String offer:offerings){
				if (isNull(offer)) 
					continue;
				req.append("<offering>");
				req.append(offer);
				req.append("</offering>");
			}
		}
		
		//eventTime
		req.append("<eventTime>    <ogc:TM_During>    <ogc:PropertyName>om:samplingTime</ogc:PropertyName>  <gml:TimePeriod> <gml:beginPosition>");
		req.append("2014-01-02T12:00:00").append(TIMEZONE);
//		req.append("2014-01-02T12:00:00+00:00");
		req.append("</gml:beginPosition><gml:endPosition>");
		req.append("2014-01-03T12:00:00").append(TIMEZONE);
//		req.append("2014-01-03T12:00:00+00:00");
		req.append("</gml:endPosition>   </gml:TimePeriod>  </ogc:TM_During></eventTime>");
		
		if (!isNull(procedure)) {
			String[] procedures = procedure.split(";");
			for(String str:procedures){
				if (!isNull(str)) {
					req.append("<procedure>");
					req.append(str);
					req.append("</procedure>");
				}
			}
		}
		
		if (!isNull(observedProperty)) {
			String[] obsProperties = observedProperty.split(";");
			for(String property:obsProperties){
				if (!isNull(property)) {
					req.append("<observedProperty>");
					req.append(property);
					req.append("</observedProperty>");
				}
			}
		}
		
		req.append("<responseFormat>text/xml;subtype=&quot;om/1.0.0&quot;</responseFormat></GetObservation>");
		
		try {
			Document doc = DocumentHelper.parseText(req.toString());
			if (doc != null) {
				Document formatDoc = formatXML(doc);
				if (formatDoc == null) {
					return doc;
				}
				return formatDoc;
			}
			
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Document formatXML(Document doc){
		OutputFormat formate = OutputFormat.createPrettyPrint();
		StringWriter out = new StringWriter();
		XMLWriter writer = new XMLWriter(out, formate);
		formate.setEncoding("UTF-8");
		try {
			writer.write(doc);
			return  DocumentHelper.parseText(out.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static boolean isNull(String str){
		if (str == null || str.trim().equals(""))
			return true;
		return false;
	}
	
	public static Document changeReqTime(String startTime,String endTime,Document doc){
		XPath startTimeXpath = DocumentHelper.createXPath("//gml:beginPosition");
		startTimeXpath.setNamespaceURIs(nsMap);
		Element startTimeElement = (Element)startTimeXpath.selectSingleNode(doc.getRootElement());
		startTimeElement.setText(startTime+TIMEZONE);
		
		XPath endTimeXPath = DocumentHelper.createXPath("//gml:endPosition");
		endTimeXPath.setNamespaceURIs(nsMap);
		Element endTimeElement = (Element)endTimeXPath.selectSingleNode(doc.getRootElement());
		endTimeElement.setText(endTime+TIMEZONE);
		return doc;
	}
	
	public static Double[] parseSOSResp(Document doc){
		XPath valueXPath = DocumentHelper
				.createXPath("om:member/om:Observation/om:result/swe:DataArray/swe:values");
		valueXPath.setNamespaceURIs(nsMap);
		Element valueElement = (Element)valueXPath.selectSingleNode(doc.getRootElement());
		if (valueElement == null) {
			return null;
		}
		String sepRecord = ";", sepValue = ",";
		String value = valueElement.getTextTrim();
		
		//获取最新的一条记录
		String records[] = value.split(sepRecord);
		int recordLength = records.length;
		if(recordLength == 0)
			return null;
		
		String lastRecord = records[recordLength-1];
		if (lastRecord.trim().equals("") && recordLength>2) {
			lastRecord = records[recordLength-2];
		}
		
		String numValue[] = lastRecord.split(sepValue);
		List<Double> numList = new ArrayList<Double>();
		for(int i=2;i<numValue.length;i++){
			try {
				numList.add(Double.parseDouble(numValue[i]));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		if (numList.size() == 0) 
			return null;
		Double[] results = new Double[numList.size()];
		numList.toArray(results);
		return results;
	}
	public static String changeTimeFormat(String timeStr){
		DateFormat oldFormat = new SimpleDateFormat("yyyy/MM/dd");  
		DateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Date date= null;
		try {
			date = oldFormat.parse(timeStr);
			return newFormat.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	
	public static String parseReposne(Document doc){
		
		return "";
	}
	
	public static void main(String[] args)
	{
		String property = "urn:ogc:def:phenomenon:coweeta:AverageTemperature;urn:ogc:def:phenomenon:coweeta:MaxTemperature";
		String offering = "CoweetaTemperatureMonitor";
		String procedure = "urn:ogc:object:feature:sensor:coweeta:temperature_gauge_1";
		Document doc = createBaseReq100(offering, procedure, property);
		System.out.println(doc.asXML());
		double timeStamp = 50121;
		Calendar endCalendar = CalendarConverter.modifiedJulian2Gregorian(timeStamp);
		String endTime = OpenMIUtilities.calendar2Str(endCalendar, "yyyy-MM-dd HH:mm:ss");
		endTime = endTime.replace(" ", "T");
		System.out.println(endTime);
	}
}
