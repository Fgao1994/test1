package cn.edu.whu.mqtt;

import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;

import cn.edu.whu.httpmodel.HttpTopmodelMainTest;
//import cn.edu.whu.httpmodel.HttpTopModel;
import cn.edu.whu.openmi.models.Hargreaves.HargreavesModel;
import cn.edu.whu.openmi.models.data.PrecipitationModel;
import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;

public class MqttTopmodelMainTest {

public static void execute(){
		
		//虚拟机中运行
		
		//String Base_Dir = "/home/fgao/"
		String Local_Dir = "D:/websocket/openmi1/";
		String Base_Dir = "/home/fgao/MQTT/openmi4/";
		//Parameters for Precipitation

		Argument precipInputArgument = new Argument("InputData",Local_Dir +"PrecipitationInput.txt",true);
		PrecipitationModel precipitationModel = new PrecipitationModel();
		precipitationModel.initialize(new Argument[]{precipInputArgument});
		
		//Parameters for Hargreaves
		Argument petOutPutArgument = new Argument("OutputData", Local_Dir + "Hargreaves_output.txt", true);
		Argument petInputFileArgument = new Argument("InputData",Local_Dir +"TemperatureInput-all.txt",true);
		Argument petProvOutArgument = new Argument("ProvOut",Local_Dir+"Hargreaves_prov.txt",true);
		HargreavesModel hargreavesModel = new HargreavesModel();
		hargreavesModel.initialize(new Argument[]{petInputFileArgument,petOutPutArgument,petProvOutArgument});
//		hargreavesModel.getTimeHorizon()

		//Parameters for TOPModel
		Argument TopmodelTIArgument = new Argument("TI",Base_Dir+"TI_raster.txt",true);
		Argument TopmodeloutputFileArgument = new Argument("OutputData",Base_Dir+"Topmodel_output.txt",true);
		Argument topmodelProvOutArgument = new Argument("ProvOut",Base_Dir+"Topmodel_prov.txt",true);
		Argument TopmodelmArgument = new Argument("m","90",true);
		Argument TopmodelTmaxArgument = new Argument("Tmax","240000",true);
		Argument TopmodelRArgument = new Argument("R","9.66",true);
		Argument TopmodelInterceptionArgument = new Argument("Interception","3",true);
		Argument TopmodelareaArgument = new Argument("WatershedArea_SquareMeters","124800",true);
		
		//Initialize the Topodel
		long topmodelinitializestart=System.currentTimeMillis();
		
		MqttTopModel topModel = new MqttTopModel();
		//MqttTopModel topModel = new MqttTopModel();
		
		topModel.initialize(new Argument[]{TopmodelTIArgument,TopmodeloutputFileArgument,TopmodelmArgument,
				TopmodelTmaxArgument,TopmodelRArgument,TopmodelInterceptionArgument,TopmodelareaArgument,topmodelProvOutArgument});
		long topmodelinitializeend = System.currentTimeMillis();
		System.out.println("initialize时间："+(topmodelinitializeend-topmodelinitializestart));
		
		ILinkableComponent trigger = new Trigger("Trigger");
		LinkManager linkManager = new LinkManager();
		
		//Create link between Precipitation and TopModel
		Link precipLink = new Link(linkManager,"PrecipLink");
		precipLink.connect(precipitationModel, precipitationModel.getOutputExchangeItem(0), topModel, topModel.getInputExchangeItem(0));
		precipitationModel.addLink(precipLink);
		topModel.addLink(precipLink);
		
		//Create link between Hargreaves and TopModel
		Link petLink = new Link(linkManager,"PETLink");
		petLink.connect(hargreavesModel, hargreavesModel.getOutputExchangeItem(0), topModel, topModel.getInputExchangeItem(1));
		hargreavesModel.addLink(petLink);
		topModel.addLink(petLink);
		
		//Create link between TopModel and Trigger
		Link triggerLink = new Link(linkManager, "Trigger");
		triggerLink.connect(topModel, topModel.getOutputExchangeItem(0), trigger, trigger.getInputExchangeItem(0));
		topModel.addLink(triggerLink);
		trigger.addLink(triggerLink);
		
		//validate components
		trigger.validate(); 
		topModel.validate();
		precipitationModel.validate();
		hargreavesModel.validate();
		
		double endTime = topModel.getTimeHorizon().getEnd().getModifiedJulianDay();
//		double timeStep = topModel.getTimeHorizon().;
		double timeStep = topModel.getTimeStep();
		
		double startTime = topModel.getTimeHorizon().getStart().getModifiedJulianDay()+timeStep;
//		double timeStep = 1;
		double start = startTime,end = endTime+0.00001;
		int num = 0;
		while (start<end) {
			IValueSet value = trigger.getValues(new TimeStamp(start), triggerLink.getID());
//			System.out.println("time is "+CalendarConverter.modifiedJulian2Gregorian(start).getTime().toString()+", value "+value);
			start+= timeStep;
			num++;
		}
		trigger.finish();
		topModel.finish();
		precipitationModel.finish();
		hargreavesModel.finish();
		
		trigger.dispose();
		topModel.dispose();
		precipitationModel.dispose();
		hargreavesModel.dispose();
		

		System.out.println("共循环"+num+"次");
		
//		componet1.dispose();
		System.out.println("Successfully!");
		//System.exit(0);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		long start_time = System.currentTimeMillis();
		MqttTopmodelMainTest.execute();
		long end_time = System.currentTimeMillis();
		System.out.println("用时"+(end_time-start_time));

	}

}
