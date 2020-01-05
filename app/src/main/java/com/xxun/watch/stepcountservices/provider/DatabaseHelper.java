package com.xxun.watch.stepcountservices.provider;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;
    private Context _context;
    private static final int VERSION = 1;

    public DatabaseHelper(Context context, String name, CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
        _context = context;

    }

    public DatabaseHelper(Context context, String name) {
        this(context, name, null, VERSION);
        db = this.getReadableDatabase();
        // File file = new File(Environment.getExternalStorageDirectory()
        // + "/mypackage" + "/" + name + ".sqllite");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ MyContentProviderMetaData.UserTableMetaData.TABLE_NAME+" (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                MyContentProviderMetaData.UserTableMetaData.USER_NAME+" TEXT" + //cfg的值
                ");");
        Log.i("aaaaa","this is database Helper onCreate!");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("db upgrade");
    }

}