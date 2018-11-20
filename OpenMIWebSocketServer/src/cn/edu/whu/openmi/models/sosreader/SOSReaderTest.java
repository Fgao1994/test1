package cn.edu.whu.openmi.models.sosreader;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;

public class SOSReaderTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		//Parameters for TOPModel
		Argument configFileArgument = new Argument("ConfigFile","F:/workspace/SOSReader-config.xml",true);
		Argument addressArgument = new Argument("Address","http://202.114.114.55:8088/52nSOSv3_WAR/sos",true);
		Argument versionArgument = new Argument("Version","1.0",true);
		Argument procedureArgument = new Argument("Procedure","urn:ogc:object:feature:sensor:coweeta:temperature_gauge_1",true);
		Argument offeringArgument = new Argument("Offering","CoweetaTemperatureMonitor",true);
		Argument propertyArgument = new Argument("ObservedProperty","urn:ogc:def:phenomenon:coweeta:AverageTemperature",true);
		
		//Initialize the SOSReader
		SOSReader sosReader = new SOSReader();
		sosReader.initialize(new Argument[]{configFileArgument,addressArgument,versionArgument,
				procedureArgument,offeringArgument,propertyArgument});
		
		
		ILinkableComponent trigger = new Trigger("Trigger");
		LinkManager linkManager = new LinkManager();
		
		//Create link between TopModel and Trigger
		Link triggerLink = new Link(linkManager, "Trigger");
		triggerLink.connect(sosReader, sosReader.getOutputExchangeItem(0), trigger, trigger.getInputExchangeItem(0));
		sosReader.addLink(triggerLink);
		trigger.addLink(triggerLink);
		
		//validate components
		trigger.validate();
		sosReader.validate();
		
		double endTime = sosReader.getTimeHorizon().getEnd().getModifiedJulianDay();
//		double timeStep = topModel.getTimeHorizon().;
		double timeStep = 1;
		
		double startTime = sosReader.getTimeHorizon().getStart().getModifiedJulianDay()+timeStep;
//		double timeStep = 1;
		double start = startTime,end = endTime+0.00001;
		while (start<end) {
			IValueSet value = trigger.getValues(new TimeStamp(start), triggerLink.getID());
			System.out.println("time is "+CalendarConverter.modifiedJulian2Gregorian(start).getTime().toString()+", value "+value);
			start+= timeStep;
		}
		trigger.finish();
		sosReader.finish();
		
		System.out.println("Successfully!");
		System.exit(0);
	
	}

}
