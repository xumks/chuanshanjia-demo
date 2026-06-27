package com.union_test.toutiao.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.union_test.toutiao.R;
import com.union_test.toutiao.config.TTAdManagerHolder;
import com.union_test.toutiao.onepointfive.TTFeedOnePointFiveView;
import com.union_test.toutiao.utils.TToast;
import com.union_test.toutiao.utils.UIUtils;
import com.union_test.toutiao.view.LoadMoreRecyclerView;
import com.union_test.toutiao.view.LoadMoreView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Feed 1.5卡广告使用示例
 */
@SuppressWarnings("unused")
public class FeedOnePointFiveActivity extends Activity implements TTFeedOnePointFiveView.TTOnePointFiveRefreshListener {
    public static final String TAG = "1.5Ad";

    private LoadMoreRecyclerView mListView;
    private MyAdapter myAdapter;

    private List<FeedAdBean> mData;


    private TTAdNative mTTAdNative;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private AdSlot mAdSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feed_recycler);

        TTAdManager ttAdManager = TTAdManagerHolder.get();

        mTTAdNative = ttAdManager.createAdNative(getApplicationContext());
        //申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        initListView();
        initRadioGroup();
        Button btn = (Button)findViewById(R.id.btn_fr_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @SuppressWarnings("RedundantCast")
    private void initRadioGroup() {
        RadioGroup radioGroupManager = (RadioGroup) findViewById(R.id.rg_fra_group);
        radioGroupManager.setVisibility(View.GONE);
        findViewById(R.id.rg_fra_group_orientation).setVisibility(View.GONE);
    }

    @SuppressWarnings("RedundantCast")
    private void initListView() {
        mListView = (LoadMoreRecyclerView) findViewById(R.id.my_list);
        mListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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

    /**
     * 加载feed广告
     */
    private void loadListAd() {
        //feed广告请求类型参数

        mAdSlot = new AdSlot.Builder()
                .setCodeId("949099102")
                .setImageAcceptedSize(640, 320)
                .setAdCount(3)
                .build();
        //调用feed广告异步请求接口

        myAdapter.setAdSlot(mAdSlot);

        mTTAdNative.loadFeedAd(mAdSlot, new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int code, String message) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }
                TToast.show(FeedOnePointFiveActivity.this, message);
            }

            @Override

            public void onFeedAdLoad(List<TTFeedAd> ads) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }
                if (ads == null || ads.isEmpty()) {
                    TToast.show(FeedOnePointFiveActivity.this, "on FeedAdLoaded: ad is null!");
                    return;
                }
                processFeedAdList(ads);
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    private void processFeedAdList(List<TTFeedAd> ads) {

        Map<String, List<TTFeedAd>> onePointFiveMetaMap= new LinkedHashMap<>(); //1.5卡集合

        List<FeedAdBean> ordinaryList = new ArrayList<>();

        for (TTFeedAd feedAd : ads) {
            try {
                JSONObject jsonObject = new JSONObject(feedAd.getMediaExtraInfo());
                JSONObject groupInfoJson  = jsonObject.optJSONObject("group_info");
                String groupId = groupInfoJson != null ? groupInfoJson.optString("group_id") : null;
                if (!TextUtils.isEmpty(groupId)) {

                    List<TTFeedAd> list = onePointFiveMetaMap.get(groupId);
                    if (list == null) {
                        list = new ArrayList<>();
                        onePointFiveMetaMap.put(groupId, list);
                    }
                    list.add(feedAd);

                } else {
                   ordinaryList.add(new FeedAdBean(feedAd));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mData.clear();
        if (onePointFiveMetaMap.size() > 0) {

            for (Map.Entry<String, List<TTFeedAd>> entry : onePointFiveMetaMap.entrySet()) {
                if (entry.getValue().size() == 1) {
                    ordinaryList.add(new FeedAdBean(entry.getValue().get(0)));
                } else {

                    FeedAdBean adBean = new FeedAdBean(entry.getValue());
                    mData.add(adBean);
                }
            }
        }
        onePointFiveMetaMap.clear();
        if (ordinaryList.size() > 0) {
            mData.addAll(ordinaryList);
        }
        ordinaryList.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TToast.reset();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRequestDataRefresh() {

    }

    @Override
    public void onAdDataRefreshed() {

    }

    private static class MyAdapter extends RecyclerView.Adapter {
        private static final int FOOTER_VIEW_COUNT = 1;

        private static final int ITEM_VIEW_TYPE_LOAD_MORE = -1;
        private static final int ITEM_VIEW_TYPE_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_GROUP_PIC_AD = 1;
        private static final int ITEM_VIEW_TYPE_SMALL_PIC_AD = 2;
        private static final int ITEM_VIEW_TYPE_LARGE_PIC_AD = 3;
        private static final int ITEM_VIEW_TYPE_VIDEO = 4;
        private static final int ITEM_VIEW_TYPE_VERTICAL_PIC_AD = 5;//竖版图片
        private static final int ITEM_VIEW_TYPE_ONEPOINTFIVE = 6;


        private List<FeedAdBean> mData;
        private Context mContext;
        private RecyclerView mRecyclerView;
        private RequestManager mRequestManager;

        private Map<AdViewHolder, TTAppDownloadListener> mTTAppDownloadListenerMap = new WeakHashMap<>();

        private AdSlot mAdSlot;


        public MyAdapter(Context context, List<FeedAdBean> data) {
            this.mContext = context;
            this.mData = data;
            mRequestManager = Glide.with(mContext);
        }


        public void setAdSlot(AdSlot adSlot) {
            mAdSlot = adSlot;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = null;
            switch (viewType) {
                case ITEM_VIEW_TYPE_LOAD_MORE:
                    return new LoadMoreViewHolder(new LoadMoreView(mContext));
                case ITEM_VIEW_TYPE_SMALL_PIC_AD:
                    return new SmallAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_small_pic, parent, false));
                case ITEM_VIEW_TYPE_LARGE_PIC_AD:
                    return new LargeAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_large_pic, parent, false));
                case ITEM_VIEW_TYPE_VERTICAL_PIC_AD:
                    return new VerticalAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_vertical_pic, parent, false));
                case ITEM_VIEW_TYPE_GROUP_PIC_AD:
                    return new GroupAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_group_pic, parent, false));
                case ITEM_VIEW_TYPE_VIDEO:
                    return new VideoAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_large_video, parent, false));
                case ITEM_VIEW_TYPE_ONEPOINTFIVE:
                    return new OnePointFiveViewHolder(new TTFeedOnePointFiveView(mContext, mAdSlot));
                default:
                    return new NormalViewHolder(LayoutInflater.from(mContext).inflate(R.layout.listitem_normal, parent, false));
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int count = mData.size();

            final TTFeedAd ttFeedAd;
            if (holder instanceof OnePointFiveViewHolder) {
                FeedAdBean adBean = mData.get(position);
                OnePointFiveViewHolder adViewHolder = (OnePointFiveViewHolder) holder;
//                ArrayList<View> images = new ArrayList<>();
//                images.add(((AdViewHolder) holder).itemView);
//                bindData(adViewHolder, images, ttFeedAd);
                if (adBean.isOnePointFive()) {
                    adViewHolder.mOnePointFiveView.setData(adBean.getOnePointFiveList());
                }
                adViewHolder.mOnePointFiveView.setRefreshListener((FeedOnePointFiveActivity)mContext);

                adViewHolder.mOnePointFiveView.setAdInteractionListener(new TTNativeAd.AdInteractionListener() {
                    @Override

                    public void onAdClicked(View view, TTNativeAd ad) {
                        Log.d(TAG, "1.5卡 onAdClicked: " + ad.getImageMode());
                    }

                    @Override

                    public void onAdCreativeClick(View view, TTNativeAd ad) {
                        Log.d(TAG, "1.5卡 onAdCreativeClick: " + ad.getImageMode());
                    }

                    @Override

                    public void onAdShow(TTNativeAd ad) {
                        Log.d(TAG, "1.5卡 onAdShow: " + ad.getImageMode());
                    }
                });

                adViewHolder.mOnePointFiveView.setVideoAdListener(new TTFeedAd.VideoAdListener() {
                    @Override

                    public void onVideoLoad(TTFeedAd ad) {
                        Log.d(TAG, "1.5卡 onVideoLoad: " + ad.getImageMode());
                    }

                    @Override

                    public void onVideoError(int errorCode, int extraCode) {
                        Log.d(TAG, "1.5卡 onVideoError: " + errorCode);
                    }

                    @Override

                    public void onVideoAdStartPlay(TTFeedAd ad) {
                        Log.d(TAG, "1.5卡 onVideoAdStartPlay: " + ad.getImageMode());
                    }

                    @Override

                    public void onVideoAdPaused(TTFeedAd ad) {
                        Log.d(TAG, "1.5卡 onVideoAdPaused: " + ad.getImageMode());
                    }

                    @Override

                    public void onVideoAdContinuePlay(TTFeedAd ad) {
                        Log.d(TAG, "1.5卡 onVideoAdContinuePlay: " + ad.getImageMode());
                    }

                    @Override

                    public void onProgressUpdate(long current, long duration) {
                        Log.d(TAG, "1.5卡 onProgressUpdate: " + current);
                    }

                    @Override

                    public void onVideoAdComplete(TTFeedAd ad) {
                        Log.d(TAG, "1.5卡 onVideoAdComplete: " + ad.getImageMode());
                    }
                });

            } else if (holder instanceof SmallAdViewHolder) {
                ttFeedAd = mData.get(position).getFeedAd();
                SmallAdViewHolder smallAdViewHolder = (SmallAdViewHolder) holder;
                ArrayList<View> images = new ArrayList<>();
                images.add(((SmallAdViewHolder) holder).mSmallImage);
                bindData(smallAdViewHolder, images, ttFeedAd);
                if (ttFeedAd.getImageList() != null && !ttFeedAd.getImageList().isEmpty()) {

                    TTImage image = ttFeedAd.getImageList().get(0);
                    if (image != null && image.isValid()) {
                        mRequestManager.load(image.getImageUrl()).into(smallAdViewHolder.mSmallImage);
                    }
                }

            } else if (holder instanceof LargeAdViewHolder) {
                ttFeedAd = mData.get(position).getFeedAd();
                LargeAdViewHolder largeAdViewHolder = (LargeAdViewHolder) holder;
                ArrayList<View> images = new ArrayList<>();
                images.add(((LargeAdViewHolder) holder).mLargeImage);
                bindData(largeAdViewHolder, images, ttFeedAd);
                if (ttFeedAd.getImageList() != null && !ttFeedAd.getImageList().isEmpty()) {
                    TTImage image = ttFeedAd.getImageList().get(0);
                    if (image != null && image.isValid()) {
                        mRequestManager.load(image.getImageUrl()).into(largeAdViewHolder.mLargeImage);
                    }
                }

            } else if (holder instanceof VerticalAdViewHolder) {
                ttFeedAd = mData.get(position).getFeedAd();
                VerticalAdViewHolder verticalAdViewHolder = (VerticalAdViewHolder) holder;
                ArrayList<View> images = new ArrayList<>();
                images.add((((VerticalAdViewHolder) holder).mVerticalImage));
                bindData(verticalAdViewHolder, images, ttFeedAd);
                if (ttFeedAd.getImageList() != null && !ttFeedAd.getImageList().isEmpty()) {
                    TTImage image = ttFeedAd.getImageList().get(0);
                    if (image != null && image.isValid()) {
                        mRequestManager.load(image.getImageUrl()).into(verticalAdViewHolder.mVerticalImage);
                    }
                }
            } else if (holder instanceof GroupAdViewHolder) {
                ttFeedAd = mData.get(position).getFeedAd();
                GroupAdViewHolder groupAdViewHolder = (GroupAdViewHolder) holder;
                ArrayList<View> images = new ArrayList<>();
                images.add(((GroupAdViewHolder) holder).mGroupImage1);
                images.add(((GroupAdViewHolder) holder).mGroupImage2);
                images.add(((GroupAdViewHolder) holder).mGroupImage3);
                bindData(groupAdViewHolder, images, ttFeedAd);
                if (ttFeedAd.getImageList() != null && ttFeedAd.getImageList().size() >= 3) {
                    TTImage image1 = ttFeedAd.getImageList().get(0);
                    TTImage image2 = ttFeedAd.getImageList().get(1);
                    TTImage image3 = ttFeedAd.getImageList().get(2);
                    if (image1 != null && image1.isValid()) {
                        mRequestManager.load(image1.getImageUrl()).into(groupAdViewHolder.mGroupImage1);
                    }
                    if (image2 != null && image2.isValid()) {
                        mRequestManager.load(image2.getImageUrl()).into(groupAdViewHolder.mGroupImage2);
                    }
                    if (image3 != null && image3.isValid()) {
                        mRequestManager.load(image3.getImageUrl()).into(groupAdViewHolder.mGroupImage3);
                    }
                }

            } else if (holder instanceof VideoAdViewHolder) {
                ttFeedAd = mData.get(position).getFeedAd();
                final VideoAdViewHolder videoAdViewHolder = (VideoAdViewHolder) holder;
                ArrayList<View> images = new ArrayList<>();
                images.add(((VideoAdViewHolder) holder).videoView);
                bindData(videoAdViewHolder, images, ttFeedAd);

                ttFeedAd.setVideoAdListener(new TTFeedAd.VideoAdListener() {
                    @Override

                    public void onVideoLoad(TTFeedAd ad) {

                    }

                    @Override
                    public void onVideoError(int errorCode, int extraCode) {

                    }

                    @Override

                    public void onVideoAdStartPlay(TTFeedAd ad) {

                    }

                    @Override

                    public void onVideoAdPaused(TTFeedAd ad) {

                    }

                    @Override

                    public void onVideoAdContinuePlay(TTFeedAd ad) {

                    }

                    @Override
                    public void onProgressUpdate(long current, long duration) {

                    }

                    @Override

                    public void onVideoAdComplete(TTFeedAd ad) {

                    }
                });
                if (videoAdViewHolder.videoView != null) {

                    View video = ttFeedAd.getAdView();
                    videoAdViewHolder.videoView.post(new Runnable() {
                        @Override
                        public void run() {
                            int width = videoAdViewHolder.videoView.getWidth();

                            int videoWidth = ttFeedAd.getAdViewWidth();
                            int videoHeight = ttFeedAd.getAdViewHeight();

                            // 根据广告View的宽高比，将adViewHolder.videoView的高度动态改变
                            UIUtils.setViewSize(videoAdViewHolder.videoView, width, (int) (width / (videoWidth / (double) videoHeight)));
                        }
                    });
                    if (video != null) {
                        if (video.getParent() == null) {
                            videoAdViewHolder.videoView.removeAllViews();
                            videoAdViewHolder.videoView.addView(video);
                        }
                    }
                }

            } else if (holder instanceof NormalViewHolder) {
                NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
                normalViewHolder.idle.setText("Recycler item " + position);
            } else if (holder instanceof LoadMoreViewHolder) {
                LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) holder;
            }

            holder.itemView.setBackgroundColor(Color.WHITE);
        }


        private void bindData(final AdViewHolder adViewHolder, List<View> images, final TTFeedAd ad) {
            //设置dislike弹窗
            //可以被点击的view, 也可以把convertView放进来意味item可被点击
            List<View> clickViewList = new ArrayList<>();
            clickViewList.add(adViewHolder.itemView);
            //触发创意广告的view（点击下载或拨打电话）
            List<View> creativeViewList = new ArrayList<>();
            creativeViewList.add(adViewHolder.mCreativeButton);
            //如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
//            creativeViewList.add(convertView);
            //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。

            ad.registerViewForInteraction((ViewGroup) adViewHolder.itemView, images, clickViewList, creativeViewList, adViewHolder.mDislike, new TTNativeAd.AdInteractionListener() {
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
            adViewHolder.mTitle.setText(ad.getTitle());
            adViewHolder.mDescription.setText(ad.getDescription());
            adViewHolder.mSource.setText(ad.getSource() == null ? "广告来源" : ad.getSource());
            TTImage icon = ad.getIcon();
            if (icon != null && icon.isValid()) {
                mRequestManager.load(icon.getImageUrl()).into(adViewHolder.mIcon);
            }
            Button adCreativeButton = adViewHolder.mCreativeButton;
            switch (ad.getInteractionType()) {

                case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                    //如果初始化ttAdManager.createAdNative(getApplicationContext())没有传入activity 则需要在此传activity，否则影响使用Dislike逻辑
                    if (mContext instanceof Activity) {
                        ad.setActivityForDownloadApp((Activity) mContext);
                    }
                    adCreativeButton.setVisibility(View.VISIBLE);
                    adViewHolder.mStopButton.setVisibility(View.VISIBLE);
                    adViewHolder.mRemoveButton.setVisibility(View.VISIBLE);

                    break;

                case TTAdConstant.INTERACTION_TYPE_DIAL:
                    adCreativeButton.setVisibility(View.VISIBLE);
                    adCreativeButton.setText("立即拨打");
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                    adViewHolder.mRemoveButton.setVisibility(View.GONE);
                    break;

                case TTAdConstant.INTERACTION_TYPE_LANDING_PAGE:

                case TTAdConstant.INTERACTION_TYPE_BROWSER:
//                    adCreativeButton.setVisibility(View.GONE);
                    adCreativeButton.setVisibility(View.VISIBLE);
                    adCreativeButton.setText("查看详情");
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                    adViewHolder.mRemoveButton.setVisibility(View.GONE);
                    break;
                default:
                    adCreativeButton.setVisibility(View.GONE);
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                    adViewHolder.mRemoveButton.setVisibility(View.GONE);
                    TToast.show(mContext, "交互类型异常");
            }
        }

        @Override
        public int getItemCount() {
            int count = mData == null ? 0 : mData.size();
            return count + FOOTER_VIEW_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (mData != null) {
                int count = mData.size();
                if (position >= count) {
                    return ITEM_VIEW_TYPE_LOAD_MORE;
                } else {

                    FeedAdBean adbean = mData.get(position);
                    if (adbean == null) {
                        return ITEM_VIEW_TYPE_NORMAL;

                    } else if (adbean.isOnePointFive()) {
                        return ITEM_VIEW_TYPE_ONEPOINTFIVE;
                    }

                    TTFeedAd ad = adbean.getFeedAd();

                    if (ad == null) {
                        return ITEM_VIEW_TYPE_NORMAL;

                    } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_SMALL_IMG) {
                        return ITEM_VIEW_TYPE_SMALL_PIC_AD;

                    } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_LARGE_IMG) {
                        return ITEM_VIEW_TYPE_LARGE_PIC_AD;

                    } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_GROUP_IMG) {
                        return ITEM_VIEW_TYPE_GROUP_PIC_AD;

                    } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO || ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO_VERTICAL || ad.getImageMode() == TTAdConstant.IMAGE_MODE_LIVE) {
                        return ITEM_VIEW_TYPE_VIDEO;

                    } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VERTICAL_IMG) {//竖版图片
                        return ITEM_VIEW_TYPE_VERTICAL_PIC_AD;
                    } else {
                        TToast.show(mContext, "图片展示样式错误");
                        return ITEM_VIEW_TYPE_NORMAL;
                    }
                }

            }
            return super.getItemViewType(position);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);

            RecyclerView.LayoutManager layout = recyclerView.getLayoutManager();
            if (layout != null && layout instanceof GridLayoutManager) {
                final GridLayoutManager manager = (GridLayoutManager) layout;
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int type = getItemViewType(position);
                        if (type == ITEM_VIEW_TYPE_LOAD_MORE || type == ITEM_VIEW_TYPE_VIDEO) {
                            return manager.getSpanCount();
                        }
                        return 1;
                    }
                });
            }
        }

        @Override
        public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
            //noinspection unchecked
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                int position = holder.getLayoutPosition();
                int type = getItemViewType(position);
                if (type == ITEM_VIEW_TYPE_LOAD_MORE || type == ITEM_VIEW_TYPE_VIDEO) {
                    StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                    p.setFullSpan(true);
                }
            }
        }

        @SuppressWarnings("WeakerAccess")
        private static class VideoAdViewHolder extends AdViewHolder {
            @SuppressWarnings("CanBeFinal")
            FrameLayout videoView;

            @SuppressWarnings("RedundantCast")
            public VideoAdViewHolder(View itemView) {
                super(itemView);

                mDislike = (ImageView) itemView.findViewById(R.id.iv_listitem_dislike);
                mTitle = (TextView) itemView.findViewById(R.id.tv_listitem_ad_title);
                mDescription = (TextView) itemView.findViewById(R.id.tv_listitem_ad_desc);
                mSource = (TextView) itemView.findViewById(R.id.tv_listitem_ad_source);
                videoView = (FrameLayout) itemView.findViewById(R.id.iv_listitem_video);
                mIcon = (ImageView) itemView.findViewById(R.id.iv_listitem_icon);
                mCreativeButton = (Button) itemView.findViewById(R.id.btn_listitem_creative);
                mStopButton = (Button) itemView.findViewById(R.id.btn_listitem_stop);
                mRemoveButton = (Button) itemView.findViewById(R.id.btn_listitem_remove);

            }
        }

        private static class LargeAdViewHolder extends AdViewHolder {
            ImageView mLargeImage;

            @SuppressWarnings("RedundantCast")
            public LargeAdViewHolder(View itemView) {
                super(itemView);

                mDislike = (ImageView) itemView.findViewById(R.id.iv_listitem_dislike);
                mTitle = (TextView) itemView.findViewById(R.id.tv_listitem_ad_title);
                mDescription = (TextView) itemView.findViewById(R.id.tv_listitem_ad_desc);
                mSource = (TextView) itemView.findViewById(R.id.tv_listitem_ad_source);
                mLargeImage = (ImageView) itemView.findViewById(R.id.iv_listitem_image);
                mIcon = (ImageView) itemView.findViewById(R.id.iv_listitem_icon);
                mCreativeButton = (Button) itemView.findViewById(R.id.btn_listitem_creative);
                mStopButton = (Button) itemView.findViewById(R.id.btn_listitem_stop);
                mRemoveButton = (Button) itemView.findViewById(R.id.btn_listitem_remove);
            }
        }

        private static class SmallAdViewHolder extends AdViewHolder {
            ImageView mSmallImage;

            @SuppressWarnings("RedundantCast")
            public SmallAdViewHolder(View itemView) {
                super(itemView);

                mDislike = (ImageView) itemView.findViewById(R.id.iv_listitem_dislike);
                mTitle = (TextView) itemView.findViewById(R.id.tv_listitem_ad_title);
                mSource = (TextView) itemView.findViewById(R.id.tv_listitem_ad_source);
                mDescription = (TextView) itemView.findViewById(R.id.tv_listitem_ad_desc);
                mSmallImage = (ImageView) itemView.findViewById(R.id.iv_listitem_image);
                mIcon = (ImageView) itemView.findViewById(R.id.iv_listitem_icon);
                mCreativeButton = (Button) itemView.findViewById(R.id.btn_listitem_creative);
                mStopButton = (Button) itemView.findViewById(R.id.btn_listitem_stop);
                mRemoveButton = (Button) itemView.findViewById(R.id.btn_listitem_remove);
            }
        }

        private static class VerticalAdViewHolder extends AdViewHolder {
            ImageView mVerticalImage;

            @SuppressWarnings("RedundantCast")
            public VerticalAdViewHolder(View itemView) {
                super(itemView);

                mDislike = (ImageView) itemView.findViewById(R.id.iv_listitem_dislike);
                mTitle = (TextView) itemView.findViewById(R.id.tv_listitem_ad_title);
                mSource = (TextView) itemView.findViewById(R.id.tv_listitem_ad_source);
                mDescription = (TextView) itemView.findViewById(R.id.tv_listitem_ad_desc);
                mVerticalImage = (ImageView) itemView.findViewById(R.id.iv_listitem_image);
                mIcon = (ImageView) itemView.findViewById(R.id.iv_listitem_icon);
                mCreativeButton = (Button) itemView.findViewById(R.id.btn_listitem_creative);
                mStopButton = (Button) itemView.findViewById(R.id.btn_listitem_stop);
                mRemoveButton = (Button) itemView.findViewById(R.id.btn_listitem_remove);
            }
        }


        @SuppressWarnings("CanBeFinal")
        private static class GroupAdViewHolder extends AdViewHolder {
            ImageView mGroupImage1;
            ImageView mGroupImage2;
            ImageView mGroupImage3;

            @SuppressWarnings("RedundantCast")
            public GroupAdViewHolder(View itemView) {
                super(itemView);

                mDislike = (ImageView) itemView.findViewById(R.id.iv_listitem_dislike);
                mTitle = (TextView) itemView.findViewById(R.id.tv_listitem_ad_title);
                mSource = (TextView) itemView.findViewById(R.id.tv_listitem_ad_source);
                mDescription = (TextView) itemView.findViewById(R.id.tv_listitem_ad_desc);
                mGroupImage1 = (ImageView) itemView.findViewById(R.id.iv_listitem_image1);
                mGroupImage2 = (ImageView) itemView.findViewById(R.id.iv_listitem_image2);
                mGroupImage3 = (ImageView) itemView.findViewById(R.id.iv_listitem_image3);
                mIcon = (ImageView) itemView.findViewById(R.id.iv_listitem_icon);
                mCreativeButton = (Button) itemView.findViewById(R.id.btn_listitem_creative);
                mStopButton = (Button) itemView.findViewById(R.id.btn_listitem_stop);
                mRemoveButton = (Button) itemView.findViewById(R.id.btn_listitem_remove);
            }
        }

        private static class AdViewHolder extends RecyclerView.ViewHolder {
            ImageView mIcon;
            ImageView mDislike;
            Button mCreativeButton;
            TextView mTitle;
            TextView mDescription;
            TextView mSource;
            Button mStopButton;
            Button mRemoveButton;

            public AdViewHolder(View itemView) {
                super(itemView);
            }
        }

        private static class NormalViewHolder extends RecyclerView.ViewHolder {
            TextView idle;

            @SuppressWarnings("RedundantCast")
            public NormalViewHolder(View itemView) {
                super(itemView);

                idle = (TextView) itemView.findViewById(R.id.text_idle);

            }
        }

        @SuppressWarnings({"CanBeFinal", "WeakerAccess"})
        private static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;
            ProgressBar mProgressBar;

            @SuppressWarnings("RedundantCast")
            public LoadMoreViewHolder(View itemView) {
                super(itemView);

                itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

                mTextView = (TextView) itemView.findViewById(R.id.tv_load_more_tip);
                mProgressBar = (ProgressBar) itemView.findViewById(R.id.pb_load_more_progress);
            }
        }

        @SuppressWarnings({"CanBeFinal", "WeakerAccess"})
        private static class OnePointFiveViewHolder extends RecyclerView.ViewHolder {
            TTFeedOnePointFiveView mOnePointFiveView;

            @SuppressWarnings("RedundantCast")
            public OnePointFiveViewHolder(TTFeedOnePointFiveView itemView) {
                super(itemView);
                mOnePointFiveView = itemView;
                itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    public static class FeedAdBean {

        private List<TTFeedAd> mFeedAdList;


        private TTFeedAd mNormalFeedAd;


        public FeedAdBean(List<TTFeedAd> feedAdList) {
            mFeedAdList = feedAdList;
        }


        public FeedAdBean(TTFeedAd feedAd) {
            mNormalFeedAd = feedAd;
        }


        public TTFeedAd getFeedAd() {
            return mNormalFeedAd;
        }


        public List<TTFeedAd> getOnePointFiveList() {
            return mFeedAdList;
        }

        public boolean isOnePointFive() {
            return mFeedAdList != null && mFeedAdList.size() > 0;
        }
    }

}
