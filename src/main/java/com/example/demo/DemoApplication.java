package com.example.demo;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.palindromicity.syslog.SyslogParser;
import com.github.palindromicity.syslog.SyslogParserBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Map;
import java.util.Scanner;

//@SpringBootApplication
public class DemoApplication{

    public static void main(String[] args) {
        //SpringApplication.run(DemoApplication.class, args);
        SyslogParser parser = new SyslogParserBuilder().build();
        try {
            Scanner input = new Scanner(new File(args[0]));
            while (input.hasNextLine()) {
                String line = (input.nextLine());
                Map<String,Object> syslogMap = parser.parseLine(line);
                System.out.println(syslogMap.get("syslog.header.timestamp"));
            }
            input.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

}
