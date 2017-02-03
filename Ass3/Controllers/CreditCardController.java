package Controllers;

import java.io.IOException;

import Entities.CreditCard;
import Entities.Reader;
import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ocsf.client.AbstractClient;

public class CreditCardController extends AbstractClient
{
	public boolean returned = false;
	@FXML TextField cardNumField;
	@FXML TextField monthField,yearField;
	@FXML TextField codeField;
	@FXML Text numText,dateText,codeText;
	public Button confirm;

	public CreditCardController() {
		super(Main.host, Main.port);
		try {
			this.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onConfirm()
	{
		boolean card,month,year,code;
		if(cardNumField.getText().equals("") || cardNumField.getText().length()<16)
		{
			numText.setFill(Color.RED);
			card = false;
		}
		else 
			{
				numText.setFill(Color.BLACK);
				card = true;
			}

		if(monthField.getText().equals("") || monthField.getText().length()<2 || Integer.parseInt(monthField.getText())>12)
		{
			dateText.setFill(Color.RED);
			month = false;
		}
		else 
			{
				dateText.setFill(Color.BLACK);
				month = true;
			}

		if(yearField.getText().equals("") || yearField.getText().length()<2)
		{
			dateText.setFill(Color.RED);
			year = false;
		}
		else 
			{
				if(dateText.getFill() != Color.RED)//If the month is correct but the year is, then we still want to warn the user
					dateText.setFill(Color.BLACK);
				year = true;
			}

		if(codeField.getText().equals("") || codeField.getText().length()<3)
		{
			codeText.setFill(Color.RED);
			code = false;
		}
		else 
			{
				codeText.setFill(Color.BLACK);
				code = true;
			}

		if(card && month && year && code)
		{
			CreditCard newCard = new CreditCard(cardNumField.getText(),monthField.getText(),yearField.getText(),codeField.getText(),Main.getCurrentUser().getID());
			newCard.actionNow="CreditCard";

			try {
				this.sendToServer(newCard);
				System.out.println("sent");
				while(!returned)
					Thread.sleep(10);
				Main.popup.close();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	@Override
	protected void handleMessageFromServer(Object msg) 
	{
		CreditCard card = (CreditCard)msg;
		Reader reader = (Reader)Main.getCurrentUser();
		reader.setCardnum(card.getCardNum());
		reader.setSecCode(card.getSecCode());
		reader.setExpDate(card.getExpDate());
		returned = true;
	}


}