package com.example.rlistview.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.updown.R;

/**
 * 上拉加载 下拉刷新 listview  具备添加任意控件  到 头部或底部容器(线性 默认竖直方向)
 * 
 * public boolean upRefreshEnable = true;上拉刷新的开关 默认true 可上拉刷新 
 * public boolean downRefreshEnable = true; 下拉刷新的开关 默认true 可下拉
 * public void setOnRefreshListener(RefreshListener l)设置 刷新监听器 则可上下拉动刷新 默认就可以刷新
 * public void onPulldownRefreshComplete(boolean result) 处理下拉刷新完成后事项 数据加载完后需要调用
 * public void onPullupRefreshComplete(boolean result) 处理上拉刷新完成后事项 数据加载完后需要调用
 * public void addHeaderContainerView(View child)在头部容器(线性)中添加 任意子View
 * public LinearLayout getHeaderContainer() 获取 头容器(线性)
 * public void addFooterContainerView(View child)在底部容器(线性)中添加 任意子View
 * public LinearLayout getFooterContainer()获取 底容器(线性)
 * 
 * @author 赟
 * @time 2015/03/10
 * 
 */
public class RListViewZ extends ListView {

	int firstVisibleItemIndex;// 屏幕显示的第一个item的索引值
	int lastVisibleItemIndex;// 屏幕能见的最后一个item的索引值
	private View header;
	private ImageView headerArrow;
	private ProgressBar headerProgressBar;
	private TextView headerTitle;
	private TextView headerLastUpdated;
	private View footer;
	private ImageView footerArrow;
	private ProgressBar footerProgressBar;
	private TextView footerTitle;
	private TextView footerLastUpdated;

	private int headerWidth;
	private int headerHeight;

	private Animation animation;
	private Animation reverseAnimation;

	private static final int PULL_TO_REFRESH = 0;
	private static final int RELEASE_TO_REFERESH = 1;
	private static final int REFERESHING = 2;
	private static final int DONE = 3;
	private static final float RATIO = 3;
	private static boolean isBack = false;
	/**
	 * 默认为true 可以上下拉动
	 */
	public boolean refereshEnable = true;// 是否可以进行刷新
	private int state;// 当前刷新状态

	private boolean isRecorded;
	private float startY;
	private float firstTempY = 0;
	private float secondTempY = 0;
	private RefreshListener rListener;

	/**
	 * 上拉刷新的开关 默认true 可上拉刷新
	 */
	public boolean upRefreshEnable = true;
	/**
	 * 下拉刷新的开关 默认true 可下拉
	 */
	public boolean downRefreshEnable = true;
	private RelativeLayout headerView;
	private RelativeLayout footerView;
	private LinearLayout footerContainer;
	private LinearLayout headerContainer;
	private int[] locations = new int[2]; // 0位是x轴的值, 1位是y轴的值

	private int mListViewYOnScreen = -1;// 获取Listview左上角在屏幕中y轴的值.

