package com.xunzhimei.psg.Activieties;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.xunzhimei.psg.R;
import com.xunzhimei.psg.Utils.Log;
import com.xunzhimei.psg.okhttp.CallBackUtil;
import com.xunzhimei.psg.okhttp.OkhttpUtil;

import java.util.HashMap;

import okhttp3.Call;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

/**
 * Class  :RegistActivity
 * Author :created by Vihan on 2018/8/4  22:24
 * Email  :vihanmy@google.com
 * Notes  :用户注册界面
 **/
public class RegistActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "RegistActivity";
    private android.support.v7.widget.Toolbar mToolBar;

    //Views
    private android.support.v7.widget.Toolbar mTb;
    private EditText mEt_userName;
    private ImageView mIv_cleancontent;
    private Button mBt_getIdentycode;
    private EditText mEt_userIdentyCode;
    private EditText mEt_setPassword;
    private ImageView mIv_showPwd;
    private EditText mEt_EnsurePassword;
    private ImageView mIv_checkPswSame;
    private Button mBt_regist;
    private boolean isVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        mToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.tb);
        //只有这样设置了，系统才知道，这样的额一个Toolbar的布局将用于取代Acctionibar并将Activity中lable中的内容显示在ToolBar上边
        setSupportActionBar(mToolBar);

        initViews();
        initListeners();
    }

    private void initViews()
    {
        //获取ActionBar对象
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            //使Actionbar的HomeAsUp按键显示出来，默认不显示，默认作用是是返回上一个活动，默认图标是一个箭头
            actionBar.setDisplayHomeAsUpEnabled(true);

            //修改HomeAsUp键的默认图标，使用onOptionsItemSelected(MenuItem item)方法中监听ID  android.R.id.home（固定不变的，由官方确定）  来重新定期其点击事件
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }


        mEt_userName = (EditText) findViewById(R.id.et_userName);
        mIv_cleancontent = (ImageView) findViewById(R.id.iv_cleanContent);

        mBt_getIdentycode = (Button) findViewById(R.id.bt_getIdentycode);
        mBt_getIdentycode.setOnClickListener(this);

        mEt_userIdentyCode = (EditText) findViewById(R.id.et_userIdentyCode);
        mEt_setPassword = (EditText) findViewById(R.id.et_setPassword);
        mIv_showPwd = (ImageView) findViewById(R.id.iv_showPwd);
        mIv_showPwd.setOnClickListener(this);
        mEt_EnsurePassword = (EditText) findViewById(R.id.et_EnsurePassword);
        mIv_checkPswSame = (ImageView) findViewById(R.id.iv_checkPswSame);

        mBt_regist = (Button) findViewById(R.id.bt_regist);
        mBt_regist.setOnClickListener(this);
    }

    private void initListeners()
    {
        mEt_setPassword.addTextChangedListener(new TextWatcher()
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
                mIv_checkPswSame.setVisibility(View.VISIBLE);
                if (mEt_setPassword.getText().toString().trim().equals(mEt_EnsurePassword.getText().toString().trim()))
                {
                    Log.i("", "同 ");

                    mIv_checkPswSame.setImageDrawable(getDrawable(R.drawable.ic_checked_green_24dp));
                }
                else
                {
                    Log.i("", "不同 ");
                    mIv_checkPswSame.setImageDrawable(getDrawable(R.drawable.ic_checked_red_24dp));
                }
            }


        });

        mEt_EnsurePassword.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                Log.i(TAG, "输入文字中的状态，count是输入字符数" + count);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                Log.i(TAG, "输入文本之前的状态");
            }

            @Override
            public void afterTextChanged(Editable s)
            {

                if (mEt_setPassword.getText().toString().trim().equals(mEt_EnsurePassword.getText().toString().trim()))
                {
                    Log.i("", "同 ");

                    mIv_checkPswSame.setImageDrawable(getDrawable(R.drawable.ic_checked_green_24dp));
                }
                else
                {
                    Log.i("", "不同 ");
                    mIv_checkPswSame.setImageDrawable(getDrawable(R.drawable.ic_checked_red_24dp));
                }

            }
        });


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
                    mIv_cleancontent.setVisibility(View.GONE);
                }
                else
                {
                    Log.i("", "非空");
                    mIv_cleancontent.setVisibility(View.VISIBLE);
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
                        mIv_cleancontent.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    mIv_cleancontent.setVisibility(View.GONE);
                    Log.i("", "失去焦点");   // 失去焦点
                }

            }
        });

        mIv_cleancontent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mEt_userName.setText("");
            }
        });

    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.bt_regist:  //注册逻辑
                if (!mEt_setPassword.getText().toString().trim().equals(mEt_EnsurePassword.getText().toString().trim()))
                {
                    Snackbar.make(mBt_regist, "两次密码输入不一致", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                else
                {
                    //TODO :比对校验码
                }
                break;

            case R.id.bt_getIdentycode:
                //TODO :获取验证码
                if (TextUtils.isEmpty(mEt_userName.getText()) || mEt_userName.getText().toString().trim().length() != 11)
                {
                    Toast.makeText(getApplicationContext(), "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.iv_showPwd:
                if (isVisible)
                {
                    mIv_showPwd.setImageDrawable(getDrawable(R.drawable.ic_login_visible_24dp));
                    mEt_setPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isVisible = false;
                }
                else
                {
                    mIv_showPwd.setImageDrawable(getDrawable(R.drawable.ic_login_passoff_24dp));
                    mEt_setPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isVisible = true;
                }
                break;

        }
    }

    public void userRegist(String phone, String psw, String code)
    {
        String url = "https://api.it120.cc/86d686f99519e0d218851c37975e2fd9/user/m/register";
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mobile", phone);//手机号码
        paramsMap.put("pwd", psw);//密码
        paramsMap.put("code", code);//验证码

        OkhttpUtil.okHttpPost(url, paramsMap, new CallBackUtil.CallBackString()
        {
            @Override
            public void onFailure(Call call, Exception e)
            {

            }

            @Override
            public void onResponse(String response)
            {
                Log.i("", response);
            }
        });
    }
}
