package com.xunzhimei.psg.Activieties;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xunzhimei.psg.R;
import com.xunzhimei.psg.Utils.Log;
import com.xunzhimei.psg.okhttp.CallBackUtil;
import com.xunzhimei.psg.okhttp.OkhttpUtil;

import java.util.HashMap;

import okhttp3.Call;

/**
 * Class  :LoginActivity
 * Author :created by Vihan on 2018/8/4  22:24
 * Email  :vihanmy@google.com
 * Notes  :用于用户的登录界面
 **/
public class LoginActivity extends AppCompatActivity implements OnClickListener
{

    private static final String TAG = "LoginActivity";

    //Views
    private EditText mEt_userName;
    private ImageView mIv_clean_phone;
    private EditText mEt_userPassword;
    private ImageView mIv_showPwd;
    private Button mBtn_login;
    private TextView mTv_regist;
    private TextView mTv_findPwd;
    private TextView mTv_contact;
    private TextView mTv_aboutUs;

    //密码是否可见的标志
    private boolean isVisible = false;
    private LinearLayout mCv_login;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        initListeners();
    }

    private void initListeners()
    {
        mEt_userName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                Log.i(TAG, "输入文字中的状态，count是输入字符数" + count);
                Log.i("CharSequence", s.toString());
                Log.i("start", start + "");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                Log.i(TAG, "输入文本之前的状态");
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Log.i(TAG, "输入文字后的状态");
                if (TextUtils.isEmpty(mEt_userName.getText()))
                {
                    Log.i("", "空");
                    mIv_clean_phone.setVisibility(View.GONE);
                }
                else
                {
                    Log.i("", "非空");
                    mIv_clean_phone.setVisibility(View.VISIBLE);
                }
            }
        });

        mEt_userName.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    Log.i("", "获得焦点");   // 获得焦点
                    if (TextUtils.isEmpty(mEt_userName.getText()))
                    {
                        Log.i("", "空");

                    }
                    else
                    {
                        Log.i("", "非空");
                        mIv_clean_phone.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    mIv_clean_phone.setVisibility(View.GONE);
                    Log.i("", "失去焦点");   // 失去焦点
                }

            }
        });
    }

    private void initViews()
    {
        //登录相关
        mEt_userName = (EditText) findViewById(R.id.et_userName);
        mEt_userPassword = (EditText) findViewById(R.id.et_userPassword);
        mBtn_login = (Button) findViewById(R.id.bt_login);
        mBtn_login.setOnClickListener(this);

        mCv_login = (LinearLayout) findViewById(R.id.body);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_anim);
        anim.setFillAfter(true);
        mCv_login.startAnimation(anim);


        mIv_clean_phone = (ImageView) findViewById(R.id.iv_clean_phone);

        mIv_showPwd = (ImageView) findViewById(R.id.iv_showPwd);
        mIv_showPwd.setOnClickListener(this);

        //服务相关
        mTv_regist = (TextView) findViewById(R.id.tv_regist);
        mTv_regist.setOnClickListener(this);
        mTv_regist.setFocusable(false);//让EditText失去焦点，然后获取点击事件

        mTv_findPwd = (TextView) findViewById(R.id.tv_findPwd);
        mTv_findPwd.setOnClickListener(this);

        mTv_contact = (TextView) findViewById(R.id.tv_contact);
        mTv_contact.setOnClickListener(this);

        mTv_aboutUs = (TextView) findViewById(R.id.tv_aboutUs);
        mTv_aboutUs.setOnClickListener(this);

        System.out.println("LoginActivity-->" + "initViews" + android.os.Build.VERSION.SDK_INT);

    }

    @Override
    public void onClick(View view)
    {
        Log.i(TAG, "点击事件被响应");
        switch (view.getId())
        {
            case R.id.bt_login:
                if (TextUtils.isEmpty(mEt_userName.getText()) || TextUtils.isEmpty(mEt_userPassword.getText()))
                {
                    Toast.makeText(getApplicationContext(), "请检查密码和用户名", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //TODO :登录操作
                    Log.i(TAG, "onClick: " + mEt_userName.getText());
                    Log.i(TAG, "onClick: " + mEt_userPassword.getText());
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.iv_showPwd:
                if (isVisible)
                {
                    mIv_showPwd.setImageDrawable(getDrawable(R.drawable.ic_login_visible_24dp));
                    mEt_userPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isVisible = false;
                }
                else
                {
                    mIv_showPwd.setImageDrawable(getDrawable(R.drawable.ic_login_passoff_24dp));
                    mEt_userPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isVisible = true;
                }
                break;

            case R.id.tv_regist://注册
                Log.i("", "注册逻辑");
                Intent intent = new Intent(getApplicationContext(), RegistActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_findPwd://找回密码
                Log.i("", "密码找回逻辑");
                break;
            case R.id.tv_contact://联系我们
                Log.i("", "Contact US");
                break;

            case R.id.tv_aboutUs://关于我们
                Log.i("", "about US");
                break;
        }
    }


}