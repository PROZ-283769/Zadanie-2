package atj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
	private String user;
	OutputStream userFileStream;
	private WebSocketClient webSocketClient;
	
	@FXML private void initialize() {
		webSocketClient = new WebSocketClient();
		user = userTextField.getText();
		fileChooser = new FileChooser();
		fileChooser.setTitle("Upload file");

	}
	
	@FXML private void setButton_Click() {
		if(userTextField.getText().isEmpty()) {return;}
		user = userTextField.getText();
	}
	
	@FXML private void sendButton_Click() {
		if(user.isEmpty()) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Uwaga");
			alert.setHeaderText(null);
			alert.setContentText("Przed wysłaniem wiadomości proszę ustawić nick!");
			alert.showAndWait();
			return;
		}
		webSocketClient.sendTextMessage(messageTextField.getText());
		messageTextField.clear();
	}
	
	@FXML private void fileUploadButton_Click() {
		File selectedFile = fileChooser.showOpenDialog(null);
		if(selectedFile != null) {
			try {
				String filename = selectedFile.getName();
				webSocketClient.sendFilename("DELETE_"+filename);
				InputStream input = new FileInputStream(selectedFile);
				OutputStream output = webSocketClient.session.getBasicRemote().getSendStream();
				byte[] buffer = new byte[1024];
				int read;
				while ((read = input.read(buffer)) > 0) {
					output.write(buffer, 0, read);
				}
				input.close();
				output.close();
			}catch (IOException e) {
				 e.printStackTrace();
			}
			
		}
	}
	
	@FXML
	public void handleSendEnterPressed(KeyEvent event) {
	    if (event.getCode() == KeyCode.ENTER) {
	        sendButton_Click();
	    }
	}
	
	@FXML
	public void handleSetEnterPressed(KeyEvent event) {
	    if (event.getCode() == KeyCode.ENTER) {
	        setButton_Click();
	    }
	}
	
	@FXML 
	public void fileListView_Click() {
           Integer fileIndex = fileListView.getSelectionModel().getSelectedIndex();
           
           if(fileIndex<0)
        	   return;
           	FileChooser fileChooser = new FileChooser();
           	fileChooser.setInitialFileName(fileListView.getSelectionModel().getSelectedItem());
	   		File file = fileChooser.showSaveDialog(null);
	   		if(file != null) {
				try {				
				    InputStream initialStream = new FileInputStream("DELETE_"+fileListView.getSelectionModel().getSelectedItem());
				    byte[] buffer = new byte[initialStream.available()];
				    initialStream.read(buffer);
				    OutputStream outStream = new FileOutputStream(file);
				    outStream.write(buffer);
					outStream.close();
					initialStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	   		}
	   		fileListView.getSelectionModel().clearSelection();
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
			File directory = new File("./");
			for (File f : directory.listFiles()) {
			    if (f.getName().startsWith("DELETE_")) {
			        f.delete();
			    }
			}
			System.out.println("Connection is closed due to: "+closeReason.getReasonPhrase());
		}
		
		@OnMessage public void onMessage(String message, Session session) {
			if(message.indexOf(':')>=0) {
				System.out.println("aString message was received");
				historyTextArea.appendText(message+'\n');
				return;
			}
			System.out.println("filename was received!");
			try {
			userFileStream = new FileOutputStream( new File(message));
			if(userFileStream==null)
				System.out.println("OnMessage NULL UFS");
			} catch (IOException e) {
				e.printStackTrace();
			}
			Platform.runLater(() -> fileListView.getItems().add(message));
			System.out.println("filename:"+message);
		}
		
		
		@OnMessage public void onMessage(ByteBuffer buf, boolean last, Session session) {
			try {
				if(userFileStream==null) {
					System.out.println("UFS==NULL");
					return;
				}
				//System.out.println(buf.remaining());
				userFileStream.write(buf.array());
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		
		public void sendFilename(String filename) {
			System.out.println("Sending filename: "+filename);
			try {
				session.getBasicRemote().sendText(filename);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		public void sendPartialBinaryMessage(ByteBuffer buf, boolean last) {
			//System.out.println("Sending partial binary file");
			try {
				session.getBasicRemote().sendBinary(buf, last);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
}
