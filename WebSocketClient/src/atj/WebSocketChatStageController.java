package atj;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

public class WebSocketChatStageController {
	@FXML  TextField userTextField;
	@FXML  TextArea historyTextArea;
	@FXML  TextField	messageTextField;
	@FXML  Button setButton;
	@FXML  Button sendButton;
	@FXML  Button fileUploadButton;
	@FXML  Label userLabel;
	@FXML  ListView<String> fileListView;
	
	private FileChooser fileChooser;
	//private  ObservableList<byte[]> fileList;
	private HashMap<String, byte[]> files;
	private String user;
	private WebSocketClient webSocketClient;
	
	@FXML private void initialize() {
		webSocketClient = new WebSocketClient();
		user = userTextField.getText();
		//fileList =  FXCollections.observableArrayList();
		fileChooser = new FileChooser();
		files = new HashMap<String, byte[]>();
		fileChooser.setTitle("Upload file");

	}
	
	@FXML private void setButton_Click() {
		if(userTextField.getText().isEmpty()) {return;}
		user = userTextField.getText();
	}
	
	@FXML private void sendButton_Click() {
		webSocketClient.sendTextMessage(messageTextField.getText());
		messageTextField.clear();
	}
	
	
	
	@FXML private void fileUploadButton_Click() {
		File selectedFile = fileChooser.showOpenDialog(null);
		if(selectedFile != null) {
			try {
				byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
				String filename = selectedFile.getName();
				System.out.println("ff"+filename);
				ByteBuffer buf = ByteBuffer.allocateDirect((int)4+filename.getBytes().length+fileContent.length);
				buf.putInt(filename.length());
				buf.put(filename.getBytes());
				buf.put(fileContent);
				buf.flip();
				//byte[] a = new byte[10];
				//ByteBuffer buf = ByteBuffer.allocateDirect((int)10);
				//buf.put((byte)0x11);
				
				webSocketClient.sendBinaryMessage(buf);
			} catch (IOException e) {
				e.printStackTrace();
		    }
		}
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
			int len = buf.getInt();
			byte[] tmp = new byte[len];
			buf.get(tmp);
			String filename = new String(tmp);
			byte[] fileContent = new byte[buf.remaining()];
			buf.get(fileContent);
			files.put(filename, fileContent);
			Platform.runLater(() -> fileListView.getItems().add(filename));
			System.out.println("filename:"+filename);
		}
		
		private void connectToWebSocket() {
			WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
			try {
				URI uri = URI.create("ws://localhost:8080/WebSocketEndpoint/websocketendpoint");
				webSocketContainer.connectToServer(this, uri);
			} catch (DeploymentException | IOException e) { e.printStackTrace(); }
		}
		
		public void sendTextMessage(String message) {
			System.out.println("sending text message: "+message);
			try {
				session.getBasicRemote().sendText(user+": "+message);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		public void sendBinaryMessage(ByteBuffer buf) {
			System.out.println("sending file of size:");
			try {
				session.getBasicRemote().sendBinary(buf);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
}
