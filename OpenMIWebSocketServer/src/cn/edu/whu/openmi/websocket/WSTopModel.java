package cn.edu.whu.openmi.websocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.IOutputExchangeItem;

import cn.edu.whu.openmi.models.topmodel.TopModelData;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;
import cn.edu.whu.openmi.websocket2.WSTopModel2;

@ServerEndpoint(value = "/ws/{nickName}")
public class WSTopModel extends SimpleWrapper {

	private List<WSInputItem> inputItems = new ArrayList<>();
	private List<WSOutputItem> outputItems = new ArrayList<>();
	
	private int flag = 1;
	private String nickName;

	// Topmodel中使用的一些参数变量
	double R;// subsurface Recharge rate [L/T]
	double c; // recession parameter (m)
	double Tmax; // Average effective transmissivity of the soil when the
					// profile is just saturated
	double interception;// intial interciption of the watershed

	double[] TI;// topographic index
	double[] freq;// topographic index frequency

	double lamda_average;// average lamda
	double PPT_daily;
	double ET_daily;
	double q_overland;
	double q_subsurface;
	double q_infiltration;
	double S_average; // average saturation deficit

	double _watershedArea = 0; // area of the watershed. used to convert runoff
								// into streamflow

	Map<Date, Double> Precip = new HashMap<Date, Double>();
	Map<Date, Double> ET = new HashMap<Date, Double>();
	Map<Calendar, TopModelData> outputValues = new LinkedHashMap<Calendar, TopModelData>();
	private static String OUTPUT_N = "OutputData", PROV_N = "ProvOut";
	// private Map<Calendar, Double> outValues = new LinkedHashMap<Calendar,
	// Double>();
	private String provFile = "";

	String[] _input_elementset;
	String[] _output_elementset;
	String[] _output_quantity;
	String[] _input_quantity;

	List<Calendar> _DateTimes = new ArrayList<Calendar>();
	List<Double> q_outputs = new ArrayList<Double>();
	List<Double> q_infltration_outputs = new ArrayList<Double>();

	private String outFile = null;

	/**
	 * WebSocket Session
	 */
	private Session session;

