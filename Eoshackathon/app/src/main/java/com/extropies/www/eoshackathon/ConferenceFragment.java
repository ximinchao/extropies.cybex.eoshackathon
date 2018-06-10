package com.extropies.www.eoshackathon;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.extropies.www.eoshackathon.DataAndAdapter.EosConfData;
import com.extropies.www.eoshackathon.DataAndAdapter.ConfAdapter;

import java.util.List;

/**
 * Created by inst on 18-6-9.
 */

public class ConferenceFragment extends Fragment {
    private View view;
    private ListView confListview;
    private String userName = "";
    private String userAccountAddress = "";
    private String walletType = "";

    private ConfAdapter mConfAdapter = null;
    private List<EosConfData> mConfData = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_conference, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        confListview = view.findViewById(R.id.confListview);

        confListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                System.out.println("===== setOnItemClickListener onItemClick" );
                Intent intent = new Intent(getActivity().getApplicationContext(),ConferenceActivity.class);
                intent.putExtra("userName", userName);
                intent.putExtra("userAddress", userAccountAddress);
                if (mConfData != null) {
                    intent.putExtra("confID", mConfData.get(position).getConfId());
                    intent.putExtra("confName", mConfData.get(position).getConfName());
                    intent.putExtra("confOrgn", mConfData.get(position).getConfOrganizer());
                    intent.putExtra("confFee", mConfData.get(position).getConfFee());
                    intent.putExtra("walletType", walletType);
                } else {
                    intent.putExtra("confID", "");
                    intent.putExtra("confName", "");
                    intent.putExtra("confOrgn", "");
                    intent.putExtra("confFee", "");
                    intent.putExtra("walletType", "");
                }
                startActivity(intent);
            }
        });

    }

    public void setConfUserName(String input,String walletType) {
        userName = input;
        this.walletType = walletType;
    }
    public void setConfUserAccountAddress(String input) {
        userAccountAddress = input;
    }
    public void setConfListData(List<EosConfData> data) {
        mConfData = data;
    }

    public void setConfAdapter(ConfAdapter confAdapter) {
        this.mConfAdapter = confAdapter;
    }

    public void updateConfListview() {
        confListview.setAdapter(mConfAdapter);
        mConfAdapter.notifyDataSetChanged();
    }

}
