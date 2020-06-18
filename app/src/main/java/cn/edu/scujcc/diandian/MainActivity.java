package cn.edu.scujcc.diandian;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.net.URLConnection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private RecyclerView channelRv;
    private ChannelRvAdapter rvAdapter;
    private ChannelLab lab = ChannelLab.getInstance();
    private final static String TAG = "DianDian";
    //线程通讯第一步，在主线程创建handler
    private Handler handler = new Handler() {
        //快捷键Ctrl o
        @Override
        public void handleMessage(@NonNull Message msg) {
           switch (msg.what){
               case ChannelLab.MSG_CHANNELS:
                   rvAdapter.notifyDataSetChanged();
                   break;
               case ChannelLab.MSG_NET_FAILURE:
                   failed();
                   break;
           }
        }
    };

    private void failed(){
        Toast.makeText(MainActivity.this, "Token无效，禁止访问", Toast.LENGTH_LONG).show();
        Log.d(TAG,"服务器禁止访问，因为token无效。");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.channelRv = findViewById(R.id.channel_rv);
        //lambda简化
        //适应handler，把适配器改为实例变量
        rvAdapter = new ChannelRvAdapter(MainActivity.this, p -> {
            //跳转到新界面，使用意图Intent
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            //通过位置p得到当前频道channel，传递用户选中的频道到下一个界面
            Channel c = lab.getChannel(p);
            intent.putExtra("channel", c);
            startActivity(intent);
        });

        this.channelRv.setAdapter(rvAdapter);
        this.channelRv.setLayoutManager(new LinearLayoutManager(this));
        
        getUser();
    }

    private void getUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String u = prefs.getString("user","未登录");
        Log.d("DianDian","当前登录的用户是："+u);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //把主线程的handler传给子线程使用
        lab.getData(handler);
    }

}



//    /**
//     * 线程通讯的范例
//     */
//    public  void test(){
//        String m = "test";
//        final Handler h = new Handler(){
//            @Override
//            public void handleMessage(Message msg){
//                Log.d("DianDian","收到子线程消息："+msg.obj);
//            }
//        };
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                //子线程执行
//                //王访问网络，得到结果为"hello"
//                String a = "hello";
//                Message m = new Message();
//                m.obj = a;
//                h.sendMessage(m);
//            }
//        };
//        LinkedBlockingQueue queue = new LinkedBlockingQueue();
//        ThreadPoolExecutor pool = new ThreadPoolExecutor(
//                5,10,50, TimeUnit.MILLISECONDS,queue);
//        pool.execute(r);
//    }

