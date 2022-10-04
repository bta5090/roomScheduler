
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 */
public class RoomQueries {
    private static Connection connection;   
    private PreparedStatement selectAllRoom;
    private  static PreparedStatement insertRoom;
    private PreparedStatement selectRoomBySeats;
    private PreparedStatement insertReservation;
    public String waitlistResString;
    public String dropRoomString;
    private WaitlistQueries WaitListQ;
    private PreparedStatement deleteRoom;
    private PreparedStatement deleteReservationByRoom;
    private PreparedStatement selectReservationByRoom;
    
    public RoomQueries()//constructor of this class
    {
        try
        {   
             WaitListQ= new WaitlistQueries();
            connection = DBConnection.getConnection();
            insertReservation= connection.prepareStatement("INSERT INTO RESERVATIONS (Faculty, Room, Date, Seats, Timestamp) VALUES(?, ?, ?, ?, ?)");
            deleteRoom= connection.prepareStatement("Delete from ROOMS where Name=?");
            deleteReservationByRoom= connection.prepareStatement("Delete from Reservations where Room=?");
            selectReservationByRoom=connection.prepareStatement("Select * From Reservations where Room=?");
            selectRoomBySeats = connection.prepareStatement("SELECT * FROM ROOMS ORDER BY SEATS DESC");
            selectAllRoom = connection.prepareStatement("SELECT * FROM ROOMS");
            insertRoom= connection.prepareStatement("INSERT INTO Rooms (NAME,SEATS) VALUES(?,?)");
            waitlistResString="";
            dropRoomString="";
        }
        catch(SQLException sqlException)
        {
            sqlException.printStackTrace();
        }      
    }

    public ArrayList<RoomEntry> getAllRooms(){
            ArrayList<RoomEntry> results= null;
            ResultSet resultSet= null;
            
            try{
                resultSet= selectRoomBySeats.executeQuery();
                results= new ArrayList<RoomEntry>();
                
                while(resultSet.next()){
                    results.add(new RoomEntry(
                        resultSet.getString("Name"),
                        resultSet.getInt("Seats")));}
            
            }
            catch(SQLException sqlException)
        {
            sqlException.printStackTrace();
        }      
        return results;}
    public void addRoom(String roomName,int seat){
        connection = DBConnection.getConnection();
        try{
            insertRoom= connection.prepareStatement("INSERT INTO Rooms (NAME,SEATS) VALUES(?,?)");

            insertRoom.setString(1,roomName);
            insertRoom.setInt(2, seat);
            insertRoom.executeUpdate();
            
            waitlistResString="";
            //cancel waitlist
            for(Date day:Dates.getAllDates()){
                System.out.println(day.toString());
                for(int j=0;j<WaitListQ.getWaitlistByTimestamp(day).size();j++){
                if(WaitListQ.getWaitlistByTimestamp(day).get(j).getSeats()<=seat){
               
                    
                    //String faculty_name=WaitListQ.getWaitlistByTimestamp(day).get(j).getFaculty();
                    Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
                    insertReservation.setString(1,WaitListQ.getWaitlistByTimestamp(day).get(j).getFaculty());
                    insertReservation.setString(2,roomName);
                    insertReservation.setDate(3, day);                    
                    insertReservation.setInt(4,WaitListQ.getWaitlistByTimestamp(day).get(j).getSeats());
                    insertReservation.setTimestamp(5, currentTimestamp);
                    insertReservation.executeUpdate();
                    waitlistResString+=String.format("%s reserves %s for %s\n",WaitListQ.getWaitlistByTimestamp(day).get(j).getFaculty(),roomName,day.toString());
                    WaitListQ.deletWaitlist(WaitListQ.getWaitlistByTimestamp(day).get(j).getFaculty(),day);
                }
            
        }}}
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }
    }
    public void dropRoom(String name){
        ArrayList<ReservationEntry> res_deleted= null;
        ResultSet resultSet= null;
        try{
            //set initial
            Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
            dropRoomString="";
            //delete room
            deleteRoom.setString(1,name);
            deleteRoom.executeUpdate();
            
            //select deleted information from reservations
            selectReservationByRoom.setString(1,name);
            resultSet=selectReservationByRoom.executeQuery();
            res_deleted=new ArrayList<ReservationEntry>();
            
            while(resultSet.next()){
                res_deleted.add(new ReservationEntry(
                resultSet.getString("Faculty"),
                resultSet.getString("Room"),
                resultSet.getDate("Date"),
                resultSet.getInt("Seats"),
                resultSet.getTimestamp("Timestamp")));
            }
        
            //delete reservation
            deleteReservationByRoom.setString(1, name);
            deleteReservationByRoom.executeUpdate();
                        
            //add to waitlist
            for(ReservationEntry resD:res_deleted){
            WaitListQ.addWaitlist(resD.getFaculty(), resD.getDate(), resD.getSeats(), currentTimestamp);
            dropRoomString+=String.format("%s in %s is moved to waitlist\n",resD.getFaculty(),resD.getDate().toString());
            }
            
        }catch(SQLException sqlException){
            sqlException.printStackTrace();
        }
    }
}
