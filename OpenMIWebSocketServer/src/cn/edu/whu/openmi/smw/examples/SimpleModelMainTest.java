package cn.edu.whu.openmi.smw.examples;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;

public class SimpleModelMainTest {
	public static void main(String[] args){
		Argument argument = new Argument("fileconfig",
				"/cn/edu/whu/openmi/wrapper/AsciiReaderData.txt", true, "");
		SimpleWrapperComponet1_1 componet1 = new SimpleWrapperComponet1_1();
		componet1.initialize(new Argument[] { argument });
//		ascciFileComponet.initialize(new Argument[] { argument });
		ILinkableComponent trigger      = new Trigger("Trigger1");
		
		//ouputs
		/*
		 IQuantity out_Quantity = componet1.getOutputExchangeItem(0).getQuantity();
		IElementSet out_ElementSet = componet1.getOutputExchangeItem(0).getElementSet();
		*/
		
		
		Link link = new Link(new LinkManager(), "Trigger");
		link.connect(componet1, componet1.getOutputExchangeItem(0), trigger, trigger.getInputExchangeItem(0));
		
		componet1.addLink(link);
		trigger.addLink(link);
		
		trigger.validate();
		componet1.validate();
		
		double startTime = componet1.getTimeHorizon().getStart().getModifiedJulianDay();
		double endTime = componet1.getTimeHorizon().getEnd().getModifiedJulianDay();
		double timeStep = 0.125;
		double start = startTime,end = endTime+0.00001;
		while (start<end) {
			IValueSet value = trigger.getValues(new TimeStamp(start), link.getID());
			System.out.println("time is "+CalendarConverter.modifiedJulian2Gregorian(start).getTime().toString()+", value "+value);
			start+= timeStep;
		}
		componet1.finish();
		componet1.dispose();
		System.exit(0);
//		ILink triggerLink = new Link(componet1, out_ElementSet, out_Quantity, trigger, "", "", "RiverModel to Trigger Link", "RiverModelToTrigger", new ArrayList());
	}
}
