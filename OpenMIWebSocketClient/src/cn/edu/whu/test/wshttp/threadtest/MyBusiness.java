package cn.edu.whu.test.wshttp.threadtest;


public class MyBusiness {
	private int num = 10;
	private static Object LOCK = new Object();  
	volatile boolean sendReady = true;//这里相当于定义了控制该谁执行的一个信号灯  
	
	public void send(){
		for(int i=0;i<num;i++){
			synchronized (LOCK) {
				System.out.println("Send No."+i);
				if (sendReady) {
					sendReady = false;
					LOCK.notify();
					System.out.println("send notified");
					if (i<num) {
						try {
//							System.out.println("before send wait.");
							LOCK.wait();
							System.out.println("after send wait.");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public void receive(){

		for(int i=0;i<num;i++){
			synchronized (LOCK) {
				System.out.println("Receive No."+i);
				if (!sendReady) {
					sendReady = true;
					LOCK.notify();
					System.out.println("receive notified.");
					if (i<num) {
						try {
							System.out.println("before receive wait.");
							LOCK.wait();
							System.out.println("after receive wait.");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	
	}
	public static void main(String args[]) {  
        final MyBusiness business = new MyBusiness();  
        Thread a=new Thread(new Runnable(){  
            @Override  
            public void run(){  
                business.send();            }  
        });  
        Thread b=new Thread((new Runnable() {  
            @Override  
            public void run() {  
                business.receive();  
            }  
        }));  
        a.start();  
        b.start();  
    }  
	
}
