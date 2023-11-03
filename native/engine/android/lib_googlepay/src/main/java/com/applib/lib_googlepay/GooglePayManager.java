package com.applib.lib_googlepay;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.applib.lib_common.ApiCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

public class GooglePayManager {

    private static final String TAG = "Google Pay";

    private static  GooglePayManager _instance = null;
    protected static Activity mActivity = null;
    protected static ApiCallback payCallback = null;

    private BillingClient mBillingClient = null;
    private String mProductId = "";
    private String mOrderId = "";
    private String mProductType = BillingClient.ProductType.INAPP;

    private GooglePayManager() {

    }

    public static GooglePayManager getInstance(Activity activity){
        if (_instance == null) {
            _instance = new GooglePayManager();
        }
        mActivity = activity;
        return _instance;
    }

    public void init() {
        if (!checkPlayServices(mActivity))
            return;
        PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    // 购买商品成功
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // 取消购买
                    Log.i(TAG, "user canceled");
                    payCallback.onFail(Integer.toString(billingResult.getResponseCode()));
                } else {
                    // 购买失败，具体异常码可以到BillingClient.BillingResponseCode中查看
                    payCallback.onFail(Integer.toString(billingResult.getResponseCode()));
                    Log.i(TAG, "pay failed");
                }
            }
        };

        mBillingClient = BillingClient.newBuilder(mActivity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
    }

    // 与GooglePlay连接状态监听
    private BillingClientStateListener stateListener = new BillingClientStateListener() {
        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //连接成功，可以进行查询商品等操作
                queryProduct(mProductId, mOrderId, mProductType);
                //queryPurchases();
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            //连接已经断开，重新连接
            Log.e(TAG, "与Google Play建立连接失败, 1秒后重新连接");
            handler.postDelayed(retryConnectRunnable, 1000);
        }
    };

    private Handler handler = new Handler(Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper());
    private Runnable retryConnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (BillingClient.ConnectionState.DISCONNECTED == mBillingClient.getConnectionState()) {
                mBillingClient.startConnection(stateListener);
            }
        }
    };

    // 启动购买流程
    public void pay(String productId, String orderId, String productType, ApiCallback callback) {
        if (mBillingClient != null) {
            mProductId = productId;
            mOrderId = orderId;
            mProductType = productType;
            payCallback = callback;
            mBillingClient.startConnection(stateListener);
        }
    }

    /**
     * 查询指定商品
     * @param productId 产品ID(从谷歌后台获取)
     * @param orderId 内部订单号
     * @param productType BillingClient.ProductType.INAPP BillingClient.ProductType.SUBS
     */
    private void queryProduct(String productId, String orderId, String productType) {
        //查询内购类型的商品
        //设置查询参数方式有所更改，productId为产品ID(从谷歌后台获取)
        ArrayList<QueryProductDetailsParams.Product> inAppProductInfo = new ArrayList<>();
        inAppProductInfo.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build());
        QueryProductDetailsParams productDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(inAppProductInfo)
                .build();
        mBillingClient.queryProductDetailsAsync(productDetailsParams, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {
                ArrayList<BillingFlowParams.ProductDetailsParams> params = new ArrayList<>();
                //将要购买商品的商品详情配置到参数中，两种类型的商品有所区别
                for (ProductDetails details : productDetailsList) {
                    if (BillingClient.ProductType.SUBS.equals(details.getProductType()) && details.getSubscriptionOfferDetails() != null) {
                        params.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .setOfferToken(details.getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build());
                    } else {
                        params.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .build());
                    }
                }

                if (params.size() > 0) {
                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(params)
                            .setObfuscatedAccountId(orderId)
                            .build();

                    // Launch the billing flow
                    BillingResult launchResult = mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
                    if (launchResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "启动购买失败 code=" + launchResult.getResponseCode());
                        payCallback.onFail(Integer.toString(launchResult.getResponseCode()));
                    }
                } else {
                    Log.e("TAG", "商品不存在");
                    payCallback.onFail(Integer.toString(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE));
                }
            }
        });
    }

    private void queryPurchases() {
        PurchasesResponseListener purchasesResponseListener =new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                for (Purchase purchase : list) {
                    consumePurchase(purchase);
                    Log.i(TAG, purchase.toString());
                }
            }
        };

        //内购商品交易查询
        Log.i(TAG, "内购商品交易查询");
        QueryPurchasesParams inAppPurchasesQuery = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        mBillingClient.queryPurchasesAsync(inAppPurchasesQuery, purchasesResponseListener);

        //订阅商品交易查询
//        Log.i(TAG, "订阅商品交易查询");
//        QueryPurchasesParams subscriptionsPurchasesQuery = QueryPurchasesParams.newBuilder()
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build();
//        mBillingClient.queryPurchasesAsync(subscriptionsPurchasesQuery, purchasesResponseListener);
    }

    // 处理商品核销
    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // 由服务端进行核销
            purchaseByServerApi(purchase);

//            if (BillingClient.ProductType.INAPP.equals(mProductType)) {
//                consumePurchase(purchase);
//            } else {
//                acknowledgedPurchase(purchase);
//            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            /** Google Play 支持待处理的交易，即从用户发起购买交易到购买交易的付款方式得到处理期间需要执行一个或多个额外步骤的交易。在 Google 通知您已通过用户的付款方式成功扣款之前，您的应用不得授予对这些类型的购买交易的权利。
             例如，用户可以选择现金作为付款方式来创建应用内商品的 PENDING 购买交易。然后，用户可以选择在一家实体店完成交易，并通过通知和电子邮件收到一个代码。当用户到达实体店时，他们可以在收银员处兑换该代码并用现金支付。Google 随后会通知您和用户已收到现金。您的应用随后就可以授予用户权利了。
             在初始化应用的过程中，应用必须通过调用 enablePendingPurchases() 来支持待处理的交易。
             当应用通过 PurchasesUpdatedListener 或由于调用 queryPurchasesAsync() 而收到新的购买交易时，使用 getPurchaseState() 方法确定购买交易的状态是 PURCHASED 还是 PENDING。
             */
            Log.i(TAG, "待处理的交易. token=" + purchase.getPurchaseToken());
            payCallback.onFail("10000");
        }
    }

    // 可重复购买的内购商品核销
    private void consumePurchase(Purchase purchase) {
        if (mBillingClient != null && mBillingClient.isReady()) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            ConsumeResponseListener listener = new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // Handle the success of the consume operation.
                        // 核销完成后回调
                        payCallback.onSuccess(purchase.getOriginalJson());
                    } else {
                        payCallback.onFail(Integer.toString(billingResult.getResponseCode()));
                    }
                }
            };
            mBillingClient.consumeAsync(consumeParams, listener);
        }
    }

    // 非消耗型内购商品核销
    private void acknowledgedPurchase(Purchase purchase) {
        if (mBillingClient != null && mBillingClient.isReady()) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                //核销回调
                AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener=new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        //核销完成后回调
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            // Handle the success of the consume operation.
                            // 核销完成后回调
                            payCallback.onSuccess(purchase.getOriginalJson());
                        } else {
                            payCallback.onFail(Integer.toString(billingResult.getResponseCode()));
                        }
                    }
                };

                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    // 由服务端核销
    private void purchaseByServerApi(Purchase purchase) {
        payCallback.onSuccess(purchase.getOriginalJson());
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context) {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable
                (context);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i(TAG, "This device does not support Google Play Services. ");
            return false;
        }
        return true;
    }

}
