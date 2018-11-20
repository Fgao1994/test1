package cn.edu.whu.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
 
public class TestTocatWebSocket {
 
    public static void main(String[] args) throws URISyntaxException {
 
    	//String url = "ws://localhost:8080/WebSocketServerTest/ws/chat/" + args[0];
        String url = "ws://localhost:8080/OpenMIWebSocketServer/ws/models/topmodel?param1=hello&param2=world";
        //String url = "ws://localhost:8080/WebSocketServerProject/ws/chat/" + "Mingda";
        WebSocketClient wc = new WebSocketClient(new URI(url), new Draft_17()) {
 
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println(handshakedata.getHttpStatusMessage());
                System.out.println("Open the connection from the client.");
            }
 
            @Override
            public void onMessage(String message) {
            	//System.out.println(message);
            	System.out.println("the client recieve message:"+message);
            }
 
            @Override
            public void onError(Exception ex) {
            }
 
            @Override
            public void onClose(int code, String reason, boolean remote) {
            	System.out.println("Quit the connnection.");
            }
        };
 
        wc.connect();
        
        while (true) {
          /*  Scanner scanner = new Scanner(System.in);
            if (!scanner.hasNextLine()) {
				continue;
			}
            String message = scanner.nextLine();*/
        	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        	System.out.println("Enter your value:");

        	String message="";
			try {
				message = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (message.equals("q")) {
                wc.close();
                break;
            }
            //scanner.close();
            wc.send(message);
        }
    }
}
