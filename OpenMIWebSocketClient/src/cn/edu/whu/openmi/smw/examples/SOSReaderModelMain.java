package cn.edu.whu.openmi.smw.examples;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;

import cn.edu.whu.openmi.models.hargreaves2.Hargreaves;
import cn.edu.whu.openmi.models.sosreader.SOSReader;
import cn.edu.whu.openmi.models.topmodel.TopModel;


public class SOSReaderModelMain {
	public static void main(String[] args){
		
		//SOSReader提供降水
//		Argument precipConfigFile= new Argument("ConfigFile","F:/workspace/SOSReader-config-2.xml",true);
		Argument precipAddr = new Argument("Address","http://202.114.114.55:8088/52nSOSv3_WAR/sos",true);
		Argument precipVersion = new Argument("Version","1.0",true);
		Argument precipOffering= new Argument("Offering","CoweetaPrecipitationMonitor",true);
		Argument precipProcedure= new Argument("Procedure","urn:ogc:object:feature:sensor:coweeta:rain_gauge_1",true);
		Argument precipProperty= new Argument("ObservedProperty","urn:ogc:def:phenomenon:coweeta:precipitation",true);
		SOSReader preciSosReader = new SOSReader();
		preciSosReader.initialize(new Argument[]{precipAddr,precipVersion,precipOffering,
				precipProcedure,precipProperty});
		
		//SOSReader提供温度值
//		Argument tempConfigFile= new Argument("ConfigFile","F:/workspace/SOSReader-config-1.xml",true);
		Argument tempAddr = new Argument("Address","http://202.114.114.55:8088/52nSOSv3_WAR/sos",true);
		Argument tempVersion = new Argument("Version","1.0",true);
		Argument tempOffering= new Argument("Offering","CoweetaTemperatureMonitor",true);
		Argument tempProcedure= new Argument("Procedure","urn:ogc:object:feature:sensor:coweeta:temperature_gauge_1",true);
		Argument tempProperty= new Argument("ObservedProperty","urn:ogc:def:phenomenon:coweeta:AverageTemperature;urn:ogc:def:phenomenon:coweeta:MaxTemperature;urn:ogc:def:phenomenon:coweeta:MinTemperature",true);
		SOSReader tempSosReader = new SOSReader();
		tempSosReader.initialize(new Argument[]{tempAddr,tempVersion,tempOffering,
				tempProcedure,tempProperty});
		
		//设置Hargreaves模型
//		Argument petConfigFileArgument = new Argument("ConfigFile","F:/workspace/Hargreaves-config.xml",true);
		Argument petOutPutArgument = new Argument("OutputData", "F:\\Hargreaves_output.txt", true);
		Hargreaves hargreavesModel = new Hargreaves();
		hargreavesModel.initialize(new Argument[]{petOutPutArgument});
		
		//TOPModel模型
//		Argument TopmodelConfigFileArgument = new Argument("ConfigFile","F:/workspace/Topmodel-Config.xml",true);
		Argument TopmodelTIArgument = new Argument("TI","F:/workspace/CowetaWatersheld_reproj_topindex_18.tif",true);
//		Argument TopmodelTIArgument = new Argument("TI","F:\\360sync\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\models\\topmodel\\TI_raster.txt",true);
		Argument TopmodeloutputFileArgument = new Argument("OutputData","F:\\Topmodel_output.txt",true);
		Argument TopmodelmArgument = new Argument("m","90",true);
		Argument TopmodelTmaxArgument = new Argument("Tmax","240000",true);
		Argument TopmodelRArgument = new Argument("R","9.66",true);
		Argument TopmodelInterceptionArgument = new Argument("Interception","3",true);
		Argument TopmodelareaArgument = new Argument("WatershedArea_SquareMeters","124800",true);
		
		//Initialize the Topodel
		TopModel topModel = new TopModel();
		topModel.initialize(new Argument[]{TopmodelTIArgument,TopmodeloutputFileArgument,TopmodelmArgument,
				TopmodelTmaxArgument,TopmodelRArgument,TopmodelInterceptionArgument,TopmodelareaArgument});
		
		
		ILinkableComponent trigger = new Trigger("Trigger");
		LinkManager linkManager = new LinkManager();
		
		//Create link between SOSReader for temperature and Hargreaves
		Link tempLink = new Link(linkManager, "tempLink");
		tempLink.connect(tempSosReader, tempSosReader.getOutputExchangeItem(0), hargreavesModel, hargreavesModel.getInputExchangeItem(0));
		tempSosReader.addLink(tempLink);
		hargreavesModel.addLink(tempLink);
		
		//Create link between SOSReader for precipitation and TopModel
		Link precipLink = new Link(linkManager,"PrecipLink");
		precipLink.connect(preciSosReader, preciSosReader.getOutputExchangeItem(0), topModel, topModel.getInputExchangeItem(0));
		preciSosReader.addLink(precipLink);
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
		preciSosReader.validate();
		tempSosReader.validate();
		hargreavesModel.validate();
		
		double endTime = topModel.getTimeHorizon().getEnd().getModifiedJulianDay();
//		double timeStep = topModel.getTimeHorizon().;
		double timeStep = topModel.getTimeStep();
		
		double startTime = topModel.getTimeHorizon().getStart().getModifiedJulianDay()+timeStep;
//		double timeStep = 1;
		double start = startTime,end = endTime+0.00001;
		while (start<end) {
			IValueSet value = trigger.getValues(new TimeStamp(start), triggerLink.getID());
			System.out.println("time is "+CalendarConverter.modifiedJulian2Gregorian(start).getTime().toString()+", value "+value);
			start+= timeStep;
		}
		trigger.finish();
		topModel.finish();
		preciSosReader.finish();
		tempSosReader.finish();
		hargreavesModel.finish();
		
		trigger.dispose();
		topModel.dispose();
		preciSosReader.dispose();
		tempSosReader.dispose();
		hargreavesModel.dispose();
//		componet1.dispose();
		System.out.println("Successfully!");
		System.exit(0);
	}
}
