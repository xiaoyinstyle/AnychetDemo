package com.example.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatVideoCallEvent;
import com.example.AppApplication;
import com.example.bean.RoomBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChneY on 2017/3/3.
 */

public class AnyChatTools {
    private final int LOCALVIDEOAUTOROTATION = 1; // 本地视频自动旋转控制

    public final static int RoomId = 3;
    public final static String RoomPW = "";
    public final static String ip = "cloud.anychat.cn";
    public final static int port = 8906;

    private Context mContext;
    private static AnyChatTools anyChatTools;
    public static boolean isVideoing = false;
    public static boolean isInit = false;
//    public static ArrayList<UserItem> mOnlineFriendItems = new ArrayList<>();
//    public static ArrayList<Integer> mOnlineFriendIds = new ArrayList<>();

    private AnyChatCoreSDK anyChatSDK = null;
    private int selfId = 0;

    public synchronized static AnyChatTools getIntance() {
        if (anyChatTools == null)
            anyChatTools = new AnyChatTools();
        if (anyChatTools.anyChatSDK == null) {
            anyChatTools.initSDK();
        }
        if (anyChatTools.mContext == null)
            anyChatTools.mContext = AppApplication.getIntance().getApplicationContext();
        return anyChatTools;
    }

    private void initSDK() {
        if (anyChatSDK == null) {
            anyChatSDK = AnyChatCoreSDK.getInstance(mContext);
//            anyChatSDK = new AnyChatCoreSDK();
            anyChatSDK.InitSDK(android.os.Build.VERSION.SDK_INT, 0);
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION, LOCALVIDEOAUTOROTATION);
            //应用配置
//            ApplyVideoConfig();

            //好友状态监听无效
//            anyChatSDK.SetUserInfoEvent(new AnyChatUserInfoEvent() {
//                @Override
//                public void OnAnyChatUserInfoUpdate(int dwUserId, int dwType) {
//                    // 同步完成服务器中的所有好友数据，可以在此时获取数据
////                    if (dwUserId == 0 && dwType == 0) {
////                        getOnlineFriendDatas();
////                    }
//                }
//
//                @Override
//                public void OnAnyChatFriendStatus(int dwUserId, int dwStatus) {
////                    onUserOnlineStatusNotify(dwUserId, dwStatus);
//                    Log.e("AAA",""+dwUserId + "下线");
//                }
//            });

            // 注册广播
//            registerBoradcastReceiver();
        }
    }

    public void initVideoSDK() {
        if (anyChatSDK == null)
            initSDK();
        anyChatSDK.mSensorHelper.InitSensor(mContext);
        AnyChatCoreSDK.mCameraHelper.SetContext(mContext);
    }

    public AnyChatCoreSDK getAnyChat() {
        return anyChatSDK;
    }

    /**
     * 获取 在线用户
     */
    public int[] getOnLineUser() {
        if (anyChatSDK == null)
            initSDK();
        return anyChatSDK.GetOnlineUser();
    }

    //进行排序
    public int[] getOnLineUserSort() {
        return sortLine(anyChatSDK.GetOnlineUser());
    }

    private int[] sortLine(int[] ints) {
        int temp = 0;
        for (int i = 0; i < ints.length; i++) {
            for (int j = i + 1; j < ints.length; j++) {
                if (ints[i] > ints[j]) {
                    temp = ints[i];
                    ints[i] = ints[j];
                    ints[j] = temp;
                }
            }
        }
        return ints;
    }

    /**
     * 获取 房间的 List
     * @return
     */
    public List<RoomBean> getRoomInfo() {
        if (anyChatSDK == null)
            initSDK();
        List<RoomBean> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int[] users = anyChatSDK.GetRoomOnlineUsers(i);
            if (users.length > 0) {
                list.add(new RoomBean(i, users));
            }
        }
        return list;
    }
    public int getSelfId() {
        if (anyChatSDK == null)
            initSDK();
//        return anyChatSDK.GetUs();
        return selfId;
    }

    public void setSelfId(int self) {
        if (anyChatSDK == null)
            initSDK();
        selfId = self;
    }

    /**
     * 根据用户id获取 用户名
     */
    public String getUserName(int userId) {
        if (anyChatSDK == null)
            initSDK();
        return anyChatSDK.GetUserName(userId);
    }

    /**
     * 根据 用户的id 获取 用户的 摄像头 状态
     *
     * @return 监测对方摄像头状态: 0 没有摄像头设备 ,1 有摄像头设备，但没有打开 ,2 已打开摄像头设备
     */
    public int getCameraStatue(int userId) {
        if (anyChatSDK == null)
            initSDK();
        return anyChatSDK.GetCameraState(userId);
    }

    /**
     * 登录
     *
     * @param mStrIP
     * @param mSPort
     * @param mStrName
     */
    public void LoginAnyChat(String mStrIP, int mSPort, String mStrName, AnyChatBaseEvent anyChatBaseEvent) {
        if (anyChatSDK == null)
            initSDK();
        if (anyChatBaseEvent != null)
            anyChatSDK.SetBaseEvent(anyChatBaseEvent);
        /**
         *AnyChat可以连接自主部署的服务器、也可以连接AnyChat视频云平台；
         *连接自主部署服务器的地址为自设的服务器IP地址或域名、端口；
         *连接AnyChat视频云平台的服务器地址为：cloud.anychat.cn；端口为：8906
         */
        anyChatSDK.Connect(mStrIP, mSPort);

        /***
         * AnyChat支持多种用户身份验证方式，包括更安全的签名登录，
         * 详情请参考：http://bbs.anychat.cn/forum.php?mod=viewthread&tid=2211&highlight=%C7%A9%C3%FB
         */
        anyChatSDK.Login(mStrName, "");
    }

    /**
     *
     */
    public void SetBaseEvent(AnyChatBaseEvent anyChatBaseEvent) {
        if (anyChatSDK == null)
            initSDK();
        anyChatSDK.SetBaseEvent(anyChatBaseEvent);
    }

    /**
     * 视屏通信 监听
     */
    public void setVideoCallEvent(AnyChatVideoCallEvent anyChatVideoCallEvent) {
        if (anyChatSDK == null)
            initSDK();
        anyChatSDK.SetVideoCallEvent(anyChatVideoCallEvent);
    }

    /**
     * 发起视频
     */
    public void startVideoCall(int dwUserID) {
        if (anyChatSDK == null)
            initSDK();
        anyChatSDK.VideoCallControl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST, dwUserID, 0, 0, 0, "");
    }

    /**
     * 拒绝
     */
    public void setVideoRefuse(int dwUserId) {
        if (anyChatSDK == null)
            initSDK();
        Log.e("AAA", "拒绝： Self: " + AnyChatTools.getIntance().getSelfId() + " userId:" + dwUserId);
        anyChatSDK.VideoCallControl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY, dwUserId, AnyChatDefine.BRAC_ERRORCODE_SESSION_REFUSE, 0, 0, "");
    }

    /**
     * 接受
     */
    public void setVideoAccept(int dwUserId) {
        if (anyChatSDK == null)
            initSDK();
        Log.e("AAA", "接受： Self: " + AnyChatTools.getIntance().getSelfId() + "userId:" + dwUserId);
        anyChatSDK.VideoCallControl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY, dwUserId, AnyChatDefine.BRAC_ERRORCODE_SUCCESS, 0, 0, "");
    }

    /**
     * 取消呼叫
     */
    public void setVideoCancel(int dwUserId) {
        if (anyChatSDK == null)
            initSDK();
        Log.e("AAA", "取消呼叫： Self: " + AnyChatTools.getIntance().getSelfId() + "userId:" + dwUserId);
        anyChatSDK.VideoCallControl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY, dwUserId, AnyChatDefine.BRAC_ERRORCODE_SESSION_QUIT, 0, 0, "");
    }

    /**
     * 取消呼叫,关闭音频/视频
     */
    public void setVideoFinish(int dwUserId) {
        if (anyChatSDK == null)
            initSDK();
        Log.e("AAA", "取消呼叫： Self: " + AnyChatTools.getIntance().getSelfId() + "userId:" + dwUserId);
        anyChatSDK.VideoCallControl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY, dwUserId, AnyChatDefine.BRAC_VIDEOCALL_FLAGS_FBSRCVIDEO, 0, 0, "");
    }

    /**
     * 退出登录
     */
    public void LogoutAnyChat() {
        if (anyChatSDK == null)
            initSDK();
        anyChatSDK.LeaveRoom(-1);
        anyChatSDK.Logout();
    }

    public void EnterRoom(int sHourseID, String passwd) {
        if (anyChatSDK == null)
            initSDK();
        anyChatSDK.EnterRoom(sHourseID, passwd);
    }

    public void ReEnterRoom(int sHourseID, String passwd) {
        if (anyChatSDK == null)
            initSDK();

        anyChatSDK.Connect(ip, port);

        anyChatSDK.EnterRoom(sHourseID, passwd);
    }

    public void onDestroy() {
        if (anyChatSDK == null)
            return;
        anyChatSDK.LeaveRoom(-1);
        anyChatSDK.Logout();
        anyChatSDK.Release();
//        mContext.unregisterReceiver(mBroadcastReceiver);

        anyChatSDK = null;
    }

    public void ApplyVideoConfig() {
//        ConfigEntity configEntity = ConfigService.LoadConfig(this);
        ConfigEntity configEntity = new ConfigEntity();
        if (configEntity.mConfigMode == 1) // 自定义视频参数配置
        {
            // 设置本地视频编码的码率（如果码率为0，则表示使用质量优先模式）
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_BITRATECTRL, configEntity.mVideoBitrate);
//			if (configEntity.mVideoBitrate == 0) {
            // 设置本地视频编码的质量
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_QUALITYCTRL, configEntity.mVideoQuality);
//			}
            // 设置本地视频编码的帧率
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_FPSCTRL, configEntity.mVideoFps);
            // 设置本地视频编码的关键帧间隔
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_GOPCTRL, configEntity.mVideoFps * 4);
            // 设置本地视频采集分辨率
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL, configEntity.mResolutionWidth);
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL, configEntity.mResolutionHeight);
            // 设置视频编码预设参数（值越大，编码质量越高，占用CPU资源也会越高）
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_PRESETCTRL, configEntity.mVideoPreset);
        }
        // 让视频参数生效
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_APPLYPARAM, configEntity.mConfigMode);
        // P2P设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_NETWORK_P2PPOLITIC, configEntity.mEnableP2P);
        // 本地视频Overlay模式设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_OVERLAY, configEntity.mVideoOverlay);
        // 回音消除设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_AUDIO_ECHOCTRL, configEntity.mEnableAEC);
        // 平台硬件编码设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_CORESDK_USEHWCODEC, configEntity.mUseHWCodec);
        // 视频旋转模式设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_ROTATECTRL, configEntity.mVideoRotateMode);
        // 本地视频采集偏色修正设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_FIXCOLORDEVIA, configEntity.mFixColorDeviation);
        // 视频GPU渲染设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_GPUDIRECTRENDER, configEntity.mVideoShowGPURender);
        // 本地视频自动旋转设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION, configEntity.mVideoAutoRotation);
    }

    public void showToast(Context c, int dwErrorCode) {
        if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SUCCESS) {
            Toast.makeText(c, "正在进行请求中...", Toast.LENGTH_SHORT).show();
        } else if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SESSION_OFFLINE) {
            Toast.makeText(c, "对方不在线", Toast.LENGTH_SHORT).show();
        } else if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SESSION_QUIT) {
            Toast.makeText(c, "视频通话已取消", Toast.LENGTH_SHORT).show();
        } else if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SESSION_BUSY) {
            Toast.makeText(c, "目标用户忙", Toast.LENGTH_SHORT).show();
        } else if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SESSION_REFUSE) {
            Toast.makeText(c, "对方拒绝会话 ", Toast.LENGTH_SHORT).show();
        } else if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SESSION_TIMEOUT) {
            Toast.makeText(c, "会话请求超时 ", Toast.LENGTH_SHORT).show();
        } else if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SESSION_DISCONNECT) {
            Toast.makeText(c, "网络断线 ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(c, "其它 ", Toast.LENGTH_SHORT).show();
        }
    }

