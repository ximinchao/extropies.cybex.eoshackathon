package com.extropies.www.eoshackathon;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;  
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.extropies.www.eoshackathon.DataAndAdapter.AssetsAdapter;
import com.extropies.www.eoshackathon.DataAndAdapter.EosAssetsData;
import com.extropies.www.eoshackathon.DataAndAdapter.EosConfData;
import com.extropies.www.eoshackathon.DataAndAdapter.ConfAdapter;
import com.extropies.www.eoshackathon.Utils.SharePreferenceUtils;
import com.extropies.www.eoshackathon.http.HttpUtilsCallback;
import com.extropies.www.eoshackathon.http.HttpUtilsGet;
import com.extropies.www.eoshackathon.http.HttpUtilsPost;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static final String urlHead = "http://47.75.154.248:8888";//"http://192.168.3.70:8888";
    public static final String urlWalletHead = "http://47.75.154.248:8900";
    public static final String EosSymbolString = "SYS";//sys//EOS
    public static final String ConsaleSymbolString = "eosprepay";//consale//consalebjext
    public static final int URL_GET_INFO = 1;
    public static final int URL_GET_BLOCK = 2;
    public static final int URL_GET_CURRENCY_BALANCE = 3;
    public static final int URL_ABI_JSON_TO_BIN = 4;
    public static final int URL_GET_ACCOUNT = 5;
    public static final int URL_SIGN_TRANSACTION = 6;
    public static final int URL_WALLET_UNLOCK = 7;
    public static final int URL_PUSH_TRANSACTION = 8;
    public static final int URL_GET_TABLE_ROWS = 9;
    public static final int UPDATE_PRICE = 99;
    public static final int BLE_START_SCAN_AGAIN = 98;
    public static final int LOG_TEXT_APPEND = 97;
    public static final int BLE_SIGN_TRANSATION_RSP = 96;
    public static final int BLE_SIGN_TRANSATION_TIMEOUT = 95;

    private LinearLayout mTabAssets;
    private LinearLayout mTabConf;
    private LinearLayout mTabDiscovery;
    private LinearLayout mTabMy;

    private ImageView mImgAssets;
    private ImageButton mImgConf;
    private ImageButton mImgDiscovery;
    private ImageButton mImgMy;

    private AssetsFragment fragmentAssets;
    private ConferenceFragment fragmentConf;
    private DiscoveryFragment fragmentDiscovery;
    private MyFragment fragmentMy;

    private AssetsAdapter mAssetsAdapter = null;
    private List<EosAssetsData> mAssetsData = null;
    private ConfAdapter mConfAdapter = null;
    private List<EosConfData> mConfData = null;

    private String currentAccountName;
    private String currentAccountAssets;
    private String currentCurrencyBalance;
    private String currentFragment;
    private String currentEosPrice;
    private String currentWalletType = "";

    private UrlHandler mUrlHandler;
    public HttpUtilsCallback httpRsp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//

        setContentView(R.layout.activity_main);

