package atj;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class WebSocketChatStageController {
	@FXML  TextField userTextField;
	@FXML  TextArea historyTextArea;
	@FXML  TextField	messageTextField;
	@FXML  Button setButton;
	@FXML  Button sendButton;
	@FXML  Label userLabel;
	
	private String user;
	private WebSocketClient webSocketClient;
	
	@FXML private void initialize() {
		webSocketClient = new WebSocketClient();
		user = userTextField.getText();
	}
	
	@FXML private void setButton_Click() {
		if(userTextField.getText().isEmpty()) {return;}
		user = userTextField.getText();
	}
	
	@FXML private void sendButton_Click() {
		webSocketClient.sendTextMessage(messageTextField.getText());
		messageTextField.clear();
	}
	
	@FXML
	public void handleEnterPressed(KeyEvent event) {
	    if (event.getCode() == KeyCode.ENTER) {
	        sendButton_Click();
	    }
	}
	
	public void closeSession(CloseReason closeReason) {
		try {
			System.out.println("close Session");
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
			System.out.println("String message was received");
			historyTextArea.appendText(message+'\n');
		}
		
		@OnMessage public void onMessage(ByteBuffer buf, Session session) {
			System.out.println("Binary message was received");
			//encodeBackToMessageAndPopulate(message);
		}
		
		private void connectToWebSocket() {
			WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
			try {
				URI uri = URI.create("ws://localhost:8080/WebSocketEndpoint/websocketendpoint");
				webSocketContainer.connectToServer(this, uri);
			} catch (DeploymentException | IOException e) { e.printStackTrace(); }
		}
		
		public void sendTextMessage(String message) {
			System.out.println("sending message: "+message);
			try {
				session.getBasicRemote().sendText(user+": "+message);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		public void sendBinaryMessage(ByteBuffer buf) {
			try {
				session.getBasicRemote().sendBinary(buf);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
}
