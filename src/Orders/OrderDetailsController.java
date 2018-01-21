package Orders;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import Commons.ProductInOrder;
import PacketSender.Command;
import PacketSender.IResultHandler;
import PacketSender.Packet;
import PacketSender.SystemSender;
import Products.Flower;
import Products.Product;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class OrderDetailsController implements Initializable {
	private static Stage primaryStage;
	private static Order order;
	private ArrayList<OrderPayment> payments = new ArrayList<>();
	
	public static LinkedHashMap<Product, Integer> cartProducts = new LinkedHashMap<>();
	
	public OrderDetailsController() {
		super();
		// TODO Auto-generated constructor stub
	}
	public void setOrder(Order order) {
		this.order=order;
		// TODO Auto-generated constructor stub
	}
	public void start(Stage arg0) throws Exception {
		primaryStage=arg0;
		String title = "Orders details";
		String srcFXML = "/Orders/OrderDetailsUI.fxml";
		String srcCSS = "/Orders/application.css";

		
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(srcFXML));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource(srcCSS).toExternalForm());
			arg0.setTitle(title);
			arg0.setScene(scene);
			arg0.show();
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			e.printStackTrace();
		}
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				OrderManagementController orders = new OrderManagementController();
				try {
					orders.start(new Stage());
					primaryStage.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});	
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		getOrderDetails();		
	}

	private void getOrderDetails() {
		Packet packet = new Packet();//create packet to send
		packet.addCommand(Command.getOrderPaymentDetails);
		packet.addCommand(Command.getOrderInProductsDetails);
		ArrayList<Object> list = new ArrayList<>();
		list.add(order.getoId());
		packet.setParametersForCommand(Command.getOrderPaymentDetails,list);
		packet.setParametersForCommand(Command.getOrderInProductsDetails, list);
		// create the thread for send to server the message
		SystemSender send = new SystemSender(packet);

		// register the handler that occurs when the data arrived from the server
		send.registerHandler(new IResultHandler() {

			@Override
			public void onWaitingForResult() {//waiting when send
				
			}

			@Override
			public void onReceivingResult(Packet p)//set combobox values
			{
				if (p.getResultState()) {
					@SuppressWarnings("unused")
					ArrayList<OrderPayment> orderPayments = p.<OrderPayment>convertedResultListForCommand(Command.getOrderPaymentDetails);
					ArrayList<ProductInOrder> productInOrder = p.<ProductInOrder>convertedResultListForCommand(Command.getOrderInProductsDetails);
					ArrayList<Product> products = p.<Product>convertedResultListForCommand(Command.getAllProductsInOrder);
				}
				else//if it was error in connection
					JOptionPane.showMessageDialog(null,"Connection error","Error",JOptionPane.ERROR_MESSAGE);
			}
		});
		send.start();
	}
}
