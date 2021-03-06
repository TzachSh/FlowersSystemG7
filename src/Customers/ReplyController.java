package Customers;

import java.util.ArrayList;
import java.util.Optional;

import Branches.Branch;
import Commons.Refund;
import PacketSender.Command;
import PacketSender.IResultHandler;
import PacketSender.Packet;
import PacketSender.SystemSender;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;


/**
 * Controller
 * Handling complain reply, and giving a refund to customer
 *
 */
public class ReplyController {
	@FXML
	private Button btnSend;
	@FXML
	private Button btnCancel;
	@FXML
	private TextField txtTitle;
	@FXML
	private TextArea txtDesc;
	@FXML
	private TextArea txtReply;
	@FXML
	private TextField txtRefund;
	@FXML 
	private ComboBox<Branch> cmbBranch;
	private static Stage stage;
	/***
	 * Determine if choose to refund
	 */
	boolean isRefund;
	
	/**
	 * contains all branches
	 */
	private ArrayList<Branch> branchList;
	/***
	 * List to be updated during runtime
	 */
	private ObservableList<Branch> data;
	
	/***
	 * Complain to show its details
	 */
	private Complain complain;
	/***
	 * List to save the accounts for the specific customer
	 */
	private ArrayList<Account> customerAccList;
	
	/**
	 * Passing the complain to show which is passed from the ComplainsController 
	 * @param complain - get the relevant complain to show
	 */
	public void setComplain(Complain complain)
	{
		this.complain = complain;
	}
	
	/**
	 * Handle cancel button pressed, back to the Complains view
	 * @param event - event to be handled
	 */
	@FXML
	private void handleCancelPressed(Event event)
	{
		((Node) event.getSource()).getScene().getWindow().hide();
		ComplainsController complainsController = new ComplainsController();
		complainsController.start(new Stage());
	}
	
	/**
	 * Getting a customer account by branch id
	 * @param bId - branch id to get the account of
	 * @return relevant account by branch id, if exists
	 */
	private Account getCustomerAccountInBranch(int bId)
	{
		Account retAcc = null;
		for(Account account : customerAccList)
			if(account.getBranchId() == bId)
				retAcc = account;
		
		return retAcc;		
	}
	
	/**
	 * Handle send button press, getting the texts from the text fields, parse them in to Reply object 
	 * and a Refund object if has been choose to give a refund, and save them to the DB 
	 * @param event - even to be handled
	 */
	@FXML 
	private void handleSendPressed(Event event)
	{
		String replyment = txtReply.getText().replaceAll("'", "\\'");
		double amount = 0;

		if (replyment.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR,"Must fill a replyment");
			alert.show();
			return;
		}
		
		try {
			amount = Double.parseDouble(txtRefund.getText());
			if(!txtRefund.getText().matches("[0-9]*\\.?[0-9]?[0-9]?"))
			{
				Alert alert = new Alert(AlertType.ERROR,"Invalid number format");
				alert.show();
				return;
			}
			isRefund = true;
		}
		catch (Exception e) {
			// TODO: handle exception
			Alert alert = new Alert(AlertType.ERROR,"Enter numbers only");
			alert.show();
			return;
		}

		int complainId = complain.getId();
		
		if (txtRefund.getText().equals("0.0"))
			isRefund = false;

		Packet packet = new Packet();
		ArrayList<Object> paramListReply = new ArrayList<>();

		if (isRefund) {
			java.sql.Date creationDate = new java.sql.Date(new java.util.Date().getTime());
			Refund refund = new Refund(creationDate, amount, complainId);
			
			int bId = (cmbBranch.getSelectionModel().getSelectedItem()).getbId();
			Account accountToUpdate = getCustomerAccountInBranch(bId);

			ArrayList<Object> paramListRefund = new ArrayList<>();
			packet.addCommand(Command.addComplainRefund);
			paramListRefund.add(refund);
			packet.setParametersForCommand(Command.addComplainRefund, paramListRefund);
			packet.addCommand(Command.updateAccountBalance);
			ArrayList<Object> paramListUpdateAccount = new ArrayList<>();
			paramListUpdateAccount.add(accountToUpdate.getBranchId());
			paramListUpdateAccount.add(accountToUpdate.getCustomerId());
			paramListUpdateAccount.add(amount);
			packet.setParametersForCommand(Command.updateAccountBalance, paramListUpdateAccount);
		}
		ArrayList<Object> paramListComplain = new ArrayList<>();
		complain.setActive(false);
		paramListComplain.add(complain);
		packet.setParametersForCommand(Command.updateComplain, paramListComplain);
		
		Reply reply = new Reply(complainId, replyment);

		packet.addCommand(Command.addReply);
		paramListReply.add(reply);
		packet.setParametersForCommand(Command.addReply, paramListReply);

		SystemSender sender = new SystemSender(packet);
		sender.registerHandler(new IResultHandler() {
			@Override
			public void onWaitingForResult() {
				// TODO Auto-generated method stub
			}
			@Override
			public void onReceivingResult(Packet p) {
				// TODO Auto-generated method stub
				Alert alert = null;
				if (p.getResultState()) {
					
					if(isRefund) 
						alert = new Alert(AlertType.INFORMATION,"Refund has been sent");
					else
						alert = new Alert(AlertType.INFORMATION,"Replyment has been sent");
					 Optional<ButtonType> result = alert.showAndWait();
			            if (result.get() == ButtonType.OK){
			            	alert.hide();
			            	((Node) event.getSource()).getScene().getWindow().hide();
			        		ComplainsController complainsController = new ComplainsController();
			        		complainsController.start(new Stage());			                
			            }
				}
				else
				{
					alert = new Alert(AlertType.ERROR,p.getExceptionMessage());
					alert.show();
				}
			}
		});
		sender.start();
	}
	
	/**
	 * Initializing a Combo Box component with the relevant branches which the customer is registered to 
	 */
	private void initCmb()
	{
		cmbSetConverter();
		
		ArrayList<Branch> customerBranches = new ArrayList<>();
		for(Branch branch : branchList) {
			Account account = getCustomerAccountInBranch(branch.getbId());
			if(account != null)
				customerBranches.add(branch);
		}
		if(customerBranches.size() > 0) {
			data = FXCollections.observableArrayList(customerBranches);
			cmbBranch.setItems(data);
			cmbBranch.getSelectionModel().selectFirst();
		}
		else {
			cmbBranch.setDisable(true);
			txtRefund.setDisable(true);
		}
	}

	/**
	 * Initializing the component by showing the complain's info and set the refund text field to zero.
	 * @param complain - initialize text fields with information from this complain
	 */
	public void setComponents(Complain complain)
	{
		setComplain(complain);
		initCustomerDetails();
		txtTitle.setText(complain.getTitle());
		txtDesc.setText(complain.getDetails());
		txtRefund.setText("0.0");
	}
	
	/**
	 * Set a converted for each branch object to be shown in the Combo Box,
	 * To show it's name
	 */
	private void cmbSetConverter()
	{
		cmbBranch.setConverter(new StringConverter<Branch>() {

			/**
			 * Getting a Branch object by his name
			 * @param string - string to be converted to a Branch object
			 */
			@Override
			public Branch fromString(String string) {
				return null;
			}
			/**
			 * getting a String of the name of a branch by a Branch object
			 * Branch to be converted in to a String name
			 */
			@Override
			public String toString(Branch object) {
				return String.format("%s", object.getName());
			}
		});
	}
	
	/**
	 * Initializing customer details by getting his Accounts,
	 * Then initializing the Combo Box to show branches which he has account in them.
	 */
	private void initCustomerDetails()
	{
		Packet packet = new Packet();
		ArrayList<Object> paramList = new ArrayList<>();
		paramList.add(complain.getCustomerId());
		packet.setParametersForCommand(Command.getAccountbycID, paramList);
		packet.addCommand(Command.getBranches);
		ArrayList<Object> paramListBranches = new ArrayList<>();
		packet.setParametersForCommand(Command.getBranches, paramListBranches);
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
					customerAccList = p.<Account>convertedResultListForCommand(Command.getAccountbycID);
					branchList = p.<Branch>convertedResultListForCommand(Command.getBranches);
					initCmb();
				}
			}
		});
		sender.start();
	}

}
