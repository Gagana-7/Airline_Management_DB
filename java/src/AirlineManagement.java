/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

import java.sql.Date;
import java.time.LocalDate;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class AirlineManagement {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of AirlineManagement
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public AirlineManagement(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end AirlineManagement

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            AirlineManagement.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      AirlineManagement esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the AirlineManagement object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new AirlineManagement (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String[] authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              String role = authorisedUser[1];
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");

                //**the following functionalities should only be able to be used by Management**
                if (role.equals("Management")) {
                  System.out.println("1. View Flight Schedule");
                  System.out.println("2. View Flight Seats");
                  System.out.println("3. View Flight Status");
                  System.out.println("4. View Flights of the day");  
                  System.out.println("5. View Full Order ID History");
                  System.out.println(".........................");
                  System.out.println(".........................");
                }
                

                //**the following functionalities should only be able to be used by customers**
                if (role.equals("Customer")) {
                System.out.println("1. Search Flights");
                System.out.println(".........................");
                System.out.println(".........................");
               }

                //**the following functionalities should ony be able to be used by Pilots**
                if (role.equals("Pilot")) {
                  System.out.println("1. Maintenace Request");
                  System.out.println(".........................");
                  System.out.println(".........................");
                }
                

               //**the following functionalities should ony be able to be used by Technicians**
               if (role.equals("Technician")) {
                  System.out.println(".........................");
                  System.out.println(".........................");
               }
                
                System.out.println("20. Log out");
                if (role.equals("Management")) {
                  switch (readChoice()){
                     case 1: ScheduleFromFlightNum(esql); break;
                     case 2: GetSeats(esql); break;
                     case 3: OnTime(esql); break;
                     case 4: FlightsOfTheDay(esql); break;
                     case 5: feature5(esql); break;
                     case 6: feature6(esql); break;
                     case 20: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                }
               }
                if (role.equals("Customer")) {
                  switch (readChoice()){
                     case 1: feature1(esql); break;
                     case 2: feature2(esql); break;
                     case 3: feature3(esql); break;
                     case 4: feature4(esql); break;
                     case 5: feature5(esql); break;
                     case 6: feature6(esql); break;
                     case 20: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                }
               }
                if (role.equals("Pilot")) {
                  switch (readChoice()){
                     case 1: feature1(esql); break;
                     case 2: feature2(esql); break;
                     case 3: feature3(esql); break;
                     case 4: feature4(esql); break;
                     case 5: feature5(esql); break;
                     case 6: feature6(esql); break;
                     case 20: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                }
               }
                if (role.equals("Technician")) {
                  switch (readChoice()){
                     case 1: feature1(esql); break;
                     case 2: feature2(esql); break;
                     case 3: feature3(esql); break;
                     case 4: feature4(esql); break;
                     case 5: feature5(esql); break;
                     case 6: feature6(esql); break;
                     case 20: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                }
               }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(AirlineManagement esql){
      System.out.println("CreateUser method called");
      try {
         System.out.println("Enter username: ");
         String username = in.readLine();

         String checkQuery = String.format("SELECT * FROM Users WHERE username = '%s'", username);
         List<List<String>> check = esql.executeQueryAndReturnResult(checkQuery);
         if (!check.isEmpty()) {
            System.out.println("Username already exists. Please try a different one.");
            return;
        }

         System.out.println("Enter password: ");
         String password = in.readLine();

         System.out.println("Enter role(Customer, Technician, Pilot, Management)");
         String role = in.readLine();

         // Validate role
         if (!role.equalsIgnoreCase("Management") &&
            !role.equalsIgnoreCase("Customer") &&
            !role.equalsIgnoreCase("Pilot") &&
            !role.equalsIgnoreCase("Technician")) {
            System.out.println("Invalid role. Please enter one of: Management, Customer, Pilot, Technician");
            return;
         }

         String insertQuery = String.format(
            "INSERT INTO Users (username, password, role) VALUES ('%s', '%s', '%s');",
            username, password, role
         );
         esql.executeUpdate(insertQuery);
         System.out.println("User created successfully.");
      } catch (Exception e) {
         System.err.println((e.getMessage()));
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String[] LogIn(AirlineManagement esql){
      try {
         System.out.println("Enter username: ");
         String username = in.readLine();

         System.out.println("Enter password ");
         String password = in.readLine();

         String query = String.format(
            "SELECT userID, role FROM Users WHERE username = '%s' AND password = '%s';",
            username, password
         );

         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.size() > 0) {
            System.out.println("Login successful! Welcome, " + username);
            String userID = result.get(0).get(0);
            String role = result.get(0).get(1);
            return new String[]{userID, role};
         } else {
            System.out.println("Invalid username or password.");
            return null;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void ScheduleFromFlightNum(AirlineManagement esql) {
      try {
         System.out.println("Enter Flight Number: ");
         String flNum = in.readLine();

         String query = String.format(
            "SELECT DayOfWeek, DepartureTime, ArrivalTime FROM schedule WHERE FlightNumber = '%s';",
            flNum
         );

         List<List<String>> results = esql.executeQueryAndReturnResult(query);

         if (results.isEmpty()) {
            System.out.println("No schdule found for flight number " + flNum);
         } else {
            for (List<String> row : results) {
               System.out.println(row);
            }
         }
      } catch (Exception e) {
         System.err.println("Error in ScheduleFromFlightNum: " + e.getMessage());
      }
   }
   public static void GetSeats(AirlineManagement esql) {
      try {
         System.out.println("Enter Flight: ");
         String flName = in.readLine();
         System.out.println("Enter Flight Date(YYYY-MM-DD): ");
         String dateInput = in.readLine().trim(); //get string of date
         LocalDate localDate = LocalDate.parse(dateInput); //parse string to LocalDate
         Date sqlDate = Date.valueOf(localDate); //convert to java.sql.Date

         System.out.println("Do you want to view:");
         System.out.println("(1) Number of Seats Sold");
         System.out.println("(2) Number of Seats Remaining");

         switch (readChoice()) {
            case 1: {
               String query = String.format(
                  "SELECT SeatsSold FROM FlightInstance WHERE FlightNumber = '%s' AND FlightDate = '%s';",
                  flName, sqlDate
               );
               List<List<String>> results = esql.executeQueryAndReturnResult(query);
               if (results.isEmpty()) {
                  System.out.println("No matching records found.");
               } else {
                  System.out.println("Seats Sold: " + results.get(0).get(0));
               }
               break;
            }
            case 2: {
               String query = String.format(
                  "SELECT SeatsTotal - SeatsSold FROM FlightInstance WHERE FlightNumber = '%s' AND FlightDate = '%s';", 
                  flName, sqlDate
                  );
                  List<List<String>> results = esql.executeQueryAndReturnResult(query);
                  if (results.isEmpty()) {
                     System.out.println("No matching records found.");
                  } else {
                     System.out.println("Seats Remaining: " + results.get(0).get(0));
                  }
                  break;
            }
            default:
            System.out.println("Invalid choice.");
            break;
         }
      } catch (Exception e) {
         System.err.println("Error in GetSeats: " + e.getMessage());
      }
   }
   public static void OnTime(AirlineManagement esql) {
      try {
         System.out.println("Enter Flight: ");
         String flName = in.readLine();
         System.out.println("Enter Flight Date(YYYY-MM-DD): ");
         String dateInput = in.readLine().trim(); //get string of date
         LocalDate localDate = LocalDate.parse(dateInput); //parse string to LocalDate
         Date sqlDate = Date.valueOf(localDate); //convert to java.sql.Date

         System.out.println("Do you want to view:");
         System.out.println("(1) Departure");
         System.out.println("(2) Arrival");

         switch (readChoice()) {
            case 1: {
               String query = String.format(
                  "SELECT DepartedOnTime FROM FlightInstance WHERE FlightNumber = '%s' AND FlightDate = '%s';",
                  flName, sqlDate
               );
               List<List<String>> results = esql.executeQueryAndReturnResult(query);
               if (results.isEmpty()) {
                  System.out.println("No matching records found.");
               } else {
                  String departedStr = results.get(0).get(0);
                  if (departedStr.equals("f")) {
                     System.out.println("Did not depart on time");
                  } else if (departedStr.equals("t")) {
                     System.out.println("Departed on time");
                  } else {
                     System.out.println("Unknown departure status: " + departedStr);
                  }
               }
               break;
            }
            case 2: {
               String query = String.format(
                  "SELECT ArrivedOnTime FROM FlightInstance WHERE FlightNumber = '%s' AND FlightDate = '%s';",
                  flName, sqlDate
               );
               List<List<String>> results = esql.executeQueryAndReturnResult(query);
               if (results.isEmpty()) {
                  System.out.println("No matching records found.");
               } else {
                  String arrivedStr = results.get(0).get(0);
                  if (arrivedStr.equals("f")) {
                     System.out.println("Did not arrive on time");
                  } else if (arrivedStr.equals("t")) {
                     System.out.println("Arrived on time");
                  } else {
                     System.out.println("Unknown arrival status: " + arrivedStr);
                  }
               }
               break;
            }
         }
      } catch (Exception e) {
         System.err.println("Error in GetSeats: " + e.getMessage());
      }
   }
   public static void FlightsOfTheDay(AirlineManagement esql) {
      try {
         System.out.println("Enter Flight Date(YYYY-MM-DD): ");
         String dateInput = in.readLine().trim(); //get string of date
         LocalDate localDate = LocalDate.parse(dateInput); //parse string to LocalDate
         Date sqlDate = Date.valueOf(localDate); //convert to java.sql.Date

         String query = String.format(
            "SELECT FlightNumber FROM FlightInstance WHERE FlightDate = '%s';", 
            sqlDate
            );
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
         if (results.isEmpty()) {
            System.out.println("No matching records found.");
         } else {
            for (List<String> row : results) {
               System.out.println("Flight: "+ row);
            }
         }
      } catch (Exception e) {
         System.err.println("Error in FlightsOfTheDay: "+  e.getMessage());
      }
   }
   public static void feature1(AirlineManagement esql) {}
   public static void feature2(AirlineManagement esql) {}
   public static void feature3(AirlineManagement esql) {}
   public static void feature4(AirlineManagement esql) {}
   public static void feature5(AirlineManagement esql) {}
   public static void feature6(AirlineManagement esql) {}
  


}//end AirlineManagement

