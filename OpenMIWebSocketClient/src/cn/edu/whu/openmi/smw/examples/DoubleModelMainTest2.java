package cn.edu.whu.openmi.smw.examples;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.Listeners;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IListener;
import org.openmi.standard.IValueSet;

import cn.edu.whu.openmi.event.EventListener;

public class DoubleModelMainTest2 {
	public static void main(String[] args){
		Argument argumentData = new Argument("InputData",
				"F:\\360yun\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\AsciiReaderData2.txt", true, "");
		Argument inputData1 = new Argument("ConfigFile", "F:\\360yun\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\SimpleWrapperComponet2-1-config.xml", true);
		Argument inputData2 = new Argument("ConfigFile", "F:\\360yun\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\SimpleWrapperComponet2-2-config.xml", true);
		SimpleWrapperComponet2_1 componet1 = new SimpleWrapperComponet2_1();
		componet1.initialize(new Argument[] { argumentData,inputData1 });
		SimpleWrapperComponet2_2 componet2 = new SimpleWrapperComponet2_2();
		componet2.initialize(new Argument[]{argumentData,inputData2});
		
//		ascciFileComponet.initialize(new Argument[] { argument });
		ILinkableComponent trigger      = new Trigger("Trigger");
		
		//ouputs
		/*
		 IQuantity out_Quantity = componet1.getOutputExchangeItem(0).getQuantity();
		IElementSet out_ElementSet = componet1.getOutputExchangeItem(0).getElementSet();
		*/
		LinkManager linkManager = new LinkManager();
		
		Link triggerLink = new Link(linkManager, "Trigger");
		triggerLink.connect(componet2, componet2.getOutputExchangeItem(0), trigger, trigger.getInputExchangeItem(0));
		componet2.addLink(triggerLink);
		trigger.addLink(triggerLink);
		
		Link one2twoLink = new Link(linkManager,"One2TwoLink");
		one2twoLink.connect(componet1, componet1.getOutputExchangeItem(0), componet2, componet2.getInputExchangeItem(0));
		componet1.addLink(one2twoLink);
		componet2.addLink(one2twoLink);
		
		//validate components
		trigger.validate();
		componet2.validate();
		componet1.validate();
		
		
		//--- Prepare Event listener ---
		IListener eventListener = new EventListener();
		for(int i =0;i <componet1.getPublishedEventTypeCount();i++){
			componet1.subscribe(eventListener, componet1.getPublishedEventType(i));
		}
		for(int i =0;i <componet2.getPublishedEventTypeCount();i++){
			componet1.subscribe(eventListener, componet2.getPublishedEventType(i));
		}
//		componet1.su
		/*IListener myListener = new EventListener();
		for (int i = 0; i < myListener.GetAcceptedEventTypeCount(); i++)
		{
		    for (int n = 0; n < MyRainModule.GetPublishedEventTypeCount(); n++)
		    {
		        if (myListener.GetAcceptedEventType(i) == MyRainModule.GetPublishedEventType(n))
		        {
		            MyRainModule.Subscribe(myListener, myListener.GetAcceptedEventType(i));
		        }
		    }
		    for (int n = 0; n < MyRRModel.GetPublishedEventTypeCount(); n++)
		    {
		        if (myListener.GetAcceptedEventType(i) == MyRRModel.GetPublishedEventType(n))
		        {
		            MyRRModel.Subscribe(myListener, myListener.GetAcceptedEventType(i));
		        }
		    }
		    for (int n = 0; n < MyRiverModel.GetPublishedEventTypeCount(); n++)
		    {
		        if (myListener.GetAcceptedEventType(i) == MyRiverModel.GetPublishedEventType(n))
		        {
		            MyRiverModel.Subscribe(myListener, myListener.GetAcceptedEventType(i));
		        }
		    }
		}*/
		
		
		double startTime = componet1.getTimeHorizon().getStart().getModifiedJulianDay();
		double endTime = componet1.getTimeHorizon().getEnd().getModifiedJulianDay();
		double timeStep = 0.125;
		double start = startTime,end = endTime+0.00001;
		while (start<end) {
			IValueSet value = trigger.getValues(new TimeStamp(start), triggerLink.getID());
			System.out.println("time is "+CalendarConverter.modifiedJulian2Gregorian(start).getTime().toString()+", value "+value);
			start+= timeStep;
		}
		componet1.finish();
		componet1.dispose();
		System.exit(0);
//		ILink triggerLink = new Link(componet1, out_ElementSet, out_Quantity, trigger, "", "", "RiverModel to Trigger Link", "RiverModelToTrigger", new ArrayList());
	}
}
