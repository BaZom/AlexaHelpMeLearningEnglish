package com.amazon.customskill;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Question {

	static String questionText = null;
	private int WhichText;
	private String Tipp = null;
	private String CorrectAnswer = null;
	private String AlexaCorrectAnswer = null;
	private int numbering;

	private static Connection con = null;
	private static Statement stmt = null;
	int rand = 0;

	public Question(int WhichText) {
		this.WhichText = WhichText;
	}

	
	/*
	 * selects the content of a row in the text tables for the needed text based on the level.
	 * */
	public String selectQuestion() {

		try {
			con = DBConnection.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM Frage WHERE WhichText=" + WhichText + " and Numbering =" + rand + "");

			questionText = rs.getString("FrageText");
			CorrectAnswer = rs.getString("CorrectAnswer");
			Tipp = rs.getString("Tipp");
			AlexaCorrectAnswer = rs.getString("AlexaCorrectAnswer");
			WhichText = rs.getInt("TextID");
			numbering = rs.getInt("Numbering");

			// System.out.println("Data selected! " + rand);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return questionText;
	}

	public static String getQuestionText() {
		return questionText;
	}

	public static void setQuestionText(String questionText) {
		Question.questionText = questionText;
	}

	public int getWhichText() {
		return WhichText;
	}

	public void setWhichText(int whichText) {
		WhichText = whichText;
	}

	public String getTipp() {
		return Tipp;
	}

	public void setTipp(String tipp) {
		Tipp = tipp;
	}

	public String getAlexaCorrectAnswer() {
		return AlexaCorrectAnswer;
	}

	public void setAlexaCorrectAnswer(String alexaCorrectAnswer) {
		AlexaCorrectAnswer = alexaCorrectAnswer;
	}

	public int getNumbering() {
		return numbering;
	}

	public void setNumbering(int numbering) {
		this.numbering = numbering;
	}

	public void setCorrectAnswer(String correctAnswer) {
		CorrectAnswer = correctAnswer;
	}

	public String getQuestion() {
		return questionText;
	}

	public String getCorrectAnswer() {
		return CorrectAnswer;
	}

}
