package com.smart.st8050b_demo.bean;
/**
 * Created on: 2015-5-4
 * @author luozhihao
 *
 */
import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smart.st8050b_demo.R;

public class IconInfoAdapter extends BaseAdapter{
	private Context context;
	private List<IconInfo> items;
	private LayoutInflater inflater;
	public IconInfoAdapter(Context context, List<IconInfo> items){
		this.context=context;
		this.items=items;
		this.inflater=LayoutInflater.from(context);
	}
	public void setData(List<IconInfo>items){
		this.items=items;
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub           
		return items.size(); 
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
	}
	@Override
	public long getItemId(int position) {  
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=inflater.inflate(R.layout.item_iconinfo, null);
			holder.mName=(TextView)convertView.findViewById(R.id.mTv);
			holder.mIco = (ImageView)convertView.findViewById(R.id.mIv);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}  
		final IconInfo item =items.get(position);
		holder.mName.setText(item.getName());
		int mIcoRes = items.get(position).getIco();
		if(mIcoRes==-1){
			holder.mIco.setImageResource(R.drawable.ic_launcher);
		}else{
			holder.mIco.setImageResource(mIcoRes);
		}
		
		return convertView;                 
	} 
	class ViewHolder{
		ImageView mIco;
		TextView mName;
	
		
	}
	

}
