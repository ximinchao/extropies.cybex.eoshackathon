package com.extropies.www.eoshackathon.DataAndAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.extropies.www.eoshackathon.R;

import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * Created by inst on 18-6-9.
 */

public class AssetsAdapter extends BaseAdapter{
    private LinkedList<EosAssetsData> mData; 
    private Context mContext;

    public AssetsAdapter(LinkedList<EosAssetsData> mData, Context mContext){
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
            view = LayoutInflater.from(mContext).inflate(R.layout.assets_listview,viewGroup,false);
            holder = new ViewHolder();
            view.setTag(holder);
            holder.assetsName = (TextView) view.findViewById(R.id.assetsName);
            holder.assetsValue = (TextView) view.findViewById(R.id.assetsValue);
            holder.assetsValueInY = (TextView) view.findViewById(R.id.assetsValueInY);
            holder.image = (ImageView) view.findViewById(R.id.assetsListviewIcon);
        }

        holder.assetsName.setText(mData.get(position).getName());
        holder.assetsValue.setText(mData.get(position).getAccount());
        if (!mData.get(position).getAccountY().equals("")) {
            String userAssetsString = mData.get(position).getAccount();//.substring(0, 9);
            userAssetsString = userAssetsString.substring(0, userAssetsString.length() - 4);
            Double price = Double.parseDouble(mData.get(position).getAccountY());
            Double userAssetsFloat = Double.parseDouble(userAssetsString);
            Double result = price * userAssetsFloat;
            DecimalFormat numFmt = new DecimalFormat("0.00");
            String resultString = numFmt.format(result);
            holder.assetsValueInY.setText("≈ " + resultString + " ¥");
        } else {
            holder.assetsValueInY.setText("≈ " );
        }
        return view;
    }

    public void clear(){
        mData.clear();
        notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView image;
        TextView assetsName;
        TextView assetsValue;
        TextView assetsValueInY;
    }
}
