package cn.edu.whu.openmi.smw.examples.simpleadding;

import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

public class InvokeComponentATest {
	public static void main(String[] args) {

		Argument inputValue = new Argument(
				"InputData",
				"F:\\360yun\\sync\\openmi\\eclipseWS\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\simpleadding\\AsciiReaderData2.txt",
				true, "");
		Argument modelAConfig = new Argument(
				"ConfigFile",
				"F:\\360yun\\sync\\openmi\\eclipseWS\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\simpleadding\\RunOffComponentA-config.xml",
				true);
		RunOffComponentA modelA = new RunOffComponentA();
		modelA.initialize(new Argument[] { inputValue, modelAConfig });
		ILinkableComponent trigger = new Trigger("Trigger");

		LinkManager linkManager = new LinkManager();

		Link triggerLink = new Link(linkManager, "Trigger");
		triggerLink.connect(modelA, modelA.getOutputExchangeItem(0), trigger,
				trigger.getInputExchangeItem(0));
		modelA.addLink(triggerLink);
		trigger.addLink(triggerLink);
		trigger.validate();
		modelA.validate();
		
		double timeStep = modelA.getTimeStep();
		double startTime = modelA.getTimeHorizon().getStart().getModifiedJulianDay()+timeStep;
		double endTime = modelA.getTimeHorizon().getEnd().getModifiedJulianDay();
//		double timeStep = 0.125;
		double start = startTime,end = endTime+0.00001;
		while (start<end) {
			IValueSet value = trigger.getValues(new TimeStamp(start), triggerLink.getID());
			System.out.println("time is "+CalendarConverter.modifiedJulian2Gregorian(start).getTime().toString()+", value "+value);
			start+= timeStep;
		}
		
		modelA.finish();
	}
}
