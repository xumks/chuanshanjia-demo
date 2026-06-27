package com.union_test.toutiao.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.union_test.toutiao.R;
import com.union_test.toutiao.config.TTAdManagerHolder;
import com.union_test.toutiao.utils.TToast;
import com.union_test.toutiao.utils.UIUtils;
import com.union_test.toutiao.view.LoadMoreListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Feed广告使用示例,使用ListView
 */
@SuppressWarnings("ALL")
public class NativeExpressOnePointFiveRecyclerActivity extends Activity {
    private static final String TAG = "FeedListActivity";

    private static final int AD_POSITION = 3;
    private LoadMoreListView mListView;
    private MyAdapter myAdapter;

    private List<TTNativeExpressAd> mData;
    private EditText mEtWidth;
    private EditText mEtHeight;
    private Button mButtonLoadAd;

    private TTAdNative mTTAdNative;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_native_express_listview);
        //step1:初始化sdk

        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:创建TTAdNative对象,用于调用广告请求接口

        mTTAdNative = ttAdManager.createAdNative(this);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        initListView();
        Button button = (Button)findViewById(R.id.btn_anel_back);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @SuppressWarnings("RedundantCast")
    private void initListView() {
        mEtHeight = (EditText) findViewById(R.id.express_height);
        mEtHeight.setVisibility(View.GONE);
        mEtWidth = (EditText) findViewById(R.id.express_width);
        mButtonLoadAd = (Button) findViewById(R.id.btn_express_load);
        mButtonLoadAd.setOnClickListener(mClickListener);

        mListView = (LoadMoreListView) findViewById(R.id.my_list);
        mListView.setLoadingFinish();
        mData = new ArrayList<>();
        myAdapter = new MyAdapter(this, mData);
        mListView.setAdapter(myAdapter);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadListAd();
            }
        }, 500);
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_express_load) {
                if (mData != null) {
                    mData.clear();
                    if (myAdapter != null) {
                        myAdapter.notifyDataSetChanged();
                    }
                }
                loadListAd();
            }
        }
    };

    /**
     * 加载feed广告
     */
    private void loadListAd() {
        float expressViewWidth;
        float expressViewHeight;
        try {
            expressViewWidth = Float.parseFloat(mEtWidth.getText().toString());
            //expressViewHeight = Float.parseFloat(mEtHeight.getText().toString());
        } catch (Exception e) {
            expressViewWidth = UIUtils.getScreenWidthDp(this);
        }
        //1.5 高度计算逻辑可详见文档
        expressViewHeight = 0;
        //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("949099102")
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .build();

        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染

        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override

            public void onError(int code, String message) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                    mListView.dismissLoadingShow();
                }
                TToast.show(NativeExpressOnePointFiveRecyclerActivity.this, message);
            }

            @Override

            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                    mListView.dismissLoadingShow();
                }

                if (ads == null || ads.isEmpty()) {
                    TToast.show(NativeExpressOnePointFiveRecyclerActivity.this, "on FeedAdLoaded: ad is null!");
                    return;
                }
                bindAdListener(ads);
            }
        });
    }


    private void bindAdListener(final List<TTNativeExpressAd> ads) {
        final int count = mData.size();

        for (TTNativeExpressAd ad : ads) {

            final TTNativeExpressAd adTmp = ad;
            mData.add(adTmp);
            myAdapter.notifyDataSetChanged();


            adTmp.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                @Override

                public void onAdClicked(View view, int type) {
                    TToast.show(NativeExpressOnePointFiveRecyclerActivity.this, "广告被点击");
                }

                @Override

                public void onAdShow(View view, int type) {
                    TToast.show(NativeExpressOnePointFiveRecyclerActivity.this, "广告展示");
                }

                @Override

                public void onRenderFail(View view, String msg, int code) {
                    TToast.show(NativeExpressOnePointFiveRecyclerActivity.this, msg + " code:" + code);
                }

                @Override

                public void onRenderSuccess(View view, float width, float height) {
                    //返回view的宽高 单位 dp
                    TToast.show(NativeExpressOnePointFiveRecyclerActivity.this, "渲染成功");
                }
            });

            adTmp.setVideoAdListener(new TTNativeExpressAd.ExpressVideoAdListener() {
                @Override

                public void onVideoLoad() {
                    Log.d("videotag", "onVideoLoad");
                }

                @Override

                public void onVideoError(int errorCode, int extraCode) {
                    Log.d("videotag", "onVideoError");
                }

                @Override

                public void onVideoAdStartPlay() {
                    Log.d("videotag", "onVideoAdStartPlay");
                }

                @Override

                public void onVideoAdPaused() {
                    Log.d("videotag", "onVideoAdPaused");
                }

                @Override

                public void onVideoAdContinuePlay() {
                    Log.d("videotag", "onVideoAdContinuePlay");
                }

                @Override

                public void onProgressUpdate(long current, long duration) {
                    Log.d("videotag", "onProgressUpdate: " + current);
                }

                @Override

                public void onVideoAdComplete() {
                    Log.d("videotag", "onVideoAdComplete");
                }

                @Override

                public void onClickRetry() {
                    Log.d("videotag", "onClickRetry");
                }
            });
            ad.render();
        }

    }

    @SuppressWarnings("CanBeFinal")
    private static class MyAdapter extends BaseAdapter {

        private static final int ITEM_VIEW_TYPE_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_GROUP_PIC_AD = 1;
        private static final int ITEM_VIEW_TYPE_SMALL_PIC_AD = 2;
        private static final int ITEM_VIEW_TYPE_LARGE_PIC_AD = 3;
        private static final int ITEM_VIEW_TYPE_VIDEO = 4;
        private static final int ITEM_VIEW_TYPE_VERTICAL_IMG = 5;//竖版图片
        private static final int ITEM_VIEW_TYPE_VIDEO_VERTICAL = 6;//竖版视频
        private static final int ITEM_VIEW_TYPE_ONEPOINTFIVE = 7; //1.5卡

        private int mVideoCount = 0;



        private List<TTNativeExpressAd> mData;
        private Context mContext;

        private Map<AdViewHolder, TTAppDownloadListener> mTTAppDownloadListenerMap = new WeakHashMap<>();


        public MyAdapter(Context context, List<TTNativeExpressAd> data) {
            this.mContext = context;
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size(); // for test
        }

        @Override

        public TTNativeExpressAd getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //信息流广告的样式，有大图、小图、组图和视频，通过ad.getImageMode()来判断
        @Override
        public int getItemViewType(int position) {

            TTNativeExpressAd ad = getItem(position);
            if (ad == null) {
                return ITEM_VIEW_TYPE_NORMAL;

            } else if (TTAdSdk.isOnePointFiveAdType(ad)) {
                return ITEM_VIEW_TYPE_ONEPOINTFIVE;

            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_SMALL_IMG) {
                return ITEM_VIEW_TYPE_SMALL_PIC_AD;

            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_LARGE_IMG) {
                return ITEM_VIEW_TYPE_LARGE_PIC_AD;

            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_GROUP_IMG) {
                return ITEM_VIEW_TYPE_GROUP_PIC_AD;

            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO) {
                return ITEM_VIEW_TYPE_VIDEO;

            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VERTICAL_IMG) {
                return ITEM_VIEW_TYPE_VERTICAL_IMG;

            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO_VERTICAL || ad.getImageMode() == TTAdConstant.IMAGE_MODE_LIVE) {
                return ITEM_VIEW_TYPE_VIDEO_VERTICAL;
            } else {
                TToast.show(mContext, "图片展示样式错误");
                return ITEM_VIEW_TYPE_NORMAL;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 8;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TTNativeExpressAd ad = getItem(position);
            switch (getItemViewType(position)) {
                case ITEM_VIEW_TYPE_SMALL_PIC_AD:
                case ITEM_VIEW_TYPE_LARGE_PIC_AD:
                case ITEM_VIEW_TYPE_GROUP_PIC_AD:
                case ITEM_VIEW_TYPE_VERTICAL_IMG:
                case ITEM_VIEW_TYPE_VIDEO:
                case ITEM_VIEW_TYPE_VIDEO_VERTICAL:
                case ITEM_VIEW_TYPE_ONEPOINTFIVE:
                    return getVideoView(convertView, parent, ad);
                default:
                    return getNormalView(convertView, parent, position);
            }
        }

        //渲染视频广告，以视频广告为例，以下说明
        @SuppressWarnings("RedundantCast")

        private View getVideoView(View convertView, ViewGroup parent, @NonNull final TTNativeExpressAd ad) {
            final AdViewHolder adViewHolder;
            try {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_native_express, parent, false);
                    adViewHolder = new AdViewHolder();
                    adViewHolder.videoView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_express);
                    convertView.setTag(adViewHolder);
                } else {
                    adViewHolder = (AdViewHolder) convertView.getTag();
                }

                //绑定广告数据、设置交互回调
                if (adViewHolder.videoView != null) {
                    //获取视频播放view,该view SDK内部渲染，在媒体平台可配置视频是否自动播放等设置。

                    View video = ad.getExpressAdView();
                    if (video != null) {
                        adViewHolder.videoView.removeAllViews();
                        if (video.getParent() == null) {
                            adViewHolder.videoView.addView(video);
//                            ad.render();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        /**
         * 非广告list
         *
         * @param convertView
         * @param parent
         * @param position
         * @return
         */
        @SuppressWarnings("RedundantCast")
        @SuppressLint("SetTextI18n")
        private View getNormalView(View convertView, ViewGroup parent, int position) {
            NormalViewHolder normalViewHolder;
            if (convertView == null) {
                normalViewHolder = new NormalViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_normal, parent, false);
                normalViewHolder.idle = (TextView) convertView.findViewById(R.id.text_idle);
                convertView.setTag(normalViewHolder);
            } else {
                normalViewHolder = (NormalViewHolder) convertView.getTag();
            }
            normalViewHolder.idle.setText("ListView item " + position);
            return convertView;
        }

        private static class AdViewHolder {
            FrameLayout videoView;
        }

        private static class NormalViewHolder {
            TextView idle;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mData != null) {

            for (TTNativeExpressAd ad : mData) {
                if (ad != null) {
                    ad.destroy();
                }
            }
        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
