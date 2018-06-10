package com.extropies.www.eoshackathon;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.extropies.www.eoshackathon.DataAndAdapter.AssetsAdapter;
import com.extropies.www.eoshackathon.DataAndAdapter.EosAssetsData;
import com.extropies.www.eoshackathon.Utils.ZxingUtils;

import java.text.DecimalFormat;
import java.util.List;

import static com.extropies.www.eoshackathon.MainActivity.URL_GET_CURRENCY_BALANCE;

/**
 * Created by inst on 18-6-9.
 */

public class AssetsFragment extends Fragment {
    private View view;
    private ImageView userImage;
    private TextView userNameTextView;
    private TextView userAccountAddressTextView;
    private TextView userAccountAssetsTextView;
    private TextView userAccountTotalAssetsTextView;
    private Button qrcodeButton;
    private Button reloadButton;
    private String userName = ""; 
    private String userAccountAddress = "";
    private String userAccountAssets = "";
    private String userAccountTotalAssets = "";
    private String eosPrice = "";
    private String walletType = "";

    private AssetsAdapter mAssetsAdapter = null;
    private List<EosAssetsData> mAssetsData = null; 
    private ListView mAssetsListView = null;
    private PopupWindow mPopWindow;

    private MainActivity.UrlHandler mainHandler = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_assets, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userImage = view.findViewById(R.id.userImage);
        userNameTextView = view.findViewById(R.id.userName);
        userAccountAddressTextView = view.findViewById(R.id.userAccountAddress);
        userAccountAssetsTextView = view.findViewById(R.id.userAccountAssets);
        userAccountTotalAssetsTextView = view.findViewById(R.id.userAccountTotalAssets);
        qrcodeButton = view.findViewById(R.id.qrcodeBtn);
        reloadButton = view.findViewById(R.id.reloadBtn);
        mAssetsListView = view.findViewById(R.id.assetsListview);
        mAssetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                System.out.println("===== setOnItemClickListener onItemClick" );
                Intent intent = new Intent(getActivity().getApplicationContext(),TransferActivity.class);
                intent.putExtra("userName", userName);
                intent.putExtra("userAccountAddress", userAccountAddress);
                intent.putExtra("userAccountBalance", userAccountAssets);
                intent.putExtra("walletType", walletType);
                startActivity(intent);
            }
        });

        updateAssetsString();
        if (mAssetsAdapter != null && mAssetsData != null) {
            updateAssetsListview();
        }
        qrcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!userAccountAddress.equals("")) {
                    showPopupWindow(userAccountAddress);
                }
            }
        });
        qrcodeButton.setVisibility(View.GONE);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainHandler != null && !userAccountAssets.equals("")) {
                    mainHandler.sendEmptyMessage(URL_GET_CURRENCY_BALANCE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("===== AssetsFragment onResume" );
        if (mainHandler != null && !userAccountAssets.equals("")) {
            mainHandler.sendEmptyMessage(URL_GET_CURRENCY_BALANCE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("===== AssetsFragment onPause" );
    }

    public void setAssetsListData(List<EosAssetsData> data) {
        mAssetsData = data;
    }

    public void setAssetsAdapter(AssetsAdapter assetsAdapter) {
        this.mAssetsAdapter = assetsAdapter;
    }

    public void setAssetsHandler(MainActivity.UrlHandler handler) {
        this.mainHandler = handler;
    }

    public void updateAssetsListview() {
        mAssetsListView.setAdapter(mAssetsAdapter);
        mAssetsAdapter.notifyDataSetChanged();
    }

    public void setAssetsString(String name,String address,String accountAssets,String accountTotalAssets) {
        userName = name;
        userAccountAddress = address;
        userAccountAssets = accountAssets;
        userAccountTotalAssets = accountTotalAssets;
    }
    public void setAssetsUserName(String input,String walletType) {
        userName = input;
        this.walletType = walletType;
    }

    public void clearAssetsText() {
        userNameTextView.setText("");
        userAccountAddressTextView.setText("");
        qrcodeButton.setVisibility(View.GONE);
        userAccountAssetsTextView.setText("");
        userAccountTotalAssetsTextView.setText("Total(¥):"+"");

    }
    public void setAssetsUserAccountAddress(String input) {
        userAccountAddress = input;
    }
    public void setAssetsUserAccountAssets(String input) {
        userAccountAssets = input;
    }
    public void setAssetsUserAccountTotalAssets(String input) {
        userAccountTotalAssets = input;
    }

    public void setEosPrice(String input) {
        eosPrice = input;
    }

    public void updateAssetsString() {
        userNameTextView.setText(userName);
        userAccountAddressTextView.setText(userAccountAddress);
        userAccountAssetsTextView.setText(userAccountAssets);
        userAccountTotalAssetsTextView.setText("Total(¥):"+userAccountTotalAssets);
        if (!userAccountAddress.equals("") && qrcodeButton.getVisibility()==View.GONE) {
            qrcodeButton.setVisibility(View.VISIBLE);
        }
        if (!eosPrice.equals("") && !userAccountAssets.equals("")) {
            String userAssetsString = userAccountAssets.substring(0, userAccountAssets.length()-4);
            Double price = Double.parseDouble(eosPrice);
            Double userAssetsFloat = Double.parseDouble(userAssetsString);
            Double result = price * userAssetsFloat;
            DecimalFormat numFmt= new DecimalFormat("0.00");
            String resultString = numFmt.format(result);
            userAccountTotalAssetsTextView.setText("Total(¥): "+resultString);
        }
    }


    private void showPopupWindow(String addressString) {
        View contentView = getActivity().getLayoutInflater().inflate(R.layout.layout_qrcode_popupwindow, null);
        mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setFocusable(true);
        ImageView qrcodeImageview = contentView.findViewById(R.id.qrcodeImage);
        Bitmap bitmap = ZxingUtils.createBitmap(addressString);
        qrcodeImageview.setImageBitmap(bitmap);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());//
        mPopWindow.setAnimationStyle(R.style.Popupwindow);
        mPopWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.LEFT | Gravity.BOTTOM, 0, 0);
        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener(){
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
        backgroundAlpha(0.5f);
    }
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
}