	/*
	 * 会先调用构造函数，然后调用onOpen方法。
	 */
	public WSTopModel() {
		/*InputStream inConfig = PrecipitationEngine.class
				.getClass()
				.getResourceAsStream(
						"/cn/edu/whu/openmi/models/topmodel/Topmodel-Config.xml");*/
		InputStream inConfig;
		try {
			String path = WSTopModel2.class.getClassLoader().getResource("").getPath()+"cn/edu/whu/openmi/websocket/Topmodel-Config.xml";
			inConfig = new FileInputStream(new File(path));
			this.setVariablesFromConfigFile(inConfig);
			for(int i=0;i<this.getOutputExchangeItemCount();i++){
				IOutputExchangeItem outputExchangeItem = this.getOutputExchangeItem(i);
				WSOutputItem wsOutputItem = new WSOutputItem();
				wsOutputItem.setElement(outputExchangeItem.getElementSet().getID());
				wsOutputItem.setQuantity(outputExchangeItem.getQuantity().getID());
				this.outputItems.add(wsOutputItem);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("构造函数");
	}

	/**
	 * 打开连接，进行初始化
	 * 
	 * @param session
	 * @param nickName
	 */
	@OnOpen
	public void onOpen(Session session,
			@PathParam(value = "nickName") String nickName) {

		this.session = session;
		this.nickName = nickName;
//		System.out.println("the model named " + this.nickName + " is opened.");

		// 测试传递参数
		Map<String, String> pathParameters = session.getPathParameters();
		Map<String, List<String>> reqParams = session.getRequestParameterMap();

//		System.out.println("PathParameters:" + pathParameters);
//		System.out.println("RequestParameterMap:" + reqParams);
		/*
		 * int size = reqParams.size(); Argument[] arguments = new
		 * Argument[size];
		 */
		HashMap paraMap = new HashMap<>();
		Set set = reqParams.keySet();
		Iterator it = set.iterator();
		int i = 0;
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = ((List<String>) reqParams.get(key)).get(0);
			// arguments[i++] = new Argument(key,value,true);
			paraMap.put(key, value);
		} 	
		this.initialize(paraMap);
	}

	/**
	 * 关闭连接，直接调用Finish方法即可。
	 */
	@OnClose
	public void onClose() {
		// connections.remove(this);
		String message = String.format("System> %s, %s", this.nickName,
				" has disconnection.");
		// WebSocketModels.broadCast(message);
		System.out.println(message);
		this.finish();
	}

	
	/**
	 * 客户端请求服务器端模型的数据，或者设置服务器端参数值，通过method的参数区分，get/set
	 * 
	 * @param message
	 * @param nickName
	 */
	@OnMessage
	public void onMessage(String message,
			@PathParam(value = "nickName") String nickName) {
//		System.out.println("the server recieve the message:" + message+ " from the client" + "' Number:" + (flag++));
		// WebSocketModels.broadCast(nickName + ">" + message);
		Map<String, String> paraMap = WSModelUtils.parseArguments(message);
		String time = paraMap.get(WSModelUtils.TIMEKEY);
		String method = paraMap.get(WSModelUtils.METHODKEY);
		String quantity = paraMap.get(WSModelUtils.QUANTITYKEY);
		String element = paraMap.get(WSModelUtils.ELEMENTYKEY);
		String value = paraMap.get(WSModelUtils.VALUEKEY);
		
		//从其他节点拉取数据,InputExchangeItem
		if (method.equals("set")) {
//			System.out.println("Server receives the message:"+message);
			WSInputItem inputItem = getInputItem(element, quantity);
			if (inputItem == null) {
				inputItem = new WSInputItem();
				inputItem.setElement(element);
				inputItem.setQuantity(quantity);
				inputItem.addValue(time, value);
				inputItems.add(inputItem);
			}else {
				inputItem.addValue(time, value);
			}
			return;
		}
		
		//向其他节点提供数据，OutputExchangeItem
		String resp = WSModelUtils.METHODKEY+"=set&"+WSModelUtils.ELEMENTYKEY+"="+element+"&"+
						WSModelUtils.QUANTITYKEY+"="+quantity+"&"+WSModelUtils.VALUEKEY+"=";
		if (method.equals("get")) {
			/*while (WSModelUtils.isFormerThan(WSModelUtils.timeToString(this.getCurrentTime()), time)) {
				this.performTimeStep();
			}*/
			this.performTimeStep();
			WSOutputItem outputItem = getOutputItem(element, quantity);
			if (outputItem == null) {
				resp+="NoValue";
			}else {
				resp+=outputItem.getValue(outputItem.getLatestTime());
			}
			try {
//				System.out.println("The server sends:"+resp);
				this.session.getBasicRemote().sendText(resp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	/**
	 * 错误信息响应
	 * 
	 * @param throwable
	 */
	@OnError
	public void onError(Throwable throwable) {
		System.out.println(throwable.getMessage());
	}

	@Override
	public void initialize(HashMap properties) {
		// TODO Auto-generated method stub
		super.initialize(properties);

		// Get config file path defined in sample.omi
		// String configFile = properties.get("ConfigFile").toString();

		// read topographic input file
		String topo_input = properties.get("TI").toString();
		Object proObj = properties.get(PROV_N);
		if (proObj != null) {
			this.provFile = proObj.toString();
		}

		// read model input parameters
		c = Double.parseDouble(properties.get("m").toString());
		Tmax = Double.parseDouble(properties.get("Tmax").toString());
		R = Double.parseDouble(properties.get("R").toString());
		interception = Double.parseDouble(properties.get("Interception")
				.toString());
		_watershedArea = Double.parseDouble(properties.get(
				"WatershedArea_SquareMeters").toString());

		Object obj = properties.get("OutputData");
		if (obj == null) {
			this.outFile = OpenMIUtilities.getCurrentWorkingDirectory()
					+ File.separator + new Date().getSeconds()
					+ "_TopMODELOutput.txt";
		} else {
			this.outFile = obj.toString();

		}

		// 从txt读取地形指数
		read_topo_input(topo_input);

		// ---- calculate saturation deficit
		// calculate lamda average for the watershed
		double[] TI_freq = new double[TI.length];

		for (int i = 0; i < TI.length; i++) {
			TI_freq[i] = TI[i] * freq[i];
		}

		// lamda_average = TI_freq.Sum() / freq.Sum();
		lamda_average = OpenMIUtilities.sum(TI_freq)
				/ OpenMIUtilities.sum(freq);

		// catchement average saturation deficit(S_bar)
		double S_bar = -c * ((Math.log(R / Tmax)) + lamda_average);
		S_average = S_bar;
		setInitialized(true);
	}

	/*
	 * 在请求
	 */
	@Override
	public boolean performTimeStep() {
		
		//增加计算时间
	/*	try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//将时间点向前推进，计算的数据是由客户端传送过来的，不需要advanceTime
	//	this.advanceTime();
		
		//更新输入的数据：将输入数据更新为当前时间点的数据。
		//this.updateInputs();
		
		/*// reading the appropriate value from PPT & PET dictionary
		// 所以在配置文件中，Precipitation必须放在第一位，然后是pet
		ScalarSet input_precip = (ScalarSet) this.getValues(this
				.getInputExchangeItem(0).getQuantity().getID(), this
				.getInputExchangeItem(0).getElementSet().getID()); // Rainfall
		ScalarSet input_pet = (ScalarSet) this.getValues(this
				.getInputExchangeItem(1).getQuantity().getID(), this
				.getInputExchangeItem(1).getElementSet().getID()); // PET
		
		for (int i = 0; i < input_pet.getCount(); i++) {
			ET_daily = input_pet.get(i);
		}
		for (int h = 0; h < input_precip.getCount(); h++) {
			PPT_daily = input_precip.get(h);
		}*/
		String precipElement = "Coweeta Precipitation",precipQuantity="Precipitation",petElement="Coweeta",petQuantity="PET";
		
		/*WSInputItem precip_inputItem = getInputItem(this
				.getInputExchangeItem(0).getElementSet().getID(), this
				.getInputExchangeItem(0).getQuantity().getID());
		
		WSInputItem pet_inputItem = getInputItem(this
				.getInputExchangeItem(1).getElementSet().getID(), this
				.getInputExchangeItem(1).getQuantity().getID());*/
		WSInputItem precip_inputItem = getInputItem(precipElement, precipQuantity);
		WSInputItem pet_inputItem = getInputItem(petElement,petQuantity);
		
		PPT_daily = Double.parseDouble(precip_inputItem.getValue(precip_inputItem.getLatestTime()));
		ET_daily = Double.parseDouble(pet_inputItem.getValue(pet_inputItem.getLatestTime()));

		// declaring the flow matrices here since they are related with the size
		// of input matrices
		double[] S_d = new double[TI.length];
		double[] over_flow = new double[TI.length]; // Infiltration excess
		double[] reduced_ET = new double[TI.length];// Reduced ET due to dryness

		// calculate the saturation deficit for each TIpoint
		double[] S = new double[TI.length];
		for (int j = 0; j < TI.length; j++) {
			S[j] = S_average + c * (lamda_average - TI[j]);
		}

		// remove the interception effect from PPT matrix, and update the
		// saturation deficit matrix, calculating q_infiltration
		PPT_daily = Math.max(0, (PPT_daily - interception)); //
		for (int m = 0; m < TI.length; m++) {
			S[m] = S[m] - PPT_daily + ET_daily;
		}
		q_infiltration = PPT_daily - ET_daily;
		double[] MM = new double[TI.length];
		if ((PPT_daily - ET_daily) > 0) {
			// create a list for S values<0
			for (int m = 0; m < TI.length; m++) {
				if (S[m] < 0) {
					over_flow[m] = -S[m];
					S[m] = 0;
				} else {
					over_flow[m] = 0;
				}
				MM[m] = freq[m] * over_flow[m];
			}
		} else {
			double[] NN = new double[TI.length];
			for (int m = 0; m < TI.length; m++) {
				if (S[m] > 5000) {
					reduced_ET[m] = -5000 + S[m];
					S[m] = 5000;
				} // KK.Add(S[m]);
				else {
					reduced_ET[m] = 0;
				}
				NN[m] = freq[m] * reduced_ET[m];
			}

			// q_infiltration = q_infiltration + ((NN.Sum()) / (freq.Sum()));
			q_infiltration = q_infiltration
					+ (OpenMIUtilities.sum(NN) / OpenMIUtilities.sum(freq));
		}
		q_subsurface = Tmax * (Math.exp(-lamda_average))
				* (Math.exp(-S_average / c));
		// q_overland = (MM.Sum()) / (freq.Sum());
		q_overland = OpenMIUtilities.sum(MM) / OpenMIUtilities.sum(freq);

		// calculate the new average deficit using cachement mass balance
		S_average = S_average + q_subsurface + q_overland - q_infiltration;

		// calculating runoff q
		double q = q_overland + q_subsurface;

		// Storing values of DateTimes and surface runoff values
		TimeStamp time = (TimeStamp) this.getCurrentTime();
		Calendar curr_time = CalendarConverter.modifiedJulian2Gregorian(time
				.getModifiedJulianDay());
		_DateTimes.add(curr_time);
		q_outputs.add(q);
		q_infltration_outputs.add(q_infiltration);

		// save runoff
		double[] runoff = new double[1];
		runoff[0] = q;// *_watershedArea;

		// -- calculate streamflow using watershed area
		double[] streamflow = new double[1];
		streamflow[0] = q * (_watershedArea) / 86400;

		// outputValues.put(curr_time, q);
		TopModelData modelData = new TopModelData();
		modelData.setTime(curr_time);
		modelData.setPrecip(PPT_daily);
		modelData.setPet(ET_daily);
		modelData.setRunoff(q);
		modelData.setStreamFlow(streamflow[0]);
		outputValues.put(curr_time, modelData);

		// set the basin outflow as runoff output
		// Runoff必须是第一个OutputExchangeItem
		/*this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(),
				this.getOutputExchangeItem(0).getElementSet().getID(),
				new ScalarSet(runoff));
		this.setValues(this.getOutputExchangeItem(1).getQuantity().getID(),
				this.getOutputExchangeItem(1).getElementSet().getID(),
				new ScalarSet(streamflow));*/
		
		WSOutputItem runoffItem = this.getOutputItem(this.getOutputExchangeItem(0).getElementSet().getID(), this.getOutputExchangeItem(0).getQuantity().getID());
		if (runoffItem == null) {
			runoffItem = new WSOutputItem();
			runoffItem.setElement(this.getOutputExchangeItem(0).getElementSet().getID());
			runoffItem.setQuantity(this.getOutputExchangeItem(0).getQuantity().getID());
			outputItems.add(runoffItem);
		}
		runoffItem.addValue(WSModelUtils.timeToString(this.getCurrentTime()), String.valueOf(q));
		/*
		 * string q1 = this.GetOutputExchangeItem(0).Quantity.ID; string e1 =
		 * this.GetOutputExchangeItem(0).ElementSet.ID; this.SetValues(q1, e1,
		 * new ScalarSet(runoff));
		 * 
		 * string q2 = this.GetOutputExchangeItem(1).Quantity.ID; string e2 =
		 * this.GetOutputExchangeItem(1).ElementSet.ID; this.SetValues(q2, e2,
		 * new ScalarSet(streamflow));
		 */

		// modified by zmd,20151026
		 this.advanceTime();
		return true;
	}

	private void updateInputs() {
		// TODO Auto-generated method stub
		String time = WSModelUtils.timeToString(this.getCurrentTime());
		String req = WSModelUtils.METHODKEY+"=get&"+WSModelUtils.TIMEKEY+"="+time+"&";
		for(WSInputItem inputItem:this.inputItems){
			String quantity = inputItem.getQuantity();
			String element = inputItem.getElement();
			req+= WSModelUtils.QUANTITYKEY+"="+quantity+"&";
			req+=WSModelUtils.ELEMENTYKEY+"="+element;
			try {
				this.session.getBasicRemote().sendText(req);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		try {
			if (OpenMIUtilities.isEmpty(this.outFile)) {
				this.outFile = OpenMIUtilities.getCurrentWorkingDirectory()
						+ File.separator + new Date().getSeconds()
						+ "_TopModelOutPut.txt";
			}
			File file = new File(this.outFile);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			// System.IO.Directory.CreateDirectory("wateroutput");
			String line = "";
			String timeFormat = "yyyy-MM-dd hh:mm:ss";
			bw.write("Daily Runoff....");
			bw.newLine();
			Calendar startCalendar = CalendarConverter
					.modifiedJulian2Gregorian(this.getTimeHorizon().getStart()
							.getModifiedJulianDay());
			Calendar endCalendar = CalendarConverter
					.modifiedJulian2Gregorian(this.getTimeHorizon().getEnd()
							.getModifiedJulianDay());
			// line = "StartDate: "+startCalendar.getTime().toString();
			line = "StartDate: "
					+ OpenMIUtilities.calendar2Str(startCalendar, timeFormat);
			bw.write(line);
			bw.newLine();
			// line = "EndDate: "+endCalendar.getTime().toString();
			line = "EndDate: "
					+ OpenMIUtilities.calendar2Str(endCalendar, timeFormat);
			bw.write(line);
			bw.newLine();
			bw.newLine();
			line = "Time[" + timeFormat
					+ "], Runoff, Streamflow [l/s], PET, Precipitation";
			bw.write(line);
			bw.newLine();

			for (Calendar calendar : outputValues.keySet()) {
				// String time = calendar.getTime().toString();
				String time = OpenMIUtilities
						.calendar2Str(calendar, timeFormat);
				/*
				 * double runoff = outputValues.get(calendar); double streamFlow
				 * = runoff*this._watershedArea/86400; line =
				 * time+", "+runoff+", "+streamFlow+" ";
				 */
				TopModelData modelData = outputValues.get(calendar);
				line = time + ", " + modelData.getRunoff() + ", "
						+ modelData.getStreamFlow() + ", " + modelData.getPet()
						+ ", " + modelData.getPrecip();
				bw.write(line);
				bw.newLine();
				// System.err.println(line);
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.addError(e.getMessage());
		}

	}

	/**
	 * 返回地形指数的值，以及对应的出现频率，在原有函数上有小的改动 Reads an input raster ascii file
	 * containing topographic index to produce topographic index and topographic
	 * frequency arrays topographicIndex:ASCII raster file containing
	 * topographic index values ti:output topographic index array freq:output
	 * topographic frequency array
	 */
	public boolean read_topo_input(String topographicIndex)
	// public boolean read_topo_input(String topographicIndex, double[] ti,
	// double[] freq)
	{
		// ---- begin reading the values stored in the topo file
		// StreamReader sr = new StreamReader(topographicIndex);
		InputStream is = null;
		try {
			is = new FileInputStream(new File(topographicIndex));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			this.addError(e1.getMessage());
			return false;
		}
		try {
			BufferedReader sr = new BufferedReader(new InputStreamReader(is));
			// -- read header info
			String line = null;
			for (int i = 0; i <= 4; i++) {
				line = sr.readLine();
			}

			// -- save the cellsize
			double cellsize = Double.parseDouble(line.split(" ")[line
					.split(" ").length - 1]);
			line = sr.readLine();

			// -- save the nodata value
			String nodata = line.split(" ")[line.split(" ").length - 1];
			line = sr.readLine();

			// -- store all values != nodata in a list
			List<Double> topoList = new ArrayList<Double>();
			int lineNum = 0;
			while (!OpenMIUtilities.isEmpty(line)) {
				lineNum += 1;
				String[] vals = line.trim().split(" ");
				for (int i = 0; i < vals.length; i++) {
					if (!vals[i].equals(nodata)) {
						// add by zmd,是否需要指定小数点位数，因为下边会有指数对比，判断是否相等
						topoList.add(Double.parseDouble(vals[i]));
						// modified by zmd, why
						// _watershedArea += cellsize;
					}
				}
				line = sr.readLine();
			}

			// ---- calculate frequency of each topographic index
			// -- consolidate topo list into unique values
			Map<Double, Double> d = new HashMap<Double, Double>();
			for (double t : topoList) {
				if (d.containsKey(t)) {
					d.put(t, d.get(t) + 1.0);
				} else {
					d.put(t, 1.0);
				}
			}
			// Dictionary<double, double> d = new Dictionary<double, double>();
			/*
			 * foreach (double t in topoList) if (d.ContainsKey(t)) d[t] += 1.0;
			 * else d.Add(t, 1.0);
			 */

			// -- calculate topo frequency, then return both topographic index
			// and topo frequency arrays
			double total = (double) topoList.size();
			this.TI = new double[d.size()];
			this.freq = new double[d.size()];
			int index = 0;
			for (double key : d.keySet()) {
				this.TI[index] = OpenMIUtilities.round((Double) key, 4);
				this.freq[index] = OpenMIUtilities
						.round(d.get(key) / total, 10);
				index++;
			}
			/*
			 * foreach (KeyValuePair<double, double> pair in d) { ti[index] =
			 * Math.Round(pair.Key,4); freq[index] = Math.Round(d[pair.Key] /
			 * total, 10); index ++; }
			 */
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.addError(e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public String getComponentID() {
		// TODO Auto-generated method stub
		return "TOPMODEL";
	}
	
	private WSInputItem getInputItem(String element,String quantity){
		for(WSInputItem item:inputItems){
			if(item.getElement().equals(element) && item.getQuantity().equals(quantity))
				return item;
		}
		return null;
	}
	
	private WSOutputItem getOutputItem(String element,String quantity){
		for(WSOutputItem item:outputItems){
			if(item.getElement().equals(element) && item.getQuantity().equals(quantity))
				return item;
		}
		return null;
	}
}