//        if (SharePreferenceUtils.contains(this, "AccountName")) {
//            currentAccountName = (String) SharePreferenceUtils.get(this, "AccountName", "");
//        } else {
//            currentAccountName = "";
//        }
        Intent getIntent = getIntent();
        currentAccountName = getIntent.getStringExtra("userName");
        String walletType = getIntent.getStringExtra("coldWallet");
        if (walletType.equals("true")) {
            currentWalletType = "coldWallet";
        } else {
            currentWalletType = "softWallet";
        }

        initView();//
        initEvents();
        setSelect(0);


        httpRsp = new HttpUtilsCallback() {
            @Override
            public void httpResponse(String url, String rsp) {
                getDataUrlRspString(url, rsp);
            }
        };

        mUrlHandler = new UrlHandler(this.getMainLooper(), this);
        fragmentAssets.setAssetsHandler(mUrlHandler);
        if (!currentAccountName.equals("")) {
            fragmentAssets.setAssetsUserName(currentAccountName,currentWalletType);
            fragmentConf.setConfUserName(currentAccountName,currentWalletType);

            mUrlHandler.sendEmptyMessage(URL_GET_ACCOUNT);
            mUrlHandler.sendEmptyMessage(URL_GET_CURRENCY_BALANCE);
            mUrlHandler.sendEmptyMessage(URL_GET_TABLE_ROWS);
        } else {
            fragmentAssets.setAssetsUserName("","");
            fragmentConf.setConfUserName("","");
        }

        getEosConfData("");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mTabDiscovery != null && currentFragment!=null && currentFragment.equals("fragment_tab_discovery")) {
            if (DiscoveryFragment.onKeyDown(keyCode, event,fragmentDiscovery)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    private void initView() {
        mTabAssets = (LinearLayout)findViewById(R.id.id_tab_assets);
        mTabConf = (LinearLayout)findViewById(R.id.id_tab_conf);
        mTabDiscovery = (LinearLayout)findViewById(R.id.id_tab_discovery);
        mTabMy = (LinearLayout)findViewById(R.id.id_tab_my);

        mImgAssets = (ImageView) findViewById(R.id.id_tab_assets_img);
        mImgConf = (ImageButton)findViewById(R.id.id_tab_conf_img);
        mImgDiscovery = (ImageButton)findViewById(R.id.id_tab_discovery_img);
        mImgMy = (ImageButton)findViewById(R.id.id_tab_my_img);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();//
        fragmentAssets = new AssetsFragment();
        transaction.add(R.id.frameMain, fragmentAssets,"fragmentAssetsTag").show(fragmentAssets);

        fragmentConf = new ConferenceFragment();
        transaction.add(R.id.frameMain, fragmentConf,"fragmentConfTag");

        transaction.commit();
        fm.executePendingTransactions();

        mAssetsData = new LinkedList<EosAssetsData>();
        mAssetsAdapter = new AssetsAdapter((LinkedList<EosAssetsData>) mAssetsData, this);
        fragmentAssets.setAssetsAdapter(mAssetsAdapter);
        fragmentAssets.setAssetsListData(mAssetsData);

        mConfData = new LinkedList<EosConfData>();
        mConfAdapter = new ConfAdapter((LinkedList<EosConfData>) mConfData, this);
        fragmentConf.setConfAdapter(mConfAdapter);
        fragmentConf.setConfListData(mConfData);

    }
    private void initEvents() {
        mTabAssets.setOnClickListener(this);
        mTabConf.setOnClickListener(this);
        mTabDiscovery.setOnClickListener(this);
        mTabMy.setOnClickListener(this);
    }

    private void setSelect(int i) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();//
        hideFragment(transaction);//
        switch (i) {
            case 0:
                if (fragmentAssets == null) {
                    fragmentAssets = new AssetsFragment();
                    transaction.add(R.id.frameMain, fragmentAssets).show(fragmentAssets);//

                }else {
                    transaction.show(fragmentAssets);
                }
                mImgAssets.setImageResource(R.mipmap.tab_images_assetsselected);
                break;
            case 1:
                if (fragmentConf == null) {
                    fragmentConf = new ConferenceFragment();
                    transaction.add(R.id.frameMain, fragmentConf).hide(fragmentAssets);
                }else {
                    transaction.show(fragmentConf);
                }
                mImgConf.setImageResource(R.mipmap.tab_images_confselected);
                break;
            case 2:
                if (fragmentDiscovery == null) {
                    fragmentDiscovery = new DiscoveryFragment();
                    transaction.add(R.id.frameMain, fragmentDiscovery).hide(fragmentAssets);
                }else {
                    transaction.show(fragmentDiscovery);
                }
                mImgDiscovery.setImageResource(R.mipmap.tab_images_discoveryselected);
                break;
            case 3:
                if (fragmentMy == null) {
                    fragmentMy = new MyFragment();
                    transaction.add(R.id.frameMain, fragmentMy).hide(fragmentAssets);
                }else {
                    transaction.show(fragmentMy);
                }
                mImgMy.setImageResource(R.mipmap.tab_images_myselected);
                break;

            default:
                break;
        }
        transaction.commit();//
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (fragmentAssets != null) {
            transaction.hide(fragmentAssets);
        }
        if (fragmentDiscovery != null) {
            transaction.hide(fragmentDiscovery);
        }
        if (fragmentConf != null) {
            transaction.hide(fragmentConf);
        }
        if (fragmentMy != null) {
            transaction.hide(fragmentMy);
        }
    }
    private void resetImg() {
        mImgAssets.setImageResource(R.mipmap.tab_images_assets);
        mImgConf.setImageResource(R.mipmap.tab_images_conf);
        mImgDiscovery.setImageResource(R.mipmap.tab_images_discovery);
        mImgMy.setImageResource(R.mipmap.tab_images_my);
    }

    @Override
    public void onClick(View view) {
        resetImg();
        switch (view.getId()) {
            case R.id.id_tab_assets:
                setSelect(0);
                currentFragment = "fragment_tab_assets";
                if (fragmentMy != null) {
                    String newAccountNameString = fragmentMy.getNewAccountNameString();
                    if (!newAccountNameString.equals("")) {
                        fragmentMy.setNewAccountNameString("");
                        fragmentAssets.clearAssetsText();
                        SharePreferenceUtils.put(this,"AccountName",newAccountNameString);
                        currentAccountName = newAccountNameString;
                        fragmentAssets.setAssetsUserName(currentAccountName,currentWalletType);
                        fragmentConf.setConfUserName(currentAccountName,currentWalletType);
                        mUrlHandler.sendEmptyMessage(URL_GET_ACCOUNT);
                        mUrlHandler.sendEmptyMessage(URL_GET_CURRENCY_BALANCE);
                    }
                }
                break;
            case R.id.id_tab_conf:
                setSelect(1);
                currentFragment = "fragment_tab_conf";
                break;
            case R.id.id_tab_discovery:
                setSelect(2);
                currentFragment = "fragment_tab_discovery";
                break;
            case R.id.id_tab_my:
                setSelect(3);
                currentFragment = "fragment_tab_my";
                break;
            default:
                break;
        }
    }

    public void getEosPriceRsp( ) {
        fragmentAssets.setEosPrice(currentEosPrice);
        if (mAssetsData.size() != 0) {
            mAssetsData.get(0).setAccountY(currentEosPrice);
        }
        mUrlHandler.sendEmptyMessage(UPDATE_PRICE);
    }

    public void getDataUrlRspString(String targeurl,String stringvalue) {
        if (targeurl.endsWith("get_account")) {
            currentAccountAssets = "";
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                JSONArray dataObjectArray = jsonObject.getJSONArray("permissions");
                JSONObject dataObject = (JSONObject) dataObjectArray.get(0);
                JSONArray keysArray = dataObject.getJSONObject("required_auth").getJSONArray("keys");
                JSONObject keysObject = (JSONObject) keysArray.get(0);
                String keysString = keysObject.getString("key");
                currentAccountAssets = keysString;
                fragmentAssets.setAssetsUserAccountAddress(currentAccountAssets);
                fragmentConf.setConfUserAccountAddress(currentAccountAssets);
                fragmentAssets.updateAssetsString();
            }catch (Exception e) {
                Log.e("exception:","error");
            }
        }else if (targeurl.endsWith("get_currency_balance")) {
            String[] stringlist = stringvalue.split("\"");
            if (stringlist.length == 3) {
                currentCurrencyBalance = stringlist[1];
                fragmentAssets.setAssetsUserAccountAssets(currentCurrencyBalance);
                fragmentAssets.updateAssetsString();
                mAssetsData.clear();
                if (currentEosPrice!=null && !currentEosPrice.equals("")) {
                    ((LinkedList<EosAssetsData>) mAssetsData).add(new EosAssetsData("EOS", currentCurrencyBalance, currentEosPrice));
                } else {
                    ((LinkedList<EosAssetsData>) mAssetsData).add(new EosAssetsData("EOS", currentCurrencyBalance, ""));
                }
                fragmentAssets.updateAssetsListview();
            }
        }else if (targeurl.endsWith("get_table_rows")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    String keyJSON = jsonObject.optString(key);
                    System.out.println("===== "+ key + ':' +  keyJSON );
                }
                if (jsonObject.has("rows")) {
                    JSONArray confArray =jsonObject.getJSONArray("rows");
                    JSONObject confObject;
                    mConfData.clear();
                    for (int loop=0;loop<confArray.length();loop++) {
                        confObject = (JSONObject)confArray.get(loop);
                        mConfData.add(new EosConfData(
                                confObject.getString("conf_name"),
                                confObject.getInt("id"),
                                confObject.getString("organizer"),
                                confObject.getString("fee")
                        ));
                    }
                    fragmentConf.updateConfListview();
                } else {
                }
            } catch (Exception e) {
                Log.e("exception:","error");
            }
        }
    }

    public class UrlHandler extends Handler {
        private final WeakReference<MainActivity> reference;
        UrlHandler(Looper looper, MainActivity activity) {
            super(looper);
            reference = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case URL_GET_ACCOUNT:
                    try {
                        JSONObject jsonObj = new JSONObject();//
                        jsonObj.put("account_name", currentAccountName);//
                        new HttpUtilsPost(urlHead + "/v1/chain/get_account",jsonObj.toString(),httpRsp).execute();
                    }catch (Exception e) {
                        Log.e("exception:","error");
                    }
                    break;
                case URL_GET_INFO:
                    new HttpUtilsGet(urlHead + "/v1/chain/get_info",httpRsp).execute();
                    break;
                case URL_GET_CURRENCY_BALANCE:
                    try {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("account", currentAccountName);
                        jsonObj.put("code","eosio.token");
                        jsonObj.put("symbol", EosSymbolString);
                        new HttpUtilsPost(urlHead + "/v1/chain/get_currency_balance",jsonObj.toString(),httpRsp).execute();
                    }catch (Exception e) {
                        Log.e("exception:","error");
                    }
                    break;
                case URL_GET_TABLE_ROWS:
                    try {
                        JSONObject getTableJsonObj = new JSONObject();
                        getTableJsonObj.put("scope", ConsaleSymbolString);//"consale"
                        getTableJsonObj.put("code",ConsaleSymbolString);//"consale"
                        getTableJsonObj.put("table", "conference");//attable
                        getTableJsonObj.put("lower_bound", "0");
                        getTableJsonObj.put("limit", "100");
                        getTableJsonObj.put("json", true);
                        new HttpUtilsPost(urlHead + "/v1/chain/get_table_rows",getTableJsonObj.toString(),httpRsp).execute();
                    }catch (Exception e) {
                        Log.e("exception:","error");
                    }
                    break;
                case UPDATE_PRICE:
                    fragmentAssets.updateAssetsString();
                    fragmentAssets.updateAssetsListview();
                    break;

                default:
                    break;
            }
        };


    }

    public void getEosConfData(String blogLink){
        final String url = blogLink;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Document doc1 = Jsoup.connect("http://www.btcbibi.com/bbcoin/symbols?bbcoinId=ff80808160b72bad0160b72ea000016c&shortName=CNY").get();
                    Elements elements = doc1.select("tbody").select("tr");
                    String targetString = "/bbmarket/detial?id=ff80808160b74ffe0160b75cd7610170";
                    Elements targetElement = null;
                    for (Element element : elements) {
                        String checkString = element.select("a[href]").attr("href");
                        if (checkString.equals(targetString)) {
                            targetElement = element.select("span");
                            break;
                        }
                    }
                    if (targetElement != null) {
                        String targetPrice = targetElement.get(0).text();
                        if (!targetPrice.equals("")) {
                            currentEosPrice = targetPrice.substring(1);
                            getEosPriceRsp();
                        }
                    }
                } catch (IOException e){
                    e.printStackTrace();
                    Log.e("exception:","getEosConfData error");
                } catch (Exception e){
                    Log.e("exception:","getEosConfData error");
                }
            }
        }).start();
    }

}
