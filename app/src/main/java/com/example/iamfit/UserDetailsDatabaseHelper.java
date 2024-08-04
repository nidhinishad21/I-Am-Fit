package com.example.iamfit;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDetailsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_activities.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ACTIVITIES = "activities";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_ACTIVITY_NAME = "activity_name";

    private static final String TABLE_USER_DETAILS = "user_details";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_HEIGHT = "height";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_DATE_OF_BIRTH = "date_of_birth";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public UserDetailsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ACTIVITIES_TABLE = "CREATE TABLE " + TABLE_ACTIVITIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_CALORIES + " INTEGER,"
                + COLUMN_ACTIVITY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_ACTIVITIES_TABLE);

        String CREATE_USER_DETAILS_TABLE = "CREATE TABLE " + TABLE_USER_DETAILS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_HEIGHT + " REAL,"
                + COLUMN_WEIGHT + " REAL,"
                + COLUMN_DATE_OF_BIRTH + " TEXT" + ")";
        db.execSQL(CREATE_USER_DETAILS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DETAILS);
        onCreate(db);
    }

    public void addActivity(String type, String date, int calories, String activityName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_ACTIVITY_NAME, activityName);
        db.insert(TABLE_ACTIVITIES, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public List<ActivityItem> getActivities(String type, String date) {
        List<ActivityItem> activityList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ACTIVITIES +
                " WHERE " + COLUMN_TYPE + " = ? AND " + COLUMN_DATE + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{type, date});

        if (cursor.moveToFirst()) {
            do {
                ActivityItem activity = new ActivityItem();
                activity.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                activity.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                activity.setCalories(cursor.getInt(cursor.getColumnIndex(COLUMN_CALORIES)));
                activity.setActivityName(cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITY_NAME)));
                activityList.add(activity);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return activityList;
    }

    public void addUserDetails(float height, float weight, Date dateOfBirth) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_DATE_OF_BIRTH, dateFormat.format(dateOfBirth));
        db.insert(TABLE_USER_DETAILS, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public UserDetails getUserDetails() {
        UserDetails userDetails = new UserDetails();
        String selectQuery = "SELECT * FROM " + TABLE_USER_DETAILS + " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            userDetails.setHeight(cursor.getFloat(cursor.getColumnIndex(COLUMN_HEIGHT)));
            userDetails.setWeight(cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT)));
            String dobString = cursor.getString(cursor.getColumnIndex(COLUMN_DATE_OF_BIRTH));
            try {
                userDetails.setDateOfBirth(dateFormat.parse(dobString));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        db.close();
        return userDetails;
    }
}