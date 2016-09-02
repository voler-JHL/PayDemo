package com.voler.paydemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.voler.paydemo.alipay.PayResult;
import com.voler.paydemo.wxapi.Constants;
import com.zhy.http.okhttp.builder.PostFormBuilder;

import java.net.URLEncoder;

import okhttp3.Call;

/**
 * 支付功能基类，集成本类，调用支付方法即可
 */
public abstract class OrderPayBaseFragment extends Fragment {

    private static final String TAG = "WXPay";
    /**
     * 微信字段
     */
    public static final String PARAMS = "params";
    public static final String SIGN = "sign";
    public static final String APPID = "appid";
    public static final String PARTNERID = "partnerid";
    public static final String PREPAYID = "prepayid";
    public static final String TIMESTAMP = "timestamp";
    public static final String NONCESTR = "noncestr";

    private static final int SDK_PAY_FLAG = 1;
    protected WXReceiver receiver = null;
    protected IWXAPI msgApi;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new WXReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.voler.wxpay_message");    //Intent添加的String必须一致
        getActivity().registerReceiver(receiver, filter);//注册广播
    }


    @Override
    public void onDestroy() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);//解除
        }
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        msgApi = WXAPIFactory.createWXAPI(getActivity(), Constants.APP_ID);
        msgApi.registerApp(Constants.APP_ID);
    }



    /**
     * 发起网络支付请求，此处是okhttp，用别的套用即可
     *
     * @param payPlatForm  支付方式
     * @param postFormBuilder
     */
    protected void toPay(final String payPlatForm, PostFormBuilder postFormBuilder) {

        postFormBuilder
                .build()
                .execute(new JsonCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        Toast.makeText(getActivity(), "支付异常，请重试", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    /**
                     * 成功的返回方法
                     * @param obj
                     * @param i
                     */
                    @Override
                    public void onResponse(JSONObject obj, int i) {
                        //obj是fastjson，payZFB（）等方法中都是按照fastjson解析，可以根据不同的解析方法替换
                        try {
                            if (payPlatForm.equals("alipay")) {//判断是否是支付宝支付
                                payZFB(obj);
                            } else {
                                payWX(obj);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "支付异常，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /**
     * 接收广播，处理微信支付的结果
     *
     * @author
     */
    protected class WXReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Intent添加的String必须一致
            if (intent.getAction().equals("com.voler.wxpay_message")) {

                int code = intent.getIntExtra("errorcode", -1);
                String message = "";
                if (code == 0) {
                    message = "支付成功";
                } else if (code == -1) {
                    message = "支付失败";
                } else if (code == -2) {
                    message = "取消支付";
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }

    }


    /**
     * 微信支付,关键方法！
     */
    protected void payWX(JSONObject jsonObject) {
        /**
         后台返回微信的签名字符串,例如：
        {"params":{"sign":"6F51E3EDC1FC2900189C550FF3E22F34","timestamp":"1370212270","partnerid":"1171163701","noncestr":"afdeb7005cc9f14302cd0474fd0f3c96","prepayid":"wx201508031617508f5b8f61900096138440","package":"Sign=WXPay","appid":"wx596816a493629095"}}
        上面仅是返回的json中的有效部分，外层可能各不相同
        下面就是对返回的签名json进行取值，赋值，传递给微信，json如果不相同，灵活替换     */
        try {
            JSONObject paramsObj = jsonObject.getJSONObject(PARAMS);
            String WX_APP_ID = paramsObj.getString(APPID);
            if (WX_APP_ID == null) return;
            PayReq req = new PayReq();
            req.appId = WX_APP_ID;
            req.partnerId = paramsObj.getString(PARTNERID);
            req.prepayId = paramsObj.getString(PREPAYID);
            req.packageValue = "Sign=WXPay";
            req.nonceStr = paramsObj.getString(NONCESTR);
            req.timeStamp = paramsObj.getString(TIMESTAMP);
            req.sign = paramsObj.getString(SIGN);
            msgApi.registerApp(WX_APP_ID);//注册到微信
            msgApi.sendReq(req);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "微信支付调起异常");
        }
    }


    /**
     * 支付宝支付,关键方法！
     *
     * 在清单文件中添加H5的activity，可以再不安装支付宝的时候进行网页支付
     *
     */
    public void payZFB(JSONObject jsonObject) {
       /**
        后台返回支付宝的签名字符串,例如：
        {"sign":"LKPmr5GcLIuzK2PzkMp9ouB0Cqm243/vbQq8VQrw+IrKkNl0QUO3NRE7T8yanOEjkCocXeo+RLrC41BPcRaWw+D4Cm+UteogNOyqyTs6aE8hbYKwnnH/UlH1SmKoLMkhzEvwqeeydoK638Ut8TpsRGhOK6gd6hPpj9IGl5kuZhk=","params":"partner=\"2088711874480844\"&seller_id=\"baidu@yeah.net\"&out_trade_no=\"20150803033544485718\"&subject=\"旅游\"&body=\"票\"&total_fee=\"0.01\"&notify_url=\"http://huidiaodizhi\"&service=\"mobile.securitypay.pay\"&payment_type=\"1\"&_input_charset=\"utf-8\"&it_b_pay=\"30m\""}
        上面仅是返回的json中的有效部分，外层可能各不相同
        下面就是对返回的签名json进行取值，赋值，传递给支付宝*/
        try {
            // 订单
            String orderInfo = jsonObject.getString("params");
            // 对订单做RSA 签名
            String sign = jsonObject.getString("sign");

            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");

            // 完整的符合支付宝参数规范的订单信息
            final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                    + getSignType();

            Runnable payRunnable = new Runnable() {

                @Override
                public void run() {
                    // 构造PayTask 对象
                    PayTask alipay = new PayTask(getActivity());
                    // 调用支付接口，获取支付结果
                    String result = alipay.pay(payInfo, true);

                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            };

            // 必须异步调用
            Thread payThread = new Thread(payRunnable);
            payThread.start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "支付宝调起错误");
        }
    }


    /**
     * get the sign type we use. 获取签名方式
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }


    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
//				String resultInfo = payResult.getResult();

                    final String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {

                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(getActivity(), "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(getActivity(), "支付失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

}
