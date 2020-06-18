package cn.edu.scujcc.diandian;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 数据源，这里存放了频道的所有数据
 * 使用了单例测试用例模式保证这个类仅有一个对象
 */
public class ChannelLab {
    //用常量代替硬编码内容
    private final static String TAG="DianDian";
    public final static int  MSG_HOT_COMMENTS = 2;
    public final static int  MSG_ADD_COMMENT = 3;
    public final static int  MSG_NET_FAILURE = 4;
    public final static int  MSG_CHANNELS = 1;
    //单例第一步
    private static ChannelLab INSTANCE = null;
    private List<Channel> data ;

    //单例第二步
    private ChannelLab() {
        //初始化空白列表
        data = new ArrayList<>();

    }
    //单例第三步
    public static ChannelLab getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChannelLab();
        }
        return INSTANCE;
    }

    /**
     * 获取当前数据中总共有多少个频道
     * @return
     */
    public  int getSize(){

        return data.size();
    }

    /**
     * 获取一个指定频道
     * @param position 频道序号
     * @return 频道对象 Channel
     */
    public Channel getChannel(int position){
        return data.get(position);
    }

    /**
     * 访问网络得到真实数据，代替以前的test()方法
     */
    public  void getData(Handler handler){
        //使用单例
        Retrofit retrofit = RetrofitClient.getInstance();
        ChannelApi api = retrofit.create(ChannelApi.class);
        Call<Result<List<Channel>>> call = api.getAllChannels();
        //enqueue把代码放在子线程去运行
        call.enqueue(new Callback<Result<List<Channel>>>() {
            @Override
            public void onResponse(Call<Result<List<Channel>>> call, Response<Result<List<Channel>>> response) {
                //如果网络访问成功
                if (response.code() == 403) {  //缺少token或token错误
                    Message msg = new Message();
                    msg.what = MSG_NET_FAILURE;
                    handler.sendMessage(msg);
                } else if (null != response && null != response.body()) {
                    Log.d(TAG, "从云得到的数据是：");
                    Log.d(TAG, response.body().toString());
                    Result<List<Channel>> result = response.body();
                    data = result.getData();
                    //发出通知
                    Message msg = new Message();
                    msg.what = MSG_CHANNELS;
                    handler.sendMessage(msg);
                } else {
                    Log.w(TAG, "response没有数据！");
                }
            }

            @Override
            public void onFailure(Call<Result<List<Channel>>> call, Throwable t) {
                //如果网络访问失败
                Log.d(TAG,"访问网络出错了",t);
            }
        });
    }

    /**
     * 替换之前的getChannelData（），获取热门评论
     * @param channelId
     * @param handler
     */
    public List<Comment> getHotComments(String channelId, Handler handler){
        List<Comment> result = null;
        //2通过Retrofit访问服务器得到当前频道信息
        //调用单例

        Retrofit retrofit = RetrofitClient.getInstance();
        ChannelApi api = retrofit.create(ChannelApi.class);
        Call<Result<List<Comment>>> call = api.getHotComments(channelId);
        call.enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                if (response.code() == 403) {  //缺少token或token错误
                    Message msg = new Message();
                    msg.what = MSG_NET_FAILURE;
                    handler.sendMessage(msg);
                } else if (null != response && null != response.body()) {
                    Log.d(TAG, "从阿里云得到的评论是：");
                    Log.d(TAG, response.body().toString());
                    Result<List<Comment>> result = response.body();
                    List<Comment> comments = result.getData();
                    //发出通知
                    Message msg = new Message();
                    msg.what = MSG_HOT_COMMENTS;//自己规定2代表从阿里云获取单个频道
                    msg.obj = comments;
                    handler.sendMessage(msg);
                } else {
                    Log.w(TAG, "response没有数据！");
                }
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {

                Log.e(TAG, "访问网络失败！", t);
                }
        });
        return result;
    }

    /**
     * 添加评论
     */
    public void addComment(String channelId,Comment comment,Handler handler){
        //调用单例
        Retrofit retrofit = RetrofitClient.getInstance();
        ChannelApi api = retrofit.create(ChannelApi.class);
        Call<Channel> call = api.addComment(channelId,comment);
        call.enqueue(new Callback<Channel>() {
            @Override
            public void onResponse(Call<Channel> call, Response<Channel> response) {
                Log.d(TAG,"添加评论后从阿里云得到的数据是：");
                Log.d(TAG,response.body().toString());
                Message msg = new Message();
                msg.what = MSG_ADD_COMMENT;
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(Call<Channel> call, Throwable t) {
                Log.d(TAG,"访问网络出错了",t);
                Message msg = new Message();
                msg.what = MSG_NET_FAILURE;
                handler.sendMessage(msg);

            }
        });

    }


}
