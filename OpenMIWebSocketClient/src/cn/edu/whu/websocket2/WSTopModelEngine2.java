package cn.edu.whu.websocket2;

import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import cn.edu.whu.openmi.models.data.PrecipitationEngine;
import cn.edu.whu.openmi.models.topmodel.TopModelData;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.websocket.WSModelUtils;

/**
 * @author zhangmingda
 *
 */
public class WSTopModelEngine2 extends SimpleWrapper {

	public final static String PET_Value_KEY = "PET_Value";
	public final static String Precip_Value_KEY = "Precip_Value";
	public final static String RunOff_Value_KEY = "Runoff_Value";
	private WebSocketClient wc = null;
//	private String wsUrl = "ws://202.114.118.190:8086/OpenMIWebSocketServer/ws/topmodel2/topmodel?";
	private String wsUrl = "ws://localhost:8080/OpenMIWebSocketServer/ws/topmodel2/topmodel?";
	private String name = "OpenMIWebSocketClient";
	private boolean flag = false;
	Map<Calendar, TopModelData> outputValues = new LinkedHashMap<Calendar, TopModelData>();
	public WSTopModelEngine2() {
		InputStream inConfig = PrecipitationEngine.class
				.getClass()
				.getResourceAsStream(
						"/cn/edu/whu/openmi/models/topmodel/Topmodel-Config.xml");
		this.setVariablesFromConfigFile(inConfig);
	}

