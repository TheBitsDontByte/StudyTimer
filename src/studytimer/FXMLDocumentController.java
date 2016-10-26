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

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    
    private FileWriter fwOut;

    private Integer readInt;
    private Integer totalTime;
    private Integer sessionTime;
    
    private String sessionFormated;
    private String totalFormated;
    private String startFormated;
    
    private String sessionDate;
    
    private String logFile;
    private String timeTotalsFile;
    
    private boolean isOn;
    private Timeline tick;
    
    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
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
            sessionTime = 0;
            totalTime = 0;
            startFormated = getDate();
            updateLabels();
        }
        else if (event.getSource()==closeBtn){
            fwOut = new FileWriter(timeTotalsFile);
            fwOut.write(totalTime.toString()+"\n");
            fwOut.write(startFormated);
            fwOut.close();
            try(FileWriter logOut = new FileWriter(logFile, true)){
                logOut.write("Date: " + this.sessionDate + "\tDuration: "
                        + sessionFormated +"\n");
            }catch(Exception e){
                System.err.println(e);
            }
            Platform.exit();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isOn = false;
        sessionTime = 0;
        sessionDate = getDate();
        logFile = "log file.txt";
        timeTotalsFile = "time";
        //Change this to create one if not here (easy)
        //Will prob not work well, if one isnt good ....
        
        try(Scanner fileIn = new Scanner(new File(timeTotalsFile))){
            readInt = Integer.parseInt(fileIn.nextLine());
            startFormated = fileIn.nextLine();
        } catch (Exception e) {
            readInt = 0;
            startFormated = getDate();
        }
        
        totalTime = sessionTime + readInt;
        updateLabels();
        setupTimer();
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
        System.out.println(sdf.format(resultdate));
        return sdf.format(resultdate);
    }
    
    public void updateLabels(){
        sessionFormated = "" + sessionTime/3600 + " hours "
            + (sessionTime/60)%60 + " minutes " + sessionTime%60
            + " seconds";
        totalFormated = "" + totalTime/3600 + " hours " 
            + (totalTime/60)%60 + " minutes " + totalTime%60
            + " seconds";    
        labelSession.setText(sessionFormated);
        labelTotal.setText(totalFormated);
        labelDate.setText(startFormated);
    }
}

    


