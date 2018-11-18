package com.dw.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dw.R;
import com.dw.fragments.model.BookContent;

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
public class BookDetailFragment extends Fragment
{
	public static final String ITEM_ID = "item_id";
	// �����Fragment��ʾ��Book����
	BookContent.Book book;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// ���������Fragmentʱ������ITEM_ID����
		if (getArguments().containsKey(ITEM_ID))
		{
			book = BookContent.ITEM_MAP.get(getArguments()
				.getInt(ITEM_ID));
		}
	}

	// ��д�÷������÷������ص�View����ΪFragment��ʾ�����
	@Override
	public View onCreateView(LayoutInflater inflater
		, ViewGroup container, Bundle savedInstanceState)
	{
		// ����/res/layout/Ŀ¼�µ�fragment_book_detail.xml�����ļ�
		View rootView = inflater.inflate(R.layout.fragment_book_detail,
				container, false);
		if (book != null)
		{
			// ��book_title�ı�����ʾbook�����title����
			((TextView) rootView.findViewById(R.id.book_title))
					.setText(book.title);
			// ��book_desc�ı�����ʾbook�����desc����
			((TextView) rootView.findViewById(R.id.book_desc))
				.setText(book.desc);		
		}
		return rootView;
	}
}
