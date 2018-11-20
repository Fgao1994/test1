package cn.edu.whu.openmi.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

import javax.print.DocFlavor.STRING;
import javax.print.attribute.standard.Finishings;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.whu.openmi.websocket.WSModelUtils;



public class HttpGisVolumnServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public final static String QUANTITYKEY = "quantity";
	public final static String ELEMENTYKEY = "element";
	public final static String TIMEKEY = "time";
	public final static String METHODKEY = "method";
	public final static String VALUEKEY = "value";
	public final static String TIMEFORMAT = "yyyy-MM-dd hh:mm:ss";
	public final static String Method_Initialize= "initialize";
	public final static String Method_getvalue = "getvalue";
	public final static String Method_Finish = "finish";
	public final static String Flood_Volumn_KEY = "Flood_Volumn";
	public final static String UUIDKEY= "uuid";
	
	private HashMap<String, HttpGisVolumn> gisVolumnMap = new HashMap<>();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		//http header test
		System.out.println("this is GisVolumn");
		Enumeration names = req.getHeaderNames();
		System.out.println("===================================================================");
		while(names.hasMoreElements()){
			String name = (String)names.nextElement();
			System.out.println(name+":"+req.getHeader(name));
		}
		
		
		String method = req.getParameter(METHODKEY);
		if (method.equals(Method_Initialize)) {
			//System.out.println("initialized");
			createModel(req,resp);;
		}else if (method.equals(Method_getvalue)) {
			getValue(req, resp);
		}else if (method.equals(Method_Finish)) {
			finish(req, resp);
		}
		
	}
	
	public void finish(HttpServletRequest req,HttpServletResponse resp){
		String uuid = req.getParameter(UUIDKEY);
		HttpGisVolumn model = gisVolumnMap.get(uuid);
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
	public void getValue(HttpServletRequest req,HttpServletResponse resp){
		String uuid = req.getParameter(UUIDKEY);
		HttpGisVolumn model = gisVolumnMap.get(uuid);
		if(model == null){
			return ;
		}
		
		String flood_water = req.getParameter(Flood_Volumn_KEY);
		model.setInputFloodWater(Double.valueOf(flood_water));
		model.performTimeStep();
		double result = model.getOutputItem();
		String resultStr = WSModelUtils.appendToByte(result, 900);
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
	public void createModel(HttpServletRequest req,HttpServletResponse resp){
		String uuid = UUID.randomUUID().toString();
		HttpGisVolumn gisVolumnModel = new HttpGisVolumn();
		this.gisVolumnMap.put(uuid,gisVolumnModel);
		String out_file = req.getParameter("out_file");
		HashMap<String, String> propMap = new HashMap<>();
		propMap.put("out_file", out_file);
		gisVolumnModel.initialize(propMap);
		
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
