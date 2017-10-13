package com.afrid.common.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afid.utils.ZKCManager;
import com.afrid.common.MyApplication;
import com.afrid.common.R;
import com.afrid.common.adapter.LinenAdapter;
import com.afrid.common.bean.json.BaseJsonResult;
import com.afrid.common.bean.json.SaveReceiptRequest;
import com.afrid.common.bean.json.return_data.GetTagInfoListReturn;
import com.afrid.common.net.APIMethodManager;
import com.afrid.common.net.IRequestCallback;
import com.afrid.common.utils.PrintStrBuildUtils;
import com.yyyu.baselibrary.utils.MyLog;
import com.yyyu.baselibrary.utils.MyToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Subscription;

/**
 * 功能：收据生成界面
 *
 * @author yu
 * @version 1.0
 * @date 2017/9/13
 */

public class ReceiptActivity extends MyBaseActivity {

    private static final String TAG = "ReceiptActivity";
    public static final int RECEIPT_REQUEST_CODE = 1001;
    public static final int RECEIPT_RESULT_CODE = 1002;

    @BindView(R.id.tv_useless_tag)
    TextView tv_useless_tag;
    @BindView(R.id.tb_receipt)
    Toolbar tb_receipt;
    @BindView(R.id.rv_warehouse)
    RecyclerView rv_warehouse;
    @BindView(R.id.btn_submit)
    Button btn_submit;


    private APIMethodManager apiMethodManager;
    private LinenAdapter adapter;
    private String getTagInfoRequest;
    private MyApplication application;
    private GetTagInfoListReturn.ResultDataBean resultData;
    private ZKCManager zkcManager;
    private String warehouseName;
    private Subscription tagInfoListSubscription;
    private Subscription saveReceiptSubscription;

    @Override
    public void beforeInit() {
        super.beforeInit();
        zkcManager = ZKCManager.getInstance(this);
        zkcManager.bindService();
        apiMethodManager = APIMethodManager.getInstance();
        application = (MyApplication) getApplication();
    }

    @Override
    public int getLayoutId() {
        getTagInfoRequest = getIntent().getStringExtra("getTagInfoRequest");
        warehouseName = getIntent().getStringExtra("warehouseName");
        return R.layout.activity_receipt;
    }

