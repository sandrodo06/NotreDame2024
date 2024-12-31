package org.feup.apm.notredame.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Singleton instance
    private static DatabaseHelper instance;

    // Database Info
    private static final String DATABASE_NAME = "userProfiles.db";
    private static final int DATABASE_VERSION = 6;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ANGLES = "angles";
    private static final String TABLE_WEEKS = "weeks";
    private static final String TABLE_DAYS = "days";
    private static final String TABLE_HOURS = "hours";

    // USERS Table Columns
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_GENDER = "gender";

    // ANGLES Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_ANGLE = "angle";

    // WEEKS Table Columns
    private static final String COLUMN_WEEK_ID = "week_id";
    private static final String COLUMN_START_DATE = "start_date";
    private static final String COLUMN_END_DATE = "end_date";

    // DAYS Table Columns
    private static final String COLUMN_DAY_ID = "day_id";
    private static final String COLUMN_WEEK_FK = "week_fk";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DAY_OF_WEEK = "day_of_week";

    // HOURS Table Columns
    private static final String COLUMN_HOUR_ID = "hour_id";
    private static final String COLUMN_DAY_FK = "day_fk";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_GOOD_POSTURE = "good_posture";
    private static final String COLUMN_BAD_POSTURE = "bad_posture";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create USERS table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_EMAIL + " TEXT PRIMARY KEY, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_AGE + " INTEGER, " +
                COLUMN_GENDER + " TEXT" +
                ")";
        db.execSQL(createUsersTable);

        // Create ANGLES table
        String createAnglesTable = "CREATE TABLE " + TABLE_ANGLES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_TIMESTAMP + " TEXT, " +
                COLUMN_ANGLE + " REAL, " +
                "FOREIGN KEY(" + COLUMN_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + ") ON DELETE CASCADE" +
                ")";
        db.execSQL(createAnglesTable);

        // Create WEEKS table
        String createWeeksTable = "CREATE TABLE " + TABLE_WEEKS + " (" +
                COLUMN_WEEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_START_DATE + " TEXT, " +
                COLUMN_END_DATE + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + ") ON DELETE CASCADE" +
                ")";
        db.execSQL(createWeeksTable);

        // Create DAYS table
        String createDaysTable = "CREATE TABLE " + TABLE_DAYS + " (" +
                COLUMN_DAY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_WEEK_FK + " INTEGER, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_DAY_OF_WEEK + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_WEEK_FK + ") REFERENCES " + TABLE_WEEKS + "(" + COLUMN_WEEK_ID + ") ON DELETE CASCADE" +
                ")";
        db.execSQL(createDaysTable);

        // Create HOURS table
        String createHoursTable = "CREATE TABLE " + TABLE_HOURS + " (" +
                COLUMN_HOUR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DAY_FK + " INTEGER, " +
                COLUMN_HOUR + " INTEGER, " +
                COLUMN_GOOD_POSTURE + " INTEGER, " +
                COLUMN_BAD_POSTURE + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_DAY_FK + ") REFERENCES " + TABLE_DAYS + "(" + COLUMN_DAY_ID + ") ON DELETE CASCADE" +
                ")";
        db.execSQL(createHoursTable);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOURS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEEKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANGLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // =========================================================================================
    // Inserting methods

    public long insertWeek(String email, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_START_DATE, startDate);
        values.put(COLUMN_END_DATE, endDate);
        return db.insert(TABLE_WEEKS, null, values);
    }


    public long insertDay(int weekId, String date, String day_of_the_week) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEEK_FK, weekId);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DAY_OF_WEEK, day_of_the_week);
        return db.insert(TABLE_DAYS, null, values);
    }


    public long insertHour(int dayId, int hour, int goodPosture, int badPosture) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY_FK, dayId);
        values.put(COLUMN_HOUR, hour);
        values.put(COLUMN_GOOD_POSTURE, goodPosture);
        values.put(COLUMN_BAD_POSTURE, badPosture);
        return db.insert(TABLE_HOURS, null, values);
    }


    // =========================================================================================
    // Getting the data of a day
    public List<BarEntry> getHourlyGoodPostureTime(String date) {
        List<BarEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT h." + COLUMN_HOUR + ", SUM(h." + COLUMN_GOOD_POSTURE + ") AS total_time " +
                "FROM " + TABLE_HOURS + " h " +
                "INNER JOIN " + TABLE_DAYS + " d ON h." + COLUMN_DAY_FK + " = d." + COLUMN_DAY_ID + " " +
                "WHERE d." + COLUMN_DATE + " = ? " +
                "GROUP BY h." + COLUMN_HOUR + " " +
                "ORDER BY h." + COLUMN_HOUR + " ASC";  // Ensure hours are ordered

        Cursor cursor = db.rawQuery(query, new String[]{date});

        // Initialize the array with 24 hours, all set to 0 by default
        float[] hourlyData = new float[24];

        int hourColumnIndex = cursor.getColumnIndex("hour");
        int totalTimeColumnIndex = cursor.getColumnIndex("total_time");

        if (hourColumnIndex != -1 && totalTimeColumnIndex != -1) {
            if (cursor.getCount() == 0) {
                // If no data was found, log it and return the default array
                Log.d("FetchingDb", "No data found for date: " + date);

            } else {
                // Populate the hourlyData array with the retrieved data
                while (cursor.moveToNext()) {
                    int hour = cursor.getInt(hourColumnIndex);

                    // Convert the data to minutes
                    float totalTimeInSeconds = cursor.getFloat(totalTimeColumnIndex);
                    float totalTimeInMinutes = totalTimeInSeconds / 60;
                    hourlyData[hour] = totalTimeInMinutes;
                }
            }
        }

        cursor.close();
        db.close();

        Log.d("FetchingDb", "Hourly Data: " + Arrays.toString(hourlyData));

        // Add the data to the entries list or 0 if missing
        for (int i = 0; i < 24; i++) {
            entries.add(new BarEntry(i, hourlyData[i]));
        }

        return entries;
    }

    // =========================================================================================
    // Getting the data of a week

    public List<BarEntry> getWeeklyGoodPostureTime(String date) {

        List<BarEntry> entries = new ArrayList<>(7);

        // Initialize with 0 posture time for each day of the week
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, 0));
        }

        SQLiteDatabase db = this.getReadableDatabase();

        // Calculate the start and end date of the week for the given date
        String[] startAndEndDates = getWeekStartAndEndDates(date);
        String startDate = startAndEndDates[0];
        String endDate = startAndEndDates[1];

        String dayQuery = "SELECT " + COLUMN_DATE + ", " + COLUMN_DAY_OF_WEEK + " FROM " + TABLE_DAYS +
                " INNER JOIN " + TABLE_WEEKS + " ON " + TABLE_DAYS + "." + COLUMN_WEEK_FK + " = " + TABLE_WEEKS + "." + COLUMN_WEEK_ID +
                " WHERE " + TABLE_WEEKS + "." + COLUMN_START_DATE + " >= ? AND " + TABLE_WEEKS + "." + COLUMN_END_DATE + " <= ? " +  // Ensure the date range is within the week
                " ORDER BY " + COLUMN_DATE;  // Order by COLUMN_DATE
        Cursor dayCursor = db.rawQuery(dayQuery, new String[]{startDate, endDate});

        while (dayCursor.moveToNext()) {
            String dateEntry = dayCursor.getString(0);  // First column is COLUMN_DATE
            String dayOfWeekStr = dayCursor.getString(1);  // Second column is COLUMN_DAY_OF_WEEK

            // Gets the index that corresponds to the day of the week
            int dayOfWeek = getDayOfWeekIndex(dayOfWeekStr);

            // For each day, calculate the total good posture time from the HOURS table
            String hourQuery = "SELECT SUM(" + COLUMN_GOOD_POSTURE + ") AS total_time FROM " + TABLE_HOURS +
                    " INNER JOIN " + TABLE_DAYS + " ON " + TABLE_HOURS + "." + COLUMN_DAY_FK + " = " + TABLE_DAYS + "." + COLUMN_DAY_ID +
                    " WHERE " + TABLE_DAYS + "." + COLUMN_DATE + " = ?";
            Cursor hourCursor = db.rawQuery(hourQuery, new String[]{dateEntry});

            float totalTime = 0;
            if (hourCursor.moveToFirst()) {
                int totalTimeIndex = hourCursor.getColumnIndex("total_time");
                if (totalTimeIndex != -1) {
                    totalTime = hourCursor.getFloat(totalTimeIndex);
                }
            }

            // Convert total time from seconds to hours
            float totalTimeInHours = totalTime / 3600;

            // Update the entries array with the data
            entries.set(dayOfWeek, new BarEntry(dayOfWeek, totalTimeInHours));

            hourCursor.close();
        }

        dayCursor.close();
        db.close();

        // Log the final entries list
        Log.d("FetchingDb", "Final Entries: " + entries.toString());

        return entries;
    }

    // =========================================================================================
    // Helper methods ot get the data of a week

    // Maps the day of the week to its index
    private int getDayOfWeekIndex(String dayOfWeek) {
        switch (dayOfWeek.toLowerCase()) {
            case "sunday": return 0;
            case "monday": return 1;
            case "tuesday": return 2;
            case "wednesday": return 3;
            case "thursday": return 4;
            case "friday": return 5;
            case "saturday": return 6;

            default: return -1;  // Invalid day
        }
    }

    // Helper method to get the start and end date of the week based on a given date
    private String[] getWeekStartAndEndDates(String date) {
        String[] result = new String[2];

        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            calendar.setTime(sdf.parse(date));

            // Get the start and end date of the week
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            String startDate = sdf.format(calendar.getTime());

            calendar.add(Calendar.DATE, 6); // Add 6 days to get the end of the week
            String endDate = sdf.format(calendar.getTime());

            result[0] = startDate;
            result[1] = endDate;
            
        } catch (ParseException e) {
            Log.e("dBUpdate", "Error parsing date: " + date, e);
        }

        return result;
    }

    // =========================================================================================
    // Updates the database with new angles that are being fetched from bluetooth
    public void updatePostureData(String date, int hour, int goodPostureTime, int badPostureTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Step 1: Get or create the weekId
        int weekId = getOrCreateWeekId(date);
        if (weekId == -1) {
            Log.e("dBUpdate", "Failed to get or create weekId for date: " + date);
            return;
        }

        // Step 2: Insert the new day for the given date
        int dayId = insertDayIfNeeded(weekId, date);
        if (dayId == -1) {
            Log.e("dBUpdate", "Failed to insert or retrieve dayId for date: " + date);
            return;
        }

        // Step 3: Get the current values of good and bad posture for the given dayId and hour
        Cursor cursor = db.query(
                TABLE_HOURS, // Table name
                new String[]{COLUMN_GOOD_POSTURE, COLUMN_BAD_POSTURE},
                COLUMN_DAY_FK + " = ? AND " + COLUMN_HOUR + " = ?",
                new String[]{String.valueOf(dayId), String.valueOf(hour)},
                null, null, null
        );

        int currentGoodPosture = 0;
        int currentBadPosture = 0;


        if (cursor != null && cursor.moveToFirst()) {

            int goodPostureIndex = cursor.getColumnIndex(COLUMN_GOOD_POSTURE);
            int badPostureIndex = cursor.getColumnIndex(COLUMN_BAD_POSTURE);

            // If the column index is valid (>= 0), get the values
            if (goodPostureIndex >= 0) {
                currentGoodPosture = cursor.getInt(goodPostureIndex);
            }
            if (badPostureIndex >= 0) {
                currentBadPosture = cursor.getInt(badPostureIndex);
            }

            cursor.close();
        }

        // Step 4: Add the new posture time to the existing values
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOOD_POSTURE, currentGoodPosture + goodPostureTime);
        values.put(COLUMN_BAD_POSTURE, currentBadPosture + badPostureTime);

        // Step 5: Update the posture data for the given dayId and hour
        int rowsUpdated = db.update(
                TABLE_HOURS, // Table name
                values, // ContentValues containing the updated data
                COLUMN_DAY_FK + " = ? AND " + COLUMN_HOUR + " = ?",
                new String[]{String.valueOf(dayId), String.valueOf(hour)}
        );

        // If there are no entries for this dayId and hour, insert a new record
        if (rowsUpdated == 0) {

            values.put(COLUMN_DAY_FK, dayId);
            values.put(COLUMN_HOUR, hour);
            db.insert(TABLE_HOURS, null, values);
        }

        Log.d("dBUpdate", "Updated posture data for Day ID: " + dayId + ", Hour: " + hour);
    }

    // =========================================================================================
    // Helper methods to update the posture data

    private int getOrCreateWeekId(String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Extract the week start and end dates based on the given date
        Calendar calendar = Calendar.getInstance();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            calendar.setTime(sdf.parse(date));

            // Get the start and end date of the week
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            String startDate = sdf.format(calendar.getTime());

            calendar.add(Calendar.DATE, 6); // Add 6 days to get the end of the week
            String endDate = sdf.format(calendar.getTime());

            // Check if the week already exists for the given user
            Cursor cursor = db.query(
                    "weeks",
                    new String[]{"week_id"},
                    "start_date = ? AND end_date = ?",
                    new String[]{startDate, endDate},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) { // If week already exists
                int weekId = cursor.getInt(cursor.getColumnIndexOrThrow("week_id"));
                cursor.close();
                return weekId; // Return existing weekId
            } else { // If week does not exist
                if (cursor != null) {
                    cursor.close();
                }

                // Insert a new week
                ContentValues values = new ContentValues();
                values.put("start_date", startDate);
                values.put("end_date", endDate);
                long weekId = db.insert("weeks", null, values);

                return (int) weekId; // Return the new week ID
            }
        } catch (ParseException e) {
            Log.e("dBUpdate", "Error parsing date: " + date, e);
            return -1;
        }
    }

    private int insertDayIfNeeded(int weekId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Extract the day of the week
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            calendar.setTime(sdf.parse(date));
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday, ...

            // Convert the integer value to the corresponding day name
            String dayOfWeekName = getDayOfWeek(dayOfWeek);

            // Check if the day already exists
            Cursor cursor = db.query(
                    "days",
                    new String[]{"day_id"},
                    "week_fk = ? AND date = ?",
                    new String[]{String.valueOf(weekId), date},
                    null, null, null);


            if (cursor != null && cursor.moveToFirst()) { // If day exists
                int dayId = cursor.getInt(cursor.getColumnIndexOrThrow("day_id"));
                cursor.close();
                return dayId; // Return existing dayId
            } else { // If day does not exist
                if (cursor != null) {
                    cursor.close();
                }

                // Insert a new day
                ContentValues values = new ContentValues();
                values.put("week_fk", weekId);
                values.put("date", date);
                values.put("day_of_week", dayOfWeekName);
                long rowId = db.insert("days", null, values);

                if (rowId == -1) {
                    return -1;
                }

                return (int) rowId; // Return the new dayId
            }
        } catch (ParseException e) {
            Log.e("dBUpdate", "Error parsing date: " + date, e);
            return -1;
        }
    }


    // =========================================================================================
    // Other helper methods
    private String getDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";

            default: return "Unknown";
        }
    }
   
}
