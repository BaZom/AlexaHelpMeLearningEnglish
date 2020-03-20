package com.amazon.customskill;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class Text {

	static String Text;
	static String Level;
	static int TextID;
	private static Connection con = null;
	private static Statement stmt = null;
	Random random = new Random(); 
    int rand;
    int randOld;

	public Text(String Level) {
		this.Level = Level;
	}

	/*
	 * selects the content of a row in the question tables for the needed question based on the TestID from Texts table
	 * */
	public String selectText() {

		switch (Level) {
		case "a1": {
			int max = 115;
	        int min = 100; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			randOld = rand; 
			break;
		}
		case "a2": {
			int max = 131;
	        int min = 116; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			break;
		}
		case "b1": {
			int max = 147;
	        int min = 132; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			break;
		}
		case "b2": {
			int max = 163;
	        int min = 148; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			break;
		}

		}

		try {
			con = DBConnection.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT *  FROM Texte where NiveauID = '" + Level + "' and TextID =" + rand + "");
			while (rs.next()) {
				Text = rs.getString("Texte");
				TextID = rs.getInt("TextID");
			}
			String ID = rs.getString("NiveauID");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Text;
	}

	public static void setTextID(int textID) {
		TextID = textID;
	}

	public int getTextID() {
		return TextID;
	}

	public String getText() {
		return Text;
	}
}
