/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chris
 */
public class reverse {
    
    public static void main(String[] args) {
        run();
    }
    
    public static void run() {
        try {
        List<String> lines = new ArrayList<>();    
        BufferedReader br = new BufferedReader
                (new FileReader("./timerdata/log file.txt"));
        String line = br.readLine();
        while (line != null) {
            lines.add(line);
            line = br.readLine();
        }
        br.close();
        
        System.out.println(lines.size());
        FileWriter fw = new FileWriter("./timerdata/log file.txt");
        for (int i = lines.size()-1; i >= 0; i--) {
            System.out.println("HERE WE GO");
            fw.write(lines.get(i) + "\n");
        }
        fw.close();
           
        }
        catch (IOException e) {
            System.err.println(e);
        }
        
        
        
    }
}
