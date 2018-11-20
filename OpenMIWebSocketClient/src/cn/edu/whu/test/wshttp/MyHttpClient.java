package cn.edu.whu.test.wshttp;

import cn.edu.whu.openmi.util.RequestMethodStore;

public class MyHttpClient {
	public static String httpurl = "http://192.168.73.150:8080/OpenMIWebSocketServer/httptest";
	public static String sendMsg = ValueSent.STR_1000B;
	public static int num = 200;
	public static long  execute() {
		long startTime = System.currentTimeMillis();
		for (int j = 0; j < num; j++) {
			String url = httpurl + "?value=" + sendMsg ;
			String resp = RequestMethodStore.GETStr(url.trim());
			/* 验证程序的正确性
			System.out.println("send-"+j+":"+sendMsg);
			System.out.println("receive-"+j+":"+resp);
			*/
		}
		long endTime = System.currentTimeMillis();
		long spendTime = (endTime - startTime);
		//System.out.println("次数:" + num +",时间：" + spendTime+ "ms\n数据：" + sendMsg);
		System.out.println("次数:" + num +",时间：" + spendTime);
		return spendTime;
	}

	public static void main(String[] args) {

		MyHttpClient.execute();
//使用for循环，每次运行时间逐次减少		
		/*for(int i=0;i<5;i++){
			MyHttpClient.execute();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}

}
