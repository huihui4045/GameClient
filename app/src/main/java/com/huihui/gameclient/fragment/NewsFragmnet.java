package com.huihui.gameclient.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.huihui.gameclient.R;
import com.huihui.gameclient.activity.ChapterDetialActivity;
import com.huihui.gameclient.adapter.ChapterAdapter;
import com.huihui.gameclient.adapter.ViewPagerAdapter;
import com.huihui.gameclient.constant.Constant;
import com.huihui.gameclient.databases.MyChapterDao;
import com.huihui.gameclient.entities.ChapterListItem;
import com.huihui.gameclient.utils.HttpUtils;
import com.huihui.gameclient.utils.JsonUtil;
import com.huihui.gameclient.utils.NetUtil;
import com.huihui.gameclient.utils.UrlUtil;
import com.huihui.gameclient.view.XListView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFragmnet#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragmnet extends Fragment implements Handler.Callback, AdapterView.OnItemClickListener, XListView.IXListViewListener, ViewPager.OnPageChangeListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";


    private String mParam1;
    private String mParam2;
    private String mParam3;
    private int hashCode = this.hashCode();

    private String TAG = getClass().getSimpleName();
    private XListView listView;

    private Handler handler = new Handler(this);
    private ArrayList<ChapterListItem> list;
    private ChapterAdapter adapter;

    private Context context;

    private MyChapterDao dao;

    private int currentPage = 1;
    /**
     * 下拉刷新
     */
    private static final int LOAD_FLAG_PULL = 0;
    /**
     * 上拉加载
     */
    private static final int LOAD_FLAG_DOWM = 2;
    /**
     * 普通加载
     */
    private static final int LOAD_FLAG_NOR = 1;


    private ViewPager viewPage;


    private ImageView[] dots;
    private ViewGroup mViewGroup;
    private boolean isLoop = true;
    private String[] typeids;

    private int[] getImageResIDs() {
        return new int[]{
                R.mipmap.default1,
                R.mipmap.default2,
                R.mipmap.default3,

        };
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            mParam3 = getArguments().getString(ARG_PARAM3);
        }

        context = getActivity();
        dao = new MyChapterDao(context);


    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_fragmnet, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        listView = ((XListView) view.findViewById(R.id.list_view));

        listView.setPullLoadEnable(true);

        listView.setPullRefreshEnable(true);

        listView.setXListViewListener(this);
        list = new ArrayList<>();
        adapter = new ChapterAdapter(list, getActivity());
        listView.setAdapter(adapter);

        List<ChapterListItem> listItems = null;


        listView.setOnItemClickListener(this);
        if (mParam3.equals("0")) {
            initViewPage();
            typeids = context.getResources().getStringArray(R.array.str_typeid);
            listItems = dao.getAllDataByPage(currentPage, Constant.ROW, typeids);
        } else {

            listItems = getDataFromSQLByPage(currentPage, Constant.ROW, mParam2);
        }

        if (savedInstanceState == null) {
            if (listItems != null && listItems.size() > 0) {
                adapter.setListEnd(listItems);
                adapter.notifyDataSetChanged();
                currentPage++;
            } else {
                getData(currentPage + "", mParam2, LOAD_FLAG_NOR);
            }
        } else {


            listItems = savedInstanceState.getParcelableArrayList("data");

            Log.i(TAG, "savedInstanceState != null" + listItems.size());
            adapter.setListEnd(listItems);

            adapter.notifyDataSetChanged();

        }


        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);

    }

    private void initViewPage() {

        List<ImageView> list = new ArrayList<>(getImageResIDs().length);

        for (int i = 0; i < getImageResIDs().length; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setBackgroundResource(getImageResIDs()[i]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            list.add(imageView);
        }

        View view = LayoutInflater.
                from(context).inflate(R.layout.banner, null);

        viewPage = (ViewPager) view.findViewById(R.id.viewpage);

        mViewGroup = ((ViewGroup) view.findViewById(R.id.viewGroup));


        dots = new ImageView[list.size()];

        for (int i = 0; i < dots.length; i++) {

            ImageView imageView = new ImageView(context);

            imageView.setImageResource(R.drawable.select_dot);

            dots[i] = imageView;
            /*if (i == 0) {
                dots[i].setEnabled(true);
            } else {
                dots[i].setEnabled(false);
            }*/

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = 5;

            layoutParams.rightMargin = 5;

            imageView.setEnabled(false);

            mViewGroup.addView(imageView, layoutParams);

        }


        listView.addHeaderView(view);

        viewPage.setAdapter(new ViewPagerAdapter(list));

        int n = Integer.MAX_VALUE / 2 % list.size();
        int itemPosition = Integer.MAX_VALUE / 2 - n;

        mViewGroup.getChildAt(previousSelectPosition).setEnabled(true);

        viewPage.setCurrentItem(Integer.MAX_VALUE / 2);

        viewPage.setOnPageChangeListener(this);


        // 自动切换页面功能
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (isLoop) {
                    SystemClock.sleep(2000);
                    handler.sendEmptyMessage(0);
                }
            }
        }).start();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        setImageBackground(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private int previousSelectPosition = 0;

    /**
     * 设置选中的tip的背景
     *
     * @param selectItems
     */
    private void setImageBackground(int selectItems) {
        // 切换选中的点,把前一个点置为normal状态
        mViewGroup.getChildAt(previousSelectPosition).setEnabled(false);
        mViewGroup.getChildAt(selectItems % getImageResIDs().length).setEnabled(true);
        previousSelectPosition = selectItems % getImageResIDs().length;

    }

    @Override
    public void onStart() {
        super.onStart();

        // getData("1", mParam2);
        //Toast.makeText(getActivity(), mParam1 + ":" + mParam2, Toast.LENGTH_LONG).show();

        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
    }

    @Override
    public void onResume() {
        super.onResume();

        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragmnet.
     */

    public static NewsFragmnet newInstance(String param1, String param2) {
        NewsFragmnet fragment = new NewsFragmnet();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static NewsFragmnet newInstance(String param1, String param2, String param3) {
        NewsFragmnet fragment = new NewsFragmnet();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    public NewsFragmnet() {
        // Required empty public constructor
    }


    /**
     * @param page   文章页码
     * @param typeId 文章type
     */
    private void getData(String page, String typeId, final int flag) {
        String path = UrlUtil.getChapterListString(Constant.ROW + "", page, typeId);

        Log.i("ApplicationTest", "path" + path);

        Log.i("ApplicationTest", "getData:" + Thread.currentThread().getName() + ":" + typeId);

        if (NetUtil.isNetworkAvailable(getActivity())) {
            HttpUtils.getDataFromNet(path, new HttpUtils.CallBack() {
                @Override
                public void Success(byte[] b) {

                    Log.i("ApplicationTest", "Success" + new String(b));

                    List<ChapterListItem> listData =
                            JsonUtil.getListData(ChapterListItem.class, new String(b));

                    if (listData != null) {
                        Message message = Message.obtain();

                        message.what = 1001;
                        message.arg1 = flag;

                        message.obj = listData;

                        handler.sendMessageAtTime(message, 1000);

                    } else {

                        Toast.makeText(context, "没有加载到数据", Toast.LENGTH_LONG).show();
                    }

                    Log.i("ApplicationTest", "getData:" + Thread.currentThread().getName());


                }

                @Override
                public void Failed(byte[] b) {

                    Log.i("ApplicationTest", "Failed" + new String(b));
                }

                @Override
                public void OtherFaile(String error) {

                    Log.i("ApplicationTest", "OtherFaile" + error);
                }
            });

        } else {
            Toast.makeText(context, "无网络连接", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 从数据库获取数据
     *
     * @param currentpage 当前页码
     * @param pageSize    本页显示
     * @return
     */
    public List<ChapterListItem> getDataFromSQLByPage(int currentpage, int pageSize) {


        return dao.getAllDataByPage(currentpage, pageSize);
    }

    /**
     * @param currentpage
     * @param pageSize
     * @param typeid      typeID 文章类型
     * @return
     */
    public List<ChapterListItem> getDataFromSQLByPage(int currentpage, int pageSize, String typeid) {


        return dao.getAllDataByPage(currentpage, pageSize, typeid);
    }


    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case 1001:
                List<ChapterListItem> list = (List<ChapterListItem>) msg.obj;
                if (list != null) {
                    // adapter.setListEnd(list);
                    switch (msg.arg1) {

                        case LOAD_FLAG_DOWM:

                            adapter.setListEnd(list);

                            currentPage++;
                            break;

                        case LOAD_FLAG_NOR:
                            adapter.setListEnd(list);
                            currentPage++;
                            break;
                        case LOAD_FLAG_PULL:
                            adapter.clearData();
                            adapter.setListStart(list);
                            currentPage = 1;
                            break;
                    }
                    dao.addListData(list);
                }

                onLoad();
                break;


            case 0:
                viewPage.setCurrentItem(viewPage.getCurrentItem() + 1);

                break;
        }


        return true;
    }


    private void printlog(String method) {


        Log.i(TAG, "==hashcode==" + hashCode + "===method===" + method);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ChapterListItem item = (ChapterListItem) parent.getAdapter().getItem(position);

        Intent intent = new Intent(context, ChapterDetialActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {

        if (NetUtil.isNetworkAvailable(context)) {
            // adapter.clearData();
            getData(currentPage + "", mParam2, LOAD_FLAG_PULL);
            // currentPage = 1;

        } else {

            onLoad();
        }


    }

    /**
     * 加载是否完成
     */
    private boolean isLoadFinish = false;

    /**
     * 上拉加载更多
     */
    @Override
    public void onLoadMore() {
        if (!isLoadFinish) {

            isLoadFinish = !isLoadFinish;

            if (NetUtil.isNetworkAvailable(context)) {

                // currentPage++;
                getData(currentPage + "", mParam2, LOAD_FLAG_DOWM);

            } else {
                List<ChapterListItem> items = null;
                if (mParam3.equals("0")) {

                    typeids = context.getResources().getStringArray(R.array.str_typeid);
                    items = dao.getAllDataByPage(currentPage, Constant.ROW, typeids);
                } else {

                    items = getDataFromSQLByPage(currentPage, Constant.ROW, mParam2);
                }


                //  dao.getAllDataByPage(currentPage, Constant.ROW);


                if (items != null && items.size() > 0) {
                    adapter.setListEnd(items);
                    adapter.notifyDataSetChanged();

                    currentPage++;
                    onLoad();
                } else {

                    onLoad();
                    Toast.makeText(context, "没有数据了", Toast.LENGTH_LONG).show();
                }
            }


        }


    }


    private void onLoad() {
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime("刚刚");

        adapter.notifyDataSetChanged();

        isLoadFinish = false;
    }


    @Override
    public void onPause() {
        super.onPause();
        //currentPage = 1;
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
    }

    @Override
    public void onStop() {
        super.onStop();
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        currentPage = 1;
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);

        isLoop = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        printlog(Method);

        Log.i(TAG, "onSaveInstanceState");

        outState.putParcelableArrayList("data", (ArrayList<? extends Parcelable>) adapter.getAllData());

    }

}