//    // 广播
//    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals("VideoActivity")) {
//                Toast.makeText(context, "网络已断开！", Toast.LENGTH_SHORT).show();
////                setBtnVisible(SHOWLOGINSTATEFLAG);
////                mRoleList.setAdapter(null);
////                mBottomConnMsg.setText("No content to the server");
//                anyChatSDK.LeaveRoom(-1);
//                anyChatSDK.Logout();
//            }
//        }
//    };
//
//    private void registerBoradcastReceiver() {
//        IntentFilter myIntentFilter = new IntentFilter();
//        myIntentFilter.addAction("VideoActivity");
//        // 注册广播
//        mContext.registerReceiver(mBroadcastReceiver, myIntentFilter);
//    }

//    @Override
//    public void OnAnyChatVideoCallEvent(int dwEventType, int dwUserId,int dwErrorCode, int dwFlags, int dwParam, String userStr) {
//        // TODO Auto-generated method stub
//        switch (dwEventType) {
//            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST:
//                BussinessCenter.getBussinessCenter().onVideoCallRequest(
//                        dwUserId, dwFlags, dwParam, userStr);
//                if (dialog != null && dialog.isShowing())
//                    dialog.dismiss();
//                dialog = DialogFactory.getDialog(DialogFactory.DIALOGID_REQUEST,
//                        dwUserId, this);
//                dialog.show();
//                break;
//            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY:
//                BussinessCenter.getBussinessCenter().onVideoCallReply(
//                        dwUserId, dwErrorCode, dwFlags, dwParam, userStr);
//                if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SUCCESS) {
//                    dialog = DialogFactory.getDialog(
//                            DialogFactory.DIALOGID_CALLING, dwUserId,
//                            HallActivity.this);
//                    dialog.show();
//
//                } else {
//                    if (dialog != null && dialog.isShowing()) {
//                        dialog.dismiss();
//                    }
//                }
//                break;
//            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_START:
//                if (dialog != null && dialog.isShowing())
//                    dialog.dismiss();
//                BussinessCenter.getBussinessCenter().onVideoCallStart(
//                        dwUserId, dwFlags, dwParam, userStr);
//                break;
//            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH:
//                BussinessCenter.getBussinessCenter().onVideoCallEnd(dwUserId,
//                        dwFlags, dwParam, userStr);
//                break;
//        }
//    }


