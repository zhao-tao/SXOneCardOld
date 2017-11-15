package com.sxonecard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.sxonecard.R;
import com.sxonecard.background.SoundService;
import com.sxonecard.base.BaseFragment;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;


public class FragmentTwo extends BaseFragment {
    @Bind(R.id.serviceTypeView)
    GridView serviceTypeView;
    @Bind(R.id.user_money)
    TextView userMoney;

    private int[] icon = {R.drawable.anniu1, R.drawable.anniu2, R.drawable.anniu2};
    private String[] name = {"充值", "水电", "购卡"};
    private Integer[] images = {R.drawable.bus_selected,R.drawable.elec_normal,R.drawable.card_normal};
    private List<Map<String, String>> dataList = new ArrayList<Map<String, String>>(3);
    @Override
    public int getLayoutId() {
        return R.layout.fragment_2;
    }

    @Override
    public void initView() {
//        setVoice(SoundService.CAOZUOTISHI);
        setVoice(SoundService.WUQUZOUKAPIAN);
        Bundle bundle = getArguments();
        if(bundle != null){
            String money = (String)bundle.get("msg");
            userMoney.setText("当前余额："+money+"元");
        }
        dataList = getData();
        serviceTypeView.setAdapter(new CommonAdapter<Map<String, String>>(
                getContext(), R.layout.item_service_type_2, dataList) {
            @Override
            protected void convert(ViewHolder viewHolder, Map<String, String> stringObjectMap, int position) {
                viewHolder.setImageResource(R.id.service_img,images[position]);
            }
        });
        serviceTypeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        navHandle.sendEmptyMessage(2);
                        break;
                    case 1:
                        Toast.makeText(FragmentTwo.super.context, "水电服务暂未开放!",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(FragmentTwo.super.context, "购卡服务暂未开放!",
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(FragmentTwo.super.context, "未知服务...",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });

    }

    @Override
    public void loadData() {
    }


    private List<Map<String, String>> getData() {
        //填充数据源
        Map<String, String> map = null;
        for (int i = 0; i < name.length; i++) {
            map = new HashMap<>();
            map.put("image", String.valueOf(icon[i]));
            map.put("name", name[i]);
            dataList.add(map);
        }
        return dataList;
    }
}
