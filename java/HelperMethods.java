package com.example.expensetracker;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.bson.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelperMethods {

    public static final String letters = "abcdefghijklmnopqrstuvwxyz";
    public static final String digits = "0123456789";
    public static final String symbols = "!@#$%^&*";
    private static final String DIR_NAME = "ExpenseTrackerCSVExports";
    private static final String DATE_DELIMITER = "/";

    public static int PasswordStrength(String pass){
        int letter = 0, digit = 0, symbol = 0; // verify password contains each
        for (int i = 0; i < pass.length(); i++) {
            if (in(pass.charAt(i), letters) && letter == 0){ // assure password contains a letter
                letter = 1;
            }else if (in(pass.charAt(i), symbols) && symbol == 0){ // assure password contains a symbol
                symbol = 1;
            }else if (in(pass.charAt(i), digits) && digit == 0){ // assure password contains a digit
                digit = 1;
            }
            if (digit == 1 && letter == 1 && symbol == 1){
                break; // password is strong enough, no need to continue check
            }
        }

        if (pass.length() >= 8 && pass.length() <= 16){
            return 10 * (digit + letter + symbol) + 2;
        }else {
            return 10 * (digit + letter + symbol);
        }
    }

    /**
     * MongoDB matches a java.util.Date. Convert such objects to the modern java.util.localDateTime
     * @param dateToConvert Date object
     * @return the Date object as the modern implementation of date and time in java
     */
    public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime getStartDate(String start, Context context){
        LocalDateTime startDate;
        String lastDay = context.getResources().getString(R.string.lastDay);
        String lastWeek = context.getResources().getString(R.string.lastWeek);
        String lastMonth = context.getResources().getString(R.string.lastMonth);
        String lastYear = context.getResources().getString(R.string.lastYear);

        if (start.equals(lastDay)){ // set the start date to a day ago
            startDate = LocalDateTime.now().minusDays(1);
        } else if (start.equals(lastWeek)){ // set the start date to a week ago
            startDate = LocalDateTime.now().minusWeeks(1);
        } else if (start.equals(lastMonth)){ // set the start date to a month ago
            startDate = LocalDateTime.now().minusMonths(1);
        }  else if (start.equals(lastYear)){ // set the start date to a year ago
            startDate = LocalDateTime.now().minusYears(1);
        } else { // in the default case, show all
            startDate = LocalDateTime.MIN.toLocalDate().atStartOfDay(); // get the earliest date possible
        }

        return startDate;
    }

    public static String generateCSV(Context context, ArrayList<Action> actions) throws Exception{
        String filename = "report-"+LocalDateTime.now().toString()+".csv";
        System.out.println(filename);
        // String root = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString();
        // String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File root = android.os.Environment.getExternalStorageDirectory();
        File myDir = new File(root.getAbsolutePath(), "/" + DIR_NAME); // create the directory for the reports, or open it if it already exists
        myDir.mkdirs(); // create the dir

        File csvOutputFile = new File(myDir, filename);

        double balance = 0.0; // calculate the balance of all actions
        System.out.println("FILE CREATED");
        try  { // final PrintWriter pw = new PrintWriter(csvOutputFile)
            //v System.out.println("PW CREATED");
            System.out.println("HEY " + " " + (csvOutputFile == null));
            FileOutputStream out = new FileOutputStream(filename); // create the image file
            System.out.println("FILE OPENED");
            out.write("Type,Date,Category,Sum".getBytes(StandardCharsets.UTF_8)); // TODO add description
            for (Action action : actions){
                double sign = (action instanceof Outcome) ? -1 : 1; // determine the sign of current sum
                balance += action.getSum() * sign; // take into account the current action
                out.write(convertToCSV(action).getBytes(StandardCharsets.UTF_8)); // add the current action to the file
            }

            out.write(("Total Balance,,," + balance).getBytes(StandardCharsets.UTF_8));

            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // format, quality, dest file
            System.out.println("FILE SAVED");
            // tvPath.setText(myDir.getAbsolutePath()); // update text to path
            out.flush(); // clean the buffer
            System.out.println("FILE FLUSHED");
            out.close(); // close the file
            System.out.println("FILE CLOSED");
            // Log.d("user", root + DIR_NAME);

            // pw.println("Type,Date,Category,Sum"); // write the header line

        } catch (IOException e){
            e.printStackTrace();
            throw e;
        }
        System.out.println("FILE RETURNED");

        return filename;
    }

    /**
     * Returns a map that retrieves the index in categoriesArr for a queried category
     * @param categoriesArr Array including various categories, to be mapped value-to-index
     * @return A HashMap that connects the name of the category to its index
     */
    public static HashMap<String, Integer> generateIndexMap(String[] categoriesArr){
        HashMap<String, Integer> indexMap = new HashMap<>(); // instantiate the HashMap

        int curIndex = 0; // the current position in the array
        for (String category : categoriesArr){ // traverse the provided array
            indexMap.put(category, curIndex++); // put the category and its index in the map
        }

        return indexMap; // return the generated map
    }


    private static boolean in(char c, String str){
        for (int i = 0; i < str.length(); i++) { // search for the received char in the string
            if (str.charAt(i) == c){
                return true; // return true if char was found
            }
        }

        return false; // if execution arrived here, it means the char is not in the String
    }

    /**
     * Converts the received action to CSV format
     * @param action the action to convert
     * @return a string that is the CSV representation of the action
     */
    public static String convertToCSV(Action action) { // return the received document in a matching CSV format
        return (action instanceof Income ? "Income" : "Outcome") + "," + action.getDate() + "," +
                action.getDesc() + "," + action.getCategory() + "," + action.getSum() + "\n";
    }

    /**
     * Returns the index of toFind in the received array
     * @param arr the target array to search in
     * @param toFind the item to search in the array
     * @return the index of the queried item, -1 if the array doesn't contain the string
     */
    public static int indexOfArray(String[] arr, String toFind){
        for (int i = 0; i < arr.length; i++){ // traverse the array
            if (arr[i].equals(toFind)){ // check if the required item was found
                return i; // if so, return the index
            }
        }

        return -1; // return a negative value in case the item doesn't appear in the list
    }

    /**
     * Generates a filename for the sharedPreference that is used to store the user's barriers
     * @param username the corresponding username
     * @return the generated filename
     */
    public static String getSpFilename(String username){
        return username + "_" + "barriers";
    }

    /**
     *
     * @param categoriesArr
     * @param sumsArr
     * @param actions
     */
    public static void fillDataArrays(String[] categoriesArr, double[] sumsArr, ArrayList<Action> actions){
        // create a map for the index of each category in the sums array
        HashMap<String, Integer> indexMap = HelperMethods.generateIndexMap(categoriesArr);

        // sum the actions in each category
        for (Action action : actions){
            sumsArr[indexMap.get(action.getCategory())] += action.getSum();
        }
    }

    /**
     * Returns a date that is exactly one month before the received one
     * @param dateTime a date to take 1 month backwards from
     * @return the date that is 1 month ahead of the received date
     */
    public static LocalDateTime getMonthBefore(LocalDateTime dateTime){
        if (dateTime.getMonth().equals(Month.JANUARY)){
            return LocalDateTime.of(dateTime.getYear(), Month.DECEMBER, MonthlyGoalsService.REPORT_DAY, 0, 0);
        }

        return LocalDateTime.of(dateTime.getYear(), dateTime.getMonthValue()-1, MonthlyGoalsService.REPORT_DAY, 0, 0);
    }

    /**
     * Returns the received date in string format
     * @param dateTime the date to display as string
     * @return the text representation of the received date
     */
    public static String dateAsString(LocalDateTime dateTime){
        return dateTime.getDayOfMonth() + DATE_DELIMITER + dateTime.getMonthValue() + DATE_DELIMITER + dateTime.getYear();
    }


}