package com.amazon.customskill;

import java.sql.DriverManager;
import java.sql.*;

public class DBConnection {

	static String DBName = "praxisProject2ß191.db";
	private static Connection con = null;
/*
 * establishing the connection with the SQLite database 
 * */
	public static Connection getConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			try {
				con = DriverManager.getConnection("jdbc:sqlite:D:\\WiSe19-20\\Praxisprojekt\\Praxisprojekt-master\\de.unidue.ltl.ourWWM\\" + DBName);
			} catch (SQLException ex) {
				System.out.println("Failed to create the database connection.");
			}
		} catch (ClassNotFoundException ex) {
			System.out.println("Driver not found.");
		}
		return con;
	}

}
