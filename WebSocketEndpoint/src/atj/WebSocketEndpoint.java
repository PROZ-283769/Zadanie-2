package atj;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/websocketendpoint")
public class WebSocketEndpoint {

	@OnOpen
	public void onOpen(Session session) {
	}
	
	@OnClose
	public void onClose(Session session) {
		
	}
	@OnError
	public void onError(Throwable error) {
		
	}
	
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("Got string!");
		try {
			for(Session oneSession : session.getOpenSessions()) {
				if(oneSession.isOpen()) {
					oneSession.getBasicRemote().sendText(message);
				}
			}
			
		} catch (IOException e) {e.printStackTrace();}
	}
	
	
	@OnMessage
	public void onMessage(ByteBuffer buf, Session session){
		System.out.println("Got file! of size:"+buf.capacity());
		try {
			for(Session oneSession : session.getOpenSessions()) {
				if(oneSession.isOpen()) {
					oneSession.getBasicRemote().sendBinary(buf);
				}
			}
			
		} catch (IOException e) {e.printStackTrace();}
	}
	
}
