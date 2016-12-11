/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package studytimer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import java.util.Scanner;
import java.io.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.animation.KeyFrame;

import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.application.Platform;
import javafx.util.Duration;

public class FXMLDocumentController implements Initializable {
    
    @FXML
    private Label labelTotal;
    @FXML
    private Label labelSession;
    @FXML
    private Label labelDate;
    @FXML
    private Button startBtn;
    @FXML
    private Button pauseBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private Button resetBtn;
    
    private FileWriter writeTime;
    private FileWriter writeLog;
    
    private Integer readInt;
    private Integer totalTime;
    private Integer sessionTime;
    
    private String sessionFormated;
    private String totalFormated;
    private String startFormated;
    
    
    private String sessionDate;
    private int numOfDays;
    
    //this is iff, depending on where the "current directory" is ... ok for me
    private final String logFile = 
            "./timerdata/log file.txt";
            //"C:\\Users\\Chris\\Desktop\\Program\\TimerData\\log file.txt";
    private final String timeTotalsFile = 
            "./timerdata/time";
            //"C:\\Users\\Chris\\Desktop\\Program\\TimerData\\time";
    
    private boolean isOn;
    private Timeline tick;
    
    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
        getDays();
        if (event.getSource()==startBtn && isOn == false){
            tick.play();
            isOn = true;
            pauseBtn.requestFocus();
        }
        else if (event.getSource()==pauseBtn && isOn == true){
            tick.pause();
            isOn = false;
            startBtn.requestFocus();
        }
        else if(event.getSource() ==resetBtn){
            //If no session time dont do anything;
            if (totalTime > 0) {
                writeFiles("\nTimer Reset, " + this.startFormated + " - " + 
                        this.getDate() + ", " + this.totalFormated);
                sessionTime = 0;
                totalTime = 0;
                startFormated = getDate();
                numOfDays = getDays();
                updateLabels();
            }
            
        }
        else if (event.getSource()==closeBtn){
            writeFiles("");
            Platform.exit();
        }
    }
    
    public int parseTimeFromLog()throws IOException{
        
        String lastLine = getLastLoggedLine();
        int previousTime = 0;
        
        if (lastLine.contains("Duration")){
            //I need to now read the data
            String[] fileInputs = lastLine.split(":");
            String[] numbers = fileInputs[2].split("[a-z ]+");
            
            previousTime += Integer.parseInt(numbers[1]) * 3600;
            previousTime += Integer.parseInt(numbers[2]) * 60;
            previousTime += Integer.parseInt(numbers[3]);
        }
        
        return previousTime;
    }
    
    public String parseDateFromLog() throws IOException{
        String lastLoggedLine = getLastLoggedLine();
        String lastLoggedDate = "";
        
        if (lastLoggedLine.contains("Duration"))
            lastLoggedDate = lastLoggedLine.substring(6, 17);
        return lastLoggedDate;
    }
    
    public String getLastLoggedLine() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(logFile));
        String last = br.readLine();
        br.close();
        System.out.println(last);
        
        if (last == null)
            return "";
        return last;
    }
    
        //NO LONGER USED /ORDER IS REVERSED    
/*    
    public String getLastLoggedLine() throws IOException{
        BufferedReader readLog = new BufferedReader(new FileReader(logFile));
        String lastLine = "";
        String temp;
        
        for (;;)
        {
            temp = readLog.readLine();
            if (temp == null)
                break;
            else
                lastLine = temp;
        }
        return lastLine;
    }
 */   
   
    
    public void writeFiles(String msg) throws IOException{
        //Get time if same day and last logged date
        String lastLoggedDate = this.parseDateFromLog();
        int sameDayTime = parseTimeFromLog();
        
        //Get all the lines in logFile
        List<String> allLines = new ArrayList<>();
        readLogIntoList(allLines);
        
        //Open the reader, no appending
        writeLog = new FileWriter(logFile, false);
        
        
        //Same day, diff session, add times together
        if (lastLoggedDate.equals(this.getDate()) && sessionTime > 0)
        {
            if (sameDayTime > 0){
                sessionTime += sameDayTime;
                updateFormatedTimes();
                //Replace most recent line with updated version
                allLines.set(0, "Date: " + this.sessionDate + "\tDuration: "
                        + sessionFormated);
            }
        }
        //Diff day, but active session -- Write new time
        else if (sessionTime > 0)
        {    
            //Add a new line, for a new day
            allLines.add(0, "Date: " + this.sessionDate + "\tDuration: "
                    + sessionFormated);
        }
        //In the case of reset no time, simply close 
        else
        {
            System.out.println("Reset and close ?: " + msg);
            if (msg.length() > 0)
                allLines.add(0, msg);
        }
        //always write
        for (int i = 0; i < allLines.size(); i++) {
            writeLog.write(allLines.get(i) + "\n");
        }
        writeLog.close();

        
        writeTime = new FileWriter(timeTotalsFile);
        writeTime.write(totalTime.toString()+"\n");
        writeTime.write(startFormated);
        writeTime.close();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isOn = false;
        sessionTime = 0;
        sessionDate = getDate();
        
        
        try(Scanner fileIn = new Scanner(new File(timeTotalsFile))){
            readInt = Integer.parseInt(fileIn.nextLine());
            startFormated = fileIn.nextLine();
        } catch (Exception e) {
            readInt = 0;
            startFormated = getDate();
        }
        
        
        
        totalTime = sessionTime + readInt;
        
        //If there is no time, set sessionDate to todays date
        if (totalTime <= 0)
            startFormated = getDate();
        numOfDays = getDays();
        updateLabels();
        setupTimer();
        try{
            this.parseDateFromLog();
        }catch (IOException e){
            System.err.println(e);
        }
    }

     public void readLogIntoList(List<String> allLines) throws IOException { 
            
        String temp;
        
        BufferedReader readAll = new BufferedReader
                    (new FileReader(logFile));
        while (true){
            temp = readAll.readLine();
            if (temp == null)
                break;
            allLines.add(temp);
        }
        readAll.close();
    }
    
    public void setupTimer(){
        tick = TimelineBuilder.create()//creates a new Timeline
            .keyFrames(
            new KeyFrame(
                new Duration(1000), //This is how often it updates in milliseconds
                    (ActionEvent t) -> {
                        sessionTime += 1;
                        totalTime += 1;
                        updateLabels();
                })
           )
        .cycleCount(Timeline.INDEFINITE)
        .build();
    }
    
    public String getDate(){
        long milliTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
        Date resultdate = new Date(milliTime);
        return sdf.format(resultdate);
    }
    
    public void updateFormatedTimes(){
        sessionFormated = "" + sessionTime/3600 + " hours "
            + (sessionTime/60)%60 + " minutes " + sessionTime%60
            + " seconds";
        totalFormated = "" + totalTime/3600 + " hours " 
            + (totalTime/60)%60 + " minutes " + totalTime%60
            + " seconds";    
    }
    
    public void updateLabels(){
        updateFormatedTimes();
        labelSession.setText(sessionFormated);
        labelTotal.setText(totalFormated);
        if (numOfDays > 1)
            labelDate.setText(startFormated + " (" + numOfDays + " days)");
        else 
            labelDate.setText(startFormated + " (" + numOfDays + " day)");
    }
    
    private int getDays(){
        numOfDays = 0;
        
        String currentMonth = startFormated.substring(0,3);
        String startDay = startFormated.substring(4, 6);
        
        String currentDay = getDate().substring(4, 6);
        
        int startDayInt = Integer.parseInt(startDay);
        int currentDayInt = Integer.parseInt(currentDay);
        
        if (currentDayInt < startDayInt) {
            switch (currentMonth) {
                case "Jan":
                case "Mar":
                case "May":
                case "Jul":
                case "Aug":
                case "Oct":
                case "Dec":
                    numOfDays = 31-startDayInt+1+currentDayInt;
                    break;
                case "Feb":
                    numOfDays = 28-startDayInt+1+currentDayInt;
                    break;
                default:
                    numOfDays = 30-startDayInt+1+currentDayInt;
            }
        }
            
        else
            numOfDays = currentDayInt - startDayInt + 1;
            
        return numOfDays;
    }
}

    


