
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
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
public class Dates {
    private Date date;
    private static Connection connection;
    private static PreparedStatement getDateList;
    public static ResultSet result;
    private static ArrayList<Date> alldates = new ArrayList<Date>();
    private static PreparedStatement insertDate;
    
    public Dates (Date date){
        setDate(date);
    }
    
    public void setDate (Date date) {
        this.date=date;}
    
    public Date getDate(){
        return date;}
    
    public static ArrayList<Date> getAllDates(){
        connection = DBConnection.getConnection();
        ArrayList<Date> allDates = new ArrayList<Date>();        
        try
        {
            getDateList = connection.prepareStatement("SELECT DATES FROM DATES ORDER BY DATES");
            result = getDateList.executeQuery();
            while(result.next()){
                allDates.add(result.getDate(1));
                
            }
        }
        catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return allDates;
    }
    public static void addDate(Date date){
     connection = DBConnection.getConnection();
        try{
            insertDate= connection.prepareStatement("INSERT INTO DATES (DATES) VALUES(?)");

            insertDate.setDate(1,date);
            insertDate.executeUpdate();
            
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }
    }
}
