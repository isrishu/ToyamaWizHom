package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.model.IRDevice;
import com.toyama.includes.model.NodeSwitch;
import com.toyama.includes.model.Room;
import com.toyama.includes.model.RoomNode;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ClimateActivity extends Activity {

    TextView lblActivityHeading;
    LinearLayout pnlClimateDevices;
    Room ThisRoom;
    String receivedMessage = "";
    ProgressDialog pDialog;

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_climate);

        try {
            lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
            pnlClimateDevices= (LinearLayout) findViewById(R.id.pnlClimateDevices);

            lblActivityHeading.setText("Climate ("+Globals.connectionMode+")");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void generateClimateDevices() {
        RelativeLayout.LayoutParams params;
        String idstr="";
        String tag="";
        View v;
        // ids as follows
        // 4 for label
        // 5 for image
        try {
            pnlClimateDevices.removeAllViews();
            for(IRDevice ird:Globals.climateDevices) {
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

                img.setImageDrawable(getResources().getDrawable(R.drawable.ac));
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Globals.currentIRDeviceId=Integer.valueOf(irdid);
                        Globals.currentIRDevice=Utilities.getClimateDevice(Globals.currentIRDeviceId);
                        if(Globals.currentIRDevice==null) return;
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        //Globals.isForScene=false;
                        Intent myInt=new Intent(getApplicationContext(),ACActivity.class);
                        ClimateActivity.this.startActivity(myInt);
                    }
                });

                img.setAdjustViewBounds(true);
                img.setMaxHeight((int) getPixelValue(55));
                img.setMaxWidth((int) getPixelValue(55));

                idstr=String.valueOf(ird.IRDeviceId)+"5"; // 1 for toggle button
                img.setId(Integer.valueOf(idstr));
                img.setTag(tag);

                pnl.addView(img);

                if(Globals.isForScene) {
                } else {

                }

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

                pnl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Globals.currentIRDeviceId=Integer.valueOf(irdid);
                        Globals.currentIRDevice=Utilities.getClimateDevice(Globals.currentIRDeviceId);
                        if(Globals.currentIRDevice==null) return;
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        //Globals.isForScene=false;
                        Intent myInt=new Intent(getApplicationContext(),ACActivity.class);
                        ClimateActivity.this.startActivity(myInt);
                    }
                });
                pnl.addView(v);
                pnlClimateDevices.addView(pnl);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            Log.d("Tog Error", ex.getMessage());
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
        //lblStatus.setText(recdmsg);

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
        Globals.climateDevices.clear();
        for(Room r:Globals.AllRooms) {
            for(RoomNode rn:r.RoomNodes) {
                if(rn.NodeType.equals("AHU")) {
                    IRDevice ird=new IRDevice();
                    ird.IRDeviceId=rn.RoomNodeId;
                    ird.Category=rn.NodeType;
                    ird.DeviceName=rn.NodeName;
                    ird.RoomId=r.RoomId;
                    ird.Location=r.RoomName;
                    ird.CustomValue=rn.NodeType;
                    Globals.climateDevices.add(ird);
                }
            }
        }
        for(IRDevice ird:Globals.AllIRDevices) {
            if(ird.Category.equals("AC")) {
                Globals.climateDevices.add(ird);
            }
        }
        if(Globals.climateDevices.size()==0) {
            showPopupMessage("No Devices found. Please click Refresh button in Settings page.");
        } else {
            if(Globals.connectionMode.equals("Local")) {
                connectToGateway();
            }
            generateClimateDevices();
        }
    }

    private void showPopupMessage(final String msg) {
        ClimateActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClimateActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showToast(final String msg,final int len) {
        ClimateActivity.this.runOnUiThread(new Runnable() {
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