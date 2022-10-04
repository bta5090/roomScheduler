
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 */
public class ReservationQueries {
    private static Connection connection;
    private PreparedStatement selectRoomByDate;
    private PreparedStatement insertReservation;
    private PreparedStatement selectReservationByDate;
    private PreparedStatement selectReservationByFaculty;
    private PreparedStatement deleteReservationByFacultyAndDate;
    private PreparedStatement selectRoomByFacultyAndDate;
    private PreparedStatement selectWaitlistByFaculty;
    private RoomQueries RoomQ;
    private boolean canADD;
    private ArrayList<String> reservedRoom;
    private WaitlistQueries WaitListQ;
    public String ResStatus;
    public String moveWaitlistString;
    public String displayfacultyString;
    //private PreparedStatement selectAllRes;
    
    public ReservationQueries(){
    try{
        connection = DBConnection.getConnection();
        insertReservation= connection.prepareStatement("INSERT INTO RESERVATIONS (Faculty, Room, Date, Seats, Timestamp) VALUES(?, ?, ?, ?, ?)");
        selectRoomByDate= connection.prepareStatement("SELECT ROOM FROM RESERVATIONS WHERE DATE=?");
        selectReservationByDate= connection.prepareStatement("SELECT * FROM RESERVATIONS WHERE DATE=?");
        selectReservationByFaculty= connection.prepareStatement("SELECT * FROM RESERVATIONS WHERE FACULTY=?");
        selectWaitlistByFaculty= connection.prepareStatement("SELECT * FROM Waitlist WHERE FACULTY=?");
        deleteReservationByFacultyAndDate= connection.prepareStatement("Delete from RESERVATIONS where faculty=? AND DATE=?");
        selectRoomByFacultyAndDate= connection.prepareStatement("SELECT * from RESERVATIONS where faculty=? AND DATE=?");
        canADD = true;
        ResStatus="";
        moveWaitlistString="";
        displayfacultyString="";
        reservedRoom = new ArrayList<String>();
        RoomQ= new RoomQueries();
        WaitListQ= new WaitlistQueries();

       
    }
    catch(SQLException sqlException)
        {
            sqlException.printStackTrace();
        }      
    }
    public void addReservation(String faculty, Date date, int seats){
        RoomEntry correct_room_place;
        correct_room_place = checkRoomtoPlace(seats);
        try{
            checkReserved(date,correct_room_place.getName());
        }catch(Exception e){
            canADD=false;
        }
        
        Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
        if(canADD){  //if the reservation can be added, added directly
            ResStatus = String.format("%s reserves Room %s",faculty,correct_room_place.getName());

            try
                {
                    insertReservation.setString(1,faculty);
                    insertReservation.setString(2,correct_room_place.getName());
                    insertReservation.setDate(3, date);                    
                    insertReservation.setInt(4,seats);
                    insertReservation.setTimestamp(5, currentTimestamp);
                    insertReservation.executeUpdate();
                }

            catch(SQLException sqlException)
                {
                    sqlException.printStackTrace();
                }
        }
        else{ //if the reservation cannot be added, add to waitlist
            ResStatus = String.format("%s gets waitlisted.",faculty);
            WaitListQ.addWaitlist(faculty, date, seats, currentTimestamp);
            canADD=true; //set back to initial condition
        }
        }
    public ArrayList<ReservationEntry> getReservationByDate(Date date){
        ArrayList<ReservationEntry> results=null;
        ResultSet resultSet= null;
        try{
            selectReservationByDate.setDate(1,date);
            resultSet= selectReservationByDate.executeQuery();
            results= new ArrayList<ReservationEntry>();
            
            while(resultSet.next()){
                results.add(new ReservationEntry(
                resultSet.getString("Faculty"),
                resultSet.getString("Room"),
                resultSet.getDate("Date"),
                resultSet.getInt("Seats"),
                resultSet.getTimestamp("Timestamp")));}
        }
        catch(SQLException sqlException)
     {
         sqlException.printStackTrace();
     }      
    return results;
    }
    
    public ArrayList<String> getRoomsReservedByDate(Date date){
        ArrayList<String> results=null;
        ResultSet resultSet= null;
        
        try{
            selectRoomByDate.setDate(1,date);
            resultSet= selectRoomByDate.executeQuery();
            results= new ArrayList<String>();
            
            while(resultSet.next())
                {
                    results.add(resultSet.getString(1));
                }
        }
        catch(SQLException sqlException)
     {
         sqlException.printStackTrace();
     }      
    return results;
    }
    
    public String deleteReservation(String name, Date date){
        ArrayList<String> rooms_deleted= null;
        ResultSet resultSet= null;
           try{
            // add deleted room to the arraylist
            selectRoomByFacultyAndDate.setString(1, name);
            selectRoomByFacultyAndDate.setDate(2, date);
            resultSet=selectRoomByFacultyAndDate.executeQuery();
            rooms_deleted = new ArrayList<String>();
            while(resultSet.next())
                {
                    rooms_deleted.add(resultSet.getString("Room"));
                }
            for(String rd:rooms_deleted){
                System.out.println(rd);
                
            }
            //delete reservation
            deleteReservationByFacultyAndDate.setString(1,name);
            deleteReservationByFacultyAndDate.setDate(2,date);
            deleteReservationByFacultyAndDate.executeUpdate();
            moveWaitlistString="";
            if(rooms_deleted.isEmpty()){
                return "none";
            }      
            int room_seats = 0;
            for(int i=0;i<RoomQ.getAllRooms().size();i++){
                if (rooms_deleted.get(0).equals(RoomQ.getAllRooms().get(i).getName())){
                    room_seats=RoomQ.getAllRooms().get(i).getSeats();
                    break;}                    
            }
            //check which to place into reservation
            for(int j=0;j<WaitListQ.getWaitlistByTimestamp(date).size();j++){
                if(WaitListQ.getWaitlistByTimestamp(date).get(j).getSeats()<=room_seats){
                    //place
                    
                    String faculty_name=WaitListQ.getWaitlistByTimestamp(date).get(j).getFaculty();
                    Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
                    insertReservation.setString(1,WaitListQ.getWaitlistByTimestamp(date).get(j).getFaculty());
                    insertReservation.setString(2,rooms_deleted.get(0));
                    insertReservation.setDate(3, date);                    
                    insertReservation.setInt(4,WaitListQ.getWaitlistByTimestamp(date).get(j).getSeats());
                    insertReservation.setTimestamp(5, currentTimestamp);
                    insertReservation.executeUpdate();
                    moveWaitlistString=String.format("%s gets Room %s reserved.",WaitListQ.getWaitlistByTimestamp(date).get(j).getFaculty(),rooms_deleted.get(0));
                    return faculty_name;
                }
            }
        }
        catch(SQLException sqlException){
           sqlException.printStackTrace();
        }
        return null;
    }
    
    

    public RoomEntry checkRoomtoPlace(int seats){
        int temp= -1;
        try{
        for(int i=0;i<RoomQ.getAllRooms().size();i++){    //find which room should be place for faculty
            if(RoomQ.getAllRooms().get(i).getSeats()>=seats){
                temp=i;}
            }
        return RoomQ.getAllRooms().get(temp);}
            catch(Exception e){              
                return null;
            }
    }
    
    public String displayReservation(){
        String res_message="";
        ArrayList<ReservationEntry> resEntry= new ArrayList<ReservationEntry>();
        for(Date day:Dates.getAllDates()){
            res_message+="\n\n"+day+"\n";
            resEntry=getReservationByDate(day);
            for(int i=0;i<resEntry.size();i++){
                String faculty=resEntry.get(i).getFaculty();
                String room=resEntry.get(i).getRoom();
                res_message+=faculty+ " reserved "+room+"\n";
            }
        }
        
    return res_message;}
    
    public void checkReserved(Date date,String room){ //check is the room been occupied

        reservedRoom=getRoomsReservedByDate(date);
        if(reservedRoom.contains(room)){
            canADD=false;}
        }

        
    public String getWaitlist_list(){
        
        return WaitListQ.displayWaitList();
                }
    
    public void checkDelete(String name,Date date ){
        //check reservation
        String faculty_name=deleteReservation(name,date);
        
        //cancel waitlist directly
        if("none".equals(faculty_name)){
            moveWaitlistString=String.format("%s gets cancel from waitlist",name);
            WaitListQ.deletWaitlist(name,date);
            return;           
        }
        //cancel from waitlist
        WaitListQ.deletWaitlist(faculty_name,date);
        }
    public void displayResByfaculty(String faculty){
        displayfacultyString="";
        ArrayList<ReservationEntry> res_results= null;
        ResultSet resultSet= null;
        ArrayList<WaitlistEntry> waitL_results= null;
        ResultSet resultS= null;
        try{
            //get reservation
            selectReservationByFaculty.setString(1,faculty);
            resultSet=selectReservationByFaculty.executeQuery();
            res_results=new ArrayList<ReservationEntry>();
            
            while(resultSet.next()){
                res_results.add(new ReservationEntry(
                resultSet.getString("Faculty"),
                resultSet.getString("Room"),
                resultSet.getDate("Date"),
                resultSet.getInt("Seats"),
                resultSet.getTimestamp("Timestamp")));
            }
            //get waitlist
            selectWaitlistByFaculty.setString(1,faculty);
            resultS=selectWaitlistByFaculty.executeQuery();
            waitL_results=new ArrayList<WaitlistEntry>();
            while(resultS.next()){
                waitL_results.add(new WaitlistEntry(
                resultS.getString("Faculty"),
                resultS.getDate("Date"),
                resultS.getInt("Seats"),
                resultS.getTimestamp("Timestamp")));
            }
        
            
            //set string
            displayfacultyString=faculty+"\n";
            for(ReservationEntry result:res_results){
                displayfacultyString+=String.format("has room %s on %s\n",result.getRoom(),result.getDate().toString());
            }
            for(WaitlistEntry w_result:waitL_results){
                displayfacultyString+=String.format("waitlisted on %s\n",w_result.getDate().toString());
            }
            
        }catch(Exception e){              
                
            }
    }
    }
    

    