package com.epsit.wifidemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.epsit.wifidemo.R;

/**
 * Created by Administrator on 2018/3/8/008.
 */

public class SelectItemDialog extends Dialog {
    private SelectItemDialog(@NonNull Context context) {
        super(context);
    }
    private SelectItemDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        public Builder(Context context) {
            this.context = context;
        }
        SelectItemDialog dialog;
        String showText;
        OnSelectDialogListener onSelectDialogListener;

        public OnSelectDialogListener getOnSelectDialogListener() {
            return onSelectDialogListener;
        }

        public void setOnSelectDialogListener(OnSelectDialogListener onSelectDialogListener) {
            this.onSelectDialogListener = onSelectDialogListener;
        }

        public SelectItemDialog create() {
            LayoutInflater inflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dialog = new SelectItemDialog(context, R.style.dialog);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            View layout = inflater.inflate(R.layout.select_item_dialog, null);
            TextView text = (TextView) layout.findViewById(R.id.name);
            if(!TextUtils.isEmpty(showText)){
                text.setText(showText);
            }
            layout.findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("onClick","R.id.connect点击了");
                    if(onSelectDialogListener!=null){
                        Log.e("onClick","R.id.connect点击了  onClickListener 回调了");
                        onSelectDialogListener.onClickSelectedListener(v,1);
                    }
                    if(dialog!=null){
                        dialog.cancel();
                    }
                }
            });
            layout.findViewById(R.id.cancel_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onSelectDialogListener!=null){
                        onSelectDialogListener.onClickSelectedListener(v,2);
                    }
                    if(dialog!=null){
                        dialog.cancel();
                    }
                }
            });
            /*layout.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onSelectDialogListener!=null){
                        onSelectDialogListener.onClickSelectedListener(v,3);
                    }
                    if(dialog!=null){
                        dialog.cancel();
                    }
                }
            });*/
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            return dialog;
        }

        public void setShowText(String showText) {
            this.showText = showText;
        }
    }

    public interface OnSelectDialogListener {
        //type:1  连接到网络，   type:2 取消保存   3：修改网络
        void onClickSelectedListener(View view,int type);
    }

}
