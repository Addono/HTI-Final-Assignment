/**
 * @author HTI students, Spring 2013, adjusted by N.Stash
 *
 */
package org.thermostatapp.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class HeatingSystem {

    public static String BASE_ADDRESS = "";
    public static String WEEK_PROGRAM_ADDRESS = "";
    private final static int TIME_OUT = 10000; // in milliseconds.

    /**
     * Retrieving weekProgram
     * @return
     * @throws ConnectException
     * @throws CorruptWeekProgramException
     */
    public static WeekProgram getWeekProgram() throws ConnectException,
            CorruptWeekProgramException {

        InputStream in = null;
        try {
            HttpURLConnection connect = getHttpConnection(
                    HeatingSystem.WEEK_PROGRAM_ADDRESS, "GET");
            in = connect.getInputStream();

            // Set up an XML parser.
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, "UTF-8"); // Enter the stream.
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, "week_program");

            WeekProgram program = new WeekProgram();
            ArrayList<Switch> switches = new ArrayList<Switch>();
            String current_day = "";

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                if (parser.getName().equals("day")) {
                    current_day = parser.getAttributeValue(null, "name");
                    switches = parseSwitches(parser);

                    if (switches.size() != 10) // Something is wrong, fix it
                        // automatically or throw an
                        // exception.
                        throw new CorruptWeekProgramException("Switches Size: "
                                + switches.size());
                    else {
                        int nr_switches = 0;
                        while (nr_switches < 10
                                && switches.get(nr_switches).getState())
                            nr_switches++;

                        program.setSwitches(current_day, switches, nr_switches);
                    }
                }
            }
            return program;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) { // Can also be thrown by getHttpConnection().
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close(); // Close stream.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Returns the switches for a particular day
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static ArrayList<Switch> parseSwitches(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        ArrayList<Switch> switches = new ArrayList<Switch>();
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            int eventType = parser.getEventType();
            if (eventType == XmlPullParser.END_TAG
                    && parser.getName().equals("day"))
                return switches;

            if (eventType != XmlPullParser.START_TAG)
                continue;

            if (parser.getName().equals("switch")) {
                // Get the attribute values.
                String type = parser.getAttributeValue(null, "type");
                String state = parser.getAttributeValue(null, "state");

                // Get the time.
                String time = null;
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() == XmlPullParser.TEXT) {
                        time = parser.getText();
                        break;
                    }
                }

                // Check whether everything has been correctly defined.
                if (state == null || type == null || time == null)
                    continue;

                boolean status = false;
                if (state.equals("on"))
                    status = true;

                switches.add(new Switch(type, status, time));
            }
        }
        return switches;
    }

    /**
     * Retrieves all data except for weekProgram
     * @param attribute_name
     *            = { "day", "time", "currentTemperature", "dayTemperature",
     *            "nightTemperature", "weekProgramState" }; Note that
     *            "weekProgram" has not been included, because it has a more
     *            complex value than a single value. Therefore the funciton
     *            getWeekProgram() is implemented which return a WeekProgram
     *            object that can be easily altered.
     */
    public static String get(String attribute_name) throws ConnectException,
            IllegalArgumentException {
        // If XML File does not contain the specified attribute, than
        // throw NotFound or NotFoundArgumentException
        // You can retrieve every attribute with a single value. But for the
        // WeekProgram you need to call getWeekProgram().
        String link = "";
        boolean match = false;
        String[] valid_names = { "day", "time", "currentTemperature", "targetTemperature",
                "dayTemperature", "nightTemperature", "weekProgramState" };
        String[] tag_names = { "current_day", "time", "current_temperature", "target_temperature",
                "day_temperature", "night_temperature", "week_program_state" };
        int i;
        for (i = 0; i < valid_names.length; i++) {
            if (attribute_name.equalsIgnoreCase(valid_names[i])) {
                match = true;
                link = HeatingSystem.BASE_ADDRESS + "/" + valid_names[i];
                break;
            }
        }

        if (match) {
            InputStream in = null;
            try {
                System.out.println("USED Link: " + link);
                HttpURLConnection connect = getHttpConnection(link, "GET");
                in = connect.getInputStream();

                /**
                 * For Debugging Note that when the input stream is already used
                 * with this BufferedReader, then after that the XmlPullParser
                 * can no longer use it. This will cause an error/exception.
                 *
                 * BufferedReader inn = new BufferedReader(new
                 * InputStreamReader(in)); String testLine = ""; while((testLine
                 * = inn.readLine()) != null) { System.out.println("Line: " +
                 * testLine); }
                 */
                // Set up an XML parser.
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                        false);
                parser.setInput(in, "UTF-8"); // Enter the stream.
                parser.nextTag();
                parser.require(XmlPullParser.START_TAG, null, tag_names[i]);

                int eventType = parser.getEventType();

                // Find the single value.
                String value = "";
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.TEXT) {
                        value = parser.getText();
                        break;
                    }
                    eventType = parser.next();
                }

                return value;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                System.out.println("FileNotFound Exception! " + e.getMessage());
                // e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        } else {
            // return null;
            throw new IllegalArgumentException("Invalid Input Argument: \""
                    + attribute_name + "\".");
        }
        return null;
    }

    /**
     * Return false if weekProgramState is on,
     * true otherwise
     * @return
     */
    public static boolean getVacationMode() {
        try {
            String state = HeatingSystem.get("weekProgramState");
            if (state.equals("on")) {
                return false;
            } else {
                return true;
            }
        } catch (ConnectException e) {
            // Should not be able to occur, because the "weekProgramState" IS a
            // valid argument.
            return false; // Return as default.
        }
    }

    /**
     * Method for GET and PUT requests
     * @param link
     * @param type
     * @return
     * @throws IOException
     * @throws MalformedURLException
     * @throws UnknownHostException
     * @throws FileNotFoundException
     */
    private static HttpURLConnection getHttpConnection(String link, String type)
            throws IOException, MalformedURLException, UnknownHostException,
            FileNotFoundException {
        URL url = new URL(link);
        HttpURLConnection connect = (HttpURLConnection) url.openConnection();
        connect.setReadTimeout(HeatingSystem.TIME_OUT);
        connect.setConnectTimeout(HeatingSystem.TIME_OUT);
        connect.setRequestProperty("Content-Type","application/xml");
        connect.setRequestMethod(type);
        if (type.equalsIgnoreCase("GET")) {
            connect.setDoInput(true);
            connect.setDoOutput(false);
        } else if (type.equalsIgnoreCase("PUT")) {
            connect.setDoInput(false);
            connect.setDoOutput(true);
        }
        connect.connect();
        return connect;
    }

    /**
     * Checks whether the temperature is within the range [5,30]
     * and is a double with one decimal like "20.0".
     * @param temperature
     * @return
     * @throws InvalidInputValueException
     */
    private static boolean inTemperatureBoundaries(String temperature)
            throws InvalidInputValueException {
        try {
            double temp = Double.parseDouble(temperature);
            if (temp >= 5 && temp <= 30)
                return true;
            throw new InvalidInputValueException(
                    "Invalid Value for temperature: " + temperature
                            + ", must be between 5.00 & 30.0 degrees.");
        } catch (NumberFormatException e) {
            throw new InvalidInputValueException(
                    "Invalid Value for temperature syntax: " + temperature);
        }
    }

    /**
     * Upload data to the server, everything except for weekProgram
     * @param attribute_name
     * @param value
     * @throws IllegalArgumentException
     * @throws InvalidInputValueException
     */
    public static void put(String attribute_name, String value)
            throws IllegalArgumentException, InvalidInputValueException {
        // Perform Dimension checks. En check if we get a http response status
        // code of "OK".
        // String[] xml_attribute_names

        String link = "";
        boolean match = false;
        String[] valid_names = { "day", "time", "currentTemperature", "targetTemperature",
                "dayTemperature", "nightTemperature", "weekProgramState" };
        for (int i = 0; i < valid_names.length; i++) {
            if (attribute_name.equalsIgnoreCase(valid_names[i])) {
                match = true;
                link = HeatingSystem.BASE_ADDRESS + "/" + valid_names[i];
                break;
            }
        }

        if (!match) {
            throw new IllegalArgumentException("Invalid attribute name: "
                    + attribute_name);
        }

        String tag_name = "";
        if (attribute_name.equals("day")) {
            tag_name = "current_day";
            String[] valid_days = { "Monday", "Tuesday", "Wednesday",
                    "Thursday", "Friday", "Saturday", "Sunday" };
            for (int i = 0; i < valid_days.length; i++) {
                if (value.equalsIgnoreCase(valid_days[i])) {
                    // Do not make it case-sensitive but adjust to the name with
                    // a Capital letter to be sure it has the right format.
                    value = valid_days[i];
                    break;
                }
                // Invalid day value.
                if (i == valid_days.length - 1)
                    throw new InvalidInputValueException("Not a correct day: "
                            + value);
            }
        } else if (attribute_name.equals("time")) {
            tag_name = "time";

            if (!Switch.isValidTimeSyntax(value)) {
                throw new InvalidInputValueException("Invalid Time Value: "
                        + value);
            }
        } else if (attribute_name.equals("currentTemperature")) {
            tag_name = "current_temperature";
            inTemperatureBoundaries(value);
        } else if (attribute_name.equals("targetTemperature")) {
            tag_name = "target_temperature";
            inTemperatureBoundaries(value);
        } else if (attribute_name.equals("dayTemperature")) {
            tag_name = "day_temperature";
            inTemperatureBoundaries(value);
        } else if (attribute_name.equals("nightTemperature")) {
            tag_name = "night_temperature";
            inTemperatureBoundaries(value);
        } else if (attribute_name.equals("weekProgramState")) {
            tag_name = "week_program_state";
            value = value.toLowerCase(Locale.US); // Must be spelled in lower
            // case, assure this.
            if (!value.equals("on") && !value.equals("off")) {
                throw new InvalidInputValueException(
                        "Value for weekProgramState should be \"on\" or \"off\"");
            }
        }

        // If the script gets here the attribute names & value is corresponding
        // and correct. E.g. like temperature between the boundaries
        // of 5 till 30 degrees.

        // Output string.
        String output = "<" + tag_name + ">" + value + "</" + tag_name + ">";

        DataOutputStream out = null;
        try {
            HttpURLConnection connect = getHttpConnection(link, "PUT");

            out = new DataOutputStream(connect.getOutputStream());
            out.writeBytes(output);
            out.flush();

            String response = connect.getResponseMessage();
            int responseCode = connect.getResponseCode();
            System.out.println("Http Response: " + response);
            System.out.println("Http Response Code: " + responseCode);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close(); // Close stream.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Uploading the updated or adjusted week program to the server
     * @param wpg
     */
    public static void setWeekProgram(WeekProgram wpg) {
        DataOutputStream out = null;
        try {
            String xml_output = wpg.toXML();
            System.out.println("Link: " + HeatingSystem.WEEK_PROGRAM_ADDRESS);
            HttpURLConnection connect = getHttpConnection(
                    HeatingSystem.WEEK_PROGRAM_ADDRESS, "PUT");

            out = new DataOutputStream(connect.getOutputStream());
            out.writeBytes(xml_output);
            out.flush();

            // Check the response Code it may be the case that we retrieve an
            // error, because the XML format is wrong.
            String response = connect.getResponseMessage();
            int responseCode = connect.getResponseCode();
            System.out.println("Http Response: " + response);

            System.out.println("Http Response Code: " + responseCode);

            if (responseCode != 200) {
                InputStream err = connect.getErrorStream();
                BufferedReader err_read = new BufferedReader(
                        new InputStreamReader(err));
                String errInput;
                while ((errInput = err_read.readLine()) != null) {
                    System.out.println("ErrorStream: " + errInput);
                }
                err.close(); // Close the Error Stream.
                err_read.close();
            }
            // if(response.indexOf("STATUS=OK") == -1)
            // throws WeekProgramUploadException(response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) { // Gets thrown by the toXML()
            // function.
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}