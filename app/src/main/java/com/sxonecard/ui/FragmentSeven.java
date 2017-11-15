package com.sxonecard.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sxonecard.R;
import com.sxonecard.background.SoundService;
import com.sxonecard.base.BaseFragment;
import com.sxonecard.base.RxBus;
import com.sxonecard.http.HttpDataListener;
import com.sxonecard.http.HttpDataSubscriber;
import com.sxonecard.http.HttpRequestProxy;
import com.sxonecard.http.MyCountDownTimer;
import com.sxonecard.http.bean.RechargeCardBean;
import com.sxonecard.http.bean.TradeStatusBean;
import com.sxonecard.http.serialport.SerialPortUtil;
import com.sxonecard.util.AESOperator;
import com.sxonecard.util.ByteUtil;
import com.sxonecard.util.Crc16;
import com.sxonecard.util.DateTools;
import com.sxonecard.util.PrinterUtil;
import com.sxonecard.util.vender.clsToolBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import butterknife.Bind;
import rx.Observable;
import rx.functions.Action1;

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
