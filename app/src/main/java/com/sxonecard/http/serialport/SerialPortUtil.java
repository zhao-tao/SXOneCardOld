package com.sxonecard.http.serialport;

/**
 * Created by Administrator on 2017-5-23.
 */

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.sxonecard.CardApplication;
import com.sxonecard.base.CrashHandler;
import com.sxonecard.base.RxBus;
import com.sxonecard.http.bean.RechargeCardBean;
import com.sxonecard.util.ByteUtil;
import com.sxonecard.util.Crc16;
import com.sxonecard.util.DateTools;
import com.sxonecard.util.SyncSysTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * 串口操作类
 *
 * @author Jerome
 */
public class SerialPortUtil {
    private static String TAG = "serialPort";
    private static SerialPortUtil portUtil;
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private String path = "/dev/ttyS3";
    private int baudrate = 115200;
    private boolean isStop = false;

    public static SerialPortUtil getInstance() {
        if (null == portUtil) {
            portUtil = new SerialPortUtil();
            portUtil.onCreate();
        }
        return portUtil;
    }


    /**
     * 初始化串口信息
     */
    public void onCreate() {
        try {
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            mReadThread = new ReadThread();
            isStop = false;
            mReadThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送指令到串口
     *
     * @param cmd
     * @return
     */
    public boolean sendCmds(String cmd) {
        boolean result = true;
        byte[] mBuffer = (cmd).getBytes();
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBuffer);
            } else {
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean sendBuffer(byte[] mBuffer) {
        boolean result = true;
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBuffer);
            } else {
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        isStop = true;
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        if (mSerialPort != null) {
            mSerialPort.close();
        }
    }

    private void onDataReceive(byte[] srcBuffer, int size) {
        try {
            byte[] destBuff = new byte[srcBuffer.length];
            System.arraycopy(srcBuffer, 0, destBuff, 0, srcBuffer.length);

            int flag = Integer.valueOf(destBuff[7]);
            Integer.parseInt(
                    String.valueOf(Integer.valueOf(destBuff[9])) +
                            String.valueOf(Integer.valueOf(destBuff[8])));
            Log.d(TAG, "Command " + flag);
            switch (flag) {
                case 1:
                    //测试链接.
                    Log.d(TAG, "链接成功");
                    byte[] moduleCheckByte = moduleCheck();
                    String model = ByteUtil.bytesToHexString(moduleCheckByte);
                    SerialPortUtil.getInstance().sendBuffer(moduleCheckByte);
                    Log.d(TAG, "发送检测数据" + model);
                    break;
                case 2:
                    //刷卡设备各功能模块检测.
                    checkModule(destBuff);
                    Log.d(TAG, "模块检测成功");
                    break;
                case 3:
                    //.向刷卡模块发送充值命令
                    receiveCheckCardData(destBuff);
                    break;
                case 4:
                    //.接收刷卡模块发送刷卡命令
                    parseReChangeCardCmd(destBuff);
                    break;
                case 5:
                    //向刷卡模块发送关机命令.
                    parseShutDownCmd(destBuff);
                    break;
                case 6:
                    //时间同步.
                    Log.d(TAG, "时间同步");
                    break;
                case 7:
                    //检测到卡拿走
                    onInterrupt();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {

        }
    }

    private byte[] moduleCheck() {
        byte[] buff = new byte[12];
        int index = 0;
        index = ByteUtil.byte_tobuff("CV".getBytes(), buff, index);
        index = ByteUtil.int_tobuff(101, buff, index);

        index = ByteUtil.int_tobuff(0xFF, buff, index);
        index = ByteUtil.int_tobuff(0xFF, buff, index);

        index = ByteUtil.int_tobuff(0x02, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        index = ByteUtil.int_tobuff(0x02, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        index = ByteUtil.int_tobuff(0x00, buff, index);

        int crc_value = Crc16.crc(buff, index, 0xFFFF, 0xA001);

        buff[index++] = ByteUtil.byte_toL(crc_value);
        buff[index++] = ByteUtil.byte_toH(crc_value);

        return buff;
    }

    public void checkDevice() {
        Log.d(TAG, "检查设备中");
        sendBuffer(testConnections());
        //5秒后检查设备标志
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //设备未检查到，跳转至维护页面
                if (!CardApplication.getInstance().isDeviceOn()) {
                    RxBus.get().post("checkDevice", "0");
                    //继续请求
                    checkDevice();
                }
            }
        }, 5000);
    }

    private byte[] testConnections() {
        byte[] buff = new byte[12];
        int index = 0;

        index = ByteUtil.byte_tobuff("CV".getBytes(), buff, index);

        index = ByteUtil.int_tobuff(101, buff, index);
        index = ByteUtil.int_tobuff(0xFF, buff, index);
        index = ByteUtil.int_tobuff(0xFF, buff, index);

        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        index = ByteUtil.int_tobuff(0x01, buff, index);

        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        int crc_value = Crc16.crc(buff, index, 0xFFFF, 0xA001);

        buff[index++] = ByteUtil.byte_toL(crc_value);
        buff[index++] = ByteUtil.byte_toH(crc_value);

        return buff;
    }

    public void deviceTimeSync() {
        sendBuffer(timeSync());
    }

    private byte[] timeSync() {
        byte[] buff = new byte[20];
        int index = 0;

        index = ByteUtil.byte_tobuff("CV".getBytes(), buff, index);
        index = ByteUtil.int_tobuff(101, buff, index);

        index = ByteUtil.int_tobuff(0xFF, buff, index);
        index = ByteUtil.int_tobuff(0xFF, buff, index);

        index = ByteUtil.int_tobuff(0x06, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        index = ByteUtil.int_tobuff(0x06, buff, index);
        index = ByteUtil.int_tobuff(0x07, buff, index);

        index = ByteUtil.int_tobuff(0x00, buff, index);

        String phpTime = CardApplication.getInstance().getConfig().getRetTime();
        index = ByteUtil.date_tobuff(
                DateTools.getYearByDate(phpTime),
                Integer.parseInt(DateTools.getMonthByDate(phpTime)),
                Integer.parseInt(DateTools.getDayByDate(phpTime)),
                Integer.parseInt(DateTools.getHourByDate(phpTime)),
                Integer.parseInt(DateTools.getMinuteByDate(phpTime)),
                Integer.parseInt(DateTools.getSecondByDate(phpTime)),
                buff, index);

        //修改系统时间.
        try {
            SyncSysTime.setDateTime(phpTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, DateTools.getcurrent());

        int crc_value = Crc16.crc(buff, index, 0xFFFF, 0xA001);
        buff[index++] = ByteUtil.byte_toL(crc_value);
        buff[index++] = ByteUtil.byte_toH(crc_value);

        return buff;
    }

    public void deviceShutDown() {
        sendBuffer(shutDownDevice());
    }

    private byte[] shutDownDevice() {
        byte[] buff = new byte[20];
        int index = 0;

        index = ByteUtil.byte_tobuff("CV".getBytes(), buff, index);
        index = ByteUtil.int_tobuff(101, buff, index);

        index = ByteUtil.int_tobuff(0xFF, buff, index);
        index = ByteUtil.int_tobuff(0xFF, buff, index);

        index = ByteUtil.int_tobuff(0x05, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

        index = ByteUtil.int_tobuff(0x05, buff, index);
        index = ByteUtil.int_tobuff(0x08, buff, index);

        index = ByteUtil.int_tobuff(0xFA, buff, index);

        index = ByteUtil.int_tobuff(0x00, buff, index);

        //开机时间.
        String starTime = CardApplication.getInstance().getConfig().getStartTime();
        index = ByteUtil.date_tobuff(
                DateTools.getYearByDate(starTime),
                Integer.parseInt(DateTools.getMonthByDate(starTime)),
                Integer.parseInt(DateTools.getDayByDate(starTime)),
                Integer.parseInt(DateTools.getHourByDate(starTime)),
                Integer.parseInt(DateTools.getMinuteByDate(starTime)),
                Integer.parseInt(DateTools.getSecondByDate(starTime))
                , buff, index);

        //关机时间. TODO
//        String endTime = CardApplication.set.getEndTime();
//        index = ByteUtil.date_tobuff(
//                DateTools.getYearByDate(endTime),
//                Integer.parseInt(DateTools.getMonthByDate(endTime)),
//                Integer.parseInt(DateTools.getDayByDate(endTime)),
//                Integer.parseInt(DateTools.getHourByDate(endTime)),
//                Integer.parseInt(DateTools.getMinuteByDate(endTime)),
//                Integer.parseInt(DateTools.getSecondByDate(endTime))
//                , buff, index);

        int crc_value = Crc16.crc(buff, index, 0xFFFF, 0xA001);
        buff[index++] = ByteUtil.byte_toL(crc_value);
        buff[index++] = ByteUtil.byte_toH(crc_value);

        return buff;
    }


    private void parseShutDownCmd(byte[] destBuff) {
        if (205 == Integer.valueOf(destBuff[10])) {
            Log.d(TAG, "syncShutDownCmd success." + destBuff[10]);
        } else {
            Log.d(TAG, "syncShutDownCmd error." + destBuff[10]);
        }
    }

    private void checkModule(byte[] destBuff) {
        String status;
        if (5 == Integer.valueOf(destBuff[11])) {
            //各模块正常.
            status = "1";
            CardApplication.getInstance().setDevice(true);
        } else {
            status = "0";
        }
        RxBus.get().post("checkDevice", status);
    }

    /**
     * 接收感应区卡片返回信息.
     *
     * @param destBuff
     */
    private void receiveCheckCardData(byte[] destBuff) {
        String byteString = ByteUtil.bytesToHexString(destBuff);
        String balance = byteString.substring(11 * 2, 15 * 2);
        String cardNumber = byteString.substring(19 * 2, 23 * 2);
        String cardNumDate = byteString.substring(27 * 2, 31 * 2);
        String cardType = byteString.substring(31 * 2, 33 * 2);
        String cardStatus = byteString.substring(33 * 2, 34 * 2);
        int value = 0;
        try {
            value = Integer.parseInt(balance, 16);
        } catch (Exception e) {

        }
        RechargeCardBean cardBean = new RechargeCardBean(cardNumber,
                value, cardNumDate, cardType, cardStatus);
        Gson gson = new Gson();
        RxBus.get().post("checkCard", gson.toJson(cardBean));
    }

    /**
     * 解析卡充值返回的数据.
     *
     * @param destBuff
     */
    private void parseReChangeCardCmd(byte[] destBuff) {
        try {
            String byteString = ByteUtil.bytesToHexString(destBuff);
            int status = destBuff[10] & 0x0FF;
            if (status == 0) {
                String balance = byteString.substring(11 * 2, 15 * 2);
                String cardNumber = byteString.substring(19 * 2, 23 * 2);
                String order = byteString.substring(23 * 2, 31 * 2);
                RechargeCardBean cardBean = new RechargeCardBean();
                try {
                    cardBean.setAmount(Integer.parseInt(balance, 16));
                } catch (Exception e) {
                    cardBean.setAmount(0);
                }
                cardBean.setCardNumber(cardNumber);
                cardBean.setOrderNo(order);
                RxBus.get().post("reChangeCard", cardBean);
            } else {
                RxBus.get().post("chargeError", String.valueOf(status));
            }
        }catch (Exception e){

        }
    }

    /**
     * @功能描述 :向刷卡模块发送充值命令
     * @返回值类型 :void
     */

    public boolean sendRechargeCmd(int money) {
        return sendBuffer(rechargeCmd(money));
    }

    private byte[] rechargeCmd(int money) {
        byte[] buff = new byte[23];
        int index = 0;

        index = ByteUtil.byte_tobuff("CV".getBytes(), buff, index);
        index = ByteUtil.int_tobuff(101, buff, index);

        index = ByteUtil.int_tobuff(0xFF, buff, index);
        index = ByteUtil.int_tobuff(0xFF, buff, index);

        index = ByteUtil.int_tobuff(0x05, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);

//        index = ByteUtil.int_tobuff(0x00, buff, index);
        index = ByteUtil.int_tobuff(0x04, buff, index);

        index = ByteUtil.int_tobuff(0x0B, buff, index);
        index = ByteUtil.int_tobuff(0x00, buff, index);
        //金额占4位
        index = ByteUtil.int_tobuff4(money, buff, index);

        //data
        index = ByteUtil.date_tobuff(DateTools.getYear(), DateTools.getMonth(), DateTools.getDay(),
                DateTools.getCurrentHour(), DateTools.getCurrentMINUTE(), DateTools.getCurrentSEC(), buff, index);

        int crc_value = Crc16.crc(buff, 11, 0xFFFF, 0xA001);

        buff[index++] = ByteUtil.byte_toL(crc_value);
        buff[index++] = ByteUtil.byte_toH(crc_value);

        return buff;
    }

    private void onInterrupt() {
        RxBus.get().post("interrupt", "card disconnected");
    }


    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isStop && !isInterrupted()) {
                int size;
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[512];
                    //读取包头
                    size = mInputStream.read(buffer,0,11);
                    if (size == 11) {
                        //判断是否数据开头，不是丢掉该桢数据
                        if(!ByteUtil.bytesToHexString(buffer,5,false).equalsIgnoreCase("435665FFFF"))
                        {
                            Thread.sleep(300);
                            mInputStream.read(buffer);
                            Log.d("Length", "读取数据错误");
                        }else {
                            //提取长度
                            int data_length = ((buffer[9] << 8 | buffer[8]) & 0x0FFFF) + 1;
                            int total = size + data_length;
                            Log.d("Length", String.valueOf(data_length));
                            //追加数据
                            size += mInputStream.read(buffer, size, data_length);
                            //只有接收到正确数据才解析
                            if (total == size) {
                                Log.d("Length", ByteUtil.bytesToHexString(buffer, total, true));
                                onDataReceive(buffer, size);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                    return;
                }
            }
        }
    }

}