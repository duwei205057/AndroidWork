package com.database;

import android.app.Activity;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.aop.DebugTrace;
import com.dw.R;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by duwei on 18-5-14.
 */

public class DBActivity extends Activity {

    private DBManager dbManager;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_activity);
        SQLiteDatabase.loadLibs(this);
        listView = (ListView) findViewById(R.id.listView);
        // 初始化DBManager
        dbManager = new DBManager(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.hello_db, menu);
        return true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        dbManager.closeDB();// 释放数据库资源
    }

    public void add(View view) throws Exception {
        ArrayList<Person> persons = new ArrayList<Person>();

        Person person1 = new Person("Ella", 22, "lively girl");
        Person person2 = new Person("Jenny", 22, "beautiful girl");
        Person person3 = new Person("Jessica", 23, "sexy girl");
        Person person4 = new Person("Kelly", 23, "hot baby");
        Person person5 = new Person("Jane", 25, "a pretty woman");

        persons.add(person1);
        persons.add(person2);
        persons.add(person3);
        persons.add(person4);
        persons.add(person5);

        dbManager.add(persons);
    }

    public void update(View view)
    {
        // 把Jane的年龄改为30（注意更改的是数据库中的值，要查询才能刷新ListView中显示的结果）
        Person person = new Person();
        person.name = "Jane";
        person.age = 30;
        dbManager.updateAge(person);
    }

    public void delete(View view)
    {
        // 删除所有三十岁以上的人（此操作在update之后进行，Jane会被删除（因为她的年龄被改为30））
        // 同样是查询才能查看更改结果
        Person person = new Person();
        person.age = 30;
        dbManager.deleteOldPerson(person);
    }

    public void query(View view) throws Exception {
        List<Person> persons = dbManager.query();
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Person person : persons)
        {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("name", person.name);
            map.put("info", person.age + " years old, " + person.info);
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list,
                android.R.layout.simple_list_item_2, new String[] { "name",
                "info" }, new int[] { android.R.id.text1,
                android.R.id.text2 });
        listView.setAdapter(adapter);
    }

    @SuppressWarnings("deprecation")
    public void queryTheCursor(View view)
    {
        Cursor c = dbManager.queryTheCursor();
        startManagingCursor(c); // 托付给activity根据自己的生命周期去管理Cursor的生命周期
        CursorWrapper cursorWrapper = new CursorWrapper(c)
        {
            @Override
            public String getString(int columnIndex)
            {
                // 将简介前加上年龄
                if (getColumnName(columnIndex).equals("info"))
                {
                    int age = getInt(getColumnIndex("age"));
                    return age + " years old, " + new String(super.getBlob(columnIndex));
                }
                return super.getString(columnIndex);
            }
        };
        // 确保查询结果中有"_id"列
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, cursorWrapper,
                new String[] { "name", "info" }, new int[] {
                android.R.id.text1, android.R.id.text2 });
        listView.setAdapter(adapter);
    }

}
