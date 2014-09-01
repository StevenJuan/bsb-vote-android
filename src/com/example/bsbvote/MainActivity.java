package com.example.bsbvote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.ff.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.Preference;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements android.view.View.OnClickListener{

    private DefaultHttpClient httpclient;
    private EditText itemID,VID,voteNumber;
    private Button startBtn;
    private TextView countShow,validShow,regUserShow;
    private int i = 0, validVote = 0, regUser = 0;
    
    private long exitTime = 0;
    
    private MainActivity mainactivity;
    
//    public boolean btnFlag = true;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainactivity = this;
		SharedPreferences sharedata=getSharedPreferences("vote_set", 0);
		
        httpclient = new DefaultHttpClient();
        
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

    //使用handler时首先要创建一个handler
    Handler handler = new Handler();
    //要用handler来处理多线程可以使用runnable接口，这里先定义该接口
    //线程中运行该接口的run函数
    Runnable update_thread = new Runnable()
    {
        public void run()
        {
            //线程每次执行时输出"UpdateThread..."文字,且自动换行
            //textview的append功能和Qt中的append类似，不会覆盖前面
            //的内容，只是Qt中的append默认是自动换行模式
//            countShow.append("i="+String.valueOf(i)+"\n");
            countShow.setText(String.valueOf(i));
            validShow.setText(String.valueOf(validVote));
            regUserShow.setText(String.valueOf(regUser));
            //延时1s后又将线程加入到线程队列中
            //handler.postDelayed(update_thread, 1000);

        }
    };
    
    Runnable vote_finish = new Runnable()
    {
        public void run()
        {
        	new AlertDialog.Builder(mainactivity).setTitle("提示" ).setMessage("投票完成" ).setPositiveButton("确定" ,null ).show();
        }
    };
    
    
	@Override
	public void onClick(View id) {
		// TODO Auto-generated method stubs
		
		switch (id.getId()) {
		case R.id.startBtn:// 开始按钮
			SharedPreferences sharedata=getSharedPreferences("vote_set", 0);
			SharedPreferences.Editor editor = sharedata.edit();
			editor.putString("item_id", itemID.getText().toString());
			editor.putString("v_id", VID.getText().toString());
			editor.putString("vote_number", voteNumber.getText().toString());
			editor.commit();
			new Thread(new Runnable() {
				@Override
				public void run() {
					int j = Integer.parseInt(voteNumber.getText().toString());
					int k=0;
					for (i = 1; i <= j; i++) {
						if(k==18){
							try {
								Thread.sleep(70000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							k=0;
						}
						try {
							regUser(httpclient);
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							vote(httpclient);
							handler.post(update_thread);
							k++;
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					handler.post(vote_finish);
					
				}
			}).start();
			
			break;
		default:
			break;
		}
	}

	// 注册用户
    private void regUser(DefaultHttpClient httpclient) throws ClientProtocolException, IOException {
//        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.ACCEPT_ALL);  
    	HttpPost httppost = new HttpPost("http://m.passport.cntv.cn/site/reg");
        System.out.println("请求: " + httppost.getRequestLine());
        // 构造最简单的字符串数据
        //生成手机号
        String n1 = (String.valueOf(System.currentTimeMillis()%(9999999-1111111)+1111111));
        String n2 = (String.valueOf(System.currentTimeMillis()%(999-111)+111));
        String phoneNum = "1"+n1+n2;
        System.out.print(phoneNum+"\n");
        StringEntity reqEntity = new StringEntity("Form%5Busername%5D="+phoneNum+"&Form%5Brealname%5D=&Form%5Bpassword%5D=123456&app=bsb");
//        System.out.println("1");
        // 设置请求的数据   
        httppost.setEntity(reqEntity);
//        System.out.println("2");
        //设置头
        httppost.setHeader("Host", "m.passport.cntv.cn");
        httppost.setHeader("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        //httppost.setHeader("Accept-Encoding", "gzip, deflate");
        httppost.setHeader("X-Requested-With", "XMLHttpRequest");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httppost.setHeader("Referer", "http://m.passport.cntv.cn/html/reg.html?rurl=http%3A%2F%2Fqr.cntv.cn%2Fbsb");
        httppost.setHeader("Connection", "keep-alive");
        httppost.setHeader("Pragma", "no-cache");
        httppost.setHeader("Cache-Control", "no-cache");
        //执行
//        System.out.println("3");
        HttpResponse response;
        response = httpclient.execute(httppost);
//        System.out.println("4");
        Header[] heads = response.getAllHeaders();
        // 打印所有响应头   
//        for (Header h : heads) {
//            System.out.println(h.getName() + ":" + h.getValue());
//        }
//        System.out.println("5");
        //获取响应信息
        HttpEntity entity = response.getEntity();
        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        if (entity != null) {
            System.out.println("Response content length: " + entity.getContentLength());
        }
//         显示结果   
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        System.out.println("返回信息-----------------------------------------");
        for (String line = reader.readLine(); line != null; line = reader 
                .readLine()) { 
            builder.append(line); 
        } 
        Log.i("cat", ">>>>>>" + builder.toString()); 
        
        try {
			JSONObject jsonObject = new JSONObject(builder.toString());
			System.out.println("+++++++++++++++++++++\n"+jsonObject.getString("error"));
			if(jsonObject.getInt("error")==0){
				regUser++;
				System.out.println("--------"+regUser);
			}
			System.out.println("--------"+jsonObject.getString("msg"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        if (entity != null) {
            entity.consumeContent();
        }

    }
  //投票
    private void vote(DefaultHttpClient httpclient) throws ClientProtocolException, IOException {
//        HttpGet httpget = new HttpGet("http://qr.cntv.cn/bsb/vote/Post?itemid=3249&vid=366");
//        HttpGet httpget = new HttpGet("http://qr.cntv.cn/bsb/vote/Post?itemid="+ITEM_ID+"&vid="+V_ID);
    	

        HttpGet httpget = new HttpGet("http://qr.cntv.cn/bsb/vote/Post?itemid="+itemID.getText()+"&vid="+VID.getText());
        
        System.out.println("请求: " + httpget.getRequestLine());
        //设置头
        httpget.setHeader("Host", "qr.cntv.cn");
        httpget.setHeader("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
        httpget.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        httpget.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        //httpget.setHeader("Accept-Encoding", "gzip, deflate");
        httpget.setHeader("DNT", "1");
        httpget.setHeader("Referer", "http://qr.cntv.cn/bsb");
        httpget.setHeader("Connection", "keep-alive");
        //httpget.setHeader("Cookie", "m_cntv_app=bsb;m_cntv_gapp=bsb;m_cntv_uid=7047108;m_cntv_nickname=%E5%A4%AE%E8%A7%86%E7%BD%91%E5%8F%8B;m_cntv_avatar=http%3A%2F%2Fm.passport.cntv.cn%2Fimages%2Favatar.jpg;m_cntv_mobile=312fJo3LydoFGja0HbDQVxa8okTmRQFeg90mk9K%2BGB4MxSQnZobzTA;m_cntv_realname=d91arKGm5cJt%2BjsVAqMgDcNhgLiEG3sVY693zP4;m_cntv_auth=070djYVzCvXdNB%2B2D06%2BQz2xUW3RgaNBewF0Os2m05ph2AQ37AXbRaKSZuoD8VEUA7EkpjhMAw;iphoneFix=FixHidden");

        HttpResponse response = httpclient.execute(httpget);

        Header[] heads = response.getAllHeaders();
        // 打印所有响应头   
//        for (Header h : heads) {
//            System.out.println(h.getName() + ":" + h.getValue());
//        }

        // 执行   
        HttpEntity entity = response.getEntity();
//        System.out.println("----------------------------------------");
//        System.out.println(response.getStatusLine());
//        if (entity != null) {
//            System.out.println("Response content length: " + entity.getContentLength());
//        }
     // 显示结果   
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        System.out.println("返回信息-----------------------------------------");
        for (String line = reader.readLine(); line != null; line = reader 
                .readLine()) { 
            builder.append(line); 
        } 
        Log.i("cat", ">>>>>>" + builder.toString()); 
        
        try {
			JSONObject jsonObject = new JSONObject(builder.toString());
			System.out.println("+++++++++++++++++++++\n"+jsonObject.getString("error"));
			if(jsonObject.getInt("error")==0){
				validVote++;
			}
			System.out.println("--------"+jsonObject.getString("msg"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        if (entity != null) {
            entity.consumeContent();
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
}
