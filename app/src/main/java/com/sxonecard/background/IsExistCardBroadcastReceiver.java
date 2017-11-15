package com.sxonecard.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.sxonecard.CardApplication;
import com.sxonecard.base.RxBus;
import com.sxonecard.http.DateUtil;
import com.sxonecard.http.HttpDataListener;
import com.sxonecard.http.HttpDataSubscriber;
import com.sxonecard.http.HttpRequestProxy;
import com.sxonecard.http.bean.AdBean;
import com.sxonecard.http.serialport.SerialPortUtil;
import com.sxonecard.util.ByteUtil;
import com.sxonecard.util.Crc16;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IsExistCardBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        Intent i = new Intent(context, IsExistCardService.class);

        //向刷卡模块发送刷卡命令.
        byte[] moduleCheckByte = checkIsExistCard();
        SerialPortUtil.getInstance().sendBuffer(moduleCheckByte);

        context.startService(i);
        Log.i("checkCardReceiver", "checkCardReceiver:" + new Date().toString());
    }

    /**
     * 检测感应区是否有卡.
     * @return
     */
    private byte[] checkIsExistCard()
    {
        byte[] buff = new byte[23];

        int index = 0;
        index = ByteUtil.byte_tobuff("CV".getBytes(), buff, index);

        index = ByteUtil.int_tobuff(101, buff, index);
        index = ByteUtil.int_tobuff(0xFF, buff, index);
        index = ByteUtil.int_tobuff(0xFF, buff, index);

        index = ByteUtil.int_tobuff(0x03, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        index = ByteUtil.int_tobuff(0x03, buff, index);

        index = ByteUtil.int_tobuff(0x0B, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        //金额.
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        //交易时间.
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        int crc_value = Crc16.crc(buff, index, 0xFFFF, 0xA001);

        buff[index++] = ByteUtil.byte_toL(crc_value);
        buff[index++] = ByteUtil.byte_toH(crc_value);

        return buff;
    }


}