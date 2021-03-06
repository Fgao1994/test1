package cn.edu.whu.mqtt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import sun.rmi.runtime.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import cn.edu.whu.httpmodel.HttpTopModel;

//import com.gaofan.test.MqttPublishSample;

import cn.edu.whu.openmi.models.data.PrecipitationEngine;
import cn.edu.whu.openmi.models.topmodel.TopModelData;
import cn.edu.whu.openmi.provenance.SmartBufferInfo;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.openmi.util.OpenMIUtilities;
import cn.edu.whu.openmi.util.RequestMethodStore;
import jdk.nashorn.internal.ir.RuntimeNode.Request;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

//import com.gaofam.server.MqttServerPS;

import org.eclipse.paho.client.mqttv3.MqttException;
/**
 * 说明：TOPMODEL中对于地形指数的不同输入数据，采用不同的处理方式，
 * read_topo_input()是处理原始的TXT文件
 * read_topo_input2()处理tif格式的数据，使用gdal.jar,需要有GDAL环境
 * read_topo_input3()处理自己生成的TXT数据，已包装服务GeoTIFF2Plain。
 * 在测试的时候需要根据自己的需要修改对应的方法。
 * @author fgao
 *
 */
public class MqttTopModelEngine extends SimpleWrapper {
	
	 double R;//subsurface Recharge rate [L/T]
     double c; //recession parameter (m)
     double Tmax; //Average effective transmissivity of the soil when the profile is just saturated
     double interception;//intial interciption of the watershed

     double[] TI;//topographic index
     double[] freq;//topographic index frequency

     double lamda_average;//average lamda
     double PPT_daily;
     double ET_daily;
     double q_overland;
     double q_subsurface;
     double q_infiltration;
     double S_average; //average saturation deficit

     double _watershedArea = 0; //area of the watershed. used to convert runoff into streamflow

     //add by zmd, 20160114,溯源使用
     protected List<SmartBufferInfo> bufferInfos = null; 
     
     //与服务器端交互
//     private String httpUrl = "http://202.114.118.190:8086/OpenMIWebSocketServer/MqttTopModel";
     //虚拟机中部署服务，还需要修改Main函数
    // private String httpUrl = "http://192.168.254.128:8080/OpenMIWebSocketServer/MqttTopModel";
 //    private String httpUrl = "http://192.168.73.150:8080/OpenMIWebSocketServer/MqttTopModel";
     //本机路径
//     private String httpUrl = "http://localhost:8080/OpenMIWebSocketServer/MqttTopModel";
     //通过apache2转发，限速
//    private String httpUrl = "http://localhost:80/openmiserver/MqttTopModel";
     private String httpUuid = "";
     
     
     Map<Date, Double> Precip = new HashMap<Date, Double>();
     Map<Date, Double> ET = new HashMap<Date, Double>();
//     Map<Calendar, Double> outputValues = new LinkedHashMap<Calendar, Double>();
     Map<Calendar, TopModelData> outputValues = new LinkedHashMap<Calendar, TopModelData>();
     /* Map<DateTime, double> Precip = new HashMap<DateTime, double>();
     Dictionary<DateTime, double> ET = new Dictionary<DateTime, double>();
     Dictionary<DateTime, double> outputValues = new Dictionary<DateTime, double>();*/
     
     public final static String topic = "topmodel";

     public final static int qos = 2;
 	//static String broker       = "tcp://iot.eclipse.org:1883";
     public final static String broker    =   "tcp://192.168.73.150:1883";
  	 //static String broker       = "tcp://localhost:1883";
     public final static String clientId     = "mqtt-client";
     public static MqttClient sampleClient ;
     public final static short KEEP_ALIVE = 60;
    
     private static final Logger LOG = Logger.getLogger(MqttTopModelEngine.class+"\n"); 
     public MqttTopModelEngine(){
    	 InputStream inConfig = PrecipitationEngine.class.getClass().getResourceAsStream("/cn/edu/whu/openmi/models/topmodel/Topmodel-Config.xml");
 		this.setVariablesFromConfigFile(inConfig);
     }
     private static String OUTPUT_N="OutputData",PROV_N = "ProvOut";
//     private Map<Calendar, Double> outValues = new LinkedHashMap<Calendar, Double>();
     private String provFile = "";
     
     String[] _input_elementset;
     String[] _output_elementset;
     String[] _output_quantity;
     String[] _input_quantity;

