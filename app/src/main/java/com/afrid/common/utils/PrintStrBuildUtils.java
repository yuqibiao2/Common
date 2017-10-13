package com.afrid.common.utils;

import android.content.Context;
import android.content.res.Resources;

import com.afrid.common.R;
import com.afrid.common.bean.SubReceiptListBean;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * 功能：创建打印内容
 *
 * @author yu
 * @version 1.0
 * @date 2017/9/13
 */

public class PrintStrBuildUtils {

    /**
     * 生成收据
     *
     * @return
     */
    public static String buildReceipt(Context context , boolean isRepeat, String username, String warehouse, String receiptId
            , List<SubReceiptListBean> subReceiptListBeanList) {

        Resources resources = context.getResources();

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");
        DATE_FORMAT.setTimeZone(TIME_ZONE);
        String date = DATE_FORMAT.format(Calendar.getInstance(TIME_ZONE).getTime());
        sb.append("\n");
        if (!isRepeat){
            sb.append(resources.getString(R.string.receipt_tip)+"\n");
        }else{
            sb.append(resources.getString(R.string.receipt_tip_repeat)+"\n");
        }
        sb.append("\n");
        sb.append("\n");
        sb.append(resources.getString(R.string.receipt_id)+receiptId+"\n");
        sb.append("" + date+ "\n");
        sb.append("\n");
        sb.append(resources.getString(R.string.receipt_user) + username+ "\n");
        sb.append("\n");
        sb.append(resources.getString(R.string.receipt_warehouse) + warehouse + "\n");
        sb.append("\n");

        for (SubReceiptListBean bean : subReceiptListBeanList) {
            String tagTypeName = bean.getTagTypeName();
            sb.append(tagTypeName);
            for (int i = 0; i <8-tagTypeName.length() ; i++) {
                sb.append("　");
            }
            sb.append("----------x" + bean.getTagNum()+"\n");
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append(resources.getString(R.string.receipt_customer_sign)+"\n");
        sb.append("\n");
        sb.append("\n");
        sb.append(resources.getString(R.string.receipt_arfid));
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        return sb.toString();
    }


}