	public boolean connectBlocking() {
		if (this.wc == null) {
			return false;
		}
		boolean flag = false;
		try {
			flag = this.wc.connectBlocking();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	public boolean sendText(String text) {
		if (this.wc == null) {
			return false;
		}
		this.wc.send(text);

		return true;
	}

	public boolean closeConnect() {
		if (this.wc == null)
			return false;

		try {
			this.wc.closeBlocking();
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void initialize(HashMap properties) {
		// TODO Auto-generated method stub
		super.initialize(properties);
		setInitialized(true);
		int i = 0;
		Iterator iter = properties.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (i == 0) {
				this.wsUrl += key + "=" + val;
			} else {
				this.wsUrl += "&" + key + "=" + val;
			}
			i++;
		}

		try {
			this.wc = new WebSocketClient(new URI(this.wsUrl), new Draft_17()) {

				@Override
				public void onOpen(ServerHandshake handshakedata) {
//					System.out.println(handshakedata.getHttpStatusMessage());
//					System.out.println("Open the connection from the client.");
				}

				// 只接收服务器端传送回来的set请求
				@Override
				public void onMessage(String message) {
					// System.out.println(message);
//					System.out.println("the client named " + name+ " recieve message:" + message);
					Map<String, String> map = WSModelUtils
							.parseArguments(message);
					String method = map.get(WSModelUtils.METHODKEY);
					if (!method.equals("set")) {
						return;
					}
					/*String time = map.get(WSModelUtils.TIMEKEY);
					String element = map.get(WSModelUtils.ELEMENTYKEY);
					String quantity = map.get(WSModelUtils.QUANTITYKEY);
					String value = map.get(WSModelUtils.VALUEKEY);*/
					String	value = map.get(RunOff_Value_KEY);
					if (value.equalsIgnoreCase("novalue")
							|| value.equalsIgnoreCase("null"))
						value = "0";

					double d1 = 0;
					try {
						d1 = Double.parseDouble(value);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					double[] val = { d1 };
					setValues("Runoff", "Coweeta Runoff", new ScalarSet(val));
					flag = false;
				}

				@Override
				public void onError(Exception ex) {
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					System.out.println("Quit the connnection.");
				}
			};
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.wc = null;
		}

		this.connectBlocking();
	}

	// 由该方法驱动，所以sendText放到该方法中。
	@Override
	public boolean performTimeStep() {
		// 在配置文件中，Precipitation必须放在第一位，然后是pet
	/*	String precipElement = this.getInputExchangeItem(0).getElementSet()
				.getID();
		String precipQuatity = this.getInputExchangeItem(0).getQuantity()
				.getID();
		String petElement = this.getInputExchangeItem(1).getElementSet()
				.getID();
		String petQuatity = this.getInputExchangeItem(1).getQuantity().getID();
		ScalarSet input_precip = (ScalarSet) this.getValues(precipQuatity,
				precipElement); // Rainfall
		ScalarSet input_pet = (ScalarSet) this
				.getValues(petQuatity, petElement); // PET
*/
		ScalarSet input_precip = (ScalarSet)this.getValues("Precipitation", "Coweeta Precipitation");
		ScalarSet input_pet = (ScalarSet) this
				.getValues("PET", "Coweeta");
		// 测试使用
		/*
		 * ScalarSet input_precip = new ScalarSet(new double[]{1.0}); ScalarSet
		 * input_pet = new ScalarSet(new double[]{1.0});
		 */

		//2016.05.16 原来使用三个sendText，分别用来传递pet、Precipitation和获取数据。直接发送一次，里边包含需要更新的数据，同时直接获取结果。
		
//		String time = WSModelUtils.timeToString(this.getCurrentTime());
		/*String baseReq = WSModelUtils.METHODKEY + "=set&"
				+ WSModelUtils.TIMEKEY + "=" + time;
		String precipReq = baseReq + "&" + WSModelUtils.ELEMENTYKEY + "="
				+ precipElement + "&" + WSModelUtils.QUANTITYKEY + "="
				+ precipQuatity + "&" + WSModelUtils.VALUEKEY + "="
				+ input_precip.get(0);
		// 更新服务器端的Precipitation
		this.sendText(precipReq);

		String petReq = baseReq + "&" + WSModelUtils.ELEMENTYKEY + "="
				+ petElement + "&" + WSModelUtils.QUANTITYKEY + "="
				+ petQuatity + "&" + WSModelUtils.VALUEKEY + "="
				+ input_pet.get(0);
		// 更新服务器端的蒸发量
		this.sendText(petReq);
		
		// Runoff必须是第一个OutputExchangeItem
		String runoffQuantity = this.getOutputExchangeItem(0).getQuantity()
				.getID();
		String runoffElement = this.getOutputExchangeItem(0).getElementSet()
				.getID();
		String getReq = WSModelUtils.METHODKEY + "=get&" + WSModelUtils.TIMEKEY
				+ "=" + time + "&" + WSModelUtils.ELEMENTYKEY + "="
				+ runoffElement + "&" + WSModelUtils.QUANTITYKEY + "="
				+ runoffQuantity;*/

		String getReq = WSModelUtils.METHODKEY + "=get&"+PET_Value_KEY+"="+input_pet.get(0)+"&"+Precip_Value_KEY+"="+input_precip.get(0);
		// 获取计算值
		this.sendText(getReq);
		// this.setValues(this.getOutputExchangeItem(0).getQuantity().getID(),this.getOutputExchangeItem(0).getElementSet().getID(),
		// new ScalarSet(runoff));
		// this.setValues(this.getOutputExchangeItem(1).getQuantity().getID(),this.getOutputExchangeItem(1).getElementSet().getID(),
		// new ScalarSet(streamflow));

		flag = true;
		// 用来同步一个交互：等待传回数据。
		while (flag) {
			
			//do nothing
			System.out.print("waiting");
			/*try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}

		return true;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		this.closeConnect();
	}

	// 直接用来测试，单个交互过程
	public static void main(String[] args) {
		HashMap<String, String> map = new HashMap<String, String>();
		// map.put("ConfigFile", "H:/workspace/topmodel/configTest2.xml");
		map.put("TI", "F:/workspace/openmi/TI_raster.txt");
		map.put("m", "180");
		map.put("Tmax", "250000");
		map.put("Interception", "3");
		map.put("R", "9.66");
		map.put("WatershedArea_SquareMeters", "0");
		WSTopModelEngine2 engine = new WSTopModelEngine2();
		engine.initialize(map);
		for (int i = 0; i < 100000000; i++) {
			engine.performTimeStep();
		}
	}

	@Override
	public String getComponentID() {
		// TODO Auto-generated method stub
		return "TOPMODEL";
	}

}
