package com.example.mxinfornetsgcoach;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ConexionSQLiteHelper extends SQLiteOpenHelper {

    private static final String TABLE_USER = "coaches";
    private static final String KEY_ID = "_id";
    private static final String KEY_ID_USER = "idCoach";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_BIOGRAFIA = "biografia";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_HORARIOS = "horarios";
    private static final String KEY_ID_GIMNASIO = "gimnasio";
    private static final String KEY_TOKEN = "token";

    public ConexionSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_COACHES_TABLE =
                "CREATE TABLE " + TABLE_USER + "("
                        + KEY_ID + " INTEGER PRIMARY KEY, "
                        + KEY_ID_USER + " INTEGER, "
                        + KEY_NOMBRE + " TEXT, "
                        + KEY_BIOGRAFIA + "TEXT, "
                        + KEY_EMAIL + " TEXT, "
                        + KEY_HORARIOS + "TEXT, "
                        + KEY_ID_GIMNASIO + "INTEGER, "
                        + KEY_TOKEN + " TEXT)";

        db.execSQL(CREATE_COACHES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
}