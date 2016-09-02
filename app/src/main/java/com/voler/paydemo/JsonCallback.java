package com.voler.paydemo;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.zhy.http.okhttp.callback.Callback;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 三尺春光驱我寒，一生戎马为长安 -- 韩经录
 * Created by voler on 2016/8/2.
 *
 * 这个类是okhttp的回调类，在parseNetworkResponse方法中可以对返回结果进行初步处理，
 * 他的返回结果会传到onResponse中
 * Callback是抽象类，可以直接在okhttp使用时new，
 * 也可以继承后在parseNetworkResponse中对返回数据做初步处理，比如解析json的公共部分
 */
public class JsonCallback extends Callback<JSONObject> {

    @Override
    public JSONObject parseNetworkResponse(Response response, int i) throws Exception {
        Log.i("-------返回的json：",response.body().string());
        JSONObject jsonObject=JSONObject.parseObject(response.body().string());


        //后台返回支付宝的签名字符串,例如：
//        {"sign":"LKPmr5GcLIuzK2PzkMp9ouB0Cqm243/vbQq8VQrw+IrKkNl0QUO3NRE7T8yanOEjkCocXeo+RLrC41BPcRaWw+D4Cm+UteogNOyqyTs6aE8hbYKwnnH/UlH1SmKoLMkhzEvwqeeydoK638Ut8TpsRGhOK6gd6hPpj9IGl5kuZhk=","params":"partner=\"2088711874480844\"&seller_id=\"baidu@yeah.net\"&out_trade_no=\"20150803033544485718\"&subject=\"旅游\"&body=\"票\"&total_fee=\"0.01\"&notify_url=\"http://huidiaodizhi\"&service=\"mobile.securitypay.pay\"&payment_type=\"1\"&_input_charset=\"utf-8\"&it_b_pay=\"30m\""}

        //后台返回微信的签名字符串,例如：
//        {"params":{"sign":"6F51E3EDC1FC2900189C550FF3E22F34","timestamp":"1370212270","partnerid":"1171163701","noncestr":"afdeb7005cc9f14302cd0474fd0f3c96","prepayid":"wx201508031617508f5b8f61900096138440","package":"Sign=WXPay","appid":"wx596816a493629095"}}

        //把这种字符串转为json形式的，返回
        return jsonObject;
    }

    @Override
    public void onError(Call call, Exception e, int i) {

    }

    @Override
    public void onResponse(JSONObject obj, int i) {

    }
}
