package com.example.lenovo.myapplication;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.myapplication.api.BmobService;
import com.example.lenovo.myapplication.api.Client;

import org.json.JSONObject;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by Qin yiyi 16301087@bjtu.edu.cn
 */

public class Login extends AppCompatActivity {

    private BmobUser current_user;
    private Button login, register;
    private TextInputLayout name, password;
    private TextInputEditText name_context, password_context;
    private String name_str, password_str;



    /**
     * 友盟开始授权(登入)
     */
    public static void shareLoginUmeng(final Activity activity, SHARE_MEDIA share_media_type) {

        UMShareAPI.get(activity).getPlatformInfo(activity, share_media_type, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
//                android.util.Log.e(TAG, "onStart授权开始: ");
                Toast.makeText(activity, "授权开始", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, java.util.Map<String, String> map) {
                //sdk是6.4.5的,但是获取值的时候用的是6.2以前的(access_token)才能获取到值,未知原因
                Toast.makeText(activity, "授权完成", Toast.LENGTH_LONG).show();
                String uid = map.get("uid");
                String openid = map.get("openid");//微博没有
                String unionid = map.get("unionid");//微博没有
                String access_token = map.get("access_token");
                String refresh_token = map.get("refresh_token");//微信,qq,微博都没有获取到
                String expires_in = map.get("expires_in");
                String name = map.get("name");//名称
                String gender = map.get("gender");//性别
                String iconurl = map.get("iconurl");//头像地址

//                Log.e(TAG, "onStart授权完成: " + openid);
//                Log.e(TAG, "onStart授权完成: " + unionid);
//                Log.e(TAG, "onStart授权完成: " + access_token);
//                Log.e(TAG, "onStart授权完成: " + refresh_token);
//                Log.e(TAG, "onStart授权完成: " + expires_in);
//                Log.e(TAG, "onStart授权完成: " + uid);
//                Log.e(TAG, "onStart授权完成: " + name);
//                Log.e(TAG, "onStart授权完成: " + gender);
//                Log.e(TAG, "onStart授权完成: " + iconurl);
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                Toast.makeText(activity, "授权失败", Toast.LENGTH_LONG).show();
//                android.util.Log.e(TAG, "onError: " + "授权失败");
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                Toast.makeText(activity, "授权取消", Toast.LENGTH_LONG).show();
//                Log.e(TAG, "onError: " + "授权取消");
            }
        });
    }

    /**
     * 友盟取消授权（登入）
     */
    public void umengDeleteOauth(SHARE_MEDIA share_media_type) {

        UMShareAPI.get(this).deleteOauth(this, share_media_type, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                //开始授权
//                android.util.Log.e(TAG, "onStart: ");
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, java.util.Map<String, String> map) {
                //取消授权成功 i=1
//                android.util.Log.e(TAG, "onComplete: ");
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                //授权出错

//                android.util.Log.e(TAG, "onError: ");
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                //取消授权

//                android.util.Log.e(TAG, "onCancel: ");
            }
        });
    }


    private void onClickLogin() {
        if (!mTencent.isSessionValid()) {
            mTencent.login(this, "all", loginListener);
        }
    }
    /**
     * 获取登录QQ腾讯平台的权限信息(用于访问QQ用户信息)
     * @param jsonObject
     */
    public static void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!android.text.TextUtils.isEmpty(token) && !android.text.TextUtils.isEmpty(expires)
                    && !android.text.TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch(Exception e) {
        }
    }
    private void updateUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new IUiListener() {
                @Override
                public void onError(UiError e) {
                }
                @Override
                public void onComplete(final Object response) {
                    android.os.Message msg = new android.os.Message();
                    msg.obj = response;
                    android.util.Log.i("tag", response.toString());
                    msg.what = 0;
                    //mHandler.sendMessage(msg);
                }
                @Override
                public void onCancel() {
                }
            };
            mInfo = new UserInfo(this, mTencent.getQQToken());
            mInfo.getUserInfo(listener);

        }
    }
    /**
     * 继承的到BaseUiListener得到doComplete()的方法信息
     */
    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {//得到用户的ID  和签名等信息  用来得到用户信息
            android.util.Log.i("lkei",values.toString());
            initOpenidAndToken(values);
            updateUserInfo();
        }
    };
    /***
     * QQ平台返回返回数据个体 loginListener的values
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode,resultCode,data,loginListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private class BaseUiListener implements IUiListener {
        @Override
        public void onComplete(Object response) {
            if (null == response) {
                Toast.makeText(LoginActivity.this, "登录失败",Toast.LENGTH_LONG).show();
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                Toast.makeText(LoginActivity.this, "登录失败",Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(LoginActivity.this, "登录成功",Toast.LENGTH_LONG).show();
            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {

        }
        @Override
        public void onError(UiError e) {
            //登录错误
        }

        @Override
        public void onCancel() {
            // 运行完成
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //初始化友盟SDK
        com.umeng.commonsdk.UMConfigure.init(this, "", "Shutang", com.umeng.commonsdk.UMConfigure.DEVICE_TYPE_PHONE, "");
        UMShareAPI.get(this);//初始化sd
        //开启debug模式，方便定位错误，具体错误检查方式可以查看
        //http://dev.umeng.com/social/android/quick-integration的报错必看，正式发布，请关闭该模式
        Config.DEBUG = true;
        com.umeng.socialize.common.QueuedWork.isUseThreadPool = true;
        //三方获取用户资料时是否每次都要进行授权
        com.umeng.socialize.UMShareConfig config = new com.umeng.socialize.UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        UMShareAPI.get(this).setShareConfig(config);

        //微信
        PlatformConfig.setWeixin("wxdc1e388c3822c80b", "3baf1193c85774b3fd9d18447d76cab0");
        //新浪微博(第三个参数为回调地址)
        PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad",
                "http://sns.whalecloud.com/sina2/callback");



        //初始化bmob服务
        Bmob.initialize(this, "5fad9f2543ffa83e56155a46398d6ede");


        findViewById(R.id.new_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLogin();
//                    Toast.makeText(LoginActivity.this,"该功能未开放",Toast.LENGTH_LONG).show();
            }
        });
        if (mTencent == null) {
            mTencent = Tencent.createInstance(mAppid, this);
        }

        findViewById(R.id.weixin_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                umengDeleteOauth(SHARE_MEDIA.WEIXIN);
                //开始授权
                shareLoginUmeng(LoginActivity.this, SHARE_MEDIA.WEIXIN);

                //shareLoginUmeng(this, SHARE_MEDIA.SINA);

            }
        });

        findViewById(R.id.weibo_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                umengDeleteOauth(SHARE_MEDIA.SINA);
                //开始授权
                shareLoginUmeng(LoginActivity.this, SHARE_MEDIA.SINA);

                //shareLoginUmeng(this, SHARE_MEDIA.SINA);
            }
        });



        //设置下划线
        TextView forget_text = findViewById(R.id.forget_text);
        forget_text.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        //设置监听
        forget_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    if (!mTencent.isSessionValid()) {
//                             mTencent.login(this, "all", loginListener);
//                        }
                Toast.makeText(LoginActivity.this,"该功能未开放",Toast.LENGTH_LONG).show();
            }
        });



        TextView signup_text = findViewById(R.id.register_text);
        signup_text.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        signup_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }


    private boolean checkUser() {
        name.setError("");
        password.setError("");
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(name_str);
        if (TextUtils.isEmpty(name_str)) {
            name.setError("Input your phone number!");
            return false;
        }
        if (!m.matches()){
            name.setError("Invalid phone number (invalid symbol)!");
            return false;
        }
        if(name_str.length()!=11){
            name.setError("Invalid phone number (must be 11 bits)!");
            return false;
        }
        if (TextUtils.isEmpty(password_str)) {
            password.setError("Input your password!");
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Bmob.initialize(this, "f96b15fd060468a58c920a119ef90035");
        /*
         * 验证本地缓存用户
         * 若用户存在，则免登陆
         * 否则需用户输入登陆信息
         */
        current_user = BmobUser.getCurrentUser();

        if (current_user != null) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
        } else {
            name = findViewById(R.id.login_phone);
            password = findViewById(R.id.login_password);
            name_context = findViewById(R.id.login_phone_input);
            password_context = findViewById(R.id.login_password_input);
            login = findViewById(R.id.login_login);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    name_str = name_context.getText().toString();
                    password_str = password_context.getText().toString();

                    if( ! checkUser())
                        return;

                    //使用retrofit实现登录请求
                    BmobService service = Client.retrofit.create(BmobService.class);
                    Call<ResponseBody> call = service.getUser(name_str, password_str);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if (response.code() == 200) {
                                Toast.makeText(Login.this, "Login success", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                intent.putExtra("username",name_str);
                                startActivity(intent);
                            } else if (response.code() == 400) {
                                password.setError("Username and Password mismatch!");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(Login.this, t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            register = findViewById(R.id.login_register);
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Login.this, Register.class);
                    startActivity(intent);
                }
            });
        }
    }
}