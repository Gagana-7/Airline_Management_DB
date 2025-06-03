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
              String userID = authorisedUser[0];
              String role = authorisedUser[1];
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");

                //**the following functionalities should only be able to be used by Management**
                if (role.equals("Management")) {
                  System.out.println("1. View Flights");
                  System.out.println("2. View Flight Seats");
                  System.out.println("3. View Flight Status");
                  System.out.println("4. View Flights of the day");  
                  System.out.println("5. View Full Order ID History");
                  System.out.println(".........................");
                  System.out.println(".........................");
                  System.out.println("9. Log out");
                  switch (readChoice()){
                     case 1: feature1(esql); break;
                     case 2: feature2(esql); break;
                     case 3: feature3(esql); break;
                     case 4: feature4(esql); break;
                     case 5: feature5(esql); break;
                     case 6: feature6(esql); break;

                     case 9: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
                } else if (role.equals("Customer")) {
                //**the following functionalities should only be able to be used by customers**
                  System.out.println("1. Search Flights");
                  System.out.println("2. Get Flight Ticket Cost");
                  System.out.println("3. Get Your Flight Airplane Type");
                  System.out.println("4. Make Flight Reservation");
                  System.out.println("9. Log out");
                  switch (readChoice()){
                     case 1: FindFlightsOnDate(esql); break;
                     case 2: GetTicketCost(esql); break;
                     case 3: GetAirplaneType(esql); break;
                     case 4: MakeReservation(esql); break;

                     case 9: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
                } else if (role.equals("Pilot")) {
                  System.out.println("1. Send Maintenance Request");
                  System.out.println("9. Log out");
                  switch (readChoice()){
                     case 1: SubmitMaintenanceRequest(esql); break;

                     case 9: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
                } else if (role.equals("Maintenance")) {
                  System.out.println("1. Get Repair History");
                  System.out.println("2. Get Pilot Maintenance Requests");
                  System.out.println("3. Mark A Repair As Completed");
                  System.out.println("9. Log out");
                  switch (readChoice()){
                     case 1: GetRepairsForPlane(esql); break;
                     case 2: GetPilotRequests(esql); break;
                     case 3: LogRepair(esql); break;

                     case 9: usermenu = false; break;
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

   public static void feature1(AirlineManagement esql) {}
   public static void feature2(AirlineManagement esql) {}
   public static void feature3(AirlineManagement esql) {}
   public static void feature4(AirlineManagement esql) {}
   public static void feature5(AirlineManagement esql) {}
   public static void feature6(AirlineManagement esql) {}

   /*
    * Customer Features
    **/

// Return departure and arrival time, number of stops scheduled, and on-time record (as a percentage)
   public static void FindFlightsOnDate(AirlineManagement esql){
      try {
         System.out.print("\tEnter departure city: ");
         String depCity = in.readline();
         System.out.print("\tEnter arrival city: ");
         String arrCity = in.readline();
         System.out.print("\tEnter flight date (YYYY-MM-DD): ");
         String flightDate = in.readline();

         String query = String.format(
            "SELECT s.DepartureTime, s.ArrivalTime, fi.NumOfStops, " + 
            "ROUND(100.0 * SUM(CASE WHEN fi.DepartedOnTime AND fi.ArrivedOnTime THEN 1 ELSE 0 END) / COUNT(*), 2) AS OnTimePercentage " + 
            "FROM Flight f JOIN Schedule s ON f.FlightNumber = s.FlightNumber " + 
            "WHERE f.DepartureCity = '%s' AND f.ArrivalCity = '%s' AND fi.FlightDate = '%s' " + 
            "GROUP BY s.DepartureTime, s.ArrivalTime, fi.NumeOfStops", 
            depCity, arrCity, flightDate
         );

         int rowCount = esql.executeQuery(query);
         System.out.println("Total row(s): " + rowCount);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void GetTicketCost(AirlineManagement esql){
      try {
         System.out.print("\tEnter flight number: ");
         String flightNumber = in.readline();
         String query = 
            "Select TicketCost FROM FlightInstance WHERE FlightNumber = '" + flightNumber + "'";

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): " + rowCount);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void GetAirplaneType(AirlineManagement esql) {
      try {
         System.out.print("\tEnter flight number: ");
         String flightNumber = in.readline();

         String query =  "
         SELECT p.make, p.Model from Flight f 
         JOIN Plane p ON f.PlaneID = p.PlaneID 
         WHERE f.FlightNumber = '" + flightNumber + "'";

         int rowCount = esql.executeQueryAndPrintResult(query);

         System.out.println("Total row(s): " + rowCount);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   } 

   public static void MakeReservation(AirlineManagement esql) {
      try {
         System.out.print("\tEnter customer ID: ");
         System.out.print("\tEnter flight instance ID: ");
         String flightInstanceID in.readline();

         String checkSeats = "
         SELECT SeatsTotal, SeatsSold 
         FROM FlightInstance 
         WHERE FlightInstance = " + flightInstanceID;

         List<List<String>> result = esql.executeQueryAndReturnResults(checkSeats);

         int total = Integer.parseInt(result.get(0).get(0));
         int sold = Integer.parseInt(result.get(0).get(0));
         String status = sold < total ? "reserved" : "waitlisted";

         String reservationID = "R" + System.currentTimeMillis();
         String insert = String.format(
            "INSERT INTO Reservation (ReservationID, CustomerID, FlightInstanceID, Status) " + 
            "VALUES ('%s', %s, %s, '%s')",
            reservationID, customerID, flightInstanceID, status
         );

         esql.exectureUpdate(insert);
         System.out.println("Reservation " + status + " created.");
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   /*
    * Technician Features
    **/
   public static void GetRepairsForPlane(AirlineManagement esql) {
      try {
         System.out.print("\tEnter plane ID: ");
         String planeID = in.readLine();
         System.out.print("\tEnter repair start date (YYYY-MM-DD): ");
         String start = in.readLine();

         String query = String.format(
            "SELECT RepairDate, RepairCode
            FROM Repair
            WHERE PlaneID = '%s' AND RepairDate BETWEEN '%s' AND '%s'",
            planeID, start, end
         );
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): " + rowCount);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void GetPilotRequests(AirlineManagement esql) {
      try {
         System.out.print("\tEnter pilot ID: ");
         String pilotID = in.readLine();

         String query =
            "SELECT RepairDate, RepairCode, PlaneID 
            FROM MaintenanceRequest 
            WHERE PilotID = '" + pilotID + "'";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): " + rowCount);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void LogRepair(AirlineManagement esql) {
      try {
         System.out.print("\tEnter plane ID: ");
         String planeID = in.readLine();
         System.out.print("\tEnter repair code: ");
         String repairCode = in.readLine();
         System.out.print("\tEnter repair date (YYYY-MM-DD): ");
         String repairDate = in.readLine();
         System.out.print("\tEnter technician ID: ");
         String techID = in.readLine();

         String insert = String.format(
            "INSERT INTO Repair (RepairID, PlaneID, RepairCode, RepairDate, TechnicianID) " + 
            "VALUES (%s, '%s', '%s', '%s', '%s')",
            esql.getCurrSeqVal("Repair_seq"), planeID, repairCode, repairDate, techID
         );
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): " + rowCount);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   /*
    * Pilot Features
    **/
   public static void SubmitMaintenanceRequest(EmbeddedSSQL esql) {
      try {
         System.out.print("\tEnter plane ID: ");
         String planeID = in.readLine();
         System.out.print("\tEnter repair code: ");
         String repairCode = in.readLine();
         String requestDate = in.readLine();
         System.out.print("\tEnter pilot ID: ");
         String pilotID = in.readLine();

         LocalDate today = LocalDate.now();
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         String requestDate = today.format(formatter);

         String insert = String.format("INSERT INTO MaintenanceRequest (RequestID, PlaneID, RepairCode, RequestCode, RequestDate, PilotID) " + 
         "VALUES (%s, '%s', '%s', '%', '%s', '%s')",
         esql.getCurrSeqVal("Request_seq"), planeID, repairCode, requestDate, pilotID
         );

         esql.executeUpdate(insert);
         System.out.println("Maintenance request submitted.");
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }


}//end AirlineManagement

