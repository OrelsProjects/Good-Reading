package Controllers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import Entities.Book;
import Entities.GeneralMessage;
import Entities.Reader;
import Entities.User;
import Entities.Worker;
import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ocsf.client.AbstractClient;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginScreenController extends AbstractClient {



	@FXML
	private TextField idTextField;
	@FXML
	private TextField passwordTextField;
	private static String whatAmI;
	private static String host = "localhost";
	private static int port = Main.port;
	private static Reader readerLogged;
	public static Worker currentWorker;
	private static boolean isLoggedFlag=false;
	final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);//For the login
	@FXML
	ImageView loginImageView;
	
	/**
	 * This method initializes a key listener for user comfort
	 * @author orel zilberman
	 */
	 public void initialize(){
		  idTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
		      @Override
		      public void handle(KeyEvent keyEvent) {
		          if (keyEvent.getCode() == KeyCode.ENTER)  {
		              onLogin();
		          }
		      }
		  });
		  passwordTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
		      @Override
		      public void handle(KeyEvent keyEvent) {
		          if (keyEvent.getCode() == KeyCode.ENTER)  {
		              onLogin();
		          }
		      }
		  });
	 }

	

	public LoginScreenController() { 
		super(host, port);
	}
	/**
	 * This function is a general function, used all across my controllers.
	 * <p>
	 * It's main purpose is to send the server a message that it knows how to deal with.
	 * @param msg is a parameter that extends GeneralMessage and is used mainly to hold the string for the server, to get to the right case.
	 * @param actionNow is the string that contains the information for to server to get us to the right case.
	 * @author orel zilberman
	 */

	public void sendServer(Object msg, String actionNow){/******************************/
		((GeneralMessage)msg).actionNow = actionNow;
		LoginScreenController client = new LoginScreenController();
		try {
			client.openConnection();
			client.sendToServer(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	


	 

	


	public void onLogin(){
	/*	File file = new File("C:\\Users\\orels\\Desktop\\Ass3Logos\\Button.png");
		Image image = new Image(file.toURI().toString());
		loginImageView.setImage(image);*/
		User user = new User(idTextField.getText(),passwordTextField.getText());
		whatAmI="";
		sendServer(user, "CheckUser");
		boolean flag=false;
		while(whatAmI==""){
			try {Thread.sleep(10);} 
			catch (InterruptedException e) {e.printStackTrace();}
		}
		if(whatAmI!="User does not exist in the DB"){
			Thread initialize = new Thread(){
				public void run(){
					Book book = new Book();
					Worker worker = new Worker();
					book.bookList = new ArrayList<Book>();
					worker.workerList = new ArrayList<Worker>();
					sendServer(book, "InitializeBookList");//Get the book list in a static array

					sendServer(worker, "InitializeWorkerList");//Get the worker list in static array

					sendServer(worker, "InitializeWorkerList");//Get the worker list in static array


				}
			};
			initialize.start();
		}

		try {
			switch(whatAmI){
			case "reader":
				Main.showReaderLoginScreen();break;
			case "worker":
				Main.showLoggedInScreenWorker();
				Thread thread = new Thread(){
					public void run(){
						while(true)
							sendServer(new GeneralMessage(), "CheckNewReviews");
					}
				};
				break;
			case "manager":
				Main.showManagerLoggedScreen();break;
			}
		} catch (Exception e) {e.printStackTrace();}

	}//End onLogin


	@SuppressWarnings("unchecked")
	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg instanceof String) {
			System.out.println((String)msg);//Wrong user name password
			whatAmI="User does not exist in the DB";
		}

		else{
			if(msg instanceof Worker){
				currentWorker = (Worker)msg;
				Main.setCurrentUser((Worker)msg);
			}
			if(msg instanceof Reader)
			{  
				System.out.println("its a reader!");
				readerLogged=(Reader)msg;
				System.out.println(readerLogged.getFirstName());
				Main.setCurrentUser((Reader)msg);
				isLoggedFlag=true;
				whatAmI="reader";
			}

			else if(msg instanceof User)//Correct details were entered
			{
				isLoggedFlag=true;
				User res = (User)msg;
				User.currentWorker = new Worker();
				User.currentWorker.setType(res.getType());
				if(res.getType()==2){
					whatAmI="worker";      
				}//end if
				else if(res.getType()==3)
					whatAmI="manager";
				Main.setCurrentUser(res);
			}
		}//end else
		if(msg instanceof ArrayList){
			if(((ArrayList<?>)msg).get(0) instanceof Book)
				Book.bookList.addAll(((ArrayList<Book>)msg));//Now we have the books in arraylist!
			else if(((ArrayList<?>)msg).get(0) instanceof Worker)
				Worker.workerList.addAll(((ArrayList<Worker>)msg));//now we have the workers in arraylist
			if(((ArrayList<?>)msg).get(0).equals("BookSearch")){
				
			}
		}//end if arraylist
	}

	public static Reader getReaderLogged() {
		return readerLogged;
	}
}