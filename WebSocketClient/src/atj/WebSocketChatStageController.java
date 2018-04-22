package atj;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class WebSocketChatStageController {
	@FXML private TextField userTextField;
	@FXML private TextArea historyTextArea;
	@FXML private TextField	messageTextField;
	@FXML private Button setButton;
	@FXML private Button sendButton;
	@FXML private Label userLabel;
	
	private String user;
	private WebSocketClient webSocketClient;
	
	@FXML private void initialize() {
		webSocketClient = new WebSocketClient();
		user =  userTextField.getText();
	}
	
	@FXML private void setButton_Click() {
		if(userTextField.getText().isEmpty()) {return;}
		user = userTextField.getText();
	}
	
	@FXML private void sendButton_Click() {
		webSocketClient.sendMessage(messageTextField.getText());
	}
	
	public void closeSession(CloseReason closeReason) {
		try {
			webSocketClient.session.close(closeReason);
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	@ClientEndpoint
	public class WebSocketClient{
		private Session session;
		public WebSocketClient() { connectToWebSocket(); }
		
		@OnOpen public void onOpen(Session session) {
			System.out.println("Connection is open");
			this.session = session;
		}
		@OnClose public void onClose(CloseReason closeReason) {
			System.out.println("Connection is closed due to: "+closeReason.getReasonPhrase());
		}
		@OnMessage public void onMessage(String message, Session session) {
			encodeBackToMessageAndPopulate(message);
			
		}
		private void connectToWebSocket() {
			WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
			try {
				URI uri = URI.create("ws://localhost:8080/WebSocketServer/websocketserver");
				webSocketContainer.connectToServer(this, uri);
			} catch (DeploymentException | IOException e) { e.printStackTrace(); }
		}
		
		public void sendMessage(String message) {
			try {
				session.getBasicRemote().sendText(message);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
}
	}
	
	
}
