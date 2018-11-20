package cn.edu.whu.test.wshttp.threadtest;

class Bussiness {  
    private static Object LOCK = new Object();  
    volatile boolean bShouldSub = true;//这里相当于定义了控制该谁执行的一个信号灯  
  
    public void MainThread() {  
        for (int i = 0; i < 2; i++) {  
            synchronized (LOCK) {//notify和wait的对象一定要和synchronized的对象保持一致  
                for (int j = 0; j < 10; j++) {  
                    System.out.println(Thread.currentThread().getName() + "+MainThread:i=" + i + ",j=" + j);  
                }  
                if (bShouldSub) {  
                    bShouldSub = false;  
                    LOCK.notify();  
                    if(i<49){  
                        try {  
                            LOCK.wait();  
                        }catch (InterruptedException e) {  
                            // TODO Auto-generatedcatch block  
                            e.printStackTrace();  
                        }  
                    }  
                }  
            }  
        }  
    }  
  
  
    public void SubThread() {  
        for (int i = 0; i < 2; i++) {  
            synchronized (LOCK){  
                for (int j = 0; j < 5; j++) {  
                    System.out.println(Thread.currentThread().getName() + "+SubThread:i=" + i + ",j=" + j);  
                }  
                if (!bShouldSub) {  
                    bShouldSub = true;  
                    LOCK.notify();  
                    if(i<49){  
                        try {  
                            LOCK.wait();  
                        } catch (InterruptedException e) {  
                            // TODO Auto-generatedcatch block  
                            e.printStackTrace();  
                        }  
                    }  
  
                }  
            }  
        }  
    }  
  
    
}  