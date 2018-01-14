package com.example.anu.popularmovies_1.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Design on 07-12-2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class CommonUtils {

    public static int SWATCH_ARRAY_LENGTH = 2;

    /**
     * method to change date format
     *
     * @param inputFormat  current format
     * @param outputFormat new format
     * @param inputDate    date
     * @return formatted date
     */
    public static String formatDate(String inputFormat, String outputFormat, String inputDate) {
        Date parsed;
        String outputDate = inputDate;
        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputDate;

    }

}