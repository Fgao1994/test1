package cn.edu.whu.openmi.smw.examples.simpleadding;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.apache.log4j.Logger;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;

import cn.edu.whu.openmi.util.InitEnv;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class ChainInvocation {
	public static void main(String[] args){
		InitEnv.doInit();
		Argument inputValue = new Argument("InputData",
				"F:\\360yun\\sync\\openmi\\eclipseWS\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\simpleadding\\AsciiReaderData2.txt", true, "");
		Argument modelAConfig = new Argument("ConfigFile", "F:\\360yun\\sync\\openmi\\eclipseWS\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\simpleadding\\RunOffComponentA-config.xml", true);
		Argument modelBConfig = new Argument("ConfigFile", "F:\\360yun\\sync\\openmi\\eclipseWS\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\simpleadding\\RunOffComponentB-config.xml", true);
		Argument modelCConfig = new Argument("ConfigFile", "F:\\360yun\\sync\\openmi\\eclipseWS\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\simpleadding\\RunOffComponentC-config.xml", true);
		
		Argument modelAOutput = new Argument("OutputData","F:\\workspace\\modelA_output.txt",true);
		Argument modelBOutput = new Argument("OutputData","F:\\workspace\\modelB_output.txt",true);
		Argument modelCOutput = new Argument("OutputData","F:\\workspace\\modelC_output.txt",true);
		/*Argument inputData2 = new Argument("ConfigFile", "F:\\360yun\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\examples\\simpleadding\\SimpleWrapperComponet2-2-config.xml", true);
		SimpleWrapperComponet2_1 componet1 = new SimpleWrapperComponet2_1();
		componet1.initialize(new Argument[] { inputValue,modelAConfig });
		SimpleWrapperComponet2_2 componet2 = new SimpleWrapperComponet2_2();
		componet2.initialize(new Argument[]{inputValue,inputData2});*/
		
		RunOffComponentA modelA = new RunOffComponentA();
		modelA.initialize(new Argument[]{inputValue,modelAConfig,modelAOutput});
		
		RunOffComponentB modelB = new RunOffComponentB();
		modelB.initialize(new Argument[]{inputValue,modelBConfig,modelBOutput});
		
		RunOffComponentC modelC = new RunOffComponentC();
		modelC.initialize(new Argument[]{modelCConfig,modelCOutput});
//		ascciFileComponet.initialize(new Argument[] { argument });
		ILinkableComponent trigger      = new Trigger("Trigger");
		
		//ouputs
		/*
		 IQuantity out_Quantity = componet1.getOutputExchangeItem(0).getQuantity();
		IElementSet out_ElementSet = componet1.getOutputExchangeItem(0).getElementSet();
		*/
		LinkManager linkManager = new LinkManager();
		
		Link triggerLink = new Link(linkManager, "Trigger");
		triggerLink.connect(modelC, modelC.getOutputExchangeItem(0), trigger, trigger.getInputExchangeItem(0));
		modelC.addLink(triggerLink);
		trigger.addLink(triggerLink);
		
		Link a2cLink = new Link(linkManager,"a2cLink");
		a2cLink.connect(modelA, modelA.getOutputExchangeItem(0), modelC, modelC.getInputExchangeItem(0));
		modelA.addLink(a2cLink);
		modelC.addLink(a2cLink);
		
		Link b2cLink = new Link(linkManager,"b2cLink");
		b2cLink.connect(modelB, modelB.getOutputExchangeItem(0), modelC, modelC.getInputExchangeItem(1));
		modelB.addLink(b2cLink);
		modelC.addLink(b2cLink);
		
		//validate components
		trigger.validate();
		modelA.validate();
		modelB.validate();
		modelC.validate();
		
		
		//--- Prepare Event listener ---
		/*IListener eventListener = new EventListener();
		for(int i =0;i <modelA.getPublishedEventTypeCount();i++){
			modelA.subscribe(eventListener, modelA.getPublishedEventType(i));
		}
		for(int i =0;i <modelB.getPublishedEventTypeCount();i++){
			modelB.subscribe(eventListener, modelB.getPublishedEventType(i));
		}
		for(int i =0;i <modelC.getPublishedEventTypeCount();i++){
			modelC.subscribe(eventListener, modelC.getPublishedEventType(i));
		}*/
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
		
		
//		double timeStep = modelC.getTimeStep();
//		double startTime = modelC.getTimeHorizon().getStart().getModifiedJulianDay()+timeStep;
//		double endTime = modelC.getTimeHorizon().getEnd().getModifiedJulianDay();
		double startTime = OpenMIUtilities.str2JulianDate("2005-01-01 03:00:00", "yyyy-MM-dd HH:mm:ss");
		double endTime = OpenMIUtilities.str2JulianDate("2005-01-01 24:00:00", "yyyy-MM-dd HH:mm:ss");
		double timeStep =  0.3;
//		double timeStep = 0.125;
		double start = startTime,end = endTime+0.00001;
		Logger logger = Logger.getLogger(ChainInvocation.class);
		while (start<end) {
			logger.info("(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)-(t)");
			logger.info("Trigger中获取"+OpenMIUtilities.timeStamp2Str(new TimeStamp(start))+"时刻的模拟值");
			IValueSet value =  trigger.getValues(new TimeStamp(start), triggerLink.getID());
			System.out.println("time is "+CalendarConverter.modifiedJulian2Gregorian(start).getTime().toString()+", value "+value);
			start+= timeStep;
		}
		modelA.finish();
		modelB.finish();
		modelC.finish();
		modelC.dispose();
		System.exit(0);
//		ILink triggerLink = new Link(componet1, out_ElementSet, out_Quantity, trigger, "", "", "RiverModel to Trigger Link", "RiverModelToTrigger", new ArrayList());
	}
}
