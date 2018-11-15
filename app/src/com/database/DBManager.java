package com.database;

/**
 * Created by duwei on 18-5-14.
 */

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.util.Log;

import com.aop.DebugTrace;
import com.dw.DynamicApplication;

//参考：http://blog.csdn.net/liuhe688/article/details/6715983
public class DBManager {
    private static final String TAG = "DBManager";
    private DatabaseHelper helper;
    private SQLiteDatabase db;
    private DatabaseHelper helper2;
    private SQLiteDatabase db2;

    public DBManager(Context context) {
        Log.d(AppConstants.LOG_TAG, "DBManager --> Constructor");
        helper = new DatabaseHelper(context);
//        helper = new DatabaseHelper(context, null, null, DatabaseHelper.DATABASE_VERSION);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase("myKey");
    }

    /**
     * add persons
     *
     * @param persons
     */
    public void add(List<Person> persons) throws Exception {
        Log.d(AppConstants.LOG_TAG, "DBManager --> add");
        // 采用事务处理，确保数据完整性
        db.beginTransaction(); // 开始事务
        try {
            for (Person person : persons) {
                db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_NAME
                        + " VALUES(null, ?, ?, ?)", new Object[] { person.name,
                        person.age, person.info.getBytes() });
                // 带两个参数的execSQL()方法，采用占位符参数？，把参数值放在后面，顺序对应
                // 一个参数的execSQL()方法中，用户输入特殊字符时需要转义
                // 使用占位符有效区分了这种情况
            }
            db.setTransactionSuccessful(); // 设置事务成功完成
        } finally {
            db.endTransaction(); // 结束事务
        }
//        helper.initTableNumber(db);
    }

    /**
     * update person's age
     *
     * @param person
     */
    public void updateAge(Person person) {
        Log.d(AppConstants.LOG_TAG, "DBManager --> updateAge");
        ContentValues cv = new ContentValues();
        cv.put("age", person.age);
        db.update(DatabaseHelper.TABLE_NAME, cv, "name = ?",
                new String[] { person.name });
    }

    /**
     * delete old person
     *
     * @param person
     */
    public void deleteOldPerson(Person person) {
        Log.d(AppConstants.LOG_TAG, "DBManager --> deleteOldPerson");
        db.delete(DatabaseHelper.TABLE_NAME, "age >= ?",
                new String[] { String.valueOf(person.age) });
    }

    /**
     * query all persons, return list
     *
     * @return List<Person>
     */
    @DebugTrace
    public List<Person> query() throws Exception {
        Log.d(AppConstants.LOG_TAG, "DBManager --> query");
        ArrayList<Person> persons = new ArrayList<Person>();
        Cursor c = queryTheCursor();
        FileOutputStream fos = new FileOutputStream(new File(DynamicApplication.mRealApplication.getFilesDir(), "backup.txt"));
        while (c.moveToNext()) {
            Person person = new Person();
            person._id = c.getInt(c.getColumnIndex("_id"));
            person.name = c.getString(c.getColumnIndex("name"));
            person.age = c.getInt(c.getColumnIndex("age"));
//            person.info = new String(c.getBlob(c.getColumnIndex("info")));
//            person.info = new String(CipherUtils.decrypt(c.getBlob(c.getColumnIndex("info"))));
            person.info = new String(c.getBlob(c.getColumnIndex("info")));
            persons.add(person);
            String line = c.getString(c.getColumnIndex("name")) + c.getInt(c.getColumnIndex("age")) +
                    new String(c.getBlob(c.getColumnIndex("info")));
            fos.write(line.getBytes());
        }
        c.close();
        fos.close();
        return persons;
    }

    /**
     * query all persons, return cursor
     *
     * @return Cursor
     */
    public Cursor queryTheCursor() {
        Log.d(AppConstants.LOG_TAG, "DBManager --> queryTheCursor");
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME,
                null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        Log.d(AppConstants.LOG_TAG, "DBManager --> closeDB");
        // 释放数据库资源
        db.close();
    }

}
