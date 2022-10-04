
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 */
public class WaitlistQueries {
    private  PreparedStatement insertWaitlist;
    private static Connection connection;
    private  PreparedStatement selectWaitlistByDate;
    private PreparedStatement selectWaitlistByTimestamp;
    private PreparedStatement deleteWaitlistByFacultyAndDate;
    
    public WaitlistQueries(){
        try{
            connection = DBConnection.getConnection();
            selectWaitlistByDate= connection.prepareStatement("SELECT * FROM WAITLIST WHERE DATE=?");
            insertWaitlist= connection.prepareStatement("INSERT INTO WAITLIST (Faculty,Date, Seats, Timestamp) VALUES(?, ?, ?, ?)");
            selectWaitlistByTimestamp= connection.prepareStatement("SELECT * FROM WAITLIST WHERE DATE=? ORDER BY Timestamp");
            deleteWaitlistByFacultyAndDate= connection.prepareStatement("Delete from WAITLIST where faculty=? AND DATE=?");

}
        catch(SQLException sqlException){
                sqlException.printStackTrace();
            }      
}
    

    public void addWaitlist(String faculty,Date date,int seats,Timestamp timestamp){
        Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
        try{
            insertWaitlist.setString(1,faculty);
            insertWaitlist.setDate(2, date);                    
            insertWaitlist.setInt(3,seats);
            insertWaitlist.setTimestamp(4, currentTimestamp);
            insertWaitlist.executeUpdate();
            
    }catch(SQLException sqlException){
                sqlException.printStackTrace();
            }      
    }
    
    public  ArrayList<WaitlistEntry> getWaitlistByDate(Date date){
        ArrayList<WaitlistEntry> results=null;
        ResultSet resultSet= null;
        
        try{
            selectWaitlistByDate.setDate(1,date);
            resultSet= selectWaitlistByDate.executeQuery();
            results= new ArrayList<WaitlistEntry>();
            
            while(resultSet.next()){
                results.add(new WaitlistEntry(
                resultSet.getString("Faculty"),
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
        
    public String displayWaitList(){
        String waitlist_message="";
        ArrayList<WaitlistEntry> waitlistEntries= new ArrayList<WaitlistEntry>();
        for(Date day:Dates.getAllDates()){
            waitlist_message+="\n\n"+day+"\n";
            waitlistEntries=getWaitlistByDate(day);
            for(int i=0;i<waitlistEntries.size();i++){
                String faculty=waitlistEntries.get(i).getFaculty();
                Date date=waitlistEntries.get(i).getDate();
                int seats=waitlistEntries.get(i).getSeats();
                waitlist_message+=faculty+ " waits "+date+ " for "+seats+" seats\n";
            }
        }
    return waitlist_message;    
    }
    public void deletWaitlist(String faculty, Date date){
        try{
            deleteWaitlistByFacultyAndDate.setString(1,faculty);
            deleteWaitlistByFacultyAndDate.setDate(2,date);
            deleteWaitlistByFacultyAndDate.executeUpdate();
        }catch(SQLException sqlException){
         sqlException.printStackTrace();
     }      
    }
    public ArrayList<WaitlistEntry> getWaitlistByTimestamp(Date date){
        ArrayList<WaitlistEntry> results=null;
        ResultSet resultSet= null;
        
        try{
            selectWaitlistByTimestamp.setDate(1,date);
            resultSet= selectWaitlistByTimestamp.executeQuery();
            results= new ArrayList<WaitlistEntry>();
            
            while(resultSet.next()){
                results.add(new WaitlistEntry(
                resultSet.getString("Faculty"),
                resultSet.getDate("Date"),
                resultSet.getInt("Seats"),
                resultSet.getTimestamp("Timestamp")));}
        }
        catch(SQLException sqlException){
         sqlException.printStackTrace();
     }      
    return results;
    }
    

} 

