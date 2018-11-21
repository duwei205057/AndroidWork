package com.dw.fragments.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * <br/>website: <a href="http://www.crazyit.org">crazyit.org</a>
 * <br/>Copyright (C), 2001-2014, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author Yeeku.H.Lee kongyeeku@163.com
 * @version 1.0
 */
public class BookContent
{
	public static class Book
	{

		public Integer id;
		public String title;
		public String desc;

		public Book(Integer id, String title, String desc)
		{
			this.id = id;
			this.title = title;
			this.desc = desc;
		}

		@Override
		public String toString()
		{
			return title;
		}
	}

	public static List<Book> ITEMS = new ArrayList<Book>();
	public static Map<Integer, Book> ITEM_MAP 
		= new HashMap<Integer, Book>();

	static
	{
		addItem(new Book(1, "���Java����"
			, "һ��ȫ�桢�����Javaѧϰͼ�飬�ѱ���Ҹ�Уѡ���̲ġ�"));
		addItem(new Book(2, "���Android����"
			, "Androidѧϰ�ߵ���ѡͼ�飬����ռ�ݾ���������������ѷ3����վAndroid�������а�İ���"));
		addItem(new Book(3, "������Java EE��ҵӦ��ʵս"
			, "ȫ�����Java EE������Struts 2��Spring 3��Hibernate 4���"));
	}

	private static void addItem(Book book)
	{
		ITEMS.add(book);
		ITEM_MAP.put(book.id, book);
	}
}
