package Login;

import java.net.URL;
import java.util.ResourceBundle;

import Branches.CustomerService;
import Branches.Employee;
import Branches.Role;
import Branches.ServiceExpert;
import Customers.ComplainsController;
import Survey.SurveyManagementController;
import Users.Permission;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServiceMenuController implements Initializable
{
	@FXML private Button btnComplains;
	
	private static Stage primaryStage;
	private static LoginController loginController;
	    
	    
	public void setLoginController(LoginController login)
	{
	   loginController = login;
	}
	    
	public void onClickingSurveyManagment()
	{
		primaryStage.close();
		SurveyManagementController sc = new SurveyManagementController();
		sc.start(new Stage());
		
	}
	
	public void onClickComplainsManagement()
	{
		primaryStage.close();
		ComplainsController cc = new ComplainsController();
		cc.start(new Stage());
	}
	
	/**
	 * Event Logged out that occurs when clicking on logout
	 */
	public void performLoggedOutHandler()
	{
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Logged Out");
		alert.setContentText("Are you Sure?");
		ButtonType okButton = new ButtonType("Yes", ButtonData.YES);
		ButtonType noButton = new ButtonType("No", ButtonData.NO);
		
		alert.getButtonTypes().setAll(okButton, noButton);
		alert.showAndWait().ifPresent(type -> {
		        if (type == okButton)
		        {
		        	loginController.performLoggedOut(LoginController.userLogged);
		        } 
		});
	}
	
	private void setComplainsButtonVisibility(boolean state)
	{
		btnComplains.setDisable(state);
	}
	
	private void initComplainsButton()
	{
		if(((Employee)LoginController.userLogged).getRole() == Role.ServiceExpert)
			setComplainsButtonVisibility(true);
	}
	
	public void start(Stage mainStage) throws Exception {
		String title = "Main Menu";
		String srcFXML = "/Login/ServiceMenu.fxml";
		
	
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(srcFXML));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			mainStage.setTitle(title);
			mainStage.setScene(scene);
			mainStage.setResizable(false);
			mainStage.show();
			primaryStage=mainStage;
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		          public void handle(WindowEvent we) {

		            Alert alert = new Alert(Alert.AlertType.WARNING);
		      		alert.setTitle("Logged Out");
		      		alert.setContentText("Are you Sure?");
		      		ButtonType okButton = new ButtonType("Yes", ButtonData.YES);
		      		ButtonType noButton = new ButtonType("No", ButtonData.NO);
		      		
		      		alert.getButtonTypes().setAll(okButton, noButton);
		      		alert.showAndWait().ifPresent(type -> {
		      		        if (type == okButton)
		      		        {
		      		        	loginController.performLoggedOut(LoginController.userLogged);
		      		        	System.exit(0);
		      		        } 
		      		        else
		      		        {
		      		        	we.consume();
		      		        }
		      		});
		          }
		      }); 
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		initComplainsButton();
	}
}
