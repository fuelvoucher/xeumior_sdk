package com.smart.st8050b_demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smart.st8050b_demo.R;
import com.smart.st8050b_demo.bean.OperatorInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/2.
 */

public class OperatorInfoAdapter extends ArrayAdapter<OperatorInfo>{
    Context mContext;
    private int mLocationPosition = -1;
    private static List<OperatorInfo> operatorInfoList = new ArrayList<>();
    private String TAG = "";
    public OperatorInfoAdapter(Context context, List<OperatorInfo> _list) {
        super(context, 0);
        mContext = context;
        this.operatorInfoList = _list;
    }

    public void setData(List<OperatorInfo> data) {
        setNotifyOnChange(false);
        clear();
        setNotifyOnChange(true);
        if (data != null) {
            for (OperatorInfo appEntry : data) {
                add(appEntry);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        ViewHolder mViewHolder;
        if (convertView == null || convertView.getTag() == null) {
            view = View.inflate(mContext, R.layout.operatorinfo_item, null);

            mViewHolder = new ViewHolder();
            mViewHolder.OperatorName = (TextView) view.findViewById(R.id.operator_name);
            view.setTag(mViewHolder);
        } else {
            view = convertView;
            mViewHolder = (ViewHolder) view.getTag();
        }

        OperatorInfo contacts = getItem(position);
        if (contacts != null) {

            String name = contacts.get_OperatorName();
            mViewHolder.OperatorName.setText(name);
        }
        return view;
    }

    class ViewHolder {
        /*
        Name
         */
        TextView OperatorName;

    }
}
