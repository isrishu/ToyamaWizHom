package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.model.IRDevice;
import com.toyama.includes.model.Room;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MediaActivity extends Activity {

    TextView lblActivityHeading;
    LinearLayout pnlMediaDevices;
    Room ThisRoom;
    String receivedMessage = "";
    ProgressDialog pDialog;

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_media);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        pnlMediaDevices= (LinearLayout) findViewById(R.id.pnlMediaDevices);

        lblActivityHeading.setText("Media ("+Globals.connectionMode+")");
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void generateMediaDevices() {
        RelativeLayout.LayoutParams params;
        String idstr="";
        String tag="";
        View v;
        // ids as follows
        // 4 for label
        // 5 for image
        try {
            pnlMediaDevices.removeAllViews();
            for(final IRDevice ird:Globals.AllIRDevices) {
                if(ird.Category.equals("AC")) continue; // media page so not listing ACs
                final String sn=Utilities.toTitleCase(ird.DeviceName);
                //final String sc=ns.Category;
                final String irdid=String.valueOf(ird.IRDeviceId);
                final RelativeLayout pnl=new RelativeLayout(this);

                params= new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(75));
                //LayoutParams.WRAP_CONTENT);
                pnl.setLayoutParams(params);
                pnl.setBackgroundColor(getResources().getColor(R.color.transparent));
                pnl.setFocusableInTouchMode(false);

                final ImageView img = (ImageView) getLayoutInflater().inflate(R.layout.switch_image_template, null);
                params= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                params.setMargins((int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10));
                img.setLayoutParams(params);

                tag=String.valueOf(ird.IRDeviceId);

                switch(ird.Category) {
                    case "TV":
                        img.setImageDrawable(getResources().getDrawable(R.drawable.entertainment));
                        break;
                    case "Projector":
                        img.setImageDrawable(getResources().getDrawable(R.drawable.projector));
                        break;
                    case "Music System":
                        img.setImageDrawable(getResources().getDrawable(R.drawable.music));
                        break;
                    case "Set Top Box":
                        img.setImageDrawable(getResources().getDrawable(R.drawable.set_top_box));
                        break;
                }

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Globals.currentIRDeviceId=Integer.valueOf(irdid);
                        Globals.currentIRDevice=Utilities.getIRDevice(Globals.currentIRDeviceId);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        //Globals.isForScene=false;
                        Intent myInt=null;
                        switch(ird.Category) {
                            case "TV":
                                myInt=new Intent(getApplicationContext(),TVActivity.class);
                                break;
                            case "Projector":
                                myInt=new Intent(getApplicationContext(),ProjectorActivity.class);
                                break;
                            case "Music System":
                                myInt=new Intent(getApplicationContext(),TVActivity.class);
                                break;
                            case "Set Top Box":
                                myInt=new Intent(getApplicationContext(),SetTopBoxActivity.class);
                                break;
                        }
                        if(myInt!=null)
                            MediaActivity.this.startActivity(myInt);
                    }
                });

                pnl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Globals.currentIRDeviceId=Integer.valueOf(irdid);
                        Globals.currentIRDevice=Utilities.getIRDevice(Globals.currentIRDeviceId);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        //Globals.isForScene=false;
                        Intent myInt=null;
                        switch(ird.Category) {
                            case "TV":
                                myInt=new Intent(getApplicationContext(),TVActivity.class);
                                break;
                            case "Projector":
                                myInt=new Intent(getApplicationContext(),ProjectorActivity.class);
                                break;
                            case "Music System":
                                myInt=new Intent(getApplicationContext(),TVActivity.class);
                                break;
                            case "Set Top Box":
                                myInt=new Intent(getApplicationContext(),SetTopBoxActivity.class);
                                break;
                        }
                        if(myInt!=null)
                            MediaActivity.this.startActivity(myInt);
                    }
                });

                img.setAdjustViewBounds(true);
                img.setMaxHeight((int) getPixelValue(55));
                img.setMaxWidth((int) getPixelValue(55));

                idstr=String.valueOf(ird.IRDeviceId)+"5"; // 1 for toggle button
                img.setId(Integer.valueOf(idstr));
                img.setTag(tag);

                pnl.addView(img);

                final TextView olbl=(TextView) getLayoutInflater().inflate(R.layout.switch_label_template, null);
                params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.RIGHT_OF,img.getId());
                params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                params.setMargins((int) getPixelValue(5),0,0,0);
                olbl.setLayoutParams(params);

                idstr=String.valueOf(ird.IRDeviceId)+"4"; // 4 for label
                olbl.setId(Integer.valueOf(idstr));

                olbl.setText(ird.toString());

                pnl.addView(olbl);

                v=new View(this);
                params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(3));
                params.setMargins((int) getPixelValue(7), 0,(int) getPixelValue(7), 0);
                v.setLayoutParams(params);
                v.setBackground(getResources().getDrawable(R.drawable.line_lightgray));
                pnl.addView(v);
                pnlMediaDevices.addView(pnl);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage(String msg) {
        if(gatewayConnectThread==null){
            return;
        }
        gatewayConnectThread.sendMessage(msg);
    }

    private void connectToGateway() {
        receivedMessage = "";
        gatewayConnectThread = new GatewayConnectThread();
        gatewayConnectThread.start();
    }

    private class GatewayConnectThread extends Thread {
        String messageToSend = "";
        boolean goOut = false;

        GatewayConnectThread() {
        }

        @Override
        public void run() {
            while (!goOut) {
                try {
                    if(Globals.dataInputStream==null || Globals.dataOutputStream==null) {
                        // app probably crashed
                        messageToSend="PermissionToConnect_"+Globals.customerUsername+"_"+Globals.password+"_"+Globals.dataTransferMode;
                        Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
                        Globals.socket = new Socket();
                        Globals.socket.connect(Globals.serverSocketAddress,200);
                        Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
                        Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
                        Globals.dataOutputStream.writeUTF(messageToSend);
                        Globals.dataOutputStream.flush();
                        messageToSend="";
                    }
                    if(!messageToSend.equals("")){
                        Globals.dataOutputStream.writeUTF(messageToSend);
                        Globals.dataOutputStream.flush();
                        messageToSend = "";
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                } catch(Exception e) {
                    showToast(Globals.unexpectedErrorMessage,Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            }
        }

        private void sendMessage(String msg){
            messageToSend = msg;
        }

        private void disconnect(){
            goOut = true;
        }
    }

    @Override
    protected void onPause() {
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //Globals.isForScene=false;
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Globals.AllIRDevices.size()==0) {
            //showPopupMessage("No Devices found. Please click Refresh button in Settings page.");
        } else {
            if(Globals.connectionMode.equals("Local")) {
                connectToGateway();
            }
            generateMediaDevices();
        }
    }

    private void showPopupMessage(final String msg) {
        MediaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MediaActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showToast(final String msg,final int len) {
        MediaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(toast==null) {
                    toast=Toast.makeText(getApplicationContext(),msg,len);
                    toast.show();
                }
            }
        });
    }
}