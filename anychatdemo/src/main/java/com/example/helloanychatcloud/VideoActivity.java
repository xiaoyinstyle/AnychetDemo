package com.example.helloanychatcloud;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCameraHelper;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.example.bean.ChatInfoBean;
import com.example.bean.RoomBean;
import com.example.utils.AnyChatTools;
import com.example.utils.LogAnyChat;

import java.util.ArrayList;
import java.util.List;

import yin.style.recyclerlib.adapter.BaseQuickAdapter;
import yin.style.recyclerlib.holder.BaseViewHolder;
import yin.style.recyclerlib.inter.OnItemClickListener;

public class VideoActivity extends Activity implements AnyChatBaseEvent {

    private final int UPDATEVIDEOBITDELAYMILLIS = 200; //监听音频视频的码率的间隔刷新时间（毫秒）

    private boolean bSelfVideoOpened = false; // 本地视频是否已打开
    private boolean bOtherVideoOpened = false; // 对方视频是否已打开

    private SurfaceView mMainView;
    private TextView tvName;
    private List<ChatInfoBean> list = new ArrayList<ChatInfoBean>();
    private RecyclerView recyclerView;
    private BaseQuickAdapter adapter;

    private ImageButton mImgSwitchVideo;
    private Button mEndCallBtn;
    private ImageButton mBtnCameraCtrl; // 控制视频的按钮
    private ImageButton mBtnSpeakCtrl; // 控制音频的按钮

    public AnyChatCoreSDK anychatSDK;
    int MainUserId;
    ChatInfoBean chatInfoBean;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.video_frame_);
        MainUserId = getIntent().getIntExtra("userId", -1);

        AnyChatTools.getIntance().initVideoSDK();
        anychatSDK = AnyChatTools.getIntance().getAnyChat();
        anychatSDK.SetBaseEvent(this);

        initRecyclerView();
        InitLayout();

        // 如果视频流过来了，则把背景设置成透明的//每0.2秒执行一次
        handler.postDelayed(runnable, UPDATEVIDEOBITDELAYMILLIS);
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new BaseQuickAdapter<ChatInfoBean>(R.layout.item_video_surfaceview, list) {
            @Override
            protected void setViewHolder(BaseViewHolder baseViewHolder, ChatInfoBean bean, int i) {
                TextView name = baseViewHolder.getView(R.id.textview_item);
                SurfaceView surfaceView = baseViewHolder.getView(R.id.surface_item);

                setVideoSurface(surfaceView, name, bean);
            }
        };
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int i) {
                Toast.makeText(VideoActivity.this, "position:" + i + "\nsize:" + list.size(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("userId", list.get(i).getId());
                setResult(100, intent);
                finish();
            }
        });
    }

    private void InitLayout() {
        this.setTitle("对话中");
        tvName = (TextView) findViewById(R.id.textview_main);
        tvName.setText("6666");
        mMainView = (SurfaceView) findViewById(R.id.surface_main);
        mImgSwitchVideo = (ImageButton) findViewById(R.id.ImgSwichVideo);
        mEndCallBtn = (Button) findViewById(R.id.endCall);
        mBtnSpeakCtrl = (ImageButton) findViewById(R.id.btn_speakControl);
        mBtnCameraCtrl = (ImageButton) findViewById(R.id.btn_cameraControl);
        mBtnSpeakCtrl.setOnClickListener(onClickListener);
        mBtnCameraCtrl.setOnClickListener(onClickListener);
        mImgSwitchVideo.setOnClickListener(onClickListener);
        mEndCallBtn.setOnClickListener(onClickListener);

        startOtherView2();

        // 判断是否显示本地摄像头切换图标
        if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            if (AnyChatCoreSDK.mCameraHelper.GetCameraNumber() > 1) {
                // 默认打开前置摄像头
                AnyChatCoreSDK.mCameraHelper.CaptureControl(false);
                AnyChatCoreSDK.mCameraHelper.SelectVideoCapture(AnyChatCoreSDK.mCameraHelper.CAMERA_FACING_FRONT);
            }
        } else {
            String[] strVideoCaptures = anychatSDK.EnumVideoCapture();
            if (strVideoCaptures != null && strVideoCaptures.length > 1) {
                // 默认打开前置摄像头
                for (int i = 0; i < strVideoCaptures.length; i++) {
                    String strDevices = strVideoCaptures[i];
                    if (strDevices.indexOf("Front") >= 0) {
                        anychatSDK.SelectVideoCapture(strDevices);
                        break;
                    }
                }
            }
        }

        // 根据屏幕方向改变本地surfaceview的宽高比
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adjustLocalVideo(true);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            adjustLocalVideo(false);
        }
        AnyChatCoreSDK.mCameraHelper.setCameraDisplayOrientation();
    }


    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setVideoStatue();
        }
    };


    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case (R.id.ImgSwichVideo): {

                    // 如果是采用Java视频采集，则在Java层进行摄像头切换
                    if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                        AnyChatCoreSDK.mCameraHelper.SwitchCamera();
                        return;
                    }

                    String strVideoCaptures[] = anychatSDK.EnumVideoCapture();
                    String temp = anychatSDK.GetCurVideoCapture();
                    for (int i = 0; i < strVideoCaptures.length; i++) {
                        if (!temp.equals(strVideoCaptures[i])) {
                            anychatSDK.UserCameraControl(-1, 0);
                            bSelfVideoOpened = false;
                            anychatSDK.SelectVideoCapture(strVideoCaptures[i]);
                            anychatSDK.UserCameraControl(-1, 1);
                            break;
                        }
                    }
                }
                break;
                case (R.id.endCall): {
                    exitVideoDialog();
                }
                case R.id.btn_speakControl:
                    if ((anychatSDK.GetSpeakState(-1) == 1)) {
                        mBtnSpeakCtrl.setImageResource(R.drawable.speak_off);
                        anychatSDK.UserSpeakControl(-1, 0);
                    } else {
                        mBtnSpeakCtrl.setImageResource(R.drawable.speak_on);
                        anychatSDK.UserSpeakControl(-1, 1);
                    }

                    break;
                case R.id.btn_cameraControl:
                    if ((anychatSDK.GetCameraState(-1) == 2)) {
                        mBtnCameraCtrl.setImageResource(R.drawable.camera_off);
                        anychatSDK.UserCameraControl(-1, 0);
                    } else {
                        mBtnCameraCtrl.setImageResource(R.drawable.camera_on);
                        anychatSDK.UserCameraControl(-1, 1);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void refreshAV() {
        startOtherView2();

        anychatSDK.UserCameraControl(-1, 1);
        anychatSDK.UserSpeakControl(-1, 1);
        mBtnSpeakCtrl.setImageResource(R.drawable.speak_on);
        mBtnCameraCtrl.setImageResource(R.drawable.camera_on);
        bOtherVideoOpened = false;
        bSelfVideoOpened = false;
    }

    private void exitVideoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("您确定要退出视屏通话?")
                .setCancelable(false)
                .setPositiveButton("是",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                exit();
                                destroyCurActivity();
                            }
                        })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void destroyCurActivity() {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitVideoDialog();
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void onResume() {
        super.onResume();
        refreshAV();
    }

    private void exit() {
        anychatSDK.LeaveRoom(2);
        handler.removeCallbacks(runnable);
    }

    @Override
    public void finish() {
        super.finish();
        stopOtherView();
        overridePendingTransition(0, 0);
        anychatSDK.UserCameraControl(-1, 0);
        anychatSDK.UserSpeakControl(-1, 0);

        anychatSDK.mSensorHelper.DestroySensor();
    }


    public void adjustLocalVideo(boolean bLandScape) {
        if (MainUserId != -1)
            return;
        float width;
        float height = 0;
        DisplayMetrics dMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
        width = (float) dMetrics.widthPixels / 3;
        ViewGroup.LayoutParams layoutParams = mMainView.getLayoutParams();
        if (bLandScape) {
            if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL) != 0)
                height = width * AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL)
                        / AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL)
                        + 5;
            else
                height = (float) 3 / 4 * width + 5;
        } else {

            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL) != 0)
                height = width * AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL)
                        / AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL)
                        + 5;
            else
                height = (float) 4 / 3 * width + 5;
        }
        layoutParams.width = (int) width;
        layoutParams.height = (int) height;
        mMainView.setLayoutParams(layoutParams);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adjustLocalVideo(true);
            AnyChatCoreSDK.mCameraHelper.setCameraDisplayOrientation();
        } else {
            adjustLocalVideo(false);
            AnyChatCoreSDK.mCameraHelper.setCameraDisplayOrientation();
        }

    }

    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {

    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {

    }

    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {

    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {

    }

    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
//        Toast.makeText(this, bEnter + " id:" + dwUserId + "  name:" + AnyChatTools.getIntance().getUserName(dwUserId), Toast.LENGTH_SHORT).show();
        LogAnyChat.e(bEnter + " id:" + dwUserId + "  name:" + AnyChatTools.getIntance().getUserName(dwUserId));
        if (!bEnter) {
            Toast.makeText(VideoActivity.this, "对方已离开！", Toast.LENGTH_SHORT).show();
            anychatSDK.UserCameraControl(dwUserId, 0);
            anychatSDK.UserSpeakControl(dwUserId, 0);

            int post = -2;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == dwUserId) {
                    post = i;
                    break;
                }
            }
            if (post != -2) {
                list.remove(post);
                adapter.notifyDataSetChanged();
            }
        } else {
            ChatInfoBean bean = new ChatInfoBean();
            bean.setId(dwUserId);
            bean.setName(AnyChatTools.getIntance().getUserName(dwUserId));
            list.add(bean);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        // 网络连接断开之后，上层需要主动关闭已经打开的音视频设备
        if (bOtherVideoOpened) {
            stopOtherView();
        }
        if (bSelfVideoOpened) {
            anychatSDK.UserCameraControl(-1, 0);
            anychatSDK.UserSpeakControl(-1, 0);
            bSelfVideoOpened = false;
        }

        // 销毁当前界面
        destroyCurActivity();
        Intent mIntent = new Intent("VideoActivity");
        // 发送广播
        sendBroadcast(mIntent);
    }

    private void stopOtherView() {
        int users[] = AnyChatTools.getIntance().getOnLineUser();
        for (int i = 0; i < users.length; i++) {
            int userID = users[i];
            anychatSDK.UserCameraControl(userID, 0);
            anychatSDK.UserSpeakControl(userID, 0);
        }
        bOtherVideoOpened = false;
    }

    private void startOtherView2() {
        int users[] = AnyChatTools.getIntance().getOnLineUserSort();
        list.clear();
        list.add(new ChatInfoBean(-1, "我"));
        for (int i = 0; i < users.length; i++) {
            ChatInfoBean bean = new ChatInfoBean();
            bean.setId(users[i]);
            bean.setName(AnyChatTools.getIntance().getUserName(users[i]));
            list.add(bean);
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).getId() == MainUserId) {
                chatInfoBean = list.get(i);
                list.remove(i);
            }
        }
        if (chatInfoBean != null)
            setVideoSurface(mMainView, tvName, chatInfoBean);
        adapter.notifyDataSetChanged();
    }

    private void setVideoStatue() {
        int users[] = AnyChatTools.getIntance().getOnLineUserSort();
        for (int i = 0; i < users.length; i++) {
            Boolean mFirstGetVideoBitrate = false; //"第一次"获得视频码率的标致
            Boolean mFirstGetAudioBitrate = false; //"第一次"获得音频码率的标致

            int userID = users[i];
            try {
                int videoBitrate = anychatSDK.QueryUserStateInt(userID, AnyChatDefine.BRAC_USERSTATE_VIDEOBITRATE);
                int audioBitrate = anychatSDK.QueryUserStateInt(userID, AnyChatDefine.BRAC_USERSTATE_AUDIOBITRATE);
                if (videoBitrate > 0) {
                    //handler.removeCallbacks(runnable);
                    mFirstGetVideoBitrate = true;
                }

                if (audioBitrate > 0) {
                    mFirstGetAudioBitrate = true;
                }

                if (mFirstGetVideoBitrate) {
                    if (videoBitrate <= 0) {
//                        Toast.makeText(VideoActivity.this, "对方视频中断了!", Toast.LENGTH_SHORT).show();
                        // 重置下，如果对方退出了，有进去了的情况
                        mFirstGetVideoBitrate = false;
                    }
                }

                if (mFirstGetAudioBitrate) {
                    if (audioBitrate <= 0) {
//                        Toast.makeText(VideoActivity.this, "对方音频中断了!", Toast.LENGTH_SHORT).show();
                        // 重置下，如果对方退出了，有进去了的情况
                        mFirstGetAudioBitrate = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        handler.postDelayed(runnable, UPDATEVIDEOBITDELAYMILLIS);
    }


    //设置 每个Item的视屏及文字
    private void setVideoSurface(SurfaceView mView, TextView tvName, ChatInfoBean bean) {

        int selfId = AnyChatTools.getIntance().getSelfId();
        if (bean.getId() == -1 || bean.getId() == selfId) {
            // 如果是采用Java视频采集，则需要设置Surface的CallBack
            if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                AnyChatCameraHelper chatCameraHelper = AnyChatCoreSDK.mCameraHelper;
                mView.getHolder().addCallback(chatCameraHelper);
            }
//            mView.setZOrderOnTop(true);
        } else {
            int userID = bean.getId();
            // 如果是采用Java视频显示，则需要设置Surface的CallBack
            if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                int index = anychatSDK.mVideoHelper.bindVideo(mView.getHolder());
                anychatSDK.mVideoHelper.SetVideoUser(index, userID);
                LogAnyChat.e("index:" + index);
            }

            anychatSDK.UserCameraControl(userID, 1);
            anychatSDK.UserSpeakControl(userID, 1);
        }

        tvName.setText(bean.getName());
    }

}
