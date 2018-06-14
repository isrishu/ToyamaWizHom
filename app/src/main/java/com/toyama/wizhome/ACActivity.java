package com.toyama.wizhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.toyama.includes.model.IRIndex;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ACActivity extends Activity {
    TextView lblActivityHeading,lblSetTemparature,lblRoomTemparature,lblFanSpeed;
    ImageView btnOn,btnOff,btnLow,btnMedium,btnHigh,btnTemparatureUp,btnTemparatureDown;

    ArrayList<IRIndex> IRIndices=new ArrayList<IRIndex>();
    String receivedMessage = "";
    int setTemparature=0,roomTemparature=0,minTemparature=14,maxTemparature=31;
    GatewayConnectThread gatewayConnectThread = null;
    boolean isDisplayOn=false;
    Toast toast=null;
    boolean isOnOffOperated=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ac);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        lblSetTemparature = (TextView) findViewById(R.id.lblSetTemparature);
        lblRoomTemparature = (TextView) findViewById(R.id.lblRoomTemparature);
        lblFanSpeed = (TextView) findViewById(R.id.lblFanSpeed);

        btnOn=(ImageView) findViewById(R.id.btnOn);
        btnOff=(ImageView) findViewById(R.id.btnOff);
        btnLow=(ImageView) findViewById(R.id.btnLow);
        btnMedium=(ImageView) findViewById(R.id.btnMedium);
        btnHigh=(ImageView) findViewById(R.id.btnHigh);
        btnTemparatureUp=(ImageView) findViewById(R.id.btnTemparatureUp);
        btnTemparatureDown=(ImageView) findViewById(R.id.btnTemparatureDown);

        if(Globals.currentIRDevice.Category.equals("AHU")) {
            lblRoomTemparature.setVisibility(View.VISIBLE);
            minTemparature=14;
            maxTemparature=31;
        } else {
            lblRoomTemparature.setVisibility(View.GONE);
            minTemparature=18;
            maxTemparature=30;
        }
        setTemparature=minTemparature;
        lblSetTemparature.setText(String.valueOf(setTemparature));
        lblActivityHeading.setText(Globals.currentIRDevice.DeviceName+" ("+Globals.connectionMode+")");

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.currentIRDevice.Category.equals("AHU")) {
                        if(Globals.connectionMode.equals("Local")) {
                            if(!isDisplayOn) {
                                isOnOffOperated=true;
                                trySendAHUCommand(Globals.currentIRDeviceId,5,10);
                            }
                        } else {
                            isDisplayOn=false;
                            isOnOffOperated=true;
                            trySendAHUCommand(Globals.currentIRDeviceId,5,10);
                        }
                    } else {
                        trySendIRCommand("PowerOn",0);
                    }
                } catch (Exception ex) {
                    String ex1="";
                    for(StackTraceElement a1:ex.getStackTrace()) {
                        ex1+=a1.toString();
                    }
                    //showPopupMessage("Error: "+ex1);
                }
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.currentIRDevice.Category.equals("AHU")) {
                        if(Globals.connectionMode.equals("Local")) {
                            if(isDisplayOn) {
                                isOnOffOperated=true;
                                trySendAHUCommand(Globals.currentIRDeviceId,6,10);
                            }
                        } else {
                            isDisplayOn=true;
                            isOnOffOperated=true;
                            trySendAHUCommand(Globals.currentIRDeviceId,6,10);
                        }
                    } else {
                        trySendIRCommand("PowerOff",0);
                    }
                } catch (Exception ex) {
                    String ex1="";
                    for(StackTraceElement a1:ex.getStackTrace()) {
                        ex1+=a1.toString();
                    }
                }
            }
        });

        btnLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.currentIRDevice.Category.equals("AHU")) {
                        if(Globals.connectionMode.equals("Local")) {
                            if(isDisplayOn)
                                trySendAHUCommand(Globals.currentIRDeviceId,1,20);
                        } else {
                            trySendAHUCommand(Globals.currentIRDeviceId,1,20);
                        }
                    } else {
                        trySendIRCommand("FanLow",0);
                    }
                } catch (Exception ex) {
                    String ex1="";
                    for(StackTraceElement a1:ex.getStackTrace()) {
                        ex1+=a1.toString();
                    }
                }
            }
        });

        btnMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.currentIRDevice.Category.equals("AHU")) {
                        if(Globals.connectionMode.equals("Local")) {
                            if(isDisplayOn)
                                trySendAHUCommand(Globals.currentIRDeviceId,2,20);
                        } else {
                            trySendAHUCommand(Globals.currentIRDeviceId,2,20);
                        }
                    } else {
                        trySendIRCommand("FanMedium",0);
                    }
                } catch (Exception ex) {
                    String ex1="";
                    for(StackTraceElement a1:ex.getStackTrace()) {
                        ex1+=a1.toString();
                    }
                }
            }
        });

        btnHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.currentIRDevice.Category.equals("AHU")) {
                        if(Globals.connectionMode.equals("Local")) {
                            if(isDisplayOn)
                                trySendAHUCommand(Globals.currentIRDeviceId,3,20);
                        } else {
                            trySendAHUCommand(Globals.currentIRDeviceId,3,20);
                        }
                    } else {
                        trySendIRCommand("FanHigh",0);
                    }
                } catch (Exception ex) {
                    String ex1="";
                    for(StackTraceElement a1:ex.getStackTrace()) {
                        ex1+=a1.toString();
                    }
                }
            }
        });

        btnTemparatureUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.connectionMode.equals("Local")) {
                        if(Globals.currentIRDevice.Category.equals("AHU") && !isDisplayOn)
                            return;
                        if(setTemparature<maxTemparature) {
                            setTemparature++;
                        }
                        lblSetTemparature.setText(String.valueOf(setTemparature));
                        if(Globals.currentIRDevice.Category.equals("AHU")) {
                            trySendAHUCommand(Globals.currentIRDeviceId,4,10+(setTemparature-minTemparature)*10);
                        } else {
                            trySendIRCommand("Temparature",setTemparature);
                        }
                    } else {
                        if(setTemparature<maxTemparature) {
                            setTemparature++;
                        }
                        lblSetTemparature.setText(String.valueOf(setTemparature));
                        if(Globals.currentIRDevice.Category.equals("AHU")) {
                            trySendAHUCommand(Globals.currentIRDeviceId,4,10+(setTemparature-minTemparature)*10);
                        } else {
                            trySendIRCommand("Temparature",setTemparature);
                        }
                    }

                } catch (Exception ex) {
                    String ex1="";
                    for(StackTraceElement a1:ex.getStackTrace()) {
                        ex1+=a1.toString();
                    }
                }
            }
        });

        btnTemparatureDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.connectionMode.equals("Local")) {
                        if(Globals.currentIRDevice.Category.equals("AHU") && !isDisplayOn)
                            return;
                        if(setTemparature>minTemparature) {
                            setTemparature--;
                        }
                        lblSetTemparature.setText(String.valueOf(setTemparature));
                        if(Globals.currentIRDevice.Category.equals("AHU")) {
                            trySendAHUCommand(Globals.currentIRDeviceId,4,10+(setTemparature-minTemparature)*10);
                        } else {
                            trySendIRCommand("Temparature",setTemparature);
                        }
                    } else {
                        if(setTemparature>minTemparature) {
                            setTemparature--;
                        }
                        lblSetTemparature.setText(String.valueOf(setTemparature));
                        if(Globals.currentIRDevice.Category.equals("AHU")) {
                            trySendAHUCommand(Globals.currentIRDeviceId,4,10+(setTemparature-minTemparature)*10);
                        } else {
                            trySendIRCommand("Temparature",setTemparature);
                        }
                    }
                } catch (Exception ex) {
                    String ex1="";
                    for(StackTraceElement a1:ex.getStackTrace()) {
                        ex1+=a1.toString();
                    }
                }
            }
        });
    }

    private void filterIRIndices() {
        IRIndices=Utilities.filterIRIndices();
    }

    private IRIndex findIRIndex(String rb,int t) {
        if(rb.equals("")) {
            return null;
        }
        if(rb.equals("Temparature")) {
            if(t==0) {
                return null;
            } else {
                for(IRIndex iri:IRIndices) {
                    if(iri.RemoteButton.equals(rb) && iri.Value==t) {
                        return iri;
                    }
                }
            }
        } else {
            for(IRIndex iri:IRIndices) {
                if(iri.RemoteButton.equals(rb)) {
                    return iri;
                }
            }
        }
        return null;
    }

    private void trySendIRCommand(String rb,int t) {
        final IRIndex iri=findIRIndex(rb, t);
        if(iri!=null) {
            if(Globals.connectionMode.equals("Local")) {
                sendToGateway("SendIR_"+String.valueOf(iri.IRIndexId));
            } else {
                remoteTask=new RemoteConnectTask();
                String[] params={"sendIR",String.valueOf(iri.IRIndexId)+"_key_value"};
                remoteTask.execute(params);
            }
        } else
            showToast(rb+" command NOT found",Toast.LENGTH_SHORT);
    }

    private void trySendAHUCommand(int irdid,int sn,int percent) {
        try {
            int nsid = Utilities.getNodeSwitchId(irdid,sn);
            String toggleString="ToggleSwitch_"+String.valueOf(nsid)+"_"+String.valueOf(percent);
            if(Globals.connectionMode.equals("Local")) {
                sendToGateway(toggleString);
            } else {
                remoteTask=new RemoteConnectTask();
                String[] params={"toggleSwitch",String.valueOf(nsid)+"_"+String.valueOf(percent)};
                remoteTask.execute(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToGateway(String msg) {
        if(gatewayConnectThread==null) {
            return;
        }
        gatewayConnectThread.sendMessage(msg);
    }

    private void connectToGateway() {
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
                        Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
                        Globals.socket = new Socket();
                        Globals.socket.connect(Globals.serverSocketAddress,200);
                        Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
                        Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
                        Globals.dataOutputStream.writeUTF(Globals.loginMessageToSend);
                        Globals.dataOutputStream.flush();
                    }
                    if (Globals.dataInputStream.available() > 0) {
                        receivedMessage = Globals.dataInputStream.readUTF();
                        ACActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(Globals.currentIRDevice.Category.equals("AHU") && receivedMessage.startsWith("AHUStatus_")) {
                                    String x[] = receivedMessage.split("_");
                                    if(Globals.currentIRDevice.IRDeviceId==Integer.valueOf(x[1])) {
                                        lblFanSpeed.setText("Fan Speed: "+x[3]);
                                        lblSetTemparature.setText(x[4]);
                                        setTemparature=Integer.valueOf(x[4]);
                                        lblRoomTemparature.setText(x[5]);
                                        if(x[6].equals("On")) {
                                            btnOff.setEnabled(true);
                                            btnOff.setAlpha(1f);
                                            btnOn.setEnabled(false);
                                            btnOn.setAlpha(.5f);
                                            isDisplayOn=true;
                                        } else {
                                            btnOff.setEnabled(false);
                                            btnOff.setAlpha(.5f);
                                            btnOn.setEnabled(true);
                                            btnOn.setAlpha(1f);
                                            isDisplayOn=false;
                                        }
                                    }
                                }
                                if(receivedMessage.contains("PermissionResponse")) {
                                    String[] x=receivedMessage.split("_");
                                    if(x.length>1) {
                                        int responseCode=Integer.parseInt(x[1]);
                                        if(responseCode!=1) {
                                            showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                                        }
                                    }
                                }
                            }
                        });
                    }
                    if(!messageToSend.equals("")) {
                        Globals.dataOutputStream.writeUTF(messageToSend);
                        Globals.dataOutputStream.flush();
                        messageToSend = "";
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    showToast("Reconnecting to gateway...",Toast.LENGTH_LONG);
                    try {
                        Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
                        Globals.socket = new Socket();
                        Globals.socket.connect(Globals.serverSocketAddress,200);
                        Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
                        Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
                        Globals.dataOutputStream.writeUTF(Globals.loginMessageToSend);
                        Globals.dataOutputStream.flush();
                    } catch (IOException ex) {
                        showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    showToast("Reconnecting to gateway...",Toast.LENGTH_LONG);
                    try {
                        Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
                        Globals.socket = new Socket();
                        Globals.socket.connect(Globals.serverSocketAddress,200);
                        Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
                        Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
                        Globals.dataOutputStream.writeUTF(Globals.loginMessageToSend);
                        Globals.dataOutputStream.flush();
                    } catch (IOException ex) {
                        showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(Globals.unexpectedErrorMessage,Toast.LENGTH_SHORT);
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
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Globals.connectionMode.equals("Local")) {
            connectToGateway();
        }
        filterIRIndices();
        if(!Globals.isForScene)
            if(Globals.connectionMode.equals("Local"))
                sendToGateway("QueryAHU_"+Globals.currentIRDeviceId);
            else {
                remoteTask=new RemoteConnectTask();
                String[] params={"queryAHU",String.valueOf(Globals.currentIRDeviceId)};
                remoteTask.execute(params);
            }
    }

    private void showPopupMessage(final String msg) {
        ACActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ACActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showToast(final String msg,final int len) {
        ACActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(toast==null) {
                    toast=Toast.makeText(getApplicationContext(),msg,len);
                    toast.show();
                }
            }
        });
    }

    RemoteConnectTask remoteTask=null;
    public class RemoteConnectTask extends AsyncTask<String, Integer, String> {
        ServiceLayer svc;

        public RemoteConnectTask() {
            //svc=new ServiceLayer();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                svc=new ServiceLayer();
                String mode=params[0];
                String remoteParamString=params[1];
                switch(mode) {
                    case "sendIR":
                        if(svc.sendIR(remoteParamString)) {
                            return svc.getResponseString();
                        }
                        break;
                    case "toggleSwitch":
                        if(svc.toggleSwitch(remoteParamString)) {
                            return svc.getResponseString();
                        }
                        break;
                    case "queryAHU":
                        if(svc.queryAHU(remoteParamString)) {
                            return svc.getResponseString();
                        }
                        break;
                }
            } catch(Exception ex) {
                return ex.getStackTrace()[0].toString();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String message) {
            remoteTask = null;
            if(message.contains("#")) {
                String[] responses=message.split("#");
                if(responses[0].equals("1")) {
                    if(responses[1].startsWith("AHUStatus")) {
                        try {
                            String x[] = responses[1].split("_");
                            if(Globals.currentIRDevice.IRDeviceId==Integer.valueOf(x[1])) {
                                lblFanSpeed.setText("Fan Speed: "+x[3]);
                                lblSetTemparature.setText(x[4]);
                                setTemparature=Integer.valueOf(x[4]);
                                lblRoomTemparature.setText(x[5]);
                                if(x[6].equals("On")) {
                                    btnOff.setEnabled(true);
                                    btnOff.setAlpha(1f);
                                    btnOn.setEnabled(false);
                                    btnOn.setAlpha(.5f);
                                    isDisplayOn=true;
                                } else {
                                    btnOff.setEnabled(false);
                                    btnOff.setAlpha(.5f);
                                    btnOn.setEnabled(true);
                                    btnOn.setAlpha(1f);
                                    isDisplayOn=false;
                                }
                            }
                        } catch(Exception ex) {

                        }
                    }
                    if(Globals.currentIRDevice.Category.equals("AHU") && isOnOffOperated) {
                        isOnOffOperated=false;
                        if(isDisplayOn) {
                            btnOff.setEnabled(false);
                            btnOff.setAlpha(.5f);
                            btnOn.setEnabled(true);
                            btnOn.setAlpha(1f);
                            isDisplayOn=false;
                        } else {
                            btnOff.setEnabled(true);
                            btnOff.setAlpha(1f);
                            btnOn.setEnabled(false);
                            btnOn.setAlpha(.5f);
                            isDisplayOn=true;
                        }
                    }
                    showToast("Command Sent to Server",Toast.LENGTH_SHORT);
                } else {
                    showToast("Could not connect to Remote Server, pls check your internet.",Toast.LENGTH_SHORT);
                }
            } else {
                if(message.equals("1")) {
                    if(Globals.currentIRDevice.Category.equals("AHU") && isOnOffOperated) {
                        isOnOffOperated=false;
                        if(isDisplayOn) {
                            btnOff.setEnabled(false);
                            btnOff.setAlpha(.5f);
                            btnOn.setEnabled(true);
                            btnOn.setAlpha(1f);
                            isDisplayOn=false;
                        } else {
                            btnOff.setEnabled(true);
                            btnOff.setAlpha(1f);
                            btnOn.setEnabled(false);
                            btnOn.setAlpha(.5f);
                            isDisplayOn=true;
                        }
                    }
                    showToast("Command Sent to Server",Toast.LENGTH_SHORT);
                } else {
                    showToast("Could not connect to Remote Server, pls check your internet.",Toast.LENGTH_SHORT);
                }
            }
            svc.close();
        }

        @Override
        protected void onCancelled() {
            remoteTask = null;
        }
    }
}