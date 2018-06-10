package com.extropies.www.eoshackathon.DataAndAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.extropies.www.eoshackathon.R;

import java.util.LinkedList;

/**
 * Created by inst on 18-6-9.
 */

public class ConfAdapter extends BaseAdapter{
    private LinkedList<EosConfData> mData;
    private Context mContext;

    public ConfAdapter(LinkedList<EosConfData> mData, Context mContext){
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.conference_listview,viewGroup,false);
            holder = new ViewHolder();
            view.setTag(holder);
            holder.confName = (TextView) view.findViewById(R.id.confName);
            holder.confId = (TextView) view.findViewById(R.id.confId);
            holder.confOrganizer = (TextView) view.findViewById(R.id.confOrganizer);
            holder.confFee = (TextView) view.findViewById(R.id.confFee);
        }

        holder.confName.setText(mData.get(position).getConfName());
        holder.confId.setText("id: "+mData.get(position).getConfId());
        holder.confOrganizer.setText("org: "+mData.get(position).getConfOrganizer());
        holder.confFee.setText("fee: "+mData.get(position).getConfFee());

        if (position % 2 == 1) {
            view.setBackgroundResource(R.mipmap.conf_background1);
        } else {
            view.setBackgroundResource(R.mipmap.conf_background2);
        }
        return view;
    }

    public void clear(){
        mData.clear();
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView confName;
        TextView confId;
        TextView confOrganizer;
        TextView confFee;
    }
}