     List<Calendar> _DateTimes = new ArrayList<Calendar>();
     List<Double> q_outputs = new ArrayList<Double>();
     List<Double> q_infltration_outputs = new ArrayList<Double>();

     private String outFile = null;
	@Override
	public void initialize(HashMap properties) {  
		// TODO Auto-generated method stub
		super.initialize(properties);
		
		/*try {
   		 client = new MqttClient(broker, clientId);
   		 MqttConnectOptions connOpts = new MqttConnectOptions();
         connOpts.setCleanSession(false);
         LOG.info("Connecting to broker: "+broker);
   		 client.connect();
   		 LOG.info("connected");
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		
		//Get config file path defined in sample.omi
//        String configFile = properties.get("ConfigFile").toString();

        //read topographic input file
		 
        String topo_input = properties.get("TI").toString();
        Object proObj = properties.get(PROV_N);
       
        //read model input parameters
        c = Double.parseDouble(properties.get("m").toString());
        Tmax = Double.parseDouble(properties.get("Tmax").toString());
        R = Double.parseDouble(properties.get("R").toString());
        interception = Double.parseDouble(properties.get("Interception").toString());
        _watershedArea = Double.parseDouble(properties.get("WatershedArea_SquareMeters").toString());
        outFile = properties.get("OutputData").toString();
        		
        String payload = MqttTopModel.METHODKEY+"="+MqttTopModel.Method_Initialize;
        payload+="&TI="+topo_input+"&Tmax="+Tmax+"&R="+R+"&Interception="+interception+"&WatershedArea_SquareMeters="+_watershedArea+"&m="+c+"&OutputData="+outFile;
        
        MemoryPersistence persistence = new MemoryPersistence();
		try {
			sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
	        connOpts.setCleanSession(false);
	        connOpts.setKeepAliveInterval(KEEP_ALIVE);
	       
	        //connect to broker
	       // System.out.println("Connecting to broker: "+broker);
	        sampleClient.connect(connOpts);
	        System.out.println("Connected");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String uuid = SubPubMessage.getMessage(sampleClient, topic, payload, qos);
        uuid = uuid.replaceAll("\r|\n", "");
        this.httpUuid = uuid;
        /*String uuid = RequestMethodStore.GETStr(httpUrl+parameter);
        uuid = uuid.replaceAll("\r|\n", "");
        this.httpUuid = uuid;*/
        setInitialized(true);
	}