	public RListViewZ(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RListViewZ(Context context) {
		super(context);
		init(context);
	}

	/**
	 * 初始化listview
	 * 
	 * @param context
	 */
	private void init(Context context) {

		animation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(150);
		animation.setFillAfter(true);
		animation.setInterpolator(new LinearInterpolator());

		reverseAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setDuration(150);
		reverseAnimation.setFillAfter(true);
		reverseAnimation.setInterpolator(new LinearInterpolator());

		// 下拉部分
		LayoutInflater inflater = LayoutInflater.from(context);
		header = inflater.inflate(R.layout.headerz, null);
		headerArrow = (ImageView) header.findViewById(R.id.arrow);
		headerProgressBar = (ProgressBar) header.findViewById(R.id.progerssbar);
		headerTitle = (TextView) header.findViewById(R.id.title);
		headerLastUpdated = (TextView) header.findViewById(R.id.updated);
		headerArrow.setMinimumWidth(70);
		headerArrow.setMaxHeight(50);
		headerView = (RelativeLayout) header.findViewById(R.id.headview);
		headerContainer = (LinearLayout) header
				.findViewById(R.id.headcontainer);

		// 上拉部分
		footer = inflater.inflate(R.layout.footerz, null);
		footerArrow = (ImageView) footer.findViewById(R.id.arrow);
		footerArrow.startAnimation(reverseAnimation);// 把箭头方向反转过来
		footerProgressBar = (ProgressBar) footer.findViewById(R.id.progerssbar);
		footerTitle = (TextView) footer.findViewById(R.id.title);
		footerLastUpdated = (TextView) footer.findViewById(R.id.updated);
		footerTitle.setText("上拉刷新");
		footerLastUpdated.setText("上拉刷新");
		footerArrow.setMinimumWidth(70);
		footerArrow.setMaxHeight(50);
		footerView = (RelativeLayout) footer.findViewById(R.id.footview);
		footerContainer = (LinearLayout) footer
				.findViewById(R.id.footcontainer);

		headerView.measure(0, 0);

		headerWidth = headerView.getMeasuredWidth();
		headerHeight = headerView.getMeasuredHeight();

		header.setPadding(0, -1 * headerHeight, 0, 0);// 设置 与界面上边距的距离
		header.invalidate();// 控件重绘

		footer.setPadding(0, 0, 0, -1 * headerHeight);// 设置与界面上边距的距离
		footer.invalidate();// 控件重绘

		addHeaderView(header);
		addFooterView(footer);

		state = DONE;
	}

	/**
	 * 在头部容器(线性 默认竖直方向)中添加 任意子View
	 * @param child
	 */
	public void addHeaderContainerView(View child){
		headerContainer.addView(child);
	}
	/**
	 * 获取 头容器(线性 默认竖直方向)
	 * @return
	 */
	public LinearLayout getHeaderContainer(){
		return headerContainer;
	}
	/**
	 * 在底部容器(线性 默认竖直方向)中添加 任意子View
	 * @param child
	 */
	public void addFooterContainerView(View child){
		footerContainer.addView(child);
	}
	/**
	 * 获取 底容器(线性 默认竖直方向)
	 * @return
	 */
	public LinearLayout getFooterContainer(){
		return footerContainer;
	}
	
	public interface RefreshListener {
		public void pullDownRefresh();

		/**
		 * 上拉加载数据是 从start位置开始加载
		 * @param start
		 */
		public void pullUpRefresh(int start);
	}

	/**
	 * 设置 刷新监听器 则可上下拉动刷新 默认就可以刷新
	 * 
	 * @param l
	 */
	public void setOnRefreshListener(RefreshListener l) {
		rListener = l;
		refereshEnable = true;
	}

	/**
	 * 处理下拉刷新完成后事项
	 */
	public void onPulldownRefreshComplete(boolean result) {
		refereshEnable = false;
		state = DONE;
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
		headerLastUpdated.setText("最后刷新时间：" + sdf.format(new Date()));
		if (result) {
			headerTitle.setText("◆刷新成功！");
			headerProgressBar.setVisibility(View.INVISIBLE);
		} else {
			headerTitle.setText("◎ 刷新失败！");
			headerTitle.setTextColor(Color.RED);
			headerProgressBar.setVisibility(View.INVISIBLE);
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				onHeaderStateChange();
			}
		}, 800);
	}

	/**
	 * 处理上拉刷新完成后事项
	 */
	public void onPullupRefreshComplete(boolean result) {
		refereshEnable = false;
		state = DONE;
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
		footerLastUpdated.setText("最后刷新时间：" + sdf.format(new Date()));
		if (result) {
			onFooterStateChange();
		} else {
			footerTitle.setText("◎ 已经没有更多数据");
			footerTitle.setTextColor(Color.RED);
			footerProgressBar.setVisibility(View.INVISIBLE);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					onFooterStateChange();
				}
			}, 800);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		// 由于 当 其不可上下滚动的时候 需要下拉刷新的功能 所以这部分不需要的
		// //当布局发生变化的时候 判断是否可上下拉动 (listview可滚动就可以上下拉动了)
		// if
		// (getLastVisiblePosition()==getCount()-1&&getFirstVisiblePosition()==0)
		// {
		// //不可上下拉动
		// refereshEnable = false;
		// }else{
		// //listview的高度足够
		// refereshEnable = true;
		// }
	}

	/**
	 * 判断是否可以上拉
	 * 
	 * @return
	 */
	private boolean upEnable() {
		if (getLastVisiblePosition() == getCount() - 1) {

			if (mListViewYOnScreen == -1) {
				// 获取Listview在屏幕中y轴的值.
				this.getLocationOnScreen(locations);
				mListViewYOnScreen = locations[1];
			}

			footerContainer.getLocationOnScreen(locations);
			// 获取headerContainer(头部容器)左下角在屏幕y轴的值.
			int headerContainerYOnScreen = locations[1]
					+ footerContainer.getHeight();
			// 底部容器完全显示 在上拉过程中 出现的状况 前提是(getLastVisiblePosition() == getCount() - 1)
			//另一种是 当listview的高度不够时也满足但是 在MOVE那已经屏蔽了(这种情况根本不可能上拉)
			if (mListViewYOnScreen + getHeight() > headerContainerYOnScreen) {
				// System.out.println("头部容器完全显示.");
				return true;
			}
			// 临界状态 此时 底部容器刚好完全显示
			if (mListViewYOnScreen + getHeight() == headerContainerYOnScreen) {
				// System.out.println("底部容器完全显示.");
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断 是否可以下拉
	 * 
	 * @return
	 */
	private boolean downEnable() {
		// 判断(添加的子view)容器是否完全显示了, 如果没有完全显示,
		// 不执行下拉动画
		// locations = new int[2]; // 0位是x轴的值, 1位是y轴的值
		if (mListViewYOnScreen == -1) {
			// 获取Listview在屏幕中y轴的值.
			this.getLocationOnScreen(locations);
			mListViewYOnScreen = locations[1];
		}

		// 获取headerContainer(头部容器)左上角在屏幕y轴的值.
		headerContainer.getLocationOnScreen(locations);
		int headerContainerYOnScreen = locations[1];
		// 头部容器完全显示 在下拉过程中 出现的状况
		if (mListViewYOnScreen < headerContainerYOnScreen) {
			// System.out.println("头部容器完全显示.");
			return true;
		}
		// 临界状态 此时 头部容器刚好完全显示
		if (mListViewYOnScreen == headerContainerYOnScreen) {
			// System.out.println("头部容器完全显示.");
			return true;
		}
		return false;
	}

	/**
	 * 中央控制台 几科所有的拉动事件皆由此驱动
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		lastVisibleItemIndex = getLastVisiblePosition() - 1;// 因为加有一尾视图，所以这里要咸一
		int totalCounts = getCount() - 1;// 因为给listview加了一头一尾）视图所以这里要减二
		if (refereshEnable) {
			System.out.println(refereshEnable);
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:

				firstTempY = ev.getY();
				isRecorded = false;
				if (downEnable()) {
					if (!isRecorded) {
						startY = ev.getY();
						isRecorded = true;
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (downRefreshEnable && downEnable()) {
					firstTempY = secondTempY;
					secondTempY = ev.getY();
					if (!isRecorded) {
						startY = secondTempY;
						isRecorded = true;
					}
					if (state != REFERESHING) {
						if (state == DONE) {
							if (secondTempY - startY > 0) {
								// 刷新完成 /初始状态--》 进入 下拉刷新
								state = PULL_TO_REFRESH;
								onHeaderStateChange();
							}
						}
						if (state == PULL_TO_REFRESH) {
							if ((secondTempY - startY) / RATIO > headerHeight
									&& secondTempY - firstTempY > 3) {
								// 下啦刷新 --》 松开刷新
								state = RELEASE_TO_REFERESH;
								onHeaderStateChange();
							} else if (secondTempY - startY <= -5) {
								// 下啦刷新 --》 回到 刷新完成
								state = DONE;
								onHeaderStateChange();
							}
						}
						if (state == RELEASE_TO_REFERESH) {
							if (firstTempY - secondTempY > 5) {
								// 松开刷新 --》回到下拉刷新
								state = PULL_TO_REFRESH;
								isBack = true;// 从松开刷新 回到的下拉刷新
								onHeaderStateChange();
							} else if (secondTempY - startY <= -5) {
								// 松开刷新 --》 回到 刷新完成
								state = DONE;
								onHeaderStateChange();
							}
						}

						if (state == PULL_TO_REFRESH
								|| state == RELEASE_TO_REFERESH) {
							header.setPadding(0, (int) ((secondTempY - startY)
									/ RATIO - headerHeight), 0, 0);
						}
					} else {
					}
				}

				// 上拉状态改变的动画
				// 其实现前提条件是（当listview的高度没有达到可滚动的条件(listview的高度大于其显示的高度)则无法上拉）
				if (upRefreshEnable && !downEnable() && upEnable()) {
					firstTempY = secondTempY;
					secondTempY = ev.getY();
					if (!isRecorded) {
						startY = secondTempY;
						isRecorded = true;
					}

					if (state != REFERESHING) {// 不是正在刷新状态
						if (state == DONE) {
							if (startY - secondTempY > 0) {
								// 刷新完成/初始状态 --》 进入 下拉刷新
								state = PULL_TO_REFRESH;
								onFooterStateChange();
							}
						}
						if (state == PULL_TO_REFRESH) {
							if ((startY - secondTempY) / RATIO > headerHeight
									&& firstTempY - secondTempY >= 9) {
								// 上拉刷新 --》 松开刷新
								state = RELEASE_TO_REFERESH;
								onFooterStateChange();
							} else if (startY - secondTempY <= 0) {
								// 上拉刷新 --》 回到 刷新完成
								state = DONE;
								onFooterStateChange();
							}
						}
						if (state == RELEASE_TO_REFERESH) {
							if (firstTempY - secondTempY < -5) {
								state = PULL_TO_REFRESH;
								isBack = true;// 从松开刷新 回到的上拉刷新
								onFooterStateChange();
							} else if (secondTempY - startY >= 0) {
								// 松开刷新 --》 回到 刷新完成
								state = DONE;
								onFooterStateChange();
							}
						}
						if ((state == PULL_TO_REFRESH || state == RELEASE_TO_REFERESH)
								&& secondTempY < startY) {
							footer.setPadding(
									0,
									0,
									0,
									(int) ((startY - secondTempY) / RATIO - headerHeight));
						}
					} else {
					}
				}
				break;

			case MotionEvent.ACTION_UP:
				if (state != REFERESHING) {

					if (state == PULL_TO_REFRESH) {
						state = DONE;
						if (downEnable()) {// 下拉
							onHeaderStateChange();
						} else if (upEnable())
							// 上拉状态发生变化的前提是getFirstVisiblePosition() ！= 0
							onFooterStateChange();
					}
					if (state == RELEASE_TO_REFERESH) {
						state = REFERESHING;
						if (downEnable()) {
							// 下拉
							onHeaderStateChange();
							onPullDownRefresh();// 刷新得到服务器数据
						} else if (upEnable()) {
							// 上拉
							onFooterStateChange();
							onPullUpRefresh(getLastVisiblePosition()-1);// 刷新得到服务器数据
						}
					}
				}
				break;
			}
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 更改尾视图显示状态
	 */
	private void onHeaderStateChange() {
		switch (state) {
		case PULL_TO_REFRESH:
			headerProgressBar.setVisibility(View.GONE);
			headerArrow.setVisibility(View.VISIBLE);
			headerTitle.setVisibility(View.VISIBLE);
			headerLastUpdated.setVisibility(View.VISIBLE);

			headerTitle.setText("下拉刷新");
			headerArrow.clearAnimation();
			if (isBack) {
				headerArrow.startAnimation(animation);
				isBack = false;
			}
			break;

		case RELEASE_TO_REFERESH:
			headerProgressBar.setVisibility(View.GONE);
			headerArrow.setVisibility(View.VISIBLE);
			headerTitle.setVisibility(View.VISIBLE);
			headerLastUpdated.setVisibility(View.VISIBLE);

			headerTitle.setText(" 松开刷新");
			headerArrow.clearAnimation();
			headerArrow.startAnimation(reverseAnimation);
			break;

		case REFERESHING:
			headerProgressBar.setVisibility(View.VISIBLE);
			headerArrow.setVisibility(View.GONE);
			headerTitle.setVisibility(View.VISIBLE);
			headerLastUpdated.setVisibility(View.VISIBLE);

			headerTitle.setText("正在刷新");
			headerArrow.clearAnimation();

			header.setPadding(0, 0, 0, 0);
			break;
		case DONE:
			headerProgressBar.setVisibility(View.GONE);
			headerArrow.setVisibility(View.VISIBLE);
			headerTitle.setVisibility(View.VISIBLE);
			headerLastUpdated.setVisibility(View.VISIBLE);
			headerTitle.setText("下拉刷新");
			headerTitle.setTextColor(Color.BLACK);
			headerArrow.clearAnimation();
			header.setPadding(0, -1 * headerHeight, 0, 0);
			refereshEnable = true;
			break;
		}
	}

	/**
	 * 更改尾视图显示状态
	 */
	private void onFooterStateChange() {
		switch (state) {
		case PULL_TO_REFRESH:
			footerProgressBar.setVisibility(View.GONE);
			footerArrow.setVisibility(View.VISIBLE);
			footerTitle.setVisibility(View.VISIBLE);
			footerLastUpdated.setVisibility(View.VISIBLE);

			footerTitle.setText("上拉刷新");
			footerArrow.clearAnimation();
			if (isBack) {
				footerArrow.startAnimation(animation);
				isBack = false;
			}
			break;

		case RELEASE_TO_REFERESH:
			footerProgressBar.setVisibility(View.GONE);
			footerArrow.setVisibility(View.VISIBLE);
			footerTitle.setVisibility(View.VISIBLE);
			footerLastUpdated.setVisibility(View.VISIBLE);

			footerTitle.setText(" 松开刷新");
			footerArrow.clearAnimation();
			footerArrow.startAnimation(reverseAnimation);
			break;

		case REFERESHING:
			footerProgressBar.setVisibility(View.VISIBLE);
			footerArrow.setVisibility(View.GONE);
			footerTitle.setVisibility(View.VISIBLE);
			footerLastUpdated.setVisibility(View.VISIBLE);

			footerTitle.setText("正在刷新");
			footerArrow.clearAnimation();

			footer.setPadding(0, 0, 0, 0);
			break;
		case DONE:
			footerProgressBar.setVisibility(View.GONE);
			footerArrow.setVisibility(View.VISIBLE);
			footerTitle.setVisibility(View.VISIBLE);
			footerLastUpdated.setVisibility(View.VISIBLE);

			footerTitle.setText("上拉刷新");
			footerTitle.setTextColor(Color.BLACK);
			footerArrow.clearAnimation();
			footer.setPadding(0, 0, 0, -1 * headerHeight);
			refereshEnable = true;
			break;
		}
	}

	/**
	 * 下拉刷新的实现方法
	 */
	private void onPullDownRefresh() {
		if (rListener != null) {
			rListener.pullDownRefresh();
		}
	}

	/**
	 * 上拉刷新的实现方法
	 */
	private void onPullUpRefresh(int start) {
		if (rListener != null)
			rListener.pullUpRefresh(start);
	}

}
