package cn.edu.whu.wssync;

public class WSSyncThread extends Thread{
	private WebSocketSyncClient wsClient = null;
	private SyncMessage syncMsg;
	private boolean stopped = false;
	public  Object LOCK = new Object();
	//用来测试程序的正确性
	private int counter = 0;
	public WSSyncThread(WebSocketSyncClient syncClient,SyncMessage syncMsg){
		this.wsClient = syncClient;
		this.syncMsg = syncMsg;
	}
	
	
	@Override
	public void run() {
		//while循环保证线程一直运行
		while (!stopped) {
			 synchronized (LOCK) {
				 //等待更新SyncMessage内容
				 try {
					LOCK.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				 this.wsClient.send(this.syncMsg.getMsg());
				 
				 //测试正确性
//				 System.out.println("+++++++实际发送："+this.syncMsg.getMsg());
//				 System.out.println("************第"+(counter++)+"发送************");
				 
			}
		 }
		
		System.out.println("The sending thread is stopped.");
	}
	
	public void stopSend(){
		this.stopped = true;
	}
}
