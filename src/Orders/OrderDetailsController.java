package Orders;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import Commons.ProductInOrder;
import Commons.Refund;
import Commons.Status;
import Login.CustomerMenuController;
import PacketSender.Command;
import PacketSender.FileSystem;
import PacketSender.IResultHandler;
import PacketSender.Packet;
import PacketSender.SystemSender;
import Products.CatalogProduct;
import Products.ConstantData;
import Products.Flower;
import Products.FlowerInProduct;
import Products.Product;
import Products.SelectProductController;
import Products.SelectProductController.CatalogProductDetails;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class OrderDetailsController implements Initializable {
	
	@FXML
    private Button btnBack;

    @FXML
    private ListView<Product> listViewOrderLines;

    @FXML
    private Label lblCreDate;

    @FXML
    private Label lblReqDate;

    @FXML
    private CheckBox chkIsDelivery;

    @FXML
    private Button btnCacenlOrder;

    @FXML
    private ListView<OrderPayment> listViewOrderPayments;


	private ObservableList<Product> dataOrderLine;

	private ObservableList<OrderPayment> dataOrderPayment;
	
	private static Stage primaryStage;
	private static Order order;
	
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

	public void onClickBack()
	{
		OrderManagementController orders = new OrderManagementController();
		try {
			orders.start(new Stage());
			primaryStage.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		getOrderDetails();
		fillItems();
		fillPayment();
	}

	private void getOrderDetails() {
		Packet packet = new Packet();//create packet to send
		packet.addCommand(Command.getOrderInProductsDetails);
		ArrayList<Object> list = new ArrayList<>();
		list.add(order.getoId());
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
					ArrayList<ProductInOrder> productInOrder = p.<ProductInOrder>convertedResultListForCommand(Command.getOrderInProductsDetails);
					ArrayList<Product> products = p.<Product>convertedResultListForCommand(Command.getAllProductsInOrder);
					ArrayList<FlowerInProduct> flowerInProductList = p.<FlowerInProduct>convertedResultListForCommand(Command.getFlowersInProductInOrder);
					ArrayList<Flower> flowerList =p.<Flower>convertedResultListForCommand(Command.getAllFlowersInOrder);
					for(ProductInOrder pInOrder : productInOrder)
					{
						Product prod = products.stream().filter(c->c.getId()==pInOrder.getProductId()).findFirst().orElse(null);
						ArrayList<FlowerInProduct> listFlowersInProd = (ArrayList<FlowerInProduct>) flowerInProductList.stream().filter(c->c.getProductId()==prod.getId()).collect(Collectors.toList());
						for(FlowerInProduct floInProd : listFlowersInProd)
						{
							floInProd.setFlower(flowerList.stream().filter(c->c.getId()==floInProd.getFlowerId()).findFirst().orElse(null));
						}
						prod.setFlowerInProductList(listFlowersInProd);
						cartProducts.put(prod, pInOrder.getQuantity());
					}
					fillItems();
					fillPayment();
					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					lblCreDate.setText(""+formatter.format(order.getCreationDate()));
					formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
					lblReqDate.setText(formatter.format(order.getRequestedDate()));
				}
				else//if it was error in connection
					JOptionPane.showMessageDialog(null,"Connection error","Error",JOptionPane.ERROR_MESSAGE);
			}
		});
		send.start();
	}
	public void fillItems()
	{
		dataOrderLine = FXCollections.observableArrayList(new ArrayList<>(cartProducts.keySet()));
		listViewOrderLines.setCellFactory(new Callback<ListView<Product>, ListCell<Product>>() {
			
			@Override
			public ListCell<Product> call(ListView<Product> param) {
				return new ListCell<Product>() {
					
				private void setCellHandler(Product pro)
				{
					int count = cartProducts.get(pro);
					
					// name of product
					Text proName = new Text();
					if(pro instanceof CatalogProduct)
						proName.setText(((CatalogProduct) pro).getName());
					else
						proName.setText("Custom product");
					proName.setFont(new Font(14));
					proName.setStyle("-fx-font-weight: bold");
					Text txtName = new Text("Product name");
					txtName.setUnderline(true);
					txtName.setFont(new Font(14));
					txtName.setStyle("-fx-font-weight: bold");
					
					VBox productDetails = new VBox(txtName,proName);
					productDetails.setSpacing(5);
					productDetails.setAlignment(Pos.TOP_CENTER);
					
					// price of product
					Text price = new Text(String.format("%.2f�", (pro.getPrice() * count)));
					price.setFont(new Font(14));
					price.setTextAlignment(TextAlignment.CENTER);
					Text txtprice = new Text("Quantity");
					txtprice.setUnderline(true);
					txtprice.setFont(new Font(14));
					txtprice.setStyle("-fx-font-weight: bold");
					VBox productPrice = new VBox(txtprice,price);
					productPrice.setAlignment(Pos.TOP_CENTER);
					productPrice.setSpacing(8);

				   
				
					Text txtQty = new Text("Quantity");
					txtQty.setUnderline(true);
					txtQty.setFont(new Font(14));
					txtQty.setStyle("-fx-font-weight: bold");
					Text qty = new Text("" + cartProducts.get(pro)+" Unit");
					qty.setFont(new Font(13.5));
					qty.setTextAlignment(TextAlignment.CENTER);
					VBox qtyOptions = new VBox(txtQty,qty);
					qtyOptions.setAlignment(Pos.TOP_CENTER);
					qtyOptions.setSpacing(8);
					
					
					Text flowersTitle = new Text("Flowers Collection:");
					flowersTitle.setUnderline(true);
					flowersTitle.setFont(new Font(14));
					flowersTitle.setStyle("-fx-font-weight: bold");
					VBox flowers = new VBox(flowersTitle);
				
					for (FlowerInProduct fp : pro.getFlowerInProductList())
					{
						Flower flowerFound = fp.getFlower();
						if (flowerFound != null)
						{
							Text flower = new Text(String.format("%s, Qty: %d", flowerFound.getName(), fp.getQuantity()));
							flower.setFont(new Font(13.5));
							flowers.getChildren().add(flower);
						}
					}
					
					flowers.setSpacing(2);
				 	HBox hBox = new HBox(productDetails ,flowers, qtyOptions,productPrice);
				 	hBox.setStyle("-fx-border-style: solid inside;"
				 	        + "-fx-border-width: 1;" + "-fx-border-insets: 5;"
				 	        + "-fx-border-radius: 5;");
                    hBox.setSpacing(10);
                    hBox.setPadding(new Insets(10));
                    
                    
                    setGraphic(hBox);
				}

				
			    @Override
				protected void updateItem(Product item, boolean empty) {
					//super.updateItem(item, empty);
						
					 if (item != null) {	
						 	setCellHandler(item);
                        }
				}};
			}
		});
		listViewOrderLines.setItems(dataOrderLine);
	}
	public void fillPayment()
	{
		

		dataOrderPayment = FXCollections.observableArrayList(order.getOrderPaymentList());
		listViewOrderPayments.setCellFactory(new Callback<ListView<OrderPayment>, ListCell<OrderPayment>>() {
			
			@Override
			public ListCell<OrderPayment> call(ListView<OrderPayment> param) {
				return new ListCell<OrderPayment>() {
					
				private void setCellHandler(OrderPayment payment)
				{
					Text method = new Text(payment.getPaymentMethod().toString());
					VBox vMethod = new VBox(method);
					Text amount = new Text(""+payment.getAmount());
					VBox vamount = new VBox(amount);
					Text date;
					if(payment.getPaymentDate()==null)
						date = new Text("End of month");
					else
					{
						SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
						date = new Text(""+formatter.format(payment.getPaymentDate()));
					}
					VBox vDate = new VBox(date);
					vMethod.setMinWidth(100);
					vDate.setMinWidth(100);
					vamount.setMinWidth(100);
					
					method.setFont(new Font(14));
					method.setTextAlignment(TextAlignment.CENTER);
					amount.setFont(new Font(14));
					amount.setTextAlignment(TextAlignment.CENTER);
					date.setFont(new Font(14));
					date.setTextAlignment(TextAlignment.CENTER);
					
				 	HBox hBox = new HBox(vMethod ,vamount, vDate);
				 	hBox.setStyle("-fx-border-style: solid inside;"
				 	        + "-fx-border-width: 1;" + "-fx-border-insets: 5;"
				 	        + "-fx-border-radius: 5;");
                    hBox.setSpacing(10);
                    hBox.setPadding(new Insets(10));
                    
                    
                    setGraphic(hBox);
				}

				
			    @Override
				protected void updateItem(OrderPayment item, boolean empty) {						
					 if (item != null) {	
						 	setCellHandler(item);
                        }
				}};
			}
		});
		listViewOrderPayments.setItems(dataOrderPayment);
	}
	
	private Map<TimeUnit,Long> computeDiff(Timestamp requestedTime, Timestamp currentTime) {
	    long diffInMillies = requestedTime.getTime() - currentTime.getTime();
	    List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
	    Collections.reverse(units);
	    Map<TimeUnit,Long> result = new LinkedHashMap<TimeUnit,Long>();
	    long milliesRest = diffInMillies;
	    for ( TimeUnit unit : units ) {
	        long diff = unit.convert(milliesRest,TimeUnit.MILLISECONDS);
	        long diffInMilliesForUnit = unit.toMillis(diff);
	        milliesRest = milliesRest - diffInMilliesForUnit;
	        result.put(unit,diff);
	    }
	    return result;
	}
	
	private void changeOrderStatus(Order order , Status status)
	{
		order.setStatus(status);
	}
	
	private double getOrderPayments(Order order)
	{
		double payments = 0;
		for(OrderPayment orderPayment : order.getOrderPaymentList())
			payments+=orderPayment.getAmount();
		return payments;
	}
	
	private Refund getCancelRefund(Timestamp requestedTime , Timestamp currentTime)
	{
		Map<TimeUnit,Long> diffTime = computeDiff(requestedTime,currentTime);
		Refund refund = null;
		
		//Check if the cancel request is on the same day of the order's creation time
		if (diffTime.get(TimeUnit.DAYS) == 0) {

			if (diffTime.get(TimeUnit.HOURS) >= 3) // canceled 3 hours or more before the requested time
			{
				// Full refund

				java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime()); // get current date
				refund = new Refund(currentDate, getOrderPayments(order), order.getoId()); // create new full refund
				
			} else if (diffTime.get(TimeUnit.HOURS) >= 1 && diffTime.get(TimeUnit.HOURS) <= 3) {

				// 50% refund

				java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime()); // get current date
				refund = new Refund(currentDate, getOrderPayments(order) - (getOrderPayments(order) * 0.5),order.getoId()); // create new half refund
			}
			else {
				// No refund, just cancel
				
				refund = null;
			}
		}
		//Passed 1 day or more
		else {
			// No refund, just cancel
			
			refund = null;
		}
		return refund;
	}
	
	@FXML
	private void onCancelPressedHandler()
	{
		changeOrderStatus(order,Status.Canceled);
		
		Packet packet = new Packet();
		
		packet.addCommand(Command.updateOrder);
		ArrayList<Object> paramListOrder = new ArrayList<>();
		paramListOrder.add(order);
		packet.setParametersForCommand(Command.updateOrder, paramListOrder);
		
		//Get current time
		java.util.Date today = new java.util.Date();
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(today.getTime());
		
		Refund refund = getCancelRefund(order.getRequestedDate(),sqlTimestamp);
		if(refund != null)
		{
			double currentBalance = CustomerMenuController.currentAcc.getBalance();
			double newBalance = currentBalance + refund.getAmount();
			CustomerMenuController.currentAcc.setBalance(newBalance);
			
			packet.addCommand(Command.updateAccountsBycId);
			packet.addCommand(Command.addOrderRefund);
			ArrayList<Object> paramListAccount = new ArrayList<>();
			ArrayList<Object> paramListRefund = new ArrayList<>();
			paramListAccount.add(CustomerMenuController.currentAcc);
			packet.setParametersForCommand(Command.updateAccountsBycId, paramListAccount);
			paramListRefund.add(refund);
			packet.setParametersForCommand(Command.addOrderRefund, paramListRefund);
		}
		
		SystemSender sender = new SystemSender(packet);
		sender.registerHandler(new IResultHandler() {
			
			@Override
			public void onWaitingForResult() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onReceivingResult(Packet p) {
				// TODO Auto-generated method stub
				if(p.getResultState())
				{
					ConstantData.displayAlert(AlertType.INFORMATION, "Cancel Order", "Success", "Order has been canceled successfully");
				}
				else
				{
					ConstantData.displayAlert(AlertType.ERROR, "Cancel Order", "Error", p.getExceptionMessage());
				}
			}
		});

		sender.start();
	}
	
}
