
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 */
public class ReservationEntry {
    private static Connection connection;
    private String faculty;
    private String room;
    private Date date;
    private int seats;
    private Timestamp timestamp;

    
    public ReservationEntry(String faculty, String room, 
            Date date, int seats, Timestamp timestamp){
        setFaculty(faculty);
        setRoom(room);
        setSeats(seats);
        setDate(date);
        setTimestamp(timestamp);     
        }

    public void setFaculty(String faculty){
        this.faculty=faculty;}
    public String getFaculty(){
        return faculty;}
    
    public void setRoom(String room){
        this.room=room;}
    public String getRoom(){
        return room;}
    
    public void setSeats(int seats){
        this.seats=seats;}
    public int getSeats(){
        return seats;}
    
    public void setDate(Date date){
        this.date=date;}
    public Date getDate(){
        return date;}
    
    public void setTimestamp(Timestamp timestamp){
        this.timestamp=timestamp;}
    public Timestamp getTimestamp(){
        return timestamp;}
    
}