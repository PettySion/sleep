package com.szip.sleepee.Controller;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothClient;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.szip.sleepee.Adapter.MyMenuAdapter;
import com.szip.sleepee.Bean.ConnectBean;
import com.szip.sleepee.Bean.HttpBean.BaseApi;
import com.szip.sleepee.Bean.HttpBean.ClockDataBean;
import com.szip.sleepee.Bean.HttpBean.TokenBean;
import com.szip.sleepee.Bean.HttpBean.UserInfoBean;
import com.szip.sleepee.Bean.UpdataReportBean;
import com.szip.sleepee.Controller.Fragment.AlarmClockFragment;
import com.szip.sleepee.Controller.Fragment.PersonFragment;
import com.szip.sleepee.Controller.Fragment.report.ReportFragment;
import com.szip.sleepee.Controller.Fragment.SleepFragment;
import com.szip.sleepee.DB.LoadDataUtil;
import com.szip.sleepee.DB.SaveDataUtil;
import com.szip.sleepee.Interface.HttpCallbackWithClockData;
import com.szip.sleepee.Interface.HttpCallbackWithReport;
import com.szip.sleepee.Interface.HttpCallbackWithUserInfo;
import com.szip.sleepee.Interface.OnProgressTimeout;
import com.szip.sleepee.Model.ProgressHudModel;
import com.szip.sleepee.MyApplication;
import com.szip.sleepee.R;
import com.szip.sleepee.Service.BleService;
import com.szip.sleepee.Util.ClientManager;
import com.szip.sleepee.Util.DateUtil;
import com.szip.sleepee.Util.HttpMessgeUtil;
import com.szip.sleepee.Util.JsonGenericsSerializator;
import com.szip.sleepee.Util.StatusBarCompat;
import com.szip.sleepee.View.DateSelectView;
import com.szip.sleepee.View.MenuListView;
import com.szip.sleepee.View.MyAlerDialog;
import com.zhuoting.health.write.ProtocolWriter;
import com.zhy.http.okhttp.callback.GenericsCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.aigestudio.datepicker.cons.DPMode;
import cn.aigestudio.datepicker.views.DatePicker;
import okhttp3.Call;

import static com.szip.sleepee.Util.HttpMessgeUtil.DOWNLOADDATA_FLAG;
import static com.szip.sleepee.Util.HttpMessgeUtil.GETALARM_FLAG;
import static com.szip.sleepee.Util.HttpMessgeUtil.GETINFO_FLAG;


public class MainActivity extends BaseActivity implements HttpCallbackWithUserInfo,HttpCallbackWithClockData,HttpCallbackWithReport{

    private Context mContext;

    private long firstime = 0;
    private DateSelectView dateSelectView;
    /**
     * Fragment操作相关
     * */
    private FragmentManager fm;
    private FragmentTransaction transaction;

    /**
     * 侧滑菜单控件
     * */
    private MenuListView menuListView;
    private MyMenuAdapter adapter;
    private ListView listView;

    /**
     * 顶栏控件
     * */
    private ImageView menuIv;
    private ImageView imageOne;
    private ImageView imageTwo;
    private TextView titleTv;

    /**
     * 所在界面
     * */
    private int fragmentPos = 0;

    private SharedPreferences sharedPreferences;
    private String FILE = "sleepEE";


    /**
     * 旋转出现的动画
     * */
    private RotateAnimation rotateLeft  = new RotateAnimation(90f, 0f, Animation.RELATIVE_TO_SELF,
            0f, Animation.RELATIVE_TO_SELF, 0f);
    /**
     * 旋转消失的动画
     * */
    private RotateAnimation rotateRight  = new RotateAnimation(0f, 90f, Animation.RELATIVE_TO_SELF,
            0f, Animation.RELATIVE_TO_SELF, 0f);

    private ObjectAnimator anim1;
    private AnimatorSet set = new AnimatorSet();

    private MyApplication app;

    SleepFragment sleepFragment = SleepFragment.newInstance("");
    ReportFragment reportFragment = ReportFragment.newInstance("");
    AlarmClockFragment alarmClockFragment = AlarmClockFragment.newInstance("");
    PersonFragment personFragment = PersonFragment.newInstance("");

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 200:
                    try {
                        HttpMessgeUtil.getInstance(mContext).getForGetClockList(GETALARM_FLAG);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case 300:
                    try {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY,5);
                        calendar.set(Calendar.MINUTE,0);
                        calendar.set(Calendar.SECOND,0);
                        calendar.set(Calendar.MILLISECOND,0);
                        HttpMessgeUtil.getInstance(mContext).getForDownloadReportData(""+(calendar.getTimeInMillis()/1000-30*24*60*60),
                                "30",DOWNLOADDATA_FLAG);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 402:
                    ProgressHudModel.newInstance().diss();
                    showToast(getString(R.string.loginError));
                    if (set.isStarted())
                        set.end();
                    BleService.getInstance().disConnect();
                    SaveDataUtil.newInstance(MainActivity.this).clearDB();
                    if (sharedPreferences==null)
                        sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLogin",false);
                    editor.commit();
                    Intent intent = new Intent();
                    intent.setClass(mContext,LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        app = (MyApplication) getApplicationContext();
        StatusBarCompat.translucentStatusBar(MainActivity.this,true);
        mContext = getApplicationContext();
        initView();
        initEvent();
        intiAnimation();
        updateView(fragmentPos);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        HttpMessgeUtil.getInstance(mContext).setHttpCallbackWithReport(null);
        HttpMessgeUtil.getInstance(mContext).setHttpCallbackWithClockData(null);
        HttpMessgeUtil.getInstance(mContext).setHttpCallbackWithUserInfo(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updataBleStateImage();
        HttpMessgeUtil.getInstance(mContext).setHttpCallbackWithReport(this);
        HttpMessgeUtil.getInstance(mContext).setHttpCallbackWithClockData(this);
        HttpMessgeUtil.getInstance(mContext).setHttpCallbackWithUserInfo(this);
    }


    /**
     * 更新连接按钮
     * */
    private void updataBleStateImage(){
        if (fragmentPos!=1){
            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this);
            if (BleService.getInstance().isConnect()){
                imageOne.setImageResource(R.mipmap.sleep_icon_connect);
                if (app.isUpdating())
                    ProgressHudModel.newInstance().show(MainActivity.this,getString(R.string.syncing),getString(R.string.synError),15000);
            }else {
                imageOne.setImageResource(R.mipmap.sleep_icon_ununited);
                if (ClientManager.getClient().isBluetoothOpened()){
                }
            }
        }
    }

    /**
     * 初始化界面
     * */
    private void initView() {
        if (app.getUserInfo() == null){//没有获取信息的时候获取信息
            try {
                ProgressHudModel.newInstance().show(this,getString(R.string.logging),getString(R.string.httpError),8000,
                        false,onProgressTimeout);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (app.isConnectting()||app.isSearch()&&!set.isStarted())
                            set.start();
                    }
                },300);
                HttpMessgeUtil.getInstance(mContext).getForGetInfo(GETINFO_FLAG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {//已经获取过信息了，直接连接蓝牙
            connectBle();
        }

        menuIv = findViewById(R.id.menuIv);
        imageOne = findViewById(R.id.imageOne);
        imageTwo = findViewById(R.id.imageTwo);
        menuListView = findViewById(R.id.menuListView);
        listView = findViewById(R.id.v4_listview);
        adapter = new MyMenuAdapter(this);
        listView.setAdapter(adapter);
        dateSelectView = findViewById(R.id.dateView);
        dateSelectView.setSelectListener(dateSelectListener);
    }

    /**
     * 初始化事件监听
     * */
    private void initEvent() {
        menuIv.setOnClickListener(onClickListener);
        imageOne.setOnClickListener(onClickListener);
        imageTwo.setOnClickListener(onClickListener);
        findViewById(R.id.backView).setOnClickListener(onClickListener);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position!=fragmentPos){
                    fragmentPos = position;
                    updateView(position);
                    updataBleStateImage();
                }
                startSector(false);
            }
        });
    }

    /**
     * 更新界面
     * */
    private void updateView(int pos) {
        switch (pos){
            case 0:
                ((TextView)findViewById(R.id.titleTv)).setText(getString(R.string.sleep));
                imageOne.setVisibility(View.VISIBLE);
                imageTwo.setVisibility(View.GONE);
                if (BleService.getInstance().isConnect())
                    imageOne.setImageResource(R.mipmap.sleep_icon_connect);
                else
                    imageOne.setImageResource(R.mipmap.sleep_icon_ununited);

                fm = getSupportFragmentManager();
                transaction =  fm.beginTransaction();
                transaction.replace(R.id.fragment,sleepFragment);
                transaction.commit();
                break;
            case 1:
                ((TextView)findViewById(R.id.titleTv)).setText(getString(R.string.report));
                imageOne.setVisibility(View.VISIBLE);
                imageOne.setImageResource(R.mipmap.report_btn_refresh);
                imageTwo.setVisibility(View.VISIBLE);
                imageTwo.setImageResource(R.mipmap.report_btn_calenda);
                if (set.isStarted())
                    set.end();

                fm = getSupportFragmentManager();
                transaction =  fm.beginTransaction();
                transaction.replace(R.id.fragment,reportFragment);
                transaction.commit();
                break;
            case 2:
                ((TextView)findViewById(R.id.titleTv)).setText(getString(R.string.alarm));
                imageOne.setVisibility(View.VISIBLE);
                imageTwo.setVisibility(View.GONE);
                if (BleService.getInstance().isConnect())
                    imageOne.setImageResource(R.mipmap.sleep_icon_connect);
                else
                    imageOne.setImageResource(R.mipmap.sleep_icon_ununited);

                fm = getSupportFragmentManager();
                transaction =  fm.beginTransaction();
                transaction.replace(R.id.fragment,alarmClockFragment);
                transaction.commit();
                break;
            case 3:
                ((TextView)findViewById(R.id.titleTv)).setText(getString(R.string.me));
                imageOne.setVisibility(View.VISIBLE);
                imageTwo.setVisibility(View.GONE);
                if (BleService.getInstance().isConnect())
                    imageOne.setImageResource(R.mipmap.sleep_icon_connect);
                else
                    imageOne.setImageResource(R.mipmap.sleep_icon_ununited);

                fm = getSupportFragmentManager();
                transaction =  fm.beginTransaction();
                transaction.replace(R.id.fragment,personFragment);
                transaction.commit();
                break;
        }
    }

    /**
     * 初始化动画
     * */
    private void intiAnimation() {
        anim1 = ObjectAnimator.ofFloat(imageOne,"alpha",1,0,1);
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setRepeatCount(-1);

        set.play(anim1);
        set.setDuration(2000);

        rotateLeft.setDuration(500);//设置动画持续时间
        rotateLeft.setRepeatCount(0);//设置重复次数
        rotateLeft.setFillAfter(true);//动画执行完后是否停留在执行完的状态

        rotateRight.setDuration(500);//设置动画持续时间
        rotateRight.setRepeatCount(0);//设置重复次数
        rotateRight.setFillAfter(true);//动画执行完后是否停留在执行完的状态
    }

    /**
     * 连接蓝牙
     * */
    private void connectBle(){
        if (!BleService.getInstance().isConnect()){
            if (set.isStarted()){
                showToast(getString(R.string.lining));
            }else {
                BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
                if (blueadapter.isEnabled()){
                    set.start();
                    app.AutoConnectBle();
                }else {
                    Intent bleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(bleIntent);
                }
            }
        }
    }

    /**
     * 日期选择事件监听
     * */
    private DateSelectView.DateSelectListener dateSelectListener = new DateSelectView.DateSelectListener() {
        @Override
        public void onTouchOk() {
            dateSelectView.startAnimotion(false);
        }
    };

    /**
     * 事件监听
     * */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.menuIv:
                    if (menuListView.getVisibility() == View.GONE||menuListView.getVisibility() == View.INVISIBLE){
                        startSector(true);
                    }
                    else{
                        startSector(false);
                    }
                    break;

                case R.id.imageOne:
                    if (fragmentPos!=1){//如果不是在报告页面，则监听蓝牙断开/连接的按钮
                        if (BleService.getInstance().isConnect()){
                            MyAlerDialog.getSingle().showAlerDialog(getString(R.string.tip), getString(R.string.confirmDis),
                                    getString(R.string.ok), getString(R.string.cancel), true, new MyAlerDialog.AlerDialogOnclickListener() {
                                        @Override
                                        public void onDialogTouch(boolean flag) {
                                            BleService.getInstance().disConnect();
                                        }
                                    }, MainActivity.this).show();
                        }else {
                            connectBle();
                        }
                    }else {
                        //TODO 刷新数据
                        ((MyApplication)getApplicationContext()).setUpdating(true);
                        ProgressHudModel.newInstance().show(MainActivity.this,getString(R.string.syncing)
                                ,getString(R.string.synError),10000);
                        BleService.getInstance().write(ProtocolWriter.writeForReadSleepState());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                app.setUpdating(false);
                            }
                        },15000);
                    }
                    break;
                case R.id.imageTwo:

                    String date = DateUtil.getDateToString(app.getReportDate());
                    //TODO 选择时间
                    final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                    dialog.show();
                    final DatePicker picker = new DatePicker(MainActivity.this);
                    picker.setDate(Integer.valueOf(date.substring(0,4)), Integer.valueOf(date.substring(5,7)),Integer.valueOf(date.substring(8,10)));
                    picker.setMode(DPMode.SINGLE);
                    picker.setFestivalDisplay(false);
                    picker.setHolidayDisplay(false);
                    picker.setTodayDisplay(true);
                    picker.setOnDatePickedListener(new DatePicker.OnDatePickedListener() {
                        @Override
                        public void onDatePicked(String date) {
                            if (LoadDataUtil.newInstance().dataCanGet(date)){
                                    app.setReportDate(DateUtil.getStringToDate(date));
                                    EventBus.getDefault().post(new UpdataReportBean(true));
                                    dialog.dismiss();
                            }else {
                                showToast(getString(R.string.future));
                            }
                        }
                    });
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setContentView(picker, params);
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    break;

                case R.id.backView:
                    startSector(false);
                    break;
            }
        }
    };

    /**
     * 隐藏/显示菜单
     * */
    private void startSector(boolean visible){
        if (visible){
            menuListView.startAnimation(rotateLeft);
            menuListView.setVisibility(View.VISIBLE);
            menuListView.setClickable(true);
            listView.setVisibility(View.VISIBLE);
            findViewById(R.id.backView).setVisibility(View.VISIBLE);
        }else {
            menuListView.startAnimation(rotateRight);
            menuListView.setVisibility(View.GONE);
            menuListView.setClickable(false);
            listView.setVisibility(View.INVISIBLE);
            findViewById(R.id.backView).setVisibility(View.INVISIBLE);
        }
    }

    /**
     *接受后台返回的数据
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBleConnectStateChange(ConnectBean connectBean){
        Log.e("eventBus",connectBean.isConnect+"");
        if( connectBean.isConnect && fragmentPos!=1){
            if (set.isStarted())
                set.end();
            imageOne.setImageResource(R.mipmap.sleep_icon_connect);
            ProgressHudModel.newInstance().show(MainActivity.this,getString(R.string.syncing),getString(R.string.synError),15000);
            /**
             * 15秒后重置把更新状态置false
             * */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    app.setUpdating(false);
                }
            },15000);

        }else if (!connectBean.isConnect && fragmentPos!=1){
            if (set.isStarted()){
                showToast(getString(R.string.lineError));
                set.end();
            }
            imageOne.setImageResource(R.mipmap.sleep_icon_ununited);
        }
    }


    /**
     * 用户信息网络回调
     * */
    @Override
    public void onUserInfo(UserInfoBean userInfoBean) {
        app.setUserInfo(userInfoBean.getData());
        BleService.getInstance().setmMac(userInfoBean.getData().getSleepDeviceCode());
        handler.sendEmptyMessage(200);//获取信息成功，开始获取闹钟数据
    }

    /**
     * 闹钟网络回调
     * */
    @Override
    public void onClockData(ClockDataBean clockDataBean) {

        if(clockDataBean.getData().getArray()!=null){
            app.setClockList1(clockDataBean.getData().getArray());
        }
        handler.sendEmptyMessage(300);//获取闹钟数据成功，开始获取报告数据

    }


    /**
     * 向服务器获取数据的回调
     * */
    @Override
    public void onReport(boolean isNewData) {
        ProgressHudModel.newInstance().diss();
        connectBle();//获取云端数据成功，开始连接蓝牙
    }

    /**
     * 登录请求超时
     * */
    private OnProgressTimeout onProgressTimeout = new OnProgressTimeout() {
        @Override
        public void onTimeout() {
            handler.sendEmptyMessage(402);
        }
    };
    /**
     * 双击退出
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondtime = System.currentTimeMillis();
            if (secondtime - firstime > 3000) {
                Toast.makeText(this, getString(R.string.touchAgain),
                        Toast.LENGTH_SHORT).show();
                firstime = System.currentTimeMillis();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}