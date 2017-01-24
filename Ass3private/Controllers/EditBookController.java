package Controllers;

import java.io.IOException;

import javax.swing.JOptionPane;

import Entities.Book;
import Entities.GeneralMessage;
import Entities.Genre;
import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class EditBookController {
	@FXML
	TextField titleTextField, languageTextField, summaryTextField, authorTextField, keyWordTextField, tocTextField, 
	genresTextField;
	@FXML
	ComboBox<String> genreComboBox;
	public static Book book;
	public static String genre, firstGenre;

	public EditBookController(){
	}

	public void initialize(){
		book = new Book();
		book.setBookid(WorkerController.bookForEdit.getBookid());
		titleTextField.setText(WorkerController.bookForEdit.getTitle());
		authorTextField.setText(WorkerController.bookForEdit.getAuthor());
		languageTextField.setText(WorkerController.bookForEdit.getLanguage());
		summaryTextField.setText(WorkerController.bookForEdit.getSummary());
		keyWordTextField.setText(WorkerController.bookForEdit.getKeyword());
		tocTextField.setText(WorkerController.bookForEdit.getToc());
		genresTextField.setText(WorkerController.bookForEdit.getGenre());
		genreComboBox.setPromptText("Genres");
		genre = WorkerController.bookForEdit.getGenre();
	}

	public void sendServer(Object msg, String actionNow){/******************************/
		try {
			((GeneralMessage)msg).actionNow = actionNow;
			WorkerController client = new WorkerController();
			try {
				client.openConnection();
				client.sendToServer(msg);
			} catch (Exception e) {e.printStackTrace();}
		} catch (Exception e) {	e.printStackTrace();}
	}//end sendserver

	public void Sleep(int time){
		try{
			Thread.sleep(time);
		}catch(Exception e){e.printStackTrace();}
	}//endsleep

	public void onGenrePress(){
		Genre genre = new Genre();
		genreComboBox.getItems().clear();
		sendServer(genre, "InitializeGenreList");
		while(WorkerController.genresList==null)
			Sleep(2);
		ObservableList<String> items = FXCollections.observableArrayList();
		items.addAll(WorkerController.genresList);
		genreComboBox.setItems(items);
	}


	public void onBackFromSearch(){

		Book book1 = new Book();
		book1.query = "select * from books;";
		sendServer(book1, "UpdateBookList");
		while(WorkerController.foundBookList ==null)
			Sleep(10);

		try{
			Main.showUpdateBookScreen();
		}catch(Exception e){e.printStackTrace();}
	}//End onbackfromsearch


	public void onNewGenreChosen(){
		if(genreComboBox.getSelectionModel().getSelectedItem() == null)
			return;
		System.out.println("newgenre:" );
		String genreSelected = genreComboBox.getSelectionModel().getSelectedItem();
		String genreText="";

		for(int i=0;i<genresTextField.getText().length();i++)//Deep Copy from textfield to variable genreText
			genreText += genresTextField.getText().charAt(i);

		String newGenre = "";
		if(!genresTextField.getText().contains(genreSelected)){//The genre is not there! add it!
			if(genreText.equals(""))
				newGenre = genreSelected;
			else
				newGenre = (genreText + " " + genreSelected);
		}
		else{//The genre is already there! remove it!
			int indexOf = genresTextField.getText().indexOf(genreSelected);//Where the genreSelected String begins.
			boolean isFirst = false;
			newGenre="";
			System.out.println("indexof" +indexOf);
			for(int i=0;i<genreText.length();i++){
				if(indexOf==0&&i==0){
					isFirst = true;
					i+=genreSelected.length()+1;
				}
				if(i>=indexOf-1&&!isFirst){
					i+=genreSelected.length()+1;//For the "," the " " will be taken care of next iteration with the i++
					isFirst=true;//This block should be done only once
				}
				if(i<genreText.length())
					newGenre+= genreText.charAt(i);
			}//end for
		}//end else
		genresTextField.setText(newGenre);
	}




	public void onEditBook(){
		if(genresTextField.equals("")){
			JOptionPane.showConfirmDialog(null, "No genre chosen!");
			return;
		}
		Book book = new Book();

		book.setAuthor(authorTextField.getText());
		book.setTitle(titleTextField.getText());
		book.setLanguage(languageTextField.getText());
		book.setSummary(summaryTextField.getText());
		book.setKeyword(keyWordTextField.getText());
		book.setToc(tocTextField.getText());
		book.setGenre(genresTextField.getText());
		book.setBookid(WorkerController.bookForEdit.getBookid());
		System.out.println("edit:" + WorkerController.bookForEdit.getBookid());
		for(int i=0;i<Book.bookList.size();i++)//Update for the Book.booklist for sir
			if (Book.bookList.get(i).getBookid() == WorkerController.bookForEdit.getBookid()){
				Book.bookList.set(i, book);break;
			}

		sendServer(book,"EditBookPlz");
		/*           Show the screen after edit book    */          
		WorkerController.foundBookList = null;
		Book book1 = new Book();
		book1.query = "select * from books;";
		sendServer(book1, "UpdateBookList");
		while(WorkerController.foundBookList==null)
			Sleep(5);
		/*           Show the screen after edit book              */
		try {Main.showUpdateBookScreen();} catch (IOException e) {e.printStackTrace();}
	}

}