	@Override
	public boolean performTimeStep() {
		
		//reading the appropriate value from PPT & PET dictionary 
		//所以在配置文件中，Precipitation必须放在第一位，然后是pet
		ScalarSet input_precip = (ScalarSet)this.getValues(this.getInputExchangeItem(0).getQuantity().getID(),this.getInputExchangeItem(0).getElementSet().getID());  //Rainfall
        ScalarSet input_pet = (ScalarSet)this.getValues(this.getInputExchangeItem(1).getQuantity().getID(),this.getInputExchangeItem(1).getElementSet().getID());  //PET
		
        double pet = input_pet.get(0);
        double precip = input_precip.get(0);
        
//      String curr_time = "";
//        String curr_time = WSModelUtils.timeToString(this.getCurrentTime());
//        outputValues.put(curr_time, q);
//        TopModelData modelData = new TopModelData();
//        modelData.setTime(curr_time);
        
        //String payload = MqttTopModel.METHODKEY+"="+MqttTopModel.Method_getvalue+"&uuid="+this.httpUuid+"&"+MqttTopModel.PET_Value_KEY+"="+String.valueOf(pet)+"&"+MqttTopModel.Precip_Value_KEY+"="+String.valueOf(precip);
        //String payload1 = MqttTopModel.METHODKEY+"="+MqttTopModel.Method_getvalue+"&uuid="+this.httpUuid+"&"+MqttTopModel.PET_Value_KEY+"="+pet+"&"+MqttTopModel.Precip_Value_KEY+"="+precip;
        String payload = MqttTopModel.METHODKEY+"="+MqttTopModel.Method_getvalue+"&"+MqttTopModel.PET_Value_KEY+"="+pet+"&"+MqttTopModel.Precip_Value_KEY+"="+precip+"&uuid="+this.httpUuid;
        //String parameter = HttpTopModel.METHODKEY+"="+HttpTopModel.Method_getvalue+"&uuid="+this.httpUuid+"&"+HttpTopModel.PET_Value_KEY+"="+pet+"&"+HttpTopModel.Precip_Value_KEY+"="+precip;
        
        String out = SubPubMessage.getMessage(sampleClient, topic, payload, qos);
        //String out = RequestMethodStore.GETStr(httpUrl+parameter);
        double runoff1 = 0;
        try {
			runoff1 = Double.valueOf(out);
		} catch (Exception e) {
			// TODO: handle exception
			runoff1 = 0;
		}
        double[] runoff = {runoff1};
       
        
        //set the basin outflow as runoff output
        //Runoff必须是第一个OutputExchangeItem
        this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(),this.getOutputExchangeItem(0).getElementSet().getID(), new ScalarSet(runoff));
//       
        return true;
	}
	
	@Override
	public void finish() {
		
		String payload  = MqttTopModel.METHODKEY+"="+MqttTopModel.Method_Finish+"&"+MqttTopModel.UUIDKEY+"="+this.httpUuid;
		String out = SubPubMessage.getMessage(sampleClient, topic, payload, qos);
		//String out = RequestMethodStore.GETStr(httpUrl+parameter);
		System.out.println(out);
		/*try {
			sampleClient.disconnect();
			System.out.println("Disconnected");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		// TODO Auto-generated method stub
		/*try {
			if (OpenMIUtilities.isEmpty(this.outFile)) {
				this.outFile = OpenMIUtilities.getCurrentWorkingDirectory()+File.separator+new Date().getSeconds()+"_TopModelOutPut.txt";
			}
			 File file = new File(this.outFile);
			 if (!file.exists()) {
				file.createNewFile();
			}
			 BufferedWriter bw = new BufferedWriter(new FileWriter(file));
	         //System.IO.Directory.CreateDirectory("wateroutput");
			 String line = "";
			 String timeFormat = "yyyy-MM-dd hh:mm:ss";
			 bw.write("Daily Runoff....");
			 bw.newLine();
			 Calendar startCalendar =CalendarConverter.modifiedJulian2Gregorian( this.getTimeHorizon().getStart().getModifiedJulianDay());
			 Calendar endCalendar =CalendarConverter.modifiedJulian2Gregorian( this.getTimeHorizon().getEnd().getModifiedJulianDay());
//			 line = "StartDate: "+startCalendar.getTime().toString();
			 line = "StartDate: "+OpenMIUtilities.calendar2Str(startCalendar, timeFormat);
			 bw.write(line);
			 bw.newLine();
//			 line = "EndDate: "+endCalendar.getTime().toString();
			 line = "EndDate: "+OpenMIUtilities.calendar2Str(endCalendar, timeFormat);
			 bw.write(line);
			 bw.newLine();
			 bw.newLine();
			 line = "Time["+timeFormat+"], Runoff, Streamflow [l/s], PET, Precipitation";
			 bw.write(line);
			 bw.newLine();

			 for(Calendar calendar:outputValues.keySet()){
//				 String time = calendar.getTime().toString();
				 String time = OpenMIUtilities.calendar2Str(calendar, timeFormat);
//				 double runoff = outputValues.get(calendar);
//				 double streamFlow = runoff*this._watershedArea/86400;
//				 line = time+", "+runoff+", "+streamFlow+" ";
				 TopModelData modelData = outputValues.get(calendar);
				 line = time+", "+modelData.getRunoff()+", "+modelData.getStreamFlow()+", "+modelData.getPet()+", "+modelData.getPrecip();
				 bw.write(line);
				 bw.newLine();
//				 System.err.println(line);
			 }
	        bw.flush();
	        bw.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.addError(e.getMessage());
		}
		
		if (!OpenMIUtilities.isEmpty(this.provFile)) {
			ProvUtils.saveProv2File(this.provFile, bufferInfos);
		}*/
		
	}
	
	
	/**
	 * 返回地形指数的值，以及对应的出现频率，在原有函数上有小的改动
	 * Reads an input raster ascii file containing topographic index to produce topographic index and topographic frequency arrays
	 * topographicIndex:ASCII raster file containing topographic index values
	 * ti:output topographic index array
	 * freq:output topographic frequency array
	 */
    public boolean read_topo_input(String topographicIndex)
//    public boolean read_topo_input(String topographicIndex, double[] ti, double[] freq)
    {
        //---- begin reading the values stored in the topo file
      //  StreamReader sr = new StreamReader(topographicIndex);
        InputStream is=null;
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
	        //-- read header info
	        String line = null;
	        for (int i=0; i<=4; i++){
	        	line = sr.readLine();
	        }

	        //-- save the cellsize
	        double cellsize = Double.parseDouble(line.split(" ")[line.split(" ").length - 1]);
	        line = sr.readLine();

	        //-- save the nodata value
	        String nodata = line.split(" ")[line.split(" ").length-1];
	        line = sr.readLine();

	        //-- store all values != nodata in a list
	        List<Double> topoList = new ArrayList<Double>();
	        int lineNum = 0;
	        while (!OpenMIUtilities.isEmpty(line))
	        {
	            lineNum += 1;
	            String[] vals = line.trim().split(" ");
	            for (int i = 0; i < vals.length; i++){
	            	 if (!vals[i].equals(nodata)){
	            		 //add by zmd,是否需要指定小数点位数，因为下边会有指数对比，判断是否相等
	                 	topoList.add(Double.parseDouble(vals[i])); 
	                 	//modified by zmd, why
	                 	//_watershedArea += cellsize;
	                 }
	            }
	            line = sr.readLine();
	        }

	        //---- calculate frequency of each topographic index
	        //-- consolidate topo list into unique values 
	        Map<Double, Double> d = new HashMap<Double, Double>();
	        for(double t:topoList){
	        	if (d.containsKey(t)) {
					d.put(t, d.get(t)+1.0);
				}else{
					d.put(t, 1.0);
				}
	        }
//	        Dictionary<double, double> d = new Dictionary<double, double>();
	        /*foreach (double t in topoList)
	            if (d.ContainsKey(t))
	                d[t] += 1.0;
	            else
	                d.Add(t, 1.0);*/

	        //-- calculate topo frequency, then return both topographic index and topo frequency arrays
	        double total = (double)topoList.size();
	        this.TI = new double[d.size()];
	        this.freq = new double[d.size()];
	        int index = 0;
	        for(double key:d.keySet()){
	        	this.TI[index] =OpenMIUtilities.round((Double)key,4);
	        	this.freq[index] =OpenMIUtilities.round( d.get(key)/total,10);
	        	index++;
	        }
	        /*foreach (KeyValuePair<double, double> pair in d)
	        {
	            ti[index] = Math.Round(pair.Key,4);
	            freq[index] = Math.Round(d[pair.Key] / total, 10);
	            index ++;
	        }*/
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.addError(e.getMessage());
			return false;
		}
		
        return true;
    }
    
    /**
	 * 返回地形指数的值，以及对应的出现频率，对应于自己生成的txt文件
	 * Reads an input raster ascii file containing topographic index to produce topographic index and topographic frequency arrays
	 * topographicIndex:ASCII raster file containing topographic index values
	 * ti:output topographic index array
	 * freq:output topographic frequency array
	 */
    public boolean read_topo_input3(String topographicIndex)
