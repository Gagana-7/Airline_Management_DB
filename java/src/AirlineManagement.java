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
            System.out.println("\nMAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("0. < EXIT");
            String[] authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 0: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              String userId = authorisedUser[0];
              String role = authorisedUser[1];
              String roleId = authorisedUser[2];

              System.out.println(roleId);
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("\nMAIN MENU");
                System.out.println("---------");

                //**the following functionalities should only be able to be used by Management**
                if (role.equals("Management")) {
                  System.out.println("1. View Flight Schedule");
                  System.out.println("2. View Flight Seats");
                  System.out.println("3. View Flight Status");
                  System.out.println("4. View Flights of the day");  
                  System.out.println("5. View Passengers of Flight");
                  System.out.println("6. View Passenger Information");
                  System.out.println("7. View Plane Information");
                  System.out.println("8. View Repairs of Worker");
                  System.out.println("9. View Repairs of Dates");
                  System.out.println("10. View Flight Statistics");
                  System.out.println("0. Log out");
                  switch (readChoice()){
                     case 1: ScheduleFromFlightNum(esql); break;
                     case 2: GetSeats(esql); break;
                     case 3: OnTime(esql); break;
                     case 4: FlightsOfTheDay(esql); break;
                     case 5: ListPassangers(esql); break;
                     case 6: PassengerInfo(esql); break;
                     case 7: FindPlaneInfo(esql); break;
                     case 8: RepairsOfPerson(esql); break;
                     case 9: RepairsOnDates(esql); break;
                     case 10: ShowFlightStats(esql); break;

                     case 0: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
                } else if (role.equals("Customer")) {
                //**the following functionalities should only be able to be used by customers**
                  System.out.println("1. Search Flights");
                  System.out.println("2. Get Flight Ticket Cost");
                  System.out.println("3. Get Your Flight Airplane Type");
                  System.out.println("4. Make Flight Reservation");
                  System.out.println("0. Log out");
                  switch (readChoice()){
                     case 1: FindFlightsOnDate(esql); break;
                     case 2: GetTicketCost(esql); break;
                     case 3: GetAirplaneType(esql); break;
                     case 4: MakeReservation(esql, Integer.parseInt(roleId)); break;

                     case 0: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
                } else if (role.equals("Pilot")) {
                  System.out.println("1. Send Maintenance Request");
                  System.out.println("0. Log out");
                  switch (readChoice()){
                     case 1: SubmitMaintenanceRequest(esql, roleId); break;

                     case 0: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
                } else if (role.equals("Technician")) {
                  System.out.println("1. Get Repair History");
                  System.out.println("2. Get Pilot Maintenance Requests");
                  System.out.println("3. Mark A Repair As Completed");
                  System.out.println("0. Log out");
                  switch (readChoice()){
                     case 1: GetRepairsForPlane(esql); break;
                     case 2: GetPilotRequests(esql); break;
                     case 3: LogRepair(esql, roleId); break;

                     case 0: usermenu = false; break;
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
      try {
         System.out.print("Enter username: ");
         String username = in.readLine();

         String checkQuery = String.format("SELECT * FROM Users WHERE username = '%s'", username);
         List<List<String>> check = esql.executeQueryAndReturnResult(checkQuery);
         if (!check.isEmpty()) {
            System.out.println("Username already exists. Please try a different one.");
            return;
         }

         System.out.print("Enter password: ");
         String password = in.readLine();

         System.out.print("Enter role (Customer, Technician, Pilot, Management): ");
         String role = in.readLine();

         // Validate role
         while (!role.equalsIgnoreCase("Management") &&
               !role.equalsIgnoreCase("Customer") &&
               !role.equalsIgnoreCase("Pilot") &&
               !role.equalsIgnoreCase("Technician")) {
            System.out.print("Invalid role. Please enter a valid role (Management, Customer, Pilot, Technician): ");
            role = in.readLine();
         }

         String roleID = "";

         switch (role.toLowerCase()) {
            case "technician":
               String tPrefix = "T";
               String tQuery = "SELECT COUNT(*) FROM Technician";
               List<List<String>> tResult = esql.executeQueryAndReturnResult(tQuery);
               int tCount = Integer.parseInt(tResult.get(0).get(0)) + 1;
               roleID = String.format("%s%03d", tPrefix, tCount);

               System.out.print("Enter technician name: ");
               String tName = in.readLine();

               String insertTech = String.format(
                  "INSERT INTO Technician (TechnicianID, Name) VALUES ('%s', '%s')",
                  roleID, tName
               );
               esql.executeUpdate(insertTech);
               break;

            case "pilot":
               String pPrefix = "P";
               String pQuery = "SELECT COUNT(*) FROM Pilot";
               List<List<String>> pResult = esql.executeQueryAndReturnResult(pQuery);
               int pCount = Integer.parseInt(pResult.get(0).get(0)) + 1;
               roleID = String.format("%s%03d", pPrefix, pCount);

               System.out.print("Enter pilot name: ");
               String pName = in.readLine();

               String insertPilot = String.format(
                  "INSERT INTO Pilot (PilotID, Name) VALUES ('%s', '%s')",
                  roleID, pName
               );
               esql.executeUpdate(insertPilot);
               break;

            case "customer":
               String cQuery = "SELECT COUNT(*) FROM Customer";
               List<List<String>> cResult = esql.executeQueryAndReturnResult(cQuery);
               int cCount = Integer.parseInt(cResult.get(0).get(0)) + 1;
               roleID = Integer.toString(cCount);

               System.out.print("Enter first name: ");
               String fName = in.readLine();
               System.out.print("Enter last name: ");
               String lName = in.readLine();
               System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
               String dob = in.readLine();
               System.out.print("Enter Gender (M/F): ");
               String gender = in.readLine();
               System.out.print("Enter Address: ");
               String address = in.readLine();
               System.out.print("Enter Phone Number (###-###-####): ");
               String phone = in.readLine();
               System.out.print("Enter ZIP code: ");
               String zip = in.readLine();

               String insertCustomer = String.format(
                  "INSERT INTO Customer (CustomerID, FirstName, LastName, Gender, DOB, Address, Phone, Zip) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                  Integer.parseInt(roleID), fName, lName, gender, dob, address, phone, zip
               );
               esql.executeUpdate(insertCustomer);
               break;

            case "management":
               roleID = null; // no role-specific ID
               break;
         }

         String insertUser = String.format(
            "INSERT INTO Users (username, password, role, role_id) VALUES ('%s', '%s', '%s', '%s')",
            username, password, role.substring(0, 1).toUpperCase() + role.substring(1), roleID
         );
         esql.executeUpdate(insertUser);

         System.out.println("\nUser created successfully!");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }



   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String[] LogIn(AirlineManagement esql){
   try {
      System.out.print("Enter username: ");
      String username = in.readLine();

      System.out.print("Enter password: ");
      String password = in.readLine();

      String query = String.format(
         "SELECT userID, role, role_id FROM Users WHERE username = '%s' AND password = '%s';",
         username, password
      );

      List<List<String>> result = esql.executeQueryAndReturnResult(query);

      if (result.size() > 0) {
         System.out.println("\nLogin successful! Welcome, " + username);
         String userID = result.get(0).get(0);
         String role = result.get(0).get(1);
         String roleID = result.get(0).get(2);
         return new String[]{userID, role, roleID}; // Includes prefix ID like T001, C003
      } else {
         System.out.println("\nInvalid username or password.");
         return null;
      }
   } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
   }
}//end

// Rest of the functions definition go in here

    /*
    * Management Features
    **/


   public static void ScheduleFromFlightNum(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String flNum = in.readLine();

         String query = String.format(
            "SELECT DayOfWeek, DepartureTime, ArrivalTime FROM schedule WHERE FlightNumber = '%s';",
            flNum
         );

         List<List<String>> results = timeAndExecuteQuery(esql, query);

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
         System.out.print("\tEnter Flight: ");
         String flName = in.readLine();
         System.out.print("\tEnter Flight Date(YYYY-MM-DD): ");
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
               List<List<String>> results = timeAndExecuteQuery(esql, query);
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
                  List<List<String>> results = timeAndExecuteQuery(esql, query);
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
         System.out.print("\tEnter Flight: ");
         String flName = in.readLine();
         System.out.print("\tEnter Flight Date(YYYY-MM-DD): ");
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
               List<List<String>> results = timeAndExecuteQuery(esql, query);
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
               List<List<String>> results = timeAndExecuteQuery(esql, query);
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
         System.out.print("\tEnter Flight Date(YYYY-MM-DD): ");
         String dateInput = in.readLine().trim(); //get string of date
         LocalDate localDate = LocalDate.parse(dateInput); //parse string to LocalDate
         Date sqlDate = Date.valueOf(localDate); //convert to java.sql.Date

         String query = String.format(
            "SELECT FlightNumber FROM FlightInstance WHERE FlightDate = '%s';", 
            sqlDate
            );
         List<List<String>> results = timeAndExecuteQuery(esql, query);
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

   public static void ListPassangers(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight: ");
         String flName = in.readLine();
         System.out.print("\tEnter Flight Date(YYYY-MM-DD): ");
         String dateInput = in.readLine().trim(); //get string of date
         LocalDate localDate = LocalDate.parse(dateInput); //parse string to LocalDate
         Date sqlDate = Date.valueOf(localDate); //convert to java.sql.Date

         System.out.println("Would you like to view: ");
         System.out.println("(1) Passangers with reservations");
         System.out.println("(2) Passangers on waitlist");
         System.out.println("(3) Passangers who flew on the flight(if completed)");

         String status;
         switch(readChoice()) {
            case 1: status = "reserved"; break;
            case 2: status = "waitlist"; break;
            case 3: status = "flown"; break;
            default:
               System.out.println("Invalid choice.");
               return;
         }

         String query = String.format(
            "SELECT c.FirstName, c.LastName " + 
            "FROM Reservation r " +
            "JOIN Customer c ON r.CustomerID = c.CustomerID " +
            "JOIN FlightInstance fi ON r.FlightInstanceID = fi.FlightInstanceID " +
            "JOIN Flight f ON fi.FlightNumber = f.FlightNumber " +
            "WHERE f.FlightNumber = '%s' AND fi.FlightDate = '%s' AND r.Status = '%s';",
            flName, sqlDate, status
         );
         List<List<String>> results = timeAndExecuteQuery(esql, query);
         if (results.isEmpty()) {
            System.out.println("No passanger found with status: " + status);
         } else {
            for (List<String> row : results) {
               System.out.println("First Name | Last Name");
               System.out.println(String.join(" | ", row));
            }
         }

      } catch (Exception e) {
         System.err.println("Error is ListPassanger: " + e.getMessage());
      }
   }

   public static void PassengerInfo(AirlineManagement esql) {
      try {
         System.out.print("\tEnter reservation number: ");
         String resID = in.readLine();

         String query = String.format(
            "SELECT c.FirstName, c.LastName, c.Gender, c.DOB, c.Address, c.Phone, c.Zip " + 
            "FROM Reservation r " + 
            "JOIN Customer c ON r.CustomerID = c.CustomerID " + 
            "WHERE r.ReservationID = '%s';",
            resID
         );

         List<List<String>> results = timeAndExecuteQuery(esql, query);
         if (results.isEmpty()) {
            System.out.println("No Passanger with reservation: " + resID);
         } else {
            System.out.println("First Name | Last Name | Gender | DOB | Address | Phone | Zip");
            for (List<String> row : results) {
               System.out.println(String.join(" | ", row));
            }
         }

      } catch (Exception e) {
         System.err.println("Error in PassengerInfo: " + e.getMessage());
      }
   }

   public static void FindPlaneInfo(AirlineManagement esql) {
      try {
         System.out.print("\tEnter plane number: ");
         String pNum = in.readLine();

         String query = String.format(
            "SELECT p.Make, p.Model, DATE_PART('year', AGE(CURRENT_DATE, MAKE_DATE(p.Year, 1, 1))) AS Age, p.LastRepairDate " +
            "FROM Plane p " +
            "WHERE p.PlaneID = '%s';",
            pNum
         );

         List<List<String>> results = timeAndExecuteQuery(esql, query);
         if (results.isEmpty()) {
            System.out.println("No Plane: " + pNum);
         } else {
            System.out.println("Make | Model | Age | LastRepairDate ");
            for (List<String> row : results) {
               System.out.println(String.join(" | ", row));
            }
         }

      } catch (Exception e) {
         System.err.println("Error in FindPlaneInfo: " + e.getMessage());
      }
   }

   public static void RepairsOfPerson(AirlineManagement esql) {
      try {
         System.out.print("\tEnter technician ID: ");
         String techID = in.readLine();

         String query = String.format(
            "SELECT r.RepairID, r.PlaneID, r.RepairCode, r.RepairDate " +
            "FROM Repair r " + 
            "WHERE TechnicianID = '%s';", 
            techID
         );

         List<List<String>> results = timeAndExecuteQuery(esql, query);
         if (results.isEmpty()) {
            System.out.println("No repair by technician: " + techID);
         } else {
            System.out.println("RepairID | PlaneID | RepairCode | RepairDate ");
            for (List<String> row : results) {
               System.out.println(String.join(" | ", row));
            }
         }

      } catch (Exception e) {
         System.err.println("Error in RepairsOfPerson: " + e.getMessage());
      }
   }

   public static void RepairsOnDates(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Plane ID: ");
         String pID = in.readLine();
         System.out.print("\tEnter start date (YYYY-MM-DD): ");
         String start = in.readLine().trim();
         System.out.print("\tEnter end date (YYYY-MM-DD): ");
         String end = in.readLine().trim();

         String query = String.format(
            "SELECT r.RepairDate, r.RepairCode " +
            "FROM Repair r " + 
            "WHERE PlaneID = '%s' AND RepairDate BETWEEN '%s' AND '%s';",
            pID, start, end
            
         );

         List<List<String>> results = timeAndExecuteQuery(esql, query);
         if (results.isEmpty()) {
            System.out.println("No repairs found for Plane ID: " + pID + " in the given date range.");
         } else {
            System.out.println("RepairDate | RepairCode");
            for (List<String> row : results) {
               System.out.println(String.join(" | ", row));
            }
         }

      } catch (Exception e) {
         System.err.println("Error in RepairsOnDate: " + e.getMessage());
      }
   }

   public static void ShowFlightStats(AirlineManagement esql) {
      try {
         System.out.print("\tEnter flight: ");
         String flNum = in.readLine();
         System.out.print("\tEnter start date(YYYY-MM-DD): ");
         String start = in.readLine().trim();
         System.out.print("\tEnter end date (YYYY-MM-DD): ");
         String end = in.readLine().trim();

         String query = String.format(
            "SELECT " + 
            "  COUNT(CASE WHEN DepartedOnTime THEN 1 END) AS NumDeparted, " + 
            "  COUNT(CASE WHEN ArrivedOnTime THEN 1 END) AS NumArrived, " +
            "  SUM(SeatsSold) AS TotalSold, " +
            "  SUM(SeatsTotal - SeatsSold) AS TotalUnsold " +
            "FROM FlightInstance " +
            "WHERE FlightNumber = '%s' AND FlightDate BETWEEN '%s' AND '%s';", 
            flNum, start, end
         );

         List<List<String>> results = timeAndExecuteQuery(esql, query);
         if (results.isEmpty()) {
            System.out.println("No flight statistics found.");
         } else {
            System.out.println("Departed | Arrived | Sold | Unsold");
            for (List<String> row : results) {
               System.out.println(String.join(" | ", row));
            }
         }

      } catch (Exception e) {
         System.err.println("Error in ShowFlightStats: " + e.getMessage());
      }
   }

   /*
    * Customer Features
    **/

// Return departure and arrival time, number of stops scheduled, and on-time record (as a percentage)
   public static void FindFlightsOnDate(AirlineManagement esql){
      try {
         System.out.print("\tEnter departure city: ");
         String depCity = in.readLine();
         System.out.print("\tEnter arrival city: ");
         String arrCity = in.readLine();
         System.out.print("\tEnter flight date (YYYY-MM-DD): ");
         String flightDate = in.readLine();

      String query = String.format(
         "SELECT s.DepartureTime, s.ArrivalTime, fi.NumOfStops, " +
         "AVG(CASE " +
         "WHEN fi.DepartedOnTime AND fi.ArrivedOnTime THEN 100 " +
         "WHEN fi.DepartedOnTime OR fi.ArrivedOnTime THEN 50 " +
         "ELSE 0 END) AS OnTimePercentage " +
         "FROM Flight f " +
         "JOIN Schedule s ON f.FlightNumber = s.FlightNumber " +
         "JOIN FlightInstance fi ON fi.FlightNumber = f.FlightNumber " +
         "WHERE f.DepartureCity = '%s' " +
         "AND f.ArrivalCity = '%s' " +
         "AND fi.FlightDate = '%s' " +
         "GROUP BY s.DepartureTime, s.ArrivalTime, fi.NumOfStops",
         depCity, arrCity, flightDate
      );

         System.out.println("\n");

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): " + rowCount);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void GetTicketCost(AirlineManagement esql){
      try {
         System.out.print("\tEnter flight number: ");
         String flightNumber = in.readLine();
         String query = 
            "Select TicketCost, FlightDate FROM FlightInstance WHERE FlightNumber = '" + flightNumber + "'";

         System.out.println("\n");

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): " + rowCount);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void GetAirplaneType(AirlineManagement esql) {
      try {
         System.out.print("\tEnter flight number: ");
         String flightNumber = in.readLine();

         String query =  
         "SELECT p.make, p.Model from Flight f " + 
         "JOIN Plane p ON f.PlaneID = p.PlaneID " +
         "WHERE f.FlightNumber = '" + flightNumber + "'";

         System.out.println("\n");

         esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   } 

   public static void MakeReservation(AirlineManagement esql, int customerID) {
      try {
         System.out.print("\tEnter flight instance ID: ");
         String flightInstanceID = in.readLine();

         String checkSeats = 
         "SELECT SeatsTotal, SeatsSold " +
         "FROM FlightInstance " +
         "WHERE FlightInstanceID = " + flightInstanceID;

         List<List<String>> result = esql.executeQueryAndReturnResult(checkSeats);

         int total = Integer.parseInt(result.get(0).get(0));
         int sold = Integer.parseInt(result.get(0).get(1));
         String status = sold < total ? "reserved" : "waitlist";

         String countQuery = "SELECT COUNT(*) FROM Reservation";
         List<List<String>> countResult = esql.executeQueryAndReturnResult(countQuery);
         int count = Integer.parseInt(countResult.get(0).get(0));

         String parsedCount = "";
         if (count < 10) {
            parsedCount = "000" + (count + 1);
         } else if (count < 100) {
            parsedCount = "00" + (count + 1);
         } else if (count < 1000) {
            parsedCount = "0" + (count + 1);
         } else {
            parsedCount = Integer.toString(count + 1);
         }
         String reservationID = "R" + (parsedCount);

         String insert = String.format(
            "INSERT INTO Reservation (ReservationID, CustomerID, FlightInstanceID, Status) " + 
            "VALUES ('%s', %s, %s, '%s')",
            reservationID, customerID, flightInstanceID, status
         );

         esql.executeUpdate(insert);

         if (status.equals("waitlist")) {
            System.out.println("\nFlight " + flightInstanceID + " is full. You are now on the waitlist for this flight.");
         } else if (status.equals("reserved")) {
            System.out.println("\nReservation successfully created for Flight " + flightInstanceID + ".");
         }

         System.out.println("Confirmation Number: " + reservationID);
      } catch(Exception e) {
         System.out.println("Failed to create reservation");
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
         System.out.print("\tEnter start date (YYYY-MM-DD): ");
         String start = in.readLine();
         System.out.print("\tEnter end date (YYYY-MM-DD): ");
         String end = in.readLine();

         String query = String.format(
            "SELECT RepairDate, RepairCode " +
            "FROM Repair " +
            "WHERE PlaneID = '%s' AND RepairDate BETWEEN '%s' AND '%s'",
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
            "SELECT RequestDate, RepairCode, PlaneID " +
            "FROM MaintenanceRequest " + 
            "WHERE PilotID = '" + pilotID + "'";

         System.out.println("\n");

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): " + rowCount);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void LogRepair(AirlineManagement esql, String techID) {
      try {
         System.out.print("\tEnter plane ID: ");
         String planeID = in.readLine();
         System.out.print("\tEnter repair code: ");
         String repairCode = in.readLine();

         LocalDate today = LocalDate.now();
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         String repairDate = today.format(formatter);

         String countQuery = "SELECT COUNT(*) FROM Repair";
         List<List<String>> countResult = esql.executeQueryAndReturnResult(countQuery);
         int repairID = Integer.parseInt(countResult.get(0).get(0)) + 1;

         String insert = String.format(
            "INSERT INTO Repair (RepairID, PlaneID, RepairCode, RepairDate, TechnicianID) " + 
            "VALUES (%s, '%s', '%s', '%s', '%s')",
            repairID, planeID, repairCode, repairDate, techID
         );
         esql.executeUpdate(insert);

         System.out.println("\n");

         System.out.println("\nMaintenance on " + planeID + " with Repair Code: " + repairCode + " marked as completed." );
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   /*
    * Pilot Features
    **/
   public static void SubmitMaintenanceRequest(AirlineManagement esql, String pilotID) {
      try {
         System.out.print("\tEnter plane ID: ");
         String planeID = in.readLine();
         System.out.print("\tEnter repair code: ");
         String repairCode = in.readLine();

         LocalDate today = LocalDate.now();
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         String requestDate = today.format(formatter);

         String countQuery = "SELECT COUNT(*) FROM MaintenanceRequest";
         List<List<String>> countResult = esql.executeQueryAndReturnResult(countQuery);
         int requestID = Integer.parseInt(countResult.get(0).get(0)) + 1;

         String insert = String.format("INSERT INTO MaintenanceRequest (RequestID, PlaneID, RepairCode, RequestDate, PilotID) " + 
         "VALUES (%s, '%s', '%s', '%s', '%s')",
         requestID, planeID, repairCode, requestDate, pilotID
         );

         esql.executeUpdate(insert);
         System.out.println("\nMaintenance Request for " + planeID + " with Repair Code: " + repairCode + " has been submitted.");
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static List<List<String>> timeAndExecuteQuery(AirlineManagement esql, String query) throws Exception {
      long start = System.nanoTime();
      List<List<String>> results = esql.executeQueryAndReturnResult(query);
      long end = System.nanoTime();
      long ms = (end - start) / 1_000_000;
      System.out.println("Query executed in " + ms + " ms.");
      return results;
  }


}//end AirlineManagement

