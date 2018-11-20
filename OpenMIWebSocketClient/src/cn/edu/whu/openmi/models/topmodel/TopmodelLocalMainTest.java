package cn.edu.whu.openmi.models.topmodel;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;

import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;

import cn.edu.whu.openmi.models.Hargreaves.HargreavesModel;
import cn.edu.whu.openmi.models.data.PrecipitationModel;


public class TopmodelLocalMainTest {
	public static void execute(){
		
		//Parameters for Precipitation
		//ConfigFile在模型内部指定
//		Argument precipConfigFileArgument = new Argument("ConfigFile","F:/workspace/Precipitation-config.xml",true);
//		Argument precipConfigFileArgument = new Argument("ConfigFile","F:\\360sync\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\models\\data\\Precipitation-config.xml",true);
//		Argument precipInputArgument = new Argument("InputData","F:\\360sync\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\models\\data\\PrecipitationInput.txt",true);
		//Initialize the Precipitation model

		Argument precipInputArgument = new Argument("InputData","D:/websocket/openmi/PrecipitationInput.txt",true);
		PrecipitationModel precipitationModel = new PrecipitationModel();
		precipitationModel.initialize(new Argument[]{precipInputArgument});
		
		//Parameters for Hargreaves
//		Argument petConfigFileArgument = new Argument("ConfigFile","F:/workspace/Hargreaves-config.xml",true);
//		Argument petConfigFileArgument = new Argument("ConfigFile","F:\\360sync\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\models\\Hargreaves\\Hargreaves-config.xml",true);
//		Argument petInputFileArgument = new Argument("InputData","F:\\360sync\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\models\\Hargreaves\\TemperatureInput-all.txt",true);
		//Initialize the Precipitation model
		
		Argument petOutPutArgument = new Argument("OutputData", "D:/websocket/openmi/Hargreaves_output.txt", true);
		Argument petInputFileArgument = new Argument("InputData","D:/websocket/openmi/TemperatureInput-all.txt",true);
		Argument petProvOutArgument = new Argument("ProvOut","D:/websocket/openmi/Hargreaves_prov.txt",true);
		HargreavesModel hargreavesModel = new HargreavesModel();
		hargreavesModel.initialize(new Argument[]{petInputFileArgument,petOutPutArgument,petProvOutArgument});
//		hargreavesModel.getTimeHorizon()

		//Parameters for TOPModel
//		Argument TopmodelConfigFileArgument = new Argument("ConfigFile","F:/workspace/Topmodel-Config.xml",true);
//		Argument TopmodelConfigFileArgument = new Argument("ConfigFile","F:\\360sync\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\models\\topmodel\\Topmodel-Config.xml",true);
//		Argument TopmodelTIArgument = new Argument("TI","F:\\360sync\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\models\\topmodel\\TI_raster.txt",true);
		
		Argument TopmodelTIArgument = new Argument("TI","D:/websocket/openmi/TI_raster.txt",true);
		Argument TopmodeloutputFileArgument = new Argument("OutputData","D:/websocket/openmi/Topmodel_output.txt",true);
		Argument topmodelProvOutArgument = new Argument("ProvOut","D:/websocket/openmi/Topmodel_prov.txt",true);
		Argument TopmodelmArgument = new Argument("m","90",true);
		Argument TopmodelTmaxArgument = new Argument("Tmax","240000",true);
		Argument TopmodelRArgument = new Argument("R","9.66",true);
		Argument TopmodelInterceptionArgument = new Argument("Interception","3",true);
		Argument TopmodelareaArgument = new Argument("WatershedArea_SquareMeters","124800",true);
		
		//Initialize the Topodel
		TopModel topModel = new TopModel();
		topModel.initialize(new Argument[]{TopmodelTIArgument,TopmodeloutputFileArgument,TopmodelmArgument,
				TopmodelTmaxArgument,TopmodelRArgument,TopmodelInterceptionArgument,TopmodelareaArgument,topmodelProvOutArgument});
		
		
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
		int num = 1;
		while (start<end) {
			IValueSet value = trigger.getValues(new TimeStamp(start), triggerLink.getID());
//			System.out.println("time is "+CalendarConverter.modifiedJulian2Gregorian(start).getTime().toString()+", value "+value);
			start+= timeStep;
			num++;
		}
		System.out.println("共循环"+num+"次");
		trigger.finish();
		topModel.finish();
		precipitationModel.finish();
		hargreavesModel.finish();
		
		trigger.dispose();
		topModel.dispose();
		precipitationModel.dispose();
		hargreavesModel.dispose();
//		componet1.dispose();
		System.out.println("Successfully!");
	//	System.exit(0);
	}
	
	public static void main(String[] args){
		/*long start_time = System.currentTimeMillis();
		for(int i=0;i<5;i++){
			TopmodelLocalMainTest.execute();
		}
		long end_time = System.currentTimeMillis();
		System.out.println("用时"+(end_time-start_time)/5);*/
		
		long start_time = System.currentTimeMillis();
		TopmodelLocalMainTest.execute();
		long end_time = System.currentTimeMillis();
		System.out.println("用时"+(end_time-start_time));
	}
	
}