//    public boolean read_topo_input(String topographicIndex, double[] ti, double[] freq)
    {
        //---- begin reading the values stored in the topo file
      //  StreamReader sr = new StreamReader(topographicIndex);
        InputStream is=null;
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
	        //-- read header info
	        String line = null;
	        String nodata = null;
	        for (int i=0; i<4; i++){
	        	line = sr.readLine();
	        	String[] values = line.split(" ");
	        	if (values[0].equals("NODATA_value")) {
					nodata = values[1];
					break;
				}
	        }
	        //-- save the nodata value
//	        String nodata = line.split(" ")[line.split(" ").length-1];
	        line = sr.readLine();

	        //-- store all values != nodata in a list
	        List<Double> topoList = new ArrayList<Double>();
	        int lineNum = 0;
	        while (!OpenMIUtilities.isEmpty(line))
	        {
	            lineNum += 1;
	            String[] vals = line.trim().split(" ");
	            for (int i = 0; i < vals.length; i++){
	            	 if (!vals[i].equals(nodata)){
	            		 //add by zmd,是否需要指定小数点位数，因为下边会有指数对比，判断是否相等
	                 	topoList.add(Double.parseDouble(vals[i])); 
	                 	//modified by zmd, why
	                 	//_watershedArea += cellsize;
	                 }
	            }
	            line = sr.readLine();
	        }

	        //---- calculate frequency of each topographic index
	        //-- consolidate topo list into unique values 
	        Map<Double, Double> d = new HashMap<Double, Double>();
	        for(double t:topoList){
	        	if (d.containsKey(t)) {
					d.put(t, d.get(t)+1.0);
				}else{
					d.put(t, 1.0);
				}
	        }
//	        Dictionary<double, double> d = new Dictionary<double, double>();
	        /*foreach (double t in topoList)
	            if (d.ContainsKey(t))
	                d[t] += 1.0;
	            else
	                d.Add(t, 1.0);*/

	        //-- calculate topo frequency, then return both topographic index and topo frequency arrays
	        double total = (double)topoList.size();
	        this.TI = new double[d.size()];
	        this.freq = new double[d.size()];
	        int index = 0;
	        for(double key:d.keySet()){
	        	this.TI[index] =OpenMIUtilities.round((Double)key,4);
	        	this.freq[index] =OpenMIUtilities.round( d.get(key)/total,10);
	        	index++;
	        }
	        /*foreach (KeyValuePair<double, double> pair in d)
	        {
	            ti[index] = Math.Round(pair.Key,4);
	            freq[index] = Math.Round(d[pair.Key] / total, 10);
	            index ++;
	        }*/
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.addError(e.getMessage());
			return false;
		}
		
        return true;
    }
    /**
	 * 返回地形指数的值，以及对应的出现频率
	 * 输入的是tif格式的地形指数
	 * ti:output topographic index array
	 * freq:output topographic frequency array
	 */
    public boolean read_topo_input2(String topographicIndex)
    {
    	
    	gdal.AllRegister();
    	Dataset hDataset = gdal.Open(topographicIndex, gdalconstConstants.GA_ReadOnly);
    	
    	if (hDataset == null)  
         {  
             System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());  
             System.err.println(gdal.GetLastErrorMsg());  
             return false;
         }  
   
    	//获取图像大小
        int iXSize = hDataset.getRasterXSize();  
        int iYSize = hDataset.getRasterYSize();  
        System.out.println("Size is " + iXSize + ", " + iYSize);  
  
        Band band = hDataset.GetRasterBand(1);  
        Double[] onValueData = new Double[1];
        band.GetNoDataValue(onValueData);
        
        Double noValue = onValueData[0];
        double buf[] = new double[iXSize];     
        
        //用来存储所有的地形指数，保留10位小时
        List<Double> topoList = new ArrayList<Double>();
        for(int i=0; i<iYSize;i++)  
        {  
            band.ReadRaster(0, i, iXSize, 1, buf);  //读取一行数据  
            for(int j=0; j<iXSize; j++) 
            {
            	if (noValue !=null && noValue == buf[j]) 
            		continue;
            	topoList.add(OpenMIUtilities.round(buf[j],10));
            }
        }  
        hDataset.delete();  
        // 可选  
        gdal.GDALDestroyDriverManager(); 
    
    	
        Map<Double, Double> d = new HashMap<Double, Double>();
        for(double t:topoList){
        	if (d.containsKey(t)) {
				d.put(t, d.get(t)+1.0);
			}else{
				d.put(t, 1.0);
			}
        }
        double total = (double)topoList.size();
        this.TI = new double[d.size()];
        this.freq = new double[d.size()];
        int index = 0;
        for(double key:d.keySet()){
        	this.TI[index] =OpenMIUtilities.round((Double)key,4);
        	this.freq[index] =OpenMIUtilities.round( d.get(key)/total,10);
        	index++;
        }
        return true;
    }
    
    public static void main(String[] args){
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("ConfigFile", "H:/workspace/topmodel/configTest2.xml");
    	map.put("TI", "F:/workspace/mytopindex.txt");
    	map.put("m", "180");
    	map.put("Tmax", "250000");
    	map.put("Interception", "3");
    	map.put("R", "9.66");
    	map.put("WatershedArea_SquareMeters", "0");
    	MqttTopModelEngine engine = new MqttTopModelEngine();
    	engine.initialize(map);
    	engine.performTimeStep();
    }

    @Override
    public String getComponentID() {
    	// TODO Auto-generated method stub
    	return "TOPMODEL";
    }
    
    public void setBufferInfos(List<SmartBufferInfo> bufferInfos){
    	this.bufferInfos = bufferInfos;
    }
    
}

