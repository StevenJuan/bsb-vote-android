package com.example.bsbvote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements android.view.View.OnClickListener{

    
    private static EditText itemID,VID,voteNumber;
    private static Button startBtn;
    private static TextView countShow,validShow,regUserShow;
    public static int validVote = 0, regUser = 0, votedCount = 0;
    
    private long exitTime = 0;
    
    private static MainActivity mainactivity;
    
//    public boolean btnFlag = true;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainactivity = this;
		SharedPreferences sharedata=getSharedPreferences("vote_set", 0);
		
        
        
		String strVer = android.os.Build.VERSION.RELEASE;
		strVer = strVer.substring(0,3).trim();
		float fv=Float.valueOf(strVer);
		if(fv>2.3)
		{
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads()
		.detectDiskWrites()
		.detectNetwork() // 这里可以替换为detectAll() 就包括了磁盘读写和网络I/O
		.penaltyLog() //打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
		.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		.detectLeakedSqlLiteObjects() //探测SQLite数据库操作
		.penaltyLog() //打印logcat
		.penaltyDeath()
		.build());
		}
		
        setContentView(R.layout.activity_main);
        startBtn =(Button) this.findViewById(R.id.startBtn);
        startBtn.setOnClickListener(this);
        itemID = (EditText) this.findViewById(R.id.editText1);
        VID = (EditText) this.findViewById(R.id.editText2);
        voteNumber = (EditText) this.findViewById(R.id.voteNumber);
        countShow = (TextView) this.findViewById(R.id.countShow);
        validShow = (TextView) this.findViewById(R.id.validShow);
        regUserShow = (TextView) this.findViewById(R.id.regUserShow);
        itemID.setText(sharedata.getString("item_id", "1"));
        VID.setText(sharedata.getString("v_id", "1"));
        voteNumber.setText(sharedata.getString("vote_number", "1"));

//        httpclient.getCredentialsProvider().setCredentials(new AuthScope("127.0.0.1", 8888), new UsernamePasswordCredentials("", ""));
//        HttpHost proxy = new HttpHost("192.168.1.100", 8888);
//        httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

  //要用handler来处理多线程可以使用runnable接口，这里先定义该接口
    //线程中运行该接口的run函数
    static Runnable update_thread = new Runnable()
    {
        public void run()
        {
            //线程每次执行时输出"UpdateThread..."文字,且自动换行
            //textview的append功能和Qt中的append类似，不会覆盖前面
            //的内容，只是Qt中的append默认是自动换行模式
//            countShow.append("i="+String.valueOf(i)+"\n");
            countShow.setText(String.valueOf(votedCount));
            validShow.setText(String.valueOf(validVote));
            regUserShow.setText(String.valueOf(regUser));
            //延时1s后又将线程加入到线程队列中
            //handler.postDelayed(update_thread, 1000);

        }
    };
   
    static Runnable vote_finish = new Runnable()
    {
        public void run()
        {
        	new AlertDialog.Builder(mainactivity).setTitle("提示" ).setMessage("投票完成" ).setPositiveButton("确定" ,null ).show();
        	startBtn.setEnabled(true);
        	itemID.setEnabled(true);
        	VID.setEnabled(true);
        	voteNumber.setEnabled(true);
        }
    };
    
    
	@Override
	public void onClick(View id) {
		// TODO Auto-generated method stubs
		if (!isNetworkAvailable()) {
			// new AlertDialog.Builder(mainactivity).setTitle("提示"
			// ).setMessage("请先连接网络" ).setPositiveButton("确定" ,null ).show();
			new AlertDialog.Builder(mainactivity)
					.setTitle("提示")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage("未连接网络，现在设置网络连接？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											Settings.ACTION_SETTINGS));
								}
							}).setNegativeButton("取消", null).show();

			return;
		}

		switch (id.getId()) {
		case R.id.startBtn:// 开始按钮
			validVote = 0;
			regUser = 0;
			SharedPreferences sharedata=getSharedPreferences("vote_set", 0);
			SharedPreferences.Editor editor = sharedata.edit();
			editor.putString("item_id", itemID.getText().toString());
			editor.putString("v_id", VID.getText().toString());
			editor.putString("vote_number", voteNumber.getText().toString());
			editor.commit();
			startBtn.setEnabled(false);
        	itemID.setEnabled(false);
        	VID.setEnabled(false);
        	voteNumber.setEnabled(false);
        	votedCount = 0;
        	validVote = 0;
        	regUser = 0;
        	doVote.Vote(Integer.parseInt(voteNumber.getText().toString()),
        			itemID.getText().toString(),
        			VID.getText().toString());
		}
	}




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
                exitTime = System.currentTimeMillis();   
            } else {
                finish();
                System.exit(0);
            }
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
    
	public boolean isNetworkAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						State state = connectivity.getNetworkInfo(
								ConnectivityManager.TYPE_MOBILE).getState();
						if (State.CONNECTED == state) {
							Log.i("通知", "GPRS网络已连接");
							Toast.makeText(getApplicationContext(),
									"GPRS网络已连接", Toast.LENGTH_SHORT).show();
						}
						state = connectivity.getNetworkInfo(
								ConnectivityManager.TYPE_WIFI).getState();
						if (State.CONNECTED == state) {
							Log.i("通知", "WIFI网络已连接");
							Toast.makeText(getApplicationContext(),
									"WIFI网络已连接", Toast.LENGTH_SHORT).show();
						}
						return true;
					}
				}
			}
		}
		return false;
	}
}
