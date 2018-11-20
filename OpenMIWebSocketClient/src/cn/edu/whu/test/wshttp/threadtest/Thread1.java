package cn.edu.whu.test.wshttp.threadtest;

public class Thread1 {  
    public static void main(String args[]) {  
        final Bussiness business = new Bussiness();  
        Thread a=new Thread(new Runnable(){  
            @Override  
            public void run(){  
                business.SubThread();  
            }  
        });  
        Thread b=new Thread((new Runnable() {  
            @Override  
            public void run() {  
                business.MainThread();  
            }  
        }));  
        a.start();  
        b.start();  
    }  
}  