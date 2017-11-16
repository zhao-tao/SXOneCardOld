package com.sxonecard.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sxonecard.CardApplication;
import com.sxonecard.R;
import com.sxonecard.background.SoundService;
import com.sxonecard.base.BaseFragment;
import com.sxonecard.http.HttpDataListener;
import com.sxonecard.http.HttpDataSubscriber;
import com.sxonecard.http.HttpRequestProxy;
import com.sxonecard.http.MyCountDownTimer;
import com.sxonecard.util.DateTools;
import com.sxonecard.util.PrinterUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;

/**
 * Created by pc on 2017-04-27.
 */

public class FragmentSeven extends BaseFragment {
    @Bind(R.id.user_recharge_money)
    TextView userRechargeMoney;
    @Bind(R.id.print_ticket)
    Button printTicket;
    private MyCountDownTimer timer;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_7;
    }

    @Override
    public void initView() {
        //监听返回数据.
        setVoice(SoundService.CHONGZHI_SUCCESS);
        String msg = getArguments().getString("msg");
        userRechargeMoney.setText("卡内当前余额:" + msg + "元");
        timer = new MyCountDownTimer(5 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                navHandle.sendEmptyMessage(0);
            }
        };
        timer.start();

        printTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrinterUtil.getInstance().send();
            }
        });
        uploadTradeData();
    }

    /**
     * 上传交易数据.
     */
    private void uploadTradeData() {
        final Map<String, String> jsonObj = new HashMap<>(10);
        HttpDataListener tradeListener = new HttpDataListener<String>() {
            @Override
            public void onNext(String tradeStatusBean) {

            }

            @Override
            public void onError(Context context, int code, String msg) {
                super.onError(context, code, msg);
                Gson gson = new Gson();
//                OrderDb.insert(gson.toJson(jsonObj));
            }
        };
        jsonObj.put("Type", String.valueOf(1000));
        jsonObj.put("LiushuiId", String.valueOf(System.currentTimeMillis()));
        jsonObj.put("OrderId", CardApplication.getInstance().getCurrentOrderId());
        jsonObj.put("CardNo", CardApplication.getInstance().getCheckCard().getCardNumber());
        jsonObj.put("Time", DateTools.getCurrent());
        jsonObj.put("Operator", "tom");
        jsonObj.put("ReaderSn", "111");
        jsonObj.put("ReaderVer", "111");
        jsonObj.put("CorpCode", "111");
        jsonObj.put("MerchantSn", "1111");
        jsonObj.put("ImeiId", CardApplication.IMEI);
        jsonObj.put("TradeData", "111");
        jsonObj.put("OrderType", "1");
        jsonObj.put("mCardType", CardApplication.getInstance().getCheckCard().getType());
        HttpRequestProxy.getInstance().uploadTrade(new HttpDataSubscriber(tradeListener,
                getContext(), false), jsonObj);
    }

    @Override
    public void loadData() {

    }

    @Override
    public void onDestroy() {
        if (null != timer) {
            timer.cancel();
        }
        super.onDestroy();
    }

}
