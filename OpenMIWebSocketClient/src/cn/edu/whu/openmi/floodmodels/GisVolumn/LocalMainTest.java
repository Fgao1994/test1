package cn.edu.whu.openmi.floodmodels.GisVolumn;

import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IValueSet;


import cn.edu.whu.openmi.floodmodels.Precipitation.PrecipitationModel;
import cn.edu.whu.openmi.floodmodels.Scscn.ScscnModel;
import cn.edu.whu.openmi.floodmodels.floodvolumn.FloodVolumnModel;
import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.configuration.LinkManager;
import nl.alterra.openmi.sdk.configuration.Trigger;

public class LocalMainTest {
	
	public static void execute(){
		
		String Local_Dir = "D:/websocket/flood/";
		
		//Parameters for Precipitation:input_file
		Argument precipInputArgument = new Argument("input_file",Local_Dir+"PrecipitationInput.txt",true);
		PrecipitationModel precipitationModel = new PrecipitationModel();
		precipitationModel.initialize(new Argument[]{precipInputArgument});
		
		//Parameters for Scscn:land_use,out_file
		
		//Argument scscnCnArgument = new Argument("land_use","Grass Land and Park",true);
		Argument scscnCnArgument = new Argument("land_use","Residential District and Vehicle Lane",true);
		Argument scscnOutputArgument = new Argument("out_file",Local_Dir+"ScscnOutput.txt",true);
		ScscnModel scscnModel = new ScscnModel();
		scscnModel.initialize(new Argument[]{scscnCnArgument,scscnOutputArgument});
		
		//Parameters for FloodVolumn:input_file,s_area,out_file
		Argument floodVolumnInputArgument = new Argument("input_file",Local_Dir+"FloodVolumnInput.txt",true);
		Argument floodVolumnAreaArgument = new Argument("s_area","7288.5214",true);
		Argument floodVolumnOutputArgument = new Argument("out_file",Local_Dir+"FloodVolumnOutput.txt",true);
		FloodVolumnModel floodVolumnModel = new FloodVolumnModel();
		floodVolumnModel.initialize(new Argument[]{floodVolumnInputArgument,floodVolumnAreaArgument,floodVolumnOutputArgument});
		
		//Parameters for GisVolumn:out_file
		Argument gisVolumnInputArgument = new Argument("out_file",Local_Dir+"GisVolumnOutput.txt",true);
		GisVolumnModel gisVolumnModel = new GisVolumnModel();
		gisVolumnModel.initialize(new Argument[]{gisVolumnInputArgument});
		
		
		
		ILinkableComponent trigger = new Trigger("Trigger");
		LinkManager linkManager = new LinkManager();
		
		//Create link between PrecipitationModel and ScscnModel
		Link pre_scs_Link = new Link(linkManager, "Pre_Scs_Link");
		pre_scs_Link.connect(precipitationModel, precipitationModel.getOutputExchangeItem(0), scscnModel, scscnModel.getInputExchangeItem(0));
		precipitationModel.addLink(pre_scs_Link);
		scscnModel.addLink(pre_scs_Link);
		
		
		//Create link between ScscnModel and FloodVolumnModel
		Link scs_fv_Link = new Link(linkManager, "Scs_Fv_Link");
		scs_fv_Link.connect(scscnModel, scscnModel.getOutputExchangeItem(0), floodVolumnModel, floodVolumnModel.getInputExchangeItem(0));
		scscnModel.addLink(scs_fv_Link);
		floodVolumnModel.addLink(scs_fv_Link);
		
		//Create link between FloodVolumnModel and GisVolumnModel
		Link fv_gv_Link = new Link(linkManager,"Fv_Gv_Link");
		fv_gv_Link.connect(floodVolumnModel, floodVolumnModel.getOutputExchangeItem(0), gisVolumnModel, gisVolumnModel.getInputExchangeItem(0));
		floodVolumnModel.addLink(fv_gv_Link);
		gisVolumnModel.addLink(fv_gv_Link);
		
		
		//Create link between GisVolumnModel and Trigger
		Link gv_tr_Link = new Link(linkManager, "Gv_Tr_Link");
		gv_tr_Link.connect(gisVolumnModel, gisVolumnModel.getOutputExchangeItem(0), trigger, trigger.getInputExchangeItem(0));
		gisVolumnModel.addLink(gv_tr_Link);
		trigger.addLink(gv_tr_Link);
		
		trigger.validate();
		gisVolumnModel.validate();
		floodVolumnModel.validate();
		scscnModel.validate();
		precipitationModel.validate();
		
		double endTime = gisVolumnModel.getTimeHorizon().getEnd().getModifiedJulianDay();
		double timeStep = gisVolumnModel.getTimeStep();
		double startTime = gisVolumnModel.getTimeHorizon().getStart().getModifiedJulianDay()+timeStep;
		
		double start = startTime,end = endTime+0.00001;
		int num = 0;
		
		while(start<end){
			IValueSet value = trigger.getValues(new TimeStamp(start), gv_tr_Link.getID());
			start += timeStep;
			num++;
		}
		
		trigger.finish();
		gisVolumnModel.finish();
		floodVolumnModel.finish();
		scscnModel.finish();
		precipitationModel.finish();
		
		trigger.dispose();
		gisVolumnModel.dispose();
		floodVolumnModel.dispose();
		scscnModel.dispose();
		precipitationModel.dispose();
		
		System.out.println("共循环"+num+"次");
		System.out.println("Successfully!");
		
	}
	
	public static void main(String[] args){
		long start_time = System.currentTimeMillis();
		LocalMainTest.execute();
		long end_time = System.currentTimeMillis();
		System.out.println("用时"+(end_time-start_time));
	}

}
