package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLiteConnectivity {
	
	public static void createVendorTable() {
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:licenta.db");
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      String myTableName = "CREATE TABLE vendor (" 
		            + "prefix CHAR(100) PRIMARY KEY NOT NULL," 
		            + "name CHAR(100) NOT NULL,"
		            + "description CHAR(200))";
	      
	      stmt.executeUpdate(myTableName);
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Table created successfully");
	}
	
	public static void createPropertiesTable() {
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:licenta.db");
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      String myTableName = "CREATE TABLE properties (" 
		            + "id INTEGER PRIMARY KEY AUTOINCREMENT," 
		            + "address CHAR(100) NOT NULL,"
		            + "property CHAR(200) NOT NULL,"
		            + "value CHAR(300) NOT NULL)";
	      
	      stmt.executeUpdate(myTableName);
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Table created successfully");
	}
	
	public static void createPMKTable() {
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:licenta.db");
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      String myTableName = "CREATE TABLE PMKTable (" 
		            + "BSSID CHAR(100) PRIMARY KEY NOT NULL," 
		            + "SSID CHAR(100) NOT NULL,"
		            + "PMK CHAR(200) NOT NULL)";  
	      
	      stmt.executeUpdate(myTableName);
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Table created successfully");
	}
	
}
