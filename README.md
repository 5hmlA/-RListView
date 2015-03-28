# -RListView
下拉刷新功能，上拉加载功能，两个功能的开关

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