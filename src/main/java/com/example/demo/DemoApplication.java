package com.example.demo;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.palindromicity.syslog.SyslogParser;
import com.github.palindromicity.syslog.SyslogParserBuilder;

import java.awt.*;
import java.io.*;
import java.time.LocalTime;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@SpringBootApplication

/**
 * @autho Matej Macák
 * 
 */
public class DemoApplication{

    public static boolean isPattern(String pattern_s,Map<String,Object> syslogMap){
        Pattern pattern = Pattern.compile(pattern_s);
        Matcher matcher = pattern.matcher(syslogMap.get("syslog.header.timestamp").toString());
        return matcher.find();
    }

    //https://www.rgagnon.com/javadetails/java-0624.html
    public static boolean isHourInInterval(String target, String start, String end) {
        LocalTime t1 = LocalTime.parse( target );
        LocalTime t2 = LocalTime.parse( start );
        LocalTime t3 = LocalTime.parse( end );

        return ((t1.compareTo(t2) >= 0)
                && (t1.compareTo(t3) <= 0));
    }

    public static void writeToBuffer(BufferedWriter bw, Map<String,Object> syslogMap) throws IOException {
        bw.write("<tr><td>" + syslogMap.get("syslog.header.severity") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.version") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.timestamp") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.hostName") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.appName") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.procId") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.msgId") + "</td>" +
                "<td>" + syslogMap.get("syslog.structuredData.") + "</td>" +
                "<td>" + syslogMap.get("syslog.message") + "</td>" +
                "</tr>");
    }

    public static void main(String[] args) throws IOException {
        //SpringApplication.run(DemoApplication.class, args);
        SyslogParser parser = new SyslogParserBuilder().build();

        try {
            File f = new File("source.htm");
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("<!DOCTYPE html><html><body><h2>Resulting table</h2><table style=\"width:100%\">");
            bw.write("<tr><th>PRI</th><th>Version</th><th>Timestamp</th><th>Hostname</th><th>Application</th><th>PID</th><th>Message ID</th><th>Data</th><th>Message</th></tr>");

            Scanner input = new Scanner(new File(args[0]));

            while (input.hasNextLine()) {
                String line = (input.nextLine());
                Map<String,Object> syslogMap = parser.parseLine(line);
                System.out.println(syslogMap.get("syslog.header.timestamp"));

                if (args[1].length() == 7){
                    String pattern_s = args[1] + "-(0[1-9]|[1-2][0-9]|3[0-1])T(2[0-3]|[01][0-9]):([0-5][0-9]):[0-5][0-9].[0-5][0-5][0-9]Z";
                    if (isPattern(pattern_s,syslogMap)){
                        System.out.println(syslogMap.get("syslog.header.appName"));
                        writeToBuffer(bw,syslogMap);
                    }
                }
                else if (args[1].length() == 10){
                    String pattern_s = args[1] + "T(2[0-3]|[01][0-9]):([0-5][0-9]):[0-5][0-9].[0-5][0-5][0-9]Z";
                    if (isPattern(pattern_s,syslogMap)){
                        System.out.println(syslogMap.get("syslog.header.hostName"));
                        writeToBuffer(bw,syslogMap);
                    }
                }
                else if (args[1].length() == 8){
                    String pattern_s = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T" +args[1] + ".[0-5][0-5][0-9]Z";
                    if (isPattern(pattern_s,syslogMap)){
                        System.out.println(syslogMap.get("syslog.message"));
                        writeToBuffer(bw,syslogMap);
                    }
                }
                else if (args[1].length() == 17){
                    String [] times = args[1].split("-");
                    System.out.println(times[0]);
                    System.out.println(times[1]);
                    String [] timestampParts = syslogMap.get("syslog.header.timestamp").toString().split("T");

                    String [] timestampTime = timestampParts[1].split("\\.");

                    System.out.println(timestampTime[0]);
                    if (isHourInInterval(timestampTime[0],times[0],times[1])){
                        System.out.println(syslogMap.get("syslog.header.msgId"));
                        writeToBuffer(bw,syslogMap);
                    }
                }
            }
            input.close();
            bw.write("</table></body></html>");
            bw.close();

            Desktop.getDesktop().browse(f.toURI());
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

}