    @Override
    protected void initView() {
        //setSupportActionBar(tb_receipt);
       /* getSupportActionBar().setTitle(R.string.receipt_tb_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);*/
        rv_warehouse.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LinenAdapter(this);
        rv_warehouse.setAdapter(adapter);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        super.initData();

        showLoadDialog(resourceUtils.getStr(R.string.data_loading));

        //--根据tagId查询标签信息
        tagInfoListSubscription = apiMethodManager.getTagInfoList(getTagInfoRequest, new IRequestCallback<GetTagInfoListReturn>() {
            @Override
            public void onSuccess(GetTagInfoListReturn result) {
                int resultCode = result.getResultCode();
                if (resultCode == 200) {
                    resultData = result.getResultData();
                    adapter.setmData(result.getResultData().getMIxTagLinenList());
                    tv_useless_tag.setText("" + result.getResultData().getUselessTag().getTagNum());
                } else if (resultCode == 500) {
                    MyToast.showShort(ReceiptActivity.this, "服务器错误");
                }
                hiddenLoadingDialog();
            }

            @Override
            public void onFailure(Throwable throwable) {
                MyLog.e(TAG, "异常==" + throwable.getMessage());
                //网络加载失败
                AlertDialog alertDialog = new AlertDialog.Builder(ReceiptActivity.this)
                        .setCancelable(true)
                        .setTitle(resourceUtils.getStr(R.string.receipt_alter_title) + "\r\n")
                        .setMessage(resourceUtils.getStr(R.string.receipt_alter_msg_failed))
                        .setNegativeButton(resourceUtils.getStr(R.string.receipt_alter_retry),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        initData();
                                    }
                                })
                        .create();
                alertDialog.show();
                hiddenLoadingDialog();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //---TODO stop操作
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void back(View view) {
        finish();
    }

    /**
     * 保存收据
     *
     * @param view
     */
    public void saveReceipt(View view) {

        btn_submit.setEnabled(false);
        showLoadDialog("订单提交中.....");

        SaveReceiptRequest saveReceiptRequest = new SaveReceiptRequest();
        SaveReceiptRequest.RequestDataBean requestDataBean = new SaveReceiptRequest.RequestDataBean();
        requestDataBean.setUserId(application.getUser_id());
        requestDataBean.setReaderId(application.getCurrentReaderId());
        requestDataBean.setLinenNum(resultData.getVaildTagNum());  //设置有效标签总数
        requestDataBean.setReceiptWarehouseId(application.getCheckWarehouseId());
        requestDataBean.setSenderWarehouseId(application.getCheckWarehouseId());
        requestDataBean.setSubReceiptList(resultData.getMIxTagLinenList());
        saveReceiptRequest.setRequestData(requestDataBean);

        String request = mGson.toJson(saveReceiptRequest);
        saveReceiptSubscription = apiMethodManager.saveReceipt(request, new IRequestCallback<BaseJsonResult<String>>() {
            @Override
            public void onSuccess(BaseJsonResult<String> result) {
                int resultCode = result.getResultCode();
                String msg = result.getMsg();
                if (resultCode == 200) {
                    final String mainId = result.getResultData();
                    AlertDialog alertDialog = new AlertDialog.Builder(ReceiptActivity.this)
                            .setCancelable(false)
                            .setTitle(resourceUtils.getStr(R.string.receipt_alter_title) + "\r\n")
                            .setMessage(resourceUtils.getStr(R.string.receipt_alter_msg))
                            .setNegativeButton(resourceUtils.getStr(R.string.receipt_alter_nev), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    setResult(RECEIPT_RESULT_CODE);
                                    finish();
                                }
                            })
                            .setPositiveButton(resourceUtils.getStr(R.string.receipt_alter_pos), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String printStr = PrintStrBuildUtils.buildReceipt(ReceiptActivity.this, true, application.getUser_name(),
                                            warehouseName, mainId, ReceiptActivity.this.resultData.getMIxTagLinenList());
                                    zkcManager.getPrintManager().printText(printStr);
                                }
                            })
                            .create();
                    alertDialog.show();
                    String printStr = PrintStrBuildUtils.buildReceipt(ReceiptActivity.this, false, application.getUser_name(),
                            warehouseName, mainId, ReceiptActivity.this.resultData.getMIxTagLinenList());
                    zkcManager.getPrintManager().printText(printStr);
                } else {
                    btn_submit.setEnabled(true);
                    MyToast.showShort(ReceiptActivity.this, resourceUtils.getStr(R.string.receipt_submit_failed));
                }
                hiddenLoadingDialog();
            }

            @Override
            public void onFailure(Throwable throwable) {
                hiddenLoadingDialog();
                MyLog.e(TAG, throwable.getMessage());
                btn_submit.setEnabled(true);
                MyToast.showShort(ReceiptActivity.this, resourceUtils.getStr(R.string.net_error));
            }
        });

    }


    @Override
    protected void onDestroy() {
        try {
            zkcManager.unbindService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tagInfoListSubscription != null && !tagInfoListSubscription.isUnsubscribed()) {
            tagInfoListSubscription.unsubscribe();
        }
        if (saveReceiptSubscription != null && !saveReceiptSubscription.isUnsubscribed()) {
            saveReceiptSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    /**
     * 跳转界面
     *
     * @param activity
     * @param getTagInfoRequest GetTagInfoRequest转化的json
     */
    public static void startAction(Activity activity, String getTagInfoRequest, String warehouseName) {
        Intent intent = new Intent(activity, ReceiptActivity.class);
        intent.putExtra("getTagInfoRequest", getTagInfoRequest);
        intent.putExtra("warehouseName", warehouseName);
        activity.startActivity(intent);
    }

    /**
     * startActionForResult
     *
     * @param activity
     * @param getTagInfoRequest
     * @param warehouseName
     */
    public static void startActionForResult(Activity activity, String getTagInfoRequest, String warehouseName) {
        Intent intent = new Intent(activity, ReceiptActivity.class);
        intent.putExtra("getTagInfoRequest", getTagInfoRequest);
        intent.putExtra("warehouseName", warehouseName);
        activity.startActivityForResult(intent, RECEIPT_REQUEST_CODE);
    }

}
