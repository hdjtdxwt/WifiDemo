package com.epsit.wifidemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;


import com.epsit.wifidemo.R;

/**
 * wifi输入密码框并连接的dialog
 */

public class InputAndConnectDialog extends Dialog {

    public InputAndConnectDialog(Context context) {
        super(context);
    }

    public InputAndConnectDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {

        private Context context;

        public Builder(Context context) {
            this.context = context;
        }
        OnConnectionListener listener;
        InputAndConnectDialog dialog;
        String name;
        public OnConnectionListener getListener() {
            return listener;
        }

        public Builder setListener(OnConnectionListener listener) {
            this.listener = listener;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public InputAndConnectDialog create() {
            LayoutInflater inflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dialog = new InputAndConnectDialog(context, R.style.dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            View layout = inflater.inflate(R.layout.input_connect_dialog, null);

            final EditText editText = (EditText) layout.findViewById(R.id.password);
            TextView nameText = (TextView) layout.findViewById(R.id.name);
            layout.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(dialog!=null){
                        dialog.cancel();
                    }
                }
            });
            if(!TextUtils.isEmpty(name)){
                nameText.setText(name);
            }
            layout.findViewById(R.id.connect).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(dialog!=null){
                        dialog.cancel();
                    }
                    String passwrod = editText.getText().toString();
                    if(!TextUtils.isEmpty(passwrod) && listener!=null){
                        listener.onConnectionListener(passwrod);
                        Log.e("connect","点击了连接，password和listener都不是null");
                    }else{
                        Log.e("connect","点击了连接，"+(!TextUtils.isEmpty(passwrod))+"  "+(listener!=null));
                    }
                }
            });
            dialog.addContentView(layout, new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            return dialog;
        }

    }

    //连接按钮点击了
    public interface OnConnectionListener{
        void onConnectionListener(String password);
    }

}

