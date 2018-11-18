package com.database;

/**
 * Created by duwei on 18-5-14.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import net.sqlcipher.DatabaseErrorHandler;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import net.sqlcipher.database.SQLiteDatabaseHook;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.util.Log;

import com.aop.DebugTrace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DatabaseHelper extends SQLiteOpenHelper{
    public static final String TABLE_NAME = "PersonTable";
    public static final String TABLE_NUMBER = "numbers";
    private static final String DATABASE_NAME = "myDatabase";
    public static final int DATABASE_VERSION = 9;
    private static final String TAG = "xx";
    private Context mContext;
    private final int COUNT = 40000;
    private String mDbName;

    // 构造函数，调用父类SQLiteOpenHelper的构造函数
    @SuppressLint("NewApi")
    public DatabaseHelper(Context context, String name, CursorFactory factory,
                          int version, SQLiteDatabaseHook hook, DatabaseErrorHandler errorHandler)
    {
        super(context, name, factory, version, hook, errorHandler);
        mContext = context;
        mDbName = name;
    }

    public DatabaseHelper(Context context, String name, CursorFactory factory,
                          int version)
    {
        super(context, name, factory, version);
        // SQLiteOpenHelper的构造函数参数：
        // context：上下文环境
        // name：数据库名字
        // factory：游标工厂（可选）
        // version：数据库模型版本号
        mContext = context;
        mDbName = name;
    }

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // 数据库实际被创建是在getWritableDatabase()或getReadableDatabase()方法调用时
        // CursorFactory设置为null,使用系统默认的工厂类
        mContext = context;
        mDbName = DATABASE_NAME;
        Log.d(AppConstants.LOG_TAG, mDbName+" DatabaseHelper Constructor");
    }

    // 继承SQLiteOpenHelper类,必须要覆写的三个方法：onCreate(),onUpgrade(),onOpen()
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // 调用时间：数据库第一次创建时onCreate()方法会被调用

        // onCreate方法有一个 SQLiteDatabase对象作为参数，根据需要对这个对象填充表和初始化数据
        // 这个方法中主要完成创建数据库后对数据库的操作

        Log.d(AppConstants.LOG_TAG, mDbName+" DatabaseHelper onCreate");

        // 构建创建表的SQL语句（可以从SQLite Expert工具的DDL粘贴过来加进StringBuffer中）
        StringBuffer sBuffer = new StringBuffer();

        sBuffer.append("CREATE TABLE [" + TABLE_NAME + "] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[name] TEXT,");
        sBuffer.append("[age] INTEGER,");
        sBuffer.append("[info] BLOB)");

        // 执行创建表的SQL语句
        db.execSQL(sBuffer.toString());
        // 即便程序修改重新运行，只要数据库已经创建过，就不会再进入这个onCreate方法
        /*try {
            initTableNumber(db);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @DebugTrace
    public void initTableNumber(SQLiteDatabase db) throws Exception {
        Log.d(AppConstants.LOG_TAG, mDbName+" DBManager --> initTableNumber");
        String sqlstr = "insert into " + DatabaseHelper.TABLE_NAME + " values (null,?,?,?);";
        // 采用事务处理，确保数据完整性
        db.beginTransaction(); // 开始事务
        try {
            long start = System.currentTimeMillis();
            for (int i=0; i < COUNT; i++) {
                db.execSQL(sqlstr, new Object[] {
                        "com.dw.debug" ,
                        System.currentTimeMillis()/1000 ,
//                        CipherUtils.encrypt("hi欧符合后额话费fdjdifjid".getBytes()) ,
                        CipherUtils.encrypt("hi欧符合后额话费fdjdifjid".getBytes("UTF-8")) ,
//                        "hi欧符合后额话费fdjdifjid".getBytes(),
                });
                // 带两个参数的execSQL()方法，采用占位符参数？，把参数值放在后面，顺序对应
                // 一个参数的execSQL()方法中，用户输入特殊字符时需要转义
                // 使用占位符有效区分了这种情况
            }
            Log.d(TAG, "init number table takes " + (System.currentTimeMillis() - start));
            db.setTransactionSuccessful(); // 设置事务成功完成
            writeToFile();
        } finally {
            db.endTransaction(); // 结束事务
        }
    }

    public void writeToFile(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File(mContext.getFilesDir(), "data.txt"));
                    for (int i=0; i < COUNT; i++) {
                        fos.write("a".getBytes());
                        fos.write(String.valueOf(System.currentTimeMillis()).getBytes());
//                        fos.write(CipherUtils.encrypt("hi欧符合后额话费fdjdifjid".getBytes()));
                        fos.write(CipherUtils.encrypt("hi欧符合后额话费fdjdifjid".getBytes("UTF-8")));
//                        fos.write("hi欧符合后额话费fdjdifjid".getBytes());
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        t.start();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // 调用时间：如果DATABASE_VERSION值被改为别的数,系统发现现有数据库版本不同,即会调用onUpgrade

        // onUpgrade方法的三个参数，一个 SQLiteDatabase对象，一个旧的版本号和一个新的版本号
        // 这样就可以把一个数据库从旧的模型转变到新的模型
        // 这个方法中主要完成更改数据库版本的操作

        Log.d(AppConstants.LOG_TAG, mDbName+" DatabaseHelper onUpgrade");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        // 上述做法简单来说就是，通过检查常量值来决定如何，升级时删除旧表，然后调用onCreate来创建新表
        // 一般在实际项目中是不能这么做的，正确的做法是在更新数据表结构时，还要考虑用户存放于数据库中的数据不丢失

    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        // 每次打开数据库之后首先被执行

        Log.d(AppConstants.LOG_TAG, mDbName+" DatabaseHelper onOpen");
    }

}
