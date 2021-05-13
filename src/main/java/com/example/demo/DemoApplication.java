package com.example.demo;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.syslog.RFC5424SyslogParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Scanner;

//@SpringBootApplication
public class DemoApplication extends RFC5424SyslogParser{

    public static void main(String[] args) {
        //SpringApplication.run(DemoApplication.class, args);
        RFC5424SyslogParser mineParser = new RFC5424SyslogParser();
        try {
            Scanner input = new Scanner(new File(args[0]));
            while (input.hasNextLine()) {
                String line = (input.nextLine());
                Reader reader = new Reader(line);
                Object date = mineParser.getTimestamp(reader);
                System.out.println(date.toString());
            }
            input.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

}
