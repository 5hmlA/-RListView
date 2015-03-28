package com.example.updown;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rlistview.view.RListViewZ;
import com.example.rlistview.view.RListViewZ.RefreshListener;

public class MainActivity extends Activity {

	private RListViewZ rlv;
	private ArrayList<String> data;
	private MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		rlv = (RListViewZ) findViewById(R.id.rlv);
		bt = (Button) findViewById(R.id.bt);

		initData();
		adapter = new MyAdapter();
		rlv.setAdapter(adapter);
		// rlv.upRefreshEnable = false;
		rlv.setOnRefreshListener(new RefreshListener() {

			@Override
			public void pullUpRefresh(int start) {
				Toast.makeText(getApplicationContext(), ""+start, 0).show();
				getDataFromInternet();
			}

			@Override
			public void pullDownRefresh() {
				new AsyncTask<Void, Integer, Boolean>() {

					@Override
					protected Boolean doInBackground(Void... params) {
						SystemClock.sleep(1500);
						if (Math.random() * 10 + 4 > 10) {
							return false;
						}
						data.add(0, "新获取的数据。。。");
						return true;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						rlv.onPulldownRefreshComplete(result);
						adapter.notifyDataSetChanged();
					}
				}.execute();
			}
		});

		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ScaleAnimation sa = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, 1,
						0.5f, 1, 0.5f);
				sa.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						ScaleAnimation sa2 = new ScaleAnimation(1.2f, 1f, 1.2f,
								1f, 1, 0.5f, 1, 0.5f);
						sa2.setDuration(300);
						sa2.setFillAfter(true);
						bt.startAnimation(sa2);
					}
				});
				sa.setDuration(300);
				sa.setFillAfter(true);
				bt.startAnimation(sa);

				data.add("button新增加的数据。。");
				adapter.notifyDataSetChanged();
			}
		});
		bt.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				ImageView child = new ImageView(MainActivity.this);
				child.setBackgroundResource(R.drawable.ic_launcher);
				if (rlv.getFirstVisiblePosition()==0) {
					rlv.addHeaderContainerView(child);
				}else if (rlv.getLastVisiblePosition()==rlv.getCount()-1) {
					rlv.addFooterContainerView(child);
				}
				return true;//消费长按事件  之后不会触发点击事件
			}
		});
	}

	int count = 0;
	private Button bt;

	protected void getDataFromInternet() {
		count++;
		new AsyncTask<Void, Integer, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				SystemClock.sleep(1500);
				if (count > 3) {
					return false;
				}
				for (int i = 0; i < 5; i++) {
					data.add("新获取的数据：" + i);
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				rlv.onPullupRefreshComplete(result);
			}

		}.execute();

	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(MainActivity.this);
			tv.setText(data.get(position));
			tv.setPadding(20, 10, 10, 10);
			tv.setTextSize(15);
			return tv;
		}

	}

	private void initData() {
		data = new ArrayList<String>();
		for (int i = 0; i < 14; i++) {
			data.add("原始数据：" + i);
		}
	}

}
