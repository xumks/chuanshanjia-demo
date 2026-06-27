package com.union_test.toutiao.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdInteractionListener;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.union_test.toutiao.R;
import com.union_test.toutiao.config.TTAdManagerHolder;
import com.union_test.toutiao.utils.TToast;

import java.util.List;
import java.util.Map;

/**
 * 商城页面使用Demo
 */
public class NativeEcMallActivity extends Activity {
    private static final String TAG = "NativeEcMallActivity";

    private TTAdNative mTTAdNative;
    private FrameLayout mContainer;
    private Context mContext;

    private TTFeedAd mTTAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ec_mall);
        mContext = this.getApplicationContext();
        mContainer = (FrameLayout) findViewById(R.id.fl_container);

        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        Button button = (Button)findViewById(R.id.btn_ane_back);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loadAd();
    }

    private void loadAd() {
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("901121737") //广告位id,这里需要申请指定对应的广告位置才能加载商城页面，
                .setAdCount(1) //请求广告数量为1到3条
                .supportIconStyle()
                .setExpressViewAcceptedSize(160,0) //期望模板广告view的size,单位dp
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理

        mTTAdNative.loadFeedAd(adSlot, new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int code, String message) {
                TToast.show(NativeEcMallActivity.this, "load error : " + code + ", " + message);
            }

            @Override

            public void onFeedAdLoad(List<TTFeedAd> ads) {
                if (ads == null || ads.size() == 0){
                    return;
                }
                mTTAd = ads.get(0);

                View adView = mTTAd.getAdView();
                if (adView != null) {
                    bindAdListener(mTTAd);
                    mContainer.addView(adView);
                }

            }

        });
    }


    private void bindAdListener(TTFeedAd ad) {

        ad.setAdInteractionListener(new TTAdInteractionListener() {
            @Override
            public void onAdEvent(int code, Map map) {

                if (code == TTAdConstant.AD_EVENT_AUTH_DOUYIN && map != null) {
                    // 抖音授权成功状态回调, 媒体可以通过map获取抖音openuid用以判断是否下发奖励
                    String uid = (String) map.get("open_uid");
                    Log.i(TAG, "授权成功 --> uid：" + uid);
                }
            }
        });

        // 这里的逻辑同自渲染， 但是 onAdClicked 和 onAdCreativeClick 不会回调，仅回调onAdShow
        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。

        mTTAd.registerViewForInteraction(mContainer, mContainer,new TTNativeAd.AdInteractionListener() {
            @Override

            public void onAdClicked(View view, TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "被点击");
                }
            }

            @Override

            public void onAdCreativeClick(View view, TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "被创意按钮被点击");
                }
            }

            @Override

            public void onAdShow(TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "展示");
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTTAd != null) {
            mTTAd.destroy();
        }
    }
}
