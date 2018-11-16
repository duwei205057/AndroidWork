package com.dw.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

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
public class BookDetailActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// ָ������/res/layoutĿ¼�µ�activity_book_detail.xml�����ļ�
		// �ý��沼���ļ���ֻ������һ����Ϊbook_detail_container��FrameLayout
		setContentView(R.layout.activity_book_detail);
		// ��ActionBar��Ӧ��ͼ��ת���ɿɵ���İ�ť
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null)
		{
			// ����BookDetailFragment����
			BookDetailFragment fragment = new BookDetailFragment();
			// ����Bundle����
			Bundle arguments = new Bundle();
			arguments.putInt(BookDetailFragment.ITEM_ID, getIntent()
				.getIntExtra(BookDetailFragment.ITEM_ID, 0));
			// ��Fragment�������
			fragment.setArguments(arguments);
			// ��ָ��fragment��ӵ�book_detail_container������
			getFragmentManager().beginTransaction()
					.add(R.id.book_detail_container, fragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			// ��������BookListActivity��Intent
			Intent intent = new Intent(this, BookListActivity.class);
			// ��Ӷ����Flag����Activityջ�д���FirstActivity֮�ϵ�Activity����
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// ����intent��Ӧ��Activity
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
