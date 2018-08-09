package com.xunzhimei.psg.Activieties;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.xunzhimei.psg.R;

public class TestActivity extends AppCompatActivity
{
//    final AlertDialog.Builder normalDialog =
//            new AlertDialog.Builder(TestActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toast.makeText(this, "ert", Toast.LENGTH_SHORT).show();
    }

//
//    public void initDialog()
//    {
//        normalDialog.setIcon(R.drawable.ic_launcher);
//        normalDialog.setTitle("我是一个普通Dialog");
//        normalDialog.setMessage("你要点击哪一个按钮呢?");
//        normalDialog.setPositiveButton("确定",
//                new DialogInterface.OnClickListener()
//
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//                        //...To-do
//                    }
//                });
//        normalDialog.setNegativeButton("关闭",
//                new DialogInterface.OnClickListener()
//
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//                        //...To-do
//                    }
//                });
//        // 显示
//
//    }

//    public void bt_test(View view)
//    {
//        normalDialog.show();
//    }
}
