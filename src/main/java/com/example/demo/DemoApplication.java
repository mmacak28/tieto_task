package com.example.demo;

import com.github.palindromicity.syslog.SyslogParser;
import com.github.palindromicity.syslog.SyslogParserBuilder;

import java.awt.*;
import java.io.*;
import java.time.LocalTime;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matej Macák
 *
 */
public class DemoApplication{

    /**
     * Checks if the given regex pattern matches the timestamp
     * @param pattern_s String pattern of regex
     * @param syslogMap Map with objects from syslog parser including timestamp
     * @return ture if the pattern matches with timestamp false otherwise
     */
    public static boolean isPattern(String pattern_s,Map<String,Object> syslogMap){
        Pattern pattern = Pattern.compile(pattern_s);
        Matcher matcher = pattern.matcher(syslogMap.get("syslog.header.timestamp").toString());
        return matcher.find();
    }

    //https://www.rgagnon.com/javadetails/java-0624.html
    /**
     * Chceks if the time is within given range
     * @param time time we want to know if is in range
     * @param start lower time bound
     * @param end upper time bound
     * @return true if the time is within boundaries
     */
    public static boolean isHourInInterval(String time, String start, String end) {
        LocalTime t1 = LocalTime.parse( time );
        LocalTime t2 = LocalTime.parse( start );
        LocalTime t3 = LocalTime.parse( end );

        return ((t1.compareTo(t2) >= 0)
                && (t1.compareTo(t3) <= 0));
    }

    /**
     * writes the information about one log line into buffer
     * @param bw buffer into which is written
     * @param syslogMap map representing objects of one syslog line
     * @throws IOException thrown in case of IO error
     */
    public static void writeToBuffer(BufferedWriter bw, Map<String,Object> syslogMap) throws IOException {
        bw.write("<tr><td>" + syslogMap.get("syslog.header.pri") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.version") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.timestamp") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.hostName") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.appName") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.procId") + "</td>" +
                "<td>" + syslogMap.get("syslog.header.msgId") + "</td>" +
                "<td>" + syslogMap.get("syslog.structuredData\\\\.(.*)\\\\.(.*)$") + "</td>" +
                "<td>" + syslogMap.get("syslog.message") + "</td>" +
                "</tr>");
    }

    /**
     * filter the logs in syslog format and outputs it html table
     * @param args command line arguments
     * @throws IOException thrown in case of buffered writer or file error
     */
    public static void main(String[] args) throws IOException {

        //initialisation of syslog parser
        SyslogParser parser = new SyslogParserBuilder().build();

        try {
            File f = new File("source.html");
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));

            //prepares visualisation of table
            bw.write("<!DOCTYPE html><html>" +
                    "<head><style>table" +
                    " {font-family: arial, sans-serif;border-collapse: collapse;width: 100%;}" +
                    "td, th {border: 1px solid #dddddd;text-align: left;padding: 8px;}" +
                    "tr:nth-child(even) {background-color: #dddddd;}</style></head>" +
                    "<body><h2>Resulting table</h2><table style=\"width:100%\">");
            bw.write("<tr><th>PRI</th><th>Version</th><th>Timestamp</th><th>Hostname</th><th>Application</th><th>PID</th><th>Message ID</th><th>Data</th><th>Message</th></tr>");

            Scanner input = new Scanner(new File(args[0]));

            while (input.hasNextLine()) {
                String line = (input.nextLine());
                Map<String,Object> syslogMap = parser.parseLine(line);

                //case when year and month are on the input
                if (args[1].length() == 7){
                    String pattern_s = args[1] + "-(0[1-9]|[1-2][0-9]|3[0-1])T(2[0-3]|[01][0-9]):([0-5][0-9]):[0-5][0-9].[0-5][0-5][0-9]Z";
                    if (isPattern(pattern_s,syslogMap)){
                        writeToBuffer(bw,syslogMap);
                    }
                }
                //case when day is on the input
                else if (args[1].length() == 10){
                    String pattern_s = args[1] + "T(2[0-3]|[01][0-9]):([0-5][0-9]):[0-5][0-9].[0-5][0-5][0-9]Z";
                    if (isPattern(pattern_s,syslogMap)){
                        writeToBuffer(bw,syslogMap);
                    }
                }
                //case when time is on the input
                else if (args[1].length() == 8){
                    String pattern_s = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T" +args[1] + ".[0-5][0-5][0-9]Z";
                    if (isPattern(pattern_s,syslogMap)){
                        writeToBuffer(bw,syslogMap);
                    }
                }
                //case when time window is on the input
                else if (args[1].length() == 17){
                    String [] times = args[1].split("-");
                    String [] timestampParts = syslogMap.get("syslog.header.timestamp").toString().split("T");

                    String [] timestampTime = timestampParts[1].split("\\.");

                    if (isHourInInterval(timestampTime[0],times[0],times[1])){
                        writeToBuffer(bw,syslogMap);
                    }
                }
                else{
                    System.out.println("Invalid input");
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
