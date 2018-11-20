package cn.edu.whu.test.wshttp;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws/test/{nickName}")
public class WebSocketTestServer {
	private int counter=0;
	Session session = null;
 
    /**
     * 打开连接
     * 
     * @param session
     * @param nickName
     */
    @OnOpen
    public void onOpen(Session session,
            @PathParam(value = "nickName") String nickName) {
    	this.session = session;
    	System.out.println("the server is opened.");
    }
 
    /**
     * 关闭连接
     */
    @OnClose
    public void onClose() {
       System.out.println("The connection is closed.");
    }
 
    /**
     * 接收信息
     * 
     * @param message
     * @param nickName
     */
    @OnMessage
    public void onMessage(String message,
            @PathParam(value = "nickName") String nickName) {
    	try {
			this.session.getBasicRemote().sendText(message);
//			System.out.println("接收："+message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("Message-No-"+(counter++));
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

}
