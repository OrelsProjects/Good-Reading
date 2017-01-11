package Entities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import application.Main;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class MyServer extends AbstractServer {
	Connection conn;
	private static int bookCnt;

		public static void main(String[] args) {
	int port = 0;
	try {
		port = Integer.parseInt(args[0]);
	}
	catch (Throwable t) {
		port = Main.port;
	}
	MyServer s1 = new MyServer(port);
	}

	public MyServer(int port) {
		super(port);
		this.connectToDB();
		try {
			this.listen();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try{
			switch(((GeneralMessage)msg).actionNow){
			case "AddBook":
				addBook((Book)msg, client);break;
			case "RemoveBook":
				removeBook((Book) msg, client);break;
			case "CheckUser":
				checkUser((User)msg,client);break;
			case "InitializeBookList":
				initializeBookList((Book)msg, client);break;
			case "InitializeWorkerList":
				initializeWorkerList((Worker)msg, client);break;
			case "Logout":
				LogOutUser((User)msg,client);break;
			case "CreditCard":
				addCreditCard((CreditCard)msg,client); break;

			case "Monthly":
				subscribe((Reader)msg,1,client);break;
			case "Yearly":
				subscribe((Reader)msg,2,client);break;
			case "FindLoggedReaders":
				find("readers", "isLoggedIn='1';", " is logged in!",client);break;
			case "FindLoggedWorkers":
				find("workers", "isLoggedIn='1';", " is logged in!",client);break;
			case "FindAllManagers":
				find("workers", "isManager='1';", " is a manager!",client);break;
			case "FindAllWorkers":
				find("workers", "isManager='0';", " is a worker!",client);break;
			case "FindDebtReaders":
				find("readers", "debt is not null;", " is in debt!",client);break;
			case "FindFrozenReaders":
				find("readers", "isFrozen='1';", " has his account frozen!",client);break;
			case "FindWorkers":
				findWorkers((Worker)msg, client);break;
			case "FindReaders":
				findReaders((Reader)msg, client);break;
			default:
				break;
			}
		}catch(Exception e){System.out.println("Exception at:" + ((GeneralMessage)msg).actionNow);e.printStackTrace();}
	}
	
	public void findReaders(Reader reader, ConnectionToClient client){
		try {
			Statement stmt = conn.createStatement();
			System.out.println("Query:" + reader.query);
			ResultSet rs = stmt.executeQuery(reader.query);
			while(rs.next())
				System.out.println(rs.getString(3) + " " + rs.getString(4) + " is a reader!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void findWorkers(Worker worker, ConnectionToClient client){
		try {
			Statement stmt = conn.createStatement();
			System.out.println("Query:" + worker.query);
			ResultSet rs = stmt.executeQuery(worker.query);
			while(rs.next())
				System.out.println(rs.getString(3) + " " + rs.getString(4) + " is a worker!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void find(String from, String where,String isWhat, ConnectionToClient client){
		ArrayList<String> arr = new ArrayList<String>();
		Statement stmt;
		try{
			stmt = conn.createStatement();
			String query = "SELECT * FROM " + from + " WHERE " + where;
			System.out.println(query);
			String str;
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				str = rs.getString(3) + " " + rs.getString(4) + isWhat;
				arr.add(str);
			}
			client.sendToClient(arr);
		}catch(Exception e){e.printStackTrace();}
	}
	
		
	
	
	
/*	public void findLoggedWorkers(String from, String where, ConnectionToClient client){
		ArrayList<String> arr = new ArrayList<String>();
		Statement stmt;
		try{
			stmt = conn.createStatement();
			String query = "SELECT * FROM" + from + "WHERE" + from;
			String str;
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				str = rs.getString(3) + rs.getString(4) + " is logged in!";
				arr.add(str);
			}
			client.sendToClient(arr);
		}catch(Exception e){e.printStackTrace();}
	}
	
	public void findLoggedReaders(ConnectionToClient client){
		ArrayList<String> arr = new ArrayList<String>();
		Statement stmt;
		try{
			stmt = conn.createStatement();
			String query = "SELECT * FROM readers WHERE isLoggedIn = '1'";
			String str;
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				str = rs.getString(3) + rs.getString(4) + "is logged in!";
				arr.add(str);
			}
			client.sendToClient(arr);
		}catch(Exception e){e.printStackTrace();}
	}*/
	
	private void subscribe(Reader reader,int type, ConnectionToClient client)//type is the type of subscription
	{
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("Update readers set subscription= "+type+" where readerID = '"+reader.getID()+"';");
			client.sendToClient(type);//The subscription succeeded
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	
	
	

	public void removeBook(Book book, ConnectionToClient client){/**********************************/
		try{
			Statement stmt = conn.createStatement();
			System.out.println(book.query);
			stmt.executeUpdate(book.query);
		}catch(SQLException e){e.printStackTrace();}
	}





	@SuppressWarnings( "static-access" )
	public void initializeWorkerList(Worker worker, ConnectionToClient client){
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM workers";
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Worker> workerList = new ArrayList<Worker>();
			while(rs.next()){
				workerList.add( new Worker (rs.getString(1),rs.getString(3)
						,rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8), rs.getInt(9),rs.getInt(10)));
			}
			client.sendToClient(workerList);
		} catch (Exception  e) {
			e.printStackTrace();
		}	

	}


	public void initializeBookList(Book book, ConnectionToClient client){/*******************************************/
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM books";
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Book> bookList = new ArrayList<Book>();
			while(rs.next()){
				bookList.add( new Book (rs.getString(1),rs.getInt(2),rs.getString(3)
						,rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7), rs.getInt(8)));

			}
			client.sendToClient(bookList);
		} catch (Exception  e) {
			e.printStackTrace();
		}	
	}









	private void addCreditCard(CreditCard card,ConnectionToClient client)
	{
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("Update readers set creditcardnum = '" + card.getCardNum() + "',expdate = '" + card.getExpDate() + "',securitycode='" + card.getSecCode() +"';");
			//To add credit card checks
			client.sendToClient("Credit card added successfully");
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

 


	private void LogOutUser(User user,ConnectionToClient client)
	{
		try {
			Statement stmt = conn.createStatement();
			if(user instanceof Reader)
			{
				stmt.executeUpdate("UPDATE readers SET isLoggedIn=0 WHERE readerID='" + user.getID() + "'");
				client.sendToClient("You've logged out successfully");
			}
			if(user instanceof Worker){
				
			}

		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}






	public void addBook(Book book, ConnectionToClient client){
		Statement stmt;
		Book.bookCnt++;
		String query = "insert into books values ('" + book.getTitle() + "','" + Book.bookCnt + "','" + book.getAuthor() + "','" + 
				book.getLanguage() + "','" + book.getSummary() + "','" + book.getToc() + "','" + book.getKeyword() + "','0');";
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			client.sendToClient("Added!");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void connectToDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception var1_1) {
		}
		try {
			this.conn = DriverManager.getConnection("jdbc:mysql://localhost/librarydb", "root", "Braude");
			System.out.println("MySQL Login Successful!");
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}









	private void checkUser(User user,ConnectionToClient client)
	{
		String id = user.getID();
		String password = user.getPassword();
		Statement stmt,stmt1;
		Reader reader;
		Worker worker;
		try {
			stmt = conn.createStatement();
			stmt1 = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM workers WHERE workerID='" + id + "';");
			ResultSet rs1 = stmt1.executeQuery("SELECT * FROM readers WHERE readerID='" + id + "';");
			if (rs.next())//The ID was found in the workers table
				try {
					if(rs.getString(2).equals(password))
					{
						if(rs.getInt(9)==1)//It is a manager!
							user.setType(3);
						else{
							user.setType(2);//It is a worker!
							worker = new Worker();
							worker.setWorkerID(rs.getString(1));
							stmt1.executeUpdate("UPDATE workers SET isLoggedIn=1 WHERE workerID='" + worker.getWorkerID() + "'");
							client.sendToClient(worker);
						}
						client.sendToClient(user);
					}
					else
						client.sendToClient("Wrong password!");
				}
			catch (IOException e) {
				e.printStackTrace();
			}
			else if(rs1.next())//The ID was found in the readers table
				try {
					if(rs1.getString(2).equals(password))
					{
						if(rs1.getInt(11)==1)
							client.sendToClient("You're already signed in!");
						else
						{
							reader = new Reader(rs1.getString(1),password);
							reader.setFirstName(rs1.getString(3));
							System.out.println(reader.getFirstName());
							reader.setLastName(rs1.getString(4));
							reader.setSubscribed(rs1.getInt(5));
							reader.setIBookValid(rs1.getInt(6));
							reader.setAllowed(rs1.getInt(7));
							reader.setDebt(rs1.getInt(8));
							reader.setIsFrozen(rs1.getInt(9));
							reader.setPremission(rs1.getInt(10));
							reader.setIsLogged(1);
							reader.setCardnum(rs1.getString(12));
							reader.setExpDate(rs1.getString(13));
							reader.setSecCode(rs1.getString(14));
							stmt1.executeUpdate("UPDATE readers SET isLoggedIn=1 WHERE readerID='" + reader.getID() + "'");
							System.out.println(reader.getFirstName());
							client.sendToClient(reader);
						}
					}
					else
						client.sendToClient("Wrong password!");
				}
			catch (IOException e) {
				e.printStackTrace();
			}
			else
				try {
					client.sendToClient("User does not exist in the DB");
				} catch (IOException e) {
					e.printStackTrace();
				} 

			return;
		}
		catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}







