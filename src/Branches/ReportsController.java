package Branches;

import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale.Category;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.xpath.internal.operations.Or;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister.Pack;

import Customers.Complain;
import Customers.Membership;
import Login.CustomerMenuController;
import Login.LoginController;
import Login.ManagersMenuController;
import PacketSender.Command;
import PacketSender.IResultHandler;
import PacketSender.Packet;
import PacketSender.SystemSender;
import Products.ConstantData;
import Survey.AnswerSurvey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jdk.nashorn.internal.runtime.linker.JavaAdapterFactory;

public class ReportsController implements Initializable{
	
	@FXML private Label lblBranchName;
	@FXML private Label lblBranchNumber;
	@FXML private Label lblEmployeeName;
	@FXML private ComboBox<String> cbReports;
	@FXML private TableView<IncomeReport> table1Income;
	@FXML private TableView<IncomeReport> table2Income;
	@FXML private TableView<OrderReport> tblViewOrder1;
	@FXML private TableView<OrderReport> tblViewOrder2;
	@FXML private TableView<SatisfactionReport> tblViewSatisfaction1;
	@FXML private TableView<SatisfactionReport> tblViewSatisfaction2;
	
	@FXML private ComboBox<String> cbYear;
	@FXML
	private ComboBox<String> cbQuarterly1;
	@FXML
	private ComboBox<String> cbQuarterly2;
	@FXML
	private ComboBox<String> cbBranches;
	@FXML
	private ComboBox<String> cbBranchesName;
	@FXML
	private ComboBox<String> cbBranchTwoName;
	@FXML
	private ComboBox<String> cbBranchTwoNumber;
	@FXML
	private Label lbQuarterlycomp;
	@FXML
	private RadioButton rbcomp;
	@FXML
	private RadioButton rbcompbranch;
	@FXML
	private Button btnGenerateReport;
	@FXML
    private BarChart<String,Integer> barChart;
	@FXML
    private BarChart<String,Integer> barChart1;
	private ArrayList<Branch> branchlist;
	private ArrayList<Complain> complainList1,complainList2;
	private ArrayList<IncomeReport> incomeReport1,incomeReport2;
	private ArrayList<OrderReport> orderReport1,orderReport2;
	//choice :saving manager choice of different branches/quarterlies
	private int choice=0;
	private Employee employee=(Employee)LoginController.userLogged;
	
	/**
	 * 
	 * @param useremployee the manager / branch manager
	 */
	/*public ReportsController(Employee useremployee)
	{
		super();
		this.employee=useremployee;
	}
	*/
	public void BuildPacketForReport(Packet packet,int year,int quartely,int brId,Command cmd)
	{
		packet.addCommand(cmd);
		ArrayList<Object> info1 = new ArrayList<>();
		info1.add(brId);
		info1.add(year);
		info1.add(quartely);
		//building the second packet to get the first information
		packet.setParametersForCommand(cmd, info1);
	}
	@SuppressWarnings("unchecked")
	public void BuildTableViewForIncome(TableView<IncomeReport> table ,IncomeReport incomeReport)
	{
		table.getColumns().clear();
		ObservableList<IncomeReport> data = FXCollections.observableArrayList(incomeReport);
		table.setItems(data);
		//adding the data to the columns
		TableColumn<IncomeReport, String> branchId=new TableColumn<>("Branch Number");
		branchId.setCellValueFactory(new PropertyValueFactory<IncomeReport, String>("brId"));
		TableColumn<IncomeReport, String> branchName=new TableColumn<>("Branch Name");
		branchName.setCellValueFactory(new PropertyValueFactory<IncomeReport, String>("brName"));
		TableColumn<IncomeReport, String> branchIncome=new TableColumn<>("Income");
		branchIncome.setCellValueFactory(new PropertyValueFactory<IncomeReport, String>("amount"));

		table.getColumns().addAll(branchId,branchName,branchIncome);
		table.setVisible(true);
	}
	@SuppressWarnings("unchecked")
	public void BuildTableViewForSatisfaction(TableView<SatisfactionReport> table ,ArrayList<SatisfactionReport> surveyReport)
	{
		table.getColumns().clear();
		ObservableList<SatisfactionReport> data = FXCollections.observableArrayList(surveyReport);
		table.setItems(data);
		//adding the data to the columns
		TableColumn<SatisfactionReport, String> Question=new TableColumn<>("Question");
		Question.setCellValueFactory(new PropertyValueFactory<SatisfactionReport, String>("question"));
		TableColumn<SatisfactionReport, String> avg=new TableColumn<>("Avg Answer");
		avg.setCellValueFactory(new PropertyValueFactory<SatisfactionReport, String>("avg"));


		table.getColumns().addAll(Question,avg);
		table.setVisible(true);
	}
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void BuildTableViewForOrder(TableView<OrderReport> table ,ArrayList<OrderReport> orderReportList)
	{
		int i,j=-1;
		for(i=1;i<orderReportList.size();i++)
		{
			if(j==-1) {
				if(orderReportList.get(i).getProductCategory().equals(orderReportList.get(i-1).getProductCategory()))
				{
					j=i-1;
					orderReportList.get(i).setProductCategory("");
				}
			}
			else 
				if(orderReportList.get(i).getProductCategory().equals(orderReportList.get(j).getProductCategory()))
					orderReportList.get(i).setProductCategory("");
				else j=-1;
							
		}
		table.getColumns().clear();
		table.setEditable(false);
		
		ObservableList<OrderReport> data = FXCollections.observableArrayList(orderReportList);
		table.setItems(data);
		//adding the data to the columns
		TableColumn<OrderReport, String> productCategory=new TableColumn<>("Product Category");
		productCategory.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("productCategory"));
		productCategory.setSortable(false);
		productCategory.impl_setReorderable(false);
		TableColumn<OrderReport, String> orderId=new TableColumn<>("Order Number");
		orderId.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("orderId"));
		orderId.setSortable(false);
		orderId.impl_setReorderable(false);
		TableColumn<OrderReport, String> creationDate=new TableColumn<>("Creation Date");
		creationDate.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("creationDate"));
		creationDate.setSortable(false);
		creationDate.impl_setReorderable(false);
		
		TableColumn<OrderReport, String> status=new TableColumn<>("Status");
		status.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("status"));
		status.setSortable(false);
		status.impl_setReorderable(false);
		
		TableColumn<OrderReport, String> productId=new TableColumn<>("Product Number");
		productId.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("productId"));
		productId.setSortable(false);
		productId.impl_setReorderable(false);
		TableColumn<OrderReport, String> productName=new TableColumn<>("Product Name");
		productName.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("productName"));
		productName.setSortable(false);
		productName.impl_setReorderable(false);
		TableColumn<OrderReport, String> price=new TableColumn<>("Price");
		price.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("price"));
		price.setSortable(false);
		price.impl_setReorderable(false);
		TableColumn<OrderReport, String> paymentMethod=new TableColumn<>("Payment Method");
		paymentMethod.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("paymentMethod"));
		paymentMethod.setSortable(false);
		paymentMethod.impl_setReorderable(false);
		TableColumn<OrderReport, String> deliveryNumber=new TableColumn<>("Delivery Number");
		deliveryNumber.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("deliveryNumber"));
		deliveryNumber.setSortable(false);
		deliveryNumber.impl_setReorderable(false);
		TableColumn<OrderReport, String> address=new TableColumn<>("Address");
		address.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("address"));
		address.setSortable(false);
		address.impl_setReorderable(false);
		TableColumn<OrderReport, String> phone=new TableColumn<>("Phone");
		phone.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("phone"));
		phone.setSortable(false);
		phone.impl_setReorderable(false);
		TableColumn<OrderReport, String> receiver=new TableColumn<>("Receiver");
		receiver.setCellValueFactory(new PropertyValueFactory<OrderReport, String>("receiver"));
		receiver.setSortable(false);
		receiver.impl_setReorderable(false);
		
		for(TableColumn<OrderReport, ?> col :table.getColumns())
		{
			col.setSortable(false);
		}
		table.getColumns().addAll(productCategory,orderId,creationDate,status,productId,productName,price,paymentMethod,deliveryNumber,address,phone,receiver);
		table.setVisible(true);
	}
	public void BuildBarChartForComplain(BarChart<String,Integer> barch, int active ,int notactive)
	{
		barch.setVisible(true);
		barch.getData().clear();
		barch.setTitle("Complains Status 1");
		XYChart.Series<String, Integer> ser1=new XYChart.Series<>();
		XYChart.Series<String, Integer> ser2=new XYChart.Series<>();
		ser1.getData().add(new XYChart.Data<>("",active));
		ser1.setName("Active");
		ser2.getData().add(new XYChart.Data<>("",notactive));
		ser2.setName("Not Active");
		barch.getData().add(ser1);
		barch.getData().add(ser2);
	}
	/**
	 * 
	 * @param primaryStage starts the screen
	 */
	public void start(Stage primaryStage) {
		
		String title = "Report";
		String srcFXML = "/Branches/ReportsUI.fxml";
		
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(srcFXML));
			loader.setController(this);
			Parent root = loader.load();
			Scene scene = new Scene(root);
			primaryStage.setTitle(title);
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			e.printStackTrace();
		}
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				  primaryStage.close();
				  
				  try {
					  ManagersMenuController menumanager=new ManagersMenuController();
					  menumanager.start(new Stage());
					} catch (Exception e) {
						ConstantData.displayAlert(AlertType.ERROR, "Error", "Exception when trying to open Menu Window", e.getMessage());
					}
				
		
			
				
			}
		});
	}
	/**
	 * Generates Reports for Branches Manager 
	 */
	public void generateReportForBranchesManager(String report,int year,int quartely)
	{
		int brId;
		brId=Integer.parseInt(cbBranches.getSelectionModel().getSelectedItem());
		
		//getting what the branches manager want to do 
		//choice 0 is only 1 branch and only one quartiles
		//if he wants to compare with 2 quarterlies
		choice=0;
		if(rbcomp.isSelected())
			choice=1;
		//if her wants to compare with 2 branches
		if(rbcompbranch.isSelected())
			choice=2;
		//if he wants to compare with 2 branches and 2 quarterlies
		if(rbcomp.isSelected()&&rbcompbranch.isSelected())
			choice=3;
		//handling Complains Report
		if(report.equals("Complains"))
		{
			
			Packet packet = new Packet();
			Packet packet2 = new Packet();
			//building the query by the  choice ,and adding the relative command
			switch (choice)
			{
			case 0:
				//regular report , without compare , sending also the branch id 
				generateReportForBranchManager(brId,report,year,quartely);
				return;
	
			case 1:
				//compare with  same branches with 2 different quarterly
				int quartely2=Integer.parseInt(cbQuarterly2.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getComplainsForReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, quartely2, brId,Command.getComplainsForReport);

				break;
			case 2:
				//compare with  same quarterly with 2 different branches
				int secondBranchNumber=Integer.parseInt(cbBranchTwoNumber.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getComplainsForReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, quartely, secondBranchNumber,Command.getComplainsForReport);
				break;
			case 3:
				//compare with  different quarterly with 2 different branches
				int secondBranchNumber2=Integer.parseInt(cbBranchTwoNumber.getSelectionModel().getSelectedItem());
				int secondquartely=Integer.parseInt(cbQuarterly2.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getComplainsForReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, secondquartely, secondBranchNumber2,Command.getComplainsForReport);
				
				break;

			}
			//sending the packet
			SystemSender send = new SystemSender(packet);
			send.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onReceivingResult(Packet p) {
					// checking the result
					if(p.getResultState()) 
					{
						int active1=0,notactive1=0;
						//filling the information in list
						complainList1= p.<Complain>convertedResultListForCommand(Command.getComplainsForReport);
						//gathering information for the graphs
						for(Complain comp : complainList1)
						{
							//if the complain is active
							if(comp.isActive())
								active1++;
							else//else its not active
								notactive1++;
						}	
						//filling and initialize the barchart
						BuildBarChartForComplain(barChart, active1, notactive1);
						/*
						barChart.setVisible(true);
						barChart.getData().clear();
						barChart.setTitle("Complains Status 1");
						XYChart.Series<String, Integer> ser1=new XYChart.Series<>();
						XYChart.Series<String, Integer> ser2=new XYChart.Series<>();
						ser1.getData().add(new XYChart.Data<>("",active1));
						ser1.setName("Active");
						ser2.getData().add(new XYChart.Data<>("",notactive1));
						ser2.setName("Not Active");
						barChart.getData().add(ser1);
						barChart.getData().add(ser2);*/
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());	
				
				}
			});
			send.start();
			//sending the second packet
			SystemSender send2 = new SystemSender(packet2);
			send2.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onReceivingResult(Packet p) {
					// TODO Auto-generated method stub
					if(p.getResultState()) 
					{
						int active2=0,notactive2=0;
						//filling the information in the list
						complainList2= p.<Complain>convertedResultListForCommand(Command.getComplainsForReport);
						//gathering the information
						for(Complain comp : complainList2)
						{
							//checking if the complain are active
							if(comp.isActive())
								active2++;
							else
								notactive2++;
						}	
						//filling and initialize the barchart
						BuildBarChartForComplain(barChart1, active2, notactive2);
						/*
						barChart1.setVisible(true);
						barChart1.getData().clear();
						barChart1.setTitle("Complains Status 2");
						XYChart.Series<String, Integer> ser3=new XYChart.Series<>();
						XYChart.Series<String, Integer> ser4=new XYChart.Series<>();
						ser3.getData().add(new XYChart.Data<>("",active2));
						ser3.setName("Active");
						ser4.getData().add(new XYChart.Data<>("",notactive2));
						ser4.setName("Not Active");
						barChart1.getData().add(ser3);
						barChart1.getData().add(ser4);*/
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());	
				}
			});
			send2.start();
			
		}
		else
			if(report.equals("Income"))
			{
				Packet packet = new Packet();
				Packet packet2 = new Packet();
				//building the query by the  choice ,and adding the relative command
				switch (choice)
				{
				case 0:
					//regular report , without compare , sending also the branch id 
					generateReportForBranchManager(brId,report,year,quartely);
					return;
		
				case 1:
					//compare with  same branches with 2 different quarterly
					int quartely2=Integer.parseInt(cbQuarterly2.getSelectionModel().getSelectedItem());
					//building the first packet to get the first information
					BuildPacketForReport(packet, year, quartely, brId,Command.getIncomeReport);
					//building the second packet to get the second information
					BuildPacketForReport(packet2, year, quartely2, brId,Command.getIncomeReport);

					break;
				case 2:
					//compare with  same quarterly with 2 different branches
					int secondBranchNumber=Integer.parseInt(cbBranchTwoNumber.getSelectionModel().getSelectedItem());
					//building the first packet to get the first information
					BuildPacketForReport(packet, year, quartely, brId,Command.getIncomeReport);
					//building the second packet to get the second information
					BuildPacketForReport(packet2, year, quartely, secondBranchNumber,Command.getIncomeReport);
					break;
				case 3:
					//compare with  different quarterly with 2 different branches
					int secondBranchNumber2=Integer.parseInt(cbBranchTwoNumber.getSelectionModel().getSelectedItem());
					int secondquartely=Integer.parseInt(cbQuarterly2.getSelectionModel().getSelectedItem());
					//building the first packet to get the first information
					BuildPacketForReport(packet, year, quartely, brId,Command.getIncomeReport);
					//building the second packet to get the second information
					BuildPacketForReport(packet2, year, secondquartely, secondBranchNumber2,Command.getIncomeReport);
					break;

				}
				//sending the packet
				SystemSender send = new SystemSender(packet);
				send.registerHandler(new IResultHandler() {
					
					@Override
					public void onWaitingForResult() {
						// TODO Auto-generated method stub		
					}		
					@Override
					public void onReceivingResult(Packet p) {
						// checking the result
						if(p.getResultState()) 
						{
							
							//filling the information in list
							incomeReport1= p.<IncomeReport>convertedResultListForCommand(Command.getIncomeReport);
							if(incomeReport1.isEmpty()==true)
							{
								showError("Error,Please Try Again Later");
								return;
							}
							//building the incoming result 
							IncomeReport newincomereport=new IncomeReport(incomeReport1.get(0).getBrId(), incomeReport1.get(0).getBrName(), incomeReport1.get(0).getAmount());
							//sending the wanted table and the result to function that builds the tableview
							BuildTableViewForIncome(table1Income, newincomereport);
						}
						else
							System.out.println("Fail: " + p.getExceptionMessage());		
					}
				});
				send.start();
				//sending the second packet
				SystemSender send2 = new SystemSender(packet2);
				send2.registerHandler(new IResultHandler() {
					
					@Override
					public void onWaitingForResult() {
						// TODO Auto-generated method stub		
					}		
					@Override
					public void onReceivingResult(Packet p) {
						// TODO Auto-generated method stub
						if(p.getResultState()) 
						{
							//filling the information in the list
							incomeReport2= p.<IncomeReport>convertedResultListForCommand(Command.getIncomeReport);
							if(incomeReport2.isEmpty()==true)
							{
								showError("Error,Please Try Again Later");
								return;
							}
							//building the incoming result 
							IncomeReport newincomereport=new IncomeReport(incomeReport2.get(0).getBrId(), incomeReport2.get(0).getBrName(), incomeReport2.get(0).getAmount());
							//sending the wanted table and the result to function that builds the tableview
							BuildTableViewForIncome(table2Income, newincomereport);
						}
						else
							System.out.println("Fail: " + p.getExceptionMessage());	
					}
				});
				send2.start();
			}
		if(report.equals("Orders"))
		{
			Packet packet = new Packet();
			Packet packet2 = new Packet();
			//building the query by the  choice ,and adding the relative command
			switch (choice)
			{
			case 0:
				//regular report , without compare , sending also the branch id 
				generateReportForBranchManager(brId,report,year,quartely);
				return;
	
			case 1:
				//compare with  same branches with 2 different quarterly
				int quartely2=Integer.parseInt(cbQuarterly2.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getOrderReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, quartely2, brId,Command.getOrderReport);

				break;
			case 2:
				//compare with  same quarterly with 2 different branches
				int secondBranchNumber=Integer.parseInt(cbBranchTwoNumber.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getOrderReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, quartely, secondBranchNumber,Command.getOrderReport);
				break;
			case 3:
				//compare with  different quarterly with 2 different branches
				int secondBranchNumber2=Integer.parseInt(cbBranchTwoNumber.getSelectionModel().getSelectedItem());
				int secondquartely=Integer.parseInt(cbQuarterly2.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getOrderReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, secondquartely, secondBranchNumber2,Command.getOrderReport);
				break;

			}
			//sending the packet
			SystemSender send = new SystemSender(packet);
			send.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub		
				}		
				@Override
				public void onReceivingResult(Packet p) {
					// checking the result
					if(p.getResultState()) 
					{
						
						//filling the information in list
						orderReport1= p.<OrderReport>convertedResultListForCommand(Command.getOrderReport);
						if(orderReport1.isEmpty()==true)
						{
							showError("Error,Please Try Again Later");
							return;
						}
						//building the incoming result 
						BuildTableViewForOrder(tblViewOrder1, orderReport1);

						
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());		
				}
			});
			send.start();
			//sending the second packet
			SystemSender send2 = new SystemSender(packet2);
			send2.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub		
				}		
				@Override
				public void onReceivingResult(Packet p) {
					// TODO Auto-generated method stub
					if(p.getResultState()) 
					{
						//filling the information in list
						orderReport2= p.<OrderReport>convertedResultListForCommand(Command.getOrderReport);
						if(orderReport2.isEmpty()==true)
						{
							showError("Error,Please Try Again Later");
							return;
						}
						//building the incoming result 
						BuildTableViewForOrder(tblViewOrder2, orderReport2);

					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());	
				}
			});
			send2.start();
		}
		if(report.equals("Satisfaction"))
		{
			Packet packet = new Packet();
			Packet packet2 = new Packet();
			//building the query by the  choice ,and adding the relative command
			switch (choice)
			{
			case 0:
				//regular report , without compare , sending also the branch id 
				generateReportForBranchManager(brId,report,year,quartely);
				return;
	
			case 1:
				//compare with  same branches with 2 different quarterly
				int quartely2=Integer.parseInt(cbQuarterly2.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getSatisfactionReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, quartely2, brId,Command.getSatisfactionReport);

				break;
			case 2:
				//compare with  same quarterly with 2 different branches
				int secondBranchNumber=Integer.parseInt(cbBranchTwoNumber.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getSatisfactionReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, quartely, secondBranchNumber,Command.getSatisfactionReport);
				break;
			case 3:
				//compare with  different quarterly with 2 different branches
				int secondBranchNumber2=Integer.parseInt(cbBranchTwoNumber.getSelectionModel().getSelectedItem());
				int secondquartely=Integer.parseInt(cbQuarterly2.getSelectionModel().getSelectedItem());
				//building the first packet to get the first information
				BuildPacketForReport(packet, year, quartely, brId,Command.getSatisfactionReport);
				//building the second packet to get the second information
				BuildPacketForReport(packet2, year, secondquartely, secondBranchNumber2,Command.getSatisfactionReport);
				break;

			}
			//sending the packet
			SystemSender send = new SystemSender(packet);
			send.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub		
				}		
				@Override
				public void onReceivingResult(Packet p) {
					// checking the result
					if(p.getResultState()) 
					{
						
						//getting the information from the returned packet
						ArrayList<SatisfactionReport> surveyReport ;
						
						surveyReport= p.<SatisfactionReport>convertedResultListForCommand(Command.getSatisfactionReport);
						//checking the list
						if(surveyReport.isEmpty()==true)
						{
							showError("Error,There Is No Data For This Selection");
							return;
						}
						
						//sending the wanted table and the result to function that builds the tableview
						//BuildTableViewForSatisfaction(tblViewSatisfaction1,surveyReport);
						BuildTableViewForSatisfaction(tblViewSatisfaction1, surveyReport);
						
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());		
				}
			});
			send.start();
			//sending the second packet
			SystemSender send2 = new SystemSender(packet2);
			send2.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub		
				}		
				@Override
				public void onReceivingResult(Packet p) {
					// TODO Auto-generated method stub
					if(p.getResultState()) 
					{
						//getting the information from the returned packet
						ArrayList<SatisfactionReport> surveyReport ;
						
						surveyReport= p.<SatisfactionReport>convertedResultListForCommand(Command.getSatisfactionReport);
						//checking the list
						if(surveyReport.isEmpty()==true)
						{
							showError("Error,There Is No Data For This Selection");
							return;
						}
						
						//sending the wanted table and the result to function that builds the tableview
						//BuildTableViewForSatisfaction(tblViewSatisfaction1,surveyReport);
						BuildTableViewForSatisfaction(tblViewSatisfaction2, surveyReport);
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());	
				}
			});
			send2.start();
		}
	
		
	
	}
	/**
	 * Generates Reports for Branch Manager
	 */
	@SuppressWarnings("deprecation")
	public void generateReportForBranchManager(int brId,String report,int year,int quartely)
	{

		if(report.equals("Complains"))
		{
			//setting barchart1
			barChart1.setVisible(false);
			barChart1.getData().clear();
			//handling Complains Report
			//opening packet
			Packet packet = new Packet();
			//adding command to packet
			packet.addCommand(Command.getComplainsForReport);
			//adding parameters for the command
			ArrayList<Object> info = new ArrayList<>();
			info.add(brId);
			info.add(year);
			info.add(quartely);
			packet.setParametersForCommand(Command.getComplainsForReport, info);
			//sending the packet
			SystemSender send = new SystemSender(packet);
			send.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onReceivingResult(Packet p) {
					// TODO Auto-generated method stub
					if(p.getResultState())
					{
						//getting the information from the returned packet
						ArrayList<Complain> complainList ;
						int active=0,notactive=0;
						complainList= p.<Complain>convertedResultListForCommand(Command.getComplainsForReport);
						//checking the list
						if(complainList.isEmpty()==true)
						{
							showError("Error,There Is No Data For This Selection");
							return;
						}
						//gathering information for the chart
						for(Complain comp : complainList)
						{
							//if the complain is active
							if(comp.isActive())
								active++;
							else
								notactive++;
						}	
						//filling and initialize the barchart
						BuildBarChartForComplain(barChart, active, notactive);
						/*
						barChart.setVisible(true);
						barChart.setTitle("Complains Status");
						XYChart.Series<String, Integer> ser1=new XYChart.Series<>();
						XYChart.Series<String, Integer> ser2=new XYChart.Series<>();
						ser1.getData().add(new XYChart.Data<>("",active));
						ser1.setName("Active");
						ser2.getData().add(new XYChart.Data<>("",notactive));
						ser2.setName("Not Active");
						barChart.getData().add(ser1);
						barChart.getData().add(ser2);*/
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());	
					
				}
			});
			send.start();	
		}
		if(report.equals("Income"))
		{
			//opening packet
			Packet packet = new Packet();
			//adding command to packet
			packet.addCommand(Command.getIncomeReport);
			//adding parameters for the command
			ArrayList<Object> info = new ArrayList<>();
			info.add(brId);
			info.add(year);
			info.add(quartely);
			packet.setParametersForCommand(Command.getIncomeReport, info);
			//sending the packet
			SystemSender send = new SystemSender(packet);
			send.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onReceivingResult(Packet p) {
					// TODO Auto-generated method stub
					if(p.getResultState())
					{
						//getting the information from the returned packet
						ArrayList<IncomeReport> IncomeList ;
						
						IncomeList= p.<IncomeReport>convertedResultListForCommand(Command.getIncomeReport);
						//checking the list
						if(IncomeList.isEmpty()==true)
						{
							showError("Error,There Is No Data For This Selection");
							return;
						}
						//building the incoming result 
						IncomeReport newincomereport=new IncomeReport(IncomeList.get(0).getBrId(), IncomeList.get(0).getBrName(), IncomeList.get(0).getAmount());
						//sending the wanted table and the result to function that builds the tableview
						BuildTableViewForIncome(table1Income, newincomereport);
						
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());	
				}
			});
			send.start();	

		}
		if(report.equals("Orders"))
		{
			//opening packet
			Packet packet = new Packet();
			//adding command to packet
			packet.addCommand(Command.getOrderReport);
			//adding parameters for the command
			ArrayList<Object> info = new ArrayList<>();
			info.add(brId);
			info.add(year);
			info.add(quartely);
			packet.setParametersForCommand(Command.getOrderReport, info);
			//sending the packet
			SystemSender send = new SystemSender(packet);
			send.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onReceivingResult(Packet p) {
					// TODO Auto-generated method stub
					if(p.getResultState())
					{
						//getting the information from the returned packet
						ArrayList<OrderReport> IncomeList ;
						
						IncomeList= p.<OrderReport>convertedResultListForCommand(Command.getOrderReport);
						//checking the list
						if(IncomeList.isEmpty()==true)
						{
							showError("Error,There Is No Data For This Selection");
							return;
						}
						
						//sending the wanted table and the result to function that builds the tableview
						BuildTableViewForOrder(tblViewOrder1, IncomeList);
						
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());	
				}
			});
			send.start();	
		}
		if(report.equals("Satisfaction"))
		{
			//opening packet
			Packet packet = new Packet();
			//adding command to packet
			packet.addCommand(Command.getSatisfactionReport);
			//adding parameters for the command
			ArrayList<Object> info = new ArrayList<>();
			info.add(brId);
			info.add(year);
			info.add(quartely);
			packet.setParametersForCommand(Command.getSatisfactionReport, info);
			//sending the packet
			SystemSender send = new SystemSender(packet);
			send.registerHandler(new IResultHandler() {
				
				@Override
				public void onWaitingForResult() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onReceivingResult(Packet p) {
					// TODO Auto-generated method stub
					if(p.getResultState())
					{
						//getting the information from the returned packet
						ArrayList<SatisfactionReport> surveyReport ;
						
						surveyReport= p.<SatisfactionReport>convertedResultListForCommand(Command.getSatisfactionReport);
						//checking the list
						if(surveyReport.isEmpty()==true)
						{
							showError("Error,There Is No Data For This Selection");
							return;
						}
						
						//sending the wanted table and the result to function that builds the tableview
						//BuildTableViewForSatisfaction(tblViewSatisfaction1,surveyReport);
						BuildTableViewForSatisfaction(tblViewSatisfaction1, surveyReport);
					
					}
					else
						System.out.println("Fail: " + p.getExceptionMessage());	
				}
			});
			send.start();	
		}
	
		
	}
	/**
	 * This function starts when the user clicks on generate report button , 
	 * it handles the reports .
	 */
	public void generateReport()
	{
		String report;
		int year,quartely1;
		//gathering information from the screen
		report=cbReports.getSelectionModel().getSelectedItem();
		year=Integer.parseInt(cbYear.getSelectionModel().getSelectedItem());
		quartely1=Integer.parseInt(cbQuarterly1.getSelectionModel().getSelectedItem());
		//clearing the screen
		barChart.getData().clear();
		barChart.setVisible(false);
		barChart1.getData().clear();
		barChart1.setVisible(false);
		table1Income.getColumns().clear();
		table2Income.getColumns().clear();
		tblViewOrder1.getColumns().clear();
		tblViewOrder2.getColumns().clear();
		tblViewSatisfaction1.getColumns().clear();
		tblViewSatisfaction2.getColumns().clear();
		table1Income.getItems().clear();
		table2Income.getItems().clear();
		table1Income.setVisible(false);
		table2Income.setVisible(false);
		tblViewOrder1.setVisible(false);
		tblViewOrder2.setVisible(false);
		tblViewSatisfaction1.setVisible(false);
		tblViewSatisfaction2.setVisible(false);
		//checking which user is using this screen and by its type , function will be called depended on his type 
		if(employee.getRole().toString().equals((Role.BranchesManager).toString()))
			generateReportForBranchesManager(report,year,quartely1);
		else
			generateReportForBranchManager(employee.getBranchId(),report,year,quartely1);	

	}

	/**
	 * This function show error message 
	 * @param str error message
	 */
	public void showError(String str)
	{
		JOptionPane.showMessageDialog(null, 
				str, 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
	}
	/**
	 * handling first branch  number combo box
	 */
	public void handleBranchNumber()
	{
		cbBranchesName.getSelectionModel().select(cbBranches.getSelectionModel().getSelectedIndex());
	}
	/**
	 * handling second branch  number combo box
	 */
	public void handleBranchTwoNumber()
	{
		cbBranchTwoName.getSelectionModel().select(cbBranchTwoNumber.getSelectionModel().getSelectedIndex());

	}
	/**
	 * handling first branch  name combo box
	 */
	public void handleBranchTwoName()
	{
		cbBranchTwoNumber.getSelectionModel().select(cbBranchTwoName.getSelectionModel().getSelectedIndex());
	}
	/**
	 * handling second branch  name combo box
	 */
	public void handleBranchName()
	{
		cbBranches.getSelectionModel().select(cbBranchesName.getSelectionModel().getSelectedIndex());
	}
	/**
	 * handling radio button for branch comparison 
	 */
	public void handleBranchTwoComp()
	{
		//if the radio button was selected we show specific combo box 
		if(rbcompbranch.isSelected())
		{
			cbBranchTwoName.setVisible(true);
			cbBranchTwoNumber.setVisible(true);
		}
		else
		{
			cbBranchTwoName.setVisible(false);
			cbBranchTwoNumber.setVisible(false);
		}
	}
	/**
	 * handling radio button for quarterlies comparison 
	 */
	public void handleRadioButton()
	{
		//if the radio button was selected we show specific combo box , we show quarterlies comparison fields
		if(rbcomp.isSelected())
		{
			lbQuarterlycomp.setVisible(true);
			cbQuarterly2.setVisible(true);
		}
		else
		{
			lbQuarterlycomp.setVisible(false);
			cbQuarterly2.setVisible(false);
		}
	}
	/**
	 * initialize the combo boxes
	 */
	private void initComboBox()
	{
		ObservableList<String> observelistReport,observelistYear,observelistQuarterly,observelistBranch;
		//adding reports to combo box
		ArrayList<String> report = new ArrayList<>();
		report.add("Orders");
		report.add("Complains");
		report.add("Income");
		report.add("Satisfaction");
		observelistReport = FXCollections.observableArrayList(report);
		cbReports.setItems(observelistReport);
		cbReports.getSelectionModel().selectFirst();

		//adding Years to combobox
		ArrayList<String> yearlist = new ArrayList<>();
		for(int i=Calendar.getInstance().get(Calendar.YEAR);i>=2015;i--)
		{
			yearlist.add(i+"");
		}
		observelistYear = FXCollections.observableArrayList(yearlist);
		cbYear.setItems(observelistYear);
		cbYear.getSelectionModel().selectFirst();

		//adding Quarterlies
		ArrayList<String> Quarterlieslist = new ArrayList<>();
		Quarterlieslist.add(1+"");
		Quarterlieslist.add(2+"");
		Quarterlieslist.add(3+"");
		Quarterlieslist.add(4+"");
		observelistQuarterly = FXCollections.observableArrayList(Quarterlieslist);
		cbQuarterly1.setItems(observelistQuarterly);
		cbQuarterly2.setItems(observelistQuarterly);
		cbQuarterly1.getSelectionModel().selectFirst();
		cbQuarterly2.getSelectionModel().selectFirst();
		ArrayList<String> Branchnumber = new ArrayList<>();
		ArrayList<String> BranchName = new ArrayList<>();
		//if the employee is branches manager we add combo box for him that it give him option to pick branch
		if(employee.getRole().toString().equals((Role.BranchesManager).toString()))
		{
			//gathering information in the comb box
			for(Branch br:branchlist)
			{
				Branchnumber.add(br.getbId()+"");
				BranchName.add(br.getName());
			}
			//filling the information in the combobox
			observelistBranch = FXCollections.observableArrayList(Branchnumber);
			cbBranches.setItems(observelistBranch);
			cbBranches.getSelectionModel().selectFirst();
			cbBranchTwoNumber.setItems(observelistBranch);
			cbBranchTwoNumber.getSelectionModel().select(1);
			observelistBranch = FXCollections.observableArrayList(BranchName);
			cbBranchesName.setItems(observelistBranch);
			cbBranchesName.getSelectionModel().selectFirst();
			cbBranchTwoName.setItems(observelistBranch);
			cbBranchTwoName.getSelectionModel().select(1);		
		}
	}
	

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// hiding the not wanted fields and options
		table1Income.setVisible(false);
		table2Income.setVisible(false);
		rbcomp.setVisible(false);
		cbQuarterly2.setVisible(false);
		lbQuarterlycomp.setVisible(false);
		cbBranches.setVisible(false);
		cbBranchesName.setVisible(false);
		cbBranchTwoName.setVisible(false);
		cbBranchTwoNumber.setVisible(false);
		rbcompbranch.setVisible(false);
		barChart.setVisible(false);
		barChart1.setVisible(false);
		tblViewOrder1.setVisible(false);
		tblViewOrder2.setVisible(false);
		tblViewSatisfaction1.setVisible(false);
		tblViewSatisfaction2.setVisible(false);
		//building new packet
		Packet packet = new Packet();
		//adding command
		packet.addCommand(Command.getBranches);
		//sending the packet
		SystemSender send = new SystemSender(packet);
		send.registerHandler(new IResultHandler() {
			
			@Override
			public void onWaitingForResult() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onReceivingResult(Packet p) {
				// filling the returned information from the server
				 branchlist = p.<Branch>convertedResultListForCommand(Command.getBranches);
				 if (p.getResultState()) 
				 {
					 //initialize the combo boxes
					initComboBox(); 
					//checking the list
					 if(branchlist.isEmpty()==true)
					 {
						 showError("Error , Please Try Again");
							return;
					 }
					 //checking employee type
					 //if its branch manager , we show his fields and options
					 if(employee.getRole().toString().equals((Role.BranchManager).toString()))
					 {
						 //showing branch number
						 lblBranchNumber.setVisible(true);
						 //finding the branch name and show in the screen
						 for(Branch br:branchlist)
							 if(br.getbId()==employee.getBranchId())
							 {
								lblBranchName.setText("Branch Name : "+br.getName().toString());
								lblBranchNumber.setText("Branch Number : "+br.getbId());
							 }		 
					 }
					 else		 
					 {
						//its the branches manager , we show his fields and options
						lblBranchNumber.setVisible(true);
						cbBranches.setVisible(true);
						cbBranchesName.setVisible(true);
						rbcomp.setVisible(true);
						String name=employee.getUser().toString();
						lblEmployeeName.setText("Manager : "+name);
						rbcompbranch.setVisible(true);			
					 }
					 //showing manager user 
					 String name=employee.getUser().toString();
					 lblEmployeeName.setText("Manager : "+name);

				}
				else//showing the error
					System.out.println("Fail: " + p.getExceptionMessage());				 
			}
		});
		send.start();
	}
}
