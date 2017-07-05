package com.donkor.demo.mobsharesdk;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mob.tools.utils.UIHandler;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * @author donkor
 * blog : http://blog.csdn.net/donkor_/article/details/53422025
 */
public class MainActivity extends Activity implements View.OnClickListener,Handler.Callback, PlatformActionListener {

    private ImageView ivQQ, ivWeChat, ivSinaWeibo;
    private Button btnRemoveMsg,btnShare;

    private static final int MSG_USERID_FOUND = 1;
    private static final int MSG_LOGIN = 2;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化
        ShareSDK.initSDK(this);

        ivQQ = (ImageView) findViewById(R.id.ivQQ);
        ivWeChat = (ImageView) findViewById(R.id.ivWeChat);
        ivSinaWeibo = (ImageView) findViewById(R.id.ivSinaWeibo);

        btnRemoveMsg= (Button) findViewById(R.id.btnRemoveMsg);
        btnShare= (Button) findViewById(R.id.btnShare);

        ivQQ.setOnClickListener(this);
        ivWeChat.setOnClickListener(this);
        ivSinaWeibo.setOnClickListener(this);

        btnRemoveMsg.setOnClickListener(this);
        btnShare.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivQQ:
                //执行授权,获取用户信息
                authorize(new QQ(MainActivity.this));
                break;
            case R.id.ivWeChat:
                authorize(new Wechat(MainActivity.this));
                break;
            case R.id.ivSinaWeibo:
                authorize(new SinaWeibo(MainActivity.this));
                break;
            case R.id.btnRemoveMsg:
                Platform qq = ShareSDK.getPlatform(MainActivity.this, QQ.NAME);
                Platform wechat = ShareSDK.getPlatform(MainActivity.this, Wechat.NAME);
                Platform weibo = ShareSDK.getPlatform(MainActivity.this, SinaWeibo.NAME);
                if (qq.isValid()) {
                    qq.removeAccount();
                }
                if (wechat.isValid()) {
                    wechat.removeAccount();
                }
                if (weibo.isValid()) {
                    weibo.removeAccount();
                }
                break;
            case R.id.btnShare:
//                ShareSDK.initSDK(MainActivity.this);
                OnekeyShare oks = new OnekeyShare();
                // 关闭sso授权
                oks.disableSSOWhenAuthorize();
                // 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
                // oks.setNotification(R.drawable.ic_launcher,
                // getString(R.string.app_name));
                // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
                oks.setTitle("分享标题");
                // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
                oks.setTitleUrl("http://blog.csdn.net/donkor_");
                // text是分享文本，所有平台都需要这个字段
                oks.setText("分享文本内容");
                // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
                // oks.setImagePath("/sdcard/test.jpg");//
                // 确保SDcard下面存在此张图片
                oks.setImageUrl("http://img.blog.csdn.net/20161115193036196");
                // url仅在微信（包括好友和朋友圈）中使用
                oks.setUrl("http://blog.csdn.net/donkor_");
                // comment是我对这条分享的评论，仅在人人网和QQ空间使用
                //oks.setComment("");
                // site是分享此内容的网站名称，仅在QQ空间使用
                oks.setSite(getString(R.string.app_name));
                // siteUrl是分享此内容的网站地址，仅在QQ空间使用
                //oks.setSiteUrl(getString(R.string.settingShareSiteUrl));
                // 启动分享GUI
                oks.show(MainActivity.this);
                break;
        }
    }

    //执行授权,获取用户信息
    private void authorize(Platform plat) {
        if (plat.isValid()) {
            String userId = plat.getDb().getUserId();
            if (!TextUtils.isEmpty(userId)) {
                UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
                login(plat.getName(), userId, null);
                return;
            }
        }
        plat.setPlatformActionListener(MainActivity.this);
        //true不使用SSO授权，false使用SSO授权
        plat.SSOSetting(false);
        plat.showUser(null);
    }

    //发送登陆信息
    private void login(String plat, String userId, HashMap<String, Object> userInfo) {
        Message msg = new Message();
        msg.what = MSG_LOGIN;
        msg.obj = plat;
        UIHandler.sendMessage(msg, this);
    }
    protected void onDestroy() {
        //释放资源
        ShareSDK.stopSDK(MainActivity.this);
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_USERID_FOUND: {
                Toast.makeText(this, R.string.userid_found, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_LOGIN: {
                String text = getString(R.string.logining, msg.obj);
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_CANCEL: {
                Toast.makeText(this, R.string.auth_cancel, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_ERROR: {
                Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_COMPLETE: {
                Toast.makeText(this, R.string.auth_complete, Toast.LENGTH_SHORT).show();
            }
            break;
        }
        return false;
    }

    @Override
    public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
        if (action == Platform.ACTION_USER_INFOR) {
            //登录成功,获取需要的信息
            UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
            login(platform.getName(), platform.getDb().getUserId(), res);
            Log.e("asd", "platform.getName():" + platform.getName());
            Log.e("asd", "platform.getDb().getUserId()" + platform.getDb().getUserId());
            String openid = platform.getDb().getUserId() + "";
            String gender = platform.getDb().getUserGender();
            String head_url = platform.getDb().getUserIcon();
            String nickname = platform.getDb().getUserName();

            Log.e("asd", "openid:" + openid);
            Log.e("asd", "gender:" + gender);
            Log.e("asd", "head_url:" + head_url);
            Log.e("asd", "nickname:" + nickname);
        }
    }

    @Override
    public void onError(Platform platform,int action,Throwable t){
        if(action==Platform.ACTION_USER_INFOR){
            UIHandler.sendEmptyMessage(MSG_AUTH_ERROR,this);
        }
        t.printStackTrace();
    }

    @Override
    public void onCancel(Platform platform, int action) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
        }
    }
}
