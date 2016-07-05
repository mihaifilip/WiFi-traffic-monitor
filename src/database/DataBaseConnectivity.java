package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DataBaseConnectivity {

	private static Connection connect = null;
	private static Statement statement = null;
	private static String jdbcDriver = "com.mysql.jdbc.Driver";
	private static String dbAddress = "jdbc:mysql://localhost/";
	private static String dbName = "licenta";
	private static String userName = "?user=mihai";
	private static String password = "&password=sqluserpw";
	public static final String VENDOR_TABLE = "vendor";
	public static final String PROPERTIES_TABLE = "properties";

	public static boolean checkTable(String tableName) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		boolean response = false;
		try {
			Class.forName(jdbcDriver);
			connect = DriverManager.getConnection(dbAddress + dbName + userName + password);
	        preparedStatement = connect.prepareStatement("SHOW TABLES LIKE ?");
	        preparedStatement.setString(1, tableName);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				response = true;
				break;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return response;
	}

	private static void close() {
		try {
			if (connect != null)
				connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void createVendorTable() {
		String myTableName = "CREATE TABLE vendor (" 
	            + "prefix VARCHAR(100)," 
	            + "name VARCHAR(100),"
	            + "description VARCHAR(200),"
	            + "primary key (prefix))";  
	        try {
	            Class.forName(jdbcDriver);
	            connect = DriverManager.getConnection(dbAddress + dbName + userName + password);
	            statement = connect.createStatement();
	            statement.executeUpdate(myTableName);
	            System.out.println("Table Created");
	        }
	        catch (SQLException e ) {
	            System.out.println("An error has occurred on Table Creation");
	        }
	        catch (ClassNotFoundException e) {
	            System.out.println("Mysql drivers were not found");
	        }
	        finally {
	        	close();
	        }
	}
	
	public static void createPropertiesTable() {
		String myTableName = "CREATE TABLE properties (" 
	            + "id INT AUTO_INCREMENT KEY ," 
	            + "address VARCHAR(100),"
	            + "property VARCHAR(200),"
	            + "value VARCHAR(300))";  
	        try {
	            Class.forName(jdbcDriver);
	            connect = DriverManager.getConnection(dbAddress + dbName + userName + password);
	            statement = connect.createStatement();
	            statement.executeUpdate(myTableName);
	            System.out.println("Table Created");
	        }
	        catch (SQLException e ) {
	            System.out.println("An error has occurred on Table Creation");
	        }
	        catch (ClassNotFoundException e) {
	            System.out.println("Mysql drivers were not found");
	        }
	        finally {
	        	close();
	        }
	}
	
	public static void createPMKTable() {
		String myTableName = "CREATE TABLE PMKTable (" 
	            + "BSSID VARCHAR(100)," 
	            + "SSID VARCHAR(100),"
	            + "PMK VARCHAR(200),"
	            + "primary key (BSSID))";  
	        try {
	            Class.forName(jdbcDriver);
	            connect = DriverManager.getConnection(dbAddress + dbName + userName + password);
	            statement = connect.createStatement();
	            statement.executeUpdate(myTableName);
	            System.out.println("Table Created");
	        }
	        catch (SQLException e ) {
	            System.out.println("An error has occurred on Table Creation");
	        }
	        catch (ClassNotFoundException e) {
	            System.out.println("Mysql drivers were not found");
	        }
	        finally {
	        	close();
	        }
	}

	public static String getVendor(String prefix) {
		try {
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			
			prefix = prefix.toUpperCase();
			prefix = new String(prefix.substring(0, 2) + ":" + prefix.substring(2, 4) + ":" + prefix.substring(4, 6));
			Class.forName(jdbcDriver);
			connect = DriverManager.getConnection(dbAddress + dbName + userName + password);
	        preparedStatement = connect.prepareStatement("SELECT name FROM licenta.vendor WHERE ? = prefix;");
	        preparedStatement.setString(1, prefix);
			resultSet = preparedStatement.executeQuery();
			String response = null;
			try {
				while (resultSet.next()) {
		            response = resultSet.getString("name");
				}
			} catch (Exception e) {
				//empty set
			}
			preparedStatement.close();
			resultSet.close();
			return response;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		finally {
			close();
		}
	}
	
	public static ArrayList<ArrayList<String>> getProperties(String address) {
		try {
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			Class.forName(jdbcDriver);
			connect = DriverManager.getConnection(dbAddress + dbName + userName + password);
	        preparedStatement = connect.prepareStatement("SELECT property, value FROM licenta.properties WHERE ? = address;");
	        preparedStatement.setString(1, address);
			resultSet = preparedStatement.executeQuery();
			ArrayList<ArrayList<String>> response = new ArrayList<ArrayList<String>>();
			try {
				while (resultSet.next()) {
		            ArrayList<String> list = new ArrayList<>();
		            list.add(resultSet.getString("property"));
		            list.add(resultSet.getString("value"));
		            response.add(list);
				}
			} catch (Exception e) {
				//empty set
			}
			preparedStatement.close();
			resultSet.close();
			return response;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		finally {
			close();
		}
	}
	
	public static void addProperty(ArrayList<String> values) {
		try {
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			
			Class.forName(jdbcDriver);
			connect = DriverManager.getConnection(dbAddress + dbName + userName + password);
	        preparedStatement = connect.prepareStatement("SELECT property, value FROM licenta.properties WHERE ? = address;");
	        String address = values.get(0);
	        preparedStatement.setString(1, address);
			resultSet = preparedStatement.executeQuery();
			try {
				while (resultSet.next()) {
		            String property = resultSet.getString("property");
		            if (property.equals(values.get(1))) {
		            	preparedStatement = connect.prepareStatement("DELETE FROM licenta.properties WHERE ? = address AND ? = property;");
		            	preparedStatement.setString(1, address);
		            	preparedStatement.setString(2, property);
		            	preparedStatement.executeUpdate();
		            	break;
		            }
				}
				preparedStatement = connect.prepareStatement("INSERT INTO licenta.properties (address, property, value) VALUES (?, ?, ?);");
				preparedStatement.setString(1, values.get(0));
            	preparedStatement.setString(2, values.get(1));
            	preparedStatement.setString(3, values.get(2));
            	preparedStatement.executeUpdate();
			} catch (Exception e) {
				//empty set
			}
			preparedStatement.close();
			resultSet.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}
	
	public static void deleteProperty(ArrayList<String> values) {
		try {
			PreparedStatement preparedStatement = null;
			Class.forName(jdbcDriver);
			connect = DriverManager.getConnection(dbAddress + dbName + userName + password);
			preparedStatement = connect.prepareStatement("DELETE FROM licenta.properties WHERE ? = address AND ? = property;");
        	preparedStatement.setString(1, values.get(0));
        	preparedStatement.setString(2, values.get(1));
        	preparedStatement.executeUpdate();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}
	
	public static void populateDB() throws Exception {
		try {
			PreparedStatement preparedStatement = null;
			// this will load the MySQL driver, each DB has its own driver
			Class.forName(jdbcDriver);
			// setup the connection with the DB.
			connect = DriverManager.getConnection(dbAddress + dbName + userName + password);

			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			
			BufferedReader br = new BufferedReader(new FileReader(new File("/home/mihai/workspace/Licenta/resources/NetworkCardVendors")));
			for(String line; (line = br.readLine()) != null; ) {
		    	String after = line.trim().replaceAll(" +", " ");
		    	after = after.replaceAll("\t", " ");
		    	System.out.println(after);
		    	String[] values = after.split("#");
		    	String[] subValues = values[0].split(" ");
				preparedStatement = connect.prepareStatement("insert into  licenta.vendor values (?, ?, ?);");
				preparedStatement.setString(1, subValues[0]);
				preparedStatement.setString(2, subValues[1]);
				if (values.length > 1) {
					preparedStatement.setString(3, values[1]);
				}
				else {
					preparedStatement.setString(3, "");
				}
				preparedStatement.executeUpdate();
		    }
			br.close();
			preparedStatement.close();
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
	}
	
}
