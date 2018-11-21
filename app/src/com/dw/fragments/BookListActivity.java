package com.dw.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.dw.R;

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
public class BookListActivity extends Activity implements
		BookListFragment.Callbacks
{
	// ����һ����꣬���ڱ�ʶ��Ӧ���Ƿ�֧�ִ���Ļ
	private boolean mTwoPane;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// ָ������R.layout.activity_book_list��Ӧ�Ľ��沼���ļ�
		// ��ʵ���ϸ�Ӧ�û������Ļ�ֱ��ʼ��ڲ�ͬ�Ľ��沼���ļ�
		setContentView(R.layout.activity_book_list);
		// ������صĽ��沼���ļ��а���IDΪbook_detail_container�����
		if (findViewById(R.id.book_detail_container) != null)
		{
			mTwoPane = true;
			((BookListFragment) getFragmentManager()
				.findFragmentById(R.id.book_list))
				.setActivateOnItemClick(true);
		}
	}

	@Override
	public void onItemSelected(Integer id)
	{
		if (mTwoPane)
		{
			// ����Bundle��׼����Fragment�������
			Bundle arguments = new Bundle();
			arguments.putInt(BookDetailFragment.ITEM_ID, id);
			// ����BookDetailFragment����
			BookDetailFragment fragment = new BookDetailFragment();
			// ��Fragment�������
			fragment.setArguments(arguments);
			// ʹ��fragment�滻book_detail_container������ǰ��ʾ��Fragment
			getFragmentManager().beginTransaction()
					.replace(R.id.book_detail_container, fragment).commit();

		}
		else
		{
			// ��������BookDetailActivity��Intent
			Intent detailIntent = new Intent(this, BookDetailActivity.class);
			// ���ô���BookDetailActivity�Ĳ���
			detailIntent.putExtra(BookDetailFragment.ITEM_ID, id);
			// ����Activity
			startActivity(detailIntent);
		}
	}
}