//    /***
//     * @param userId 用户id
//     * @param status 用户在线状态，1是上线，0是下线
//     */
//    private void onUserOnlineStatusNotify(int userId, int status) {
//        UserItem userItem = getUserItemByUserId(userId);
//        if (userItem == null)
//            return;
//        if (status == UserItem.USERSTATUS_OFFLINE) {
//            if (mOnlineFriendIds.indexOf(userId) >= 0) {
//                mOnlineFriendItems.remove(userItem);
//                mOnlineFriendIds.remove((Object) userId);
//            }
//            Log.e("AAA",""+userItem.getUserName() + "下线");
//        } else {
//            Log.e("AAA",""+userItem.getUserName() + "上线");
//        }
//    }
//
//    /***
//     * 通过用户id获取用户对象
//     *
//     * @param userId
//     *            用户id
//     * @return
//     */
//    public UserItem getUserItemByUserId(int userId) {
//        int size = mOnlineFriendItems.size();
//        for (int i = 0; i < size; i++) {
//            UserItem userItem = mOnlineFriendItems.get(i);
//            if (userItem != null && userItem.getUserId() == userId) {
//                return userItem;
//            }
//        }
//        return null;
//    }
//
//    /***
//     * 同步 获取好友数据
//     */
//    private void getOnlineFriendDatas() {
//        mOnlineFriendItems.clear();
//        mOnlineFriendIds.clear();
//        // 获取本地ip
//        String ip = anyChatSDK.QueryUserStateString(-1,
//                AnyChatDefine.BRAC_USERSTATE_LOCALIP);
//        UserItem userItem = new UserItem(selfId, getUserName(selfId), ip);
//        // 获取用户好友userid列表
//        int[] friendUserIds = anyChatSDK.GetUserFriends();
//        int friendUserId = 0;
//        mOnlineFriendItems.add(userItem);
//        mOnlineFriendIds.add(selfId);
//        if (friendUserIds == null)
//            return;
//        for (int i = 0; i < friendUserIds.length; i++) {
//            friendUserId = friendUserIds[i];
//            int onlineStatus = anyChatSDK.GetFriendStatus(friendUserId);
//            if (onlineStatus == UserItem.USERSTATUS_OFFLINE) {
//                continue;
//            }
//            userItem = new UserItem();
//            userItem.setUserId(friendUserId);
//            // 获取好友昵称
//            String nickName = anyChatSDK.GetUserInfo(friendUserId, UserItem.USERINFO_NAME);
//            if (nickName != null)
//                userItem.setUserName(nickName);
//            // 获取好友IP地址
//            String strIp = anyChatSDK.GetUserInfo(friendUserId, UserItem.USERINFO_IP);
//            if (strIp != null)
//                userItem.setIp(strIp);
//            mOnlineFriendItems.add(userItem);
//            mOnlineFriendIds.add(friendUserId);
//        }
//    }

    public ClosePageListener listener;

    public void closeListener(int i) {
        if (listener == null)
            return;
        listener.onClosePageListener(i);
    }

    public void setListener(ClosePageListener closePageListener) {
//        if (closePageListener != null)
        listener = closePageListener;
    }
}
