package com.voler.paydemo.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.voler.paydemo.R;


/**
 * 微信回调Activity，必须在wxapi包下
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	
	private static final String TAG = "WXPayEntryActivity";
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        
    	api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		/**
		 * 微信支付成功回调会开启一个activity，并执行onResp方法，我不希望出现这个界面，所以finish了，在这之前，我发送一个广播
		 * 在广播中我做了回调后的操作
		 *
		 * 如果，你的界面是activity，可以改造这个类为你的回调界面（我的是fragment，所以不用他的回调activity）
		 */
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			//发送广播，为intent添加的String必须一致，接收广播处
			sendBroadcast(new Intent("com.voler.wxpay_message").putExtra("errorcode", resp.errCode));
		}
		WXPayEntryActivity.this.finish();
	}
}