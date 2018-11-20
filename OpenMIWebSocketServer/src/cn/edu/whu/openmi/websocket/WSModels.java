package cn.edu.whu.openmi.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
@ServerEndpoint(value = "/ws/models/{nickName}")
public class WSModels {

	 
    /**
     * 
     */
//    private static final Set<WebSocketModels> connections = new CopyOnWriteArraySet<WebSocketModels>();
	private int flag = 1;
    private String nickName;
 
    /**
     * WebSocket Session
     */
    private Session session;
 
    public WSModels() {
    
    }
 
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
        this.nickName = nickName;
      //  connections.add(this);
//        String message = String.format("System> %s %s", this.nickName," has joined.");
        System.out.println("the model named "+this.nickName + " is opened.");
//        WebSocketModels.broadCast(message);
        
        //测试传递参数
        Map<String, String> pathParameters=session.getPathParameters();
        Map<String, List<String>> reqParams = session.getRequestParameterMap();
        System.out.println(pathParameters);
        System.out.println(reqParams);
    }
 
    /**
     * 关闭连接
     */
    @OnClose
    public void onClose() {
//        connections.remove(this);
        String message = String.format("System> %s, %s", this.nickName,
                " has disconnection.");
//        WebSocketModels.broadCast(message);
        System.out.println(message);
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
    	System.out.println("the server recieve the message:"+message+" from the client" +"' Number:"+(flag++));
//        WebSocketModels.broadCast(nickName + ">" + message);
    	try {
			this.session.getBasicRemote().sendText("FromServer_'"+message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
