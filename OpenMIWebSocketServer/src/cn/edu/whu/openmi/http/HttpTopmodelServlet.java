package cn.edu.whu.openmi.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.whu.openmi.websocket.WSModelUtils;

public class HttpTopmodelServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String QUANTITYKEY = "quantity";
	public final static String ELEMENTYKEY = "element";
	public final static String TIMEKEY = "time";
	public final static String METHODKEY = "method";
	public final static String VALUEKEY = "value";
	public final static String TIMEFORMAT = "yyyy-MM-dd hh:mm:ss";
	public final static String Method_Initialize= "initialize";
	public final static String Method_getvalue= "getvalue";
	public final static String Method_Finish= "finish";
	String precipElement = "Coweeta Precipitation",precipQuantity="Precipitation",petElement="Coweeta",petQuantity="PET";
	
	public final static String PET_Value_KEY = "PET_Value";
	public final static String Precip_Value_KEY = "Precip_Value";
	
	public final static String UUIDKEY= "uuid";
	
	private HashMap<String, HttpTopmodel> topModelMap = new HashMap<>();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		Enumeration names = req.getHeaderNames();
		System.out.println("===================================================================");
		while(names.hasMoreElements()){
			String name = (String)names.nextElement();
			System.out.println(name+":"+req.getHeader(name));
		}
		System.out.println("this is topmodel");
		String method=req.getParameter(METHODKEY);
		if (method.equals(Method_Initialize)) {
			createTopmodel(req,resp);;
		}else if (method.equals(Method_getvalue)) {
			getValue(req, resp);
		}else if (method.equals(Method_Finish)) {
			finish(req, resp);
		}
	}

	public void finish(HttpServletRequest req,HttpServletResponse resp){
		String uuid = req.getParameter(UUIDKEY);
		HttpTopmodel model = topModelMap.get(uuid);
		if(model == null){
			return ;
		}
		model.finish();
		
		resp.setContentType("text/html;charset=GB2312");
		//write html page
		PrintWriter out = null;
		try {
			out = resp.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		out.print("true");
		out.close();
	}
	
	//先获取对应PET和Precipation的值
	public void getValue(HttpServletRequest req,HttpServletResponse resp){
		String uuid = req.getParameter(UUIDKEY);
		HttpTopmodel model = topModelMap.get(uuid);
		if(model == null){
			return ;
		}
		
		String pet = req.getParameter(PET_Value_KEY);
		String precip = req.getParameter(Precip_Value_KEY);
		String time = req.getParameter(TIMEKEY);
		
		model.setInputPET(Double.valueOf(pet));
		model.setInputPrecipataion(Double.valueOf(precip));
		
		model.performTimeStep();
		
		double result = model.getOutputItem();
		String resultStr = WSModelUtils.appendToByte(result, 900);
		//System.out.println(resultStr.getBytes().length);
		resp.setContentType("text/html;charset=GB2312");
		//write html page
		PrintWriter out = null;
		try {
			out = resp.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		out.print(resultStr);
		out.close();
	}
	
	//创建一个新的Topmodel实例，返回UUID
	private void createTopmodel(HttpServletRequest req,HttpServletResponse resp){
		String uuid = UUID.randomUUID().toString();
		HttpTopmodel topmodel  = new HttpTopmodel();
		topModelMap.put(uuid, topmodel);
		String TI = req.getParameter("TI");
		String m = req.getParameter("m");
		String Tmax = req.getParameter("Tmax");
		String R = req.getParameter("R");
		String Interception = req.getParameter("Interception");
		String WatershedArea_SquareMeters = req.getParameter("WatershedArea_SquareMeters");
		String outputdata = req.getParameter("OutputData");
		
		HashMap<String, String> propMap = new HashMap<>();
		propMap.put("TI", TI);
		propMap.put("m", m);
		propMap.put("Tmax", Tmax);
		propMap.put("R", R);
		propMap.put("Interception", Interception);
		propMap.put("WatershedArea_SquareMeters", WatershedArea_SquareMeters);
		propMap.put("OutputData", outputdata);
		topmodel.initialize(propMap);
		
		resp.setContentType("text/html;charset=GB2312");
		//write html page
		PrintWriter out = null;
		try {
			out = resp.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		out.print(uuid);
		out.close();
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
//		super.doPost(req, resp);
		this.doGet(req, resp);
	}
	
	
}
