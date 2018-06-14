package com.toyama.wizhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.toyama.includes.model.IRDevice;
import com.toyama.includes.model.IRIndex;
import com.toyama.includes.model.Room;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;
import com.toyama.wizhome.ACActivity.RemoteConnectTask;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SetTopBoxActivity extends Activity {

    ToggleButton togMute;
    TextView lblActivityHeading;
    ImageView imgPower,imgVolumeUp,imgVolumeDown,imgChannelUp,imgChannelDown,imgPlay,imgPause,imgStop,imgOk,imgCancel;
    GridLayout pnlNumberPad;
    Button btn0,btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9;

    ArrayList<IRIndex> IRIndices=new ArrayList<IRIndex>();
    String receivedMessage = "";

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set_top_box);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        imgPower=(ImageView) findViewById(R.id.ibtnPower);
        togMute=(ToggleButton) findViewById(R.id.togMute);
        imgVolumeUp=(ImageView) findViewById(R.id.ibtnVolumeUp);
        imgVolumeDown=(ImageView) findViewById(R.id.ibtnVolumeDown);
        imgChannelUp=(ImageView) findViewById(R.id.ibtnChannelUp);
        imgChannelDown=(ImageView) findViewById(R.id.ibtnChannelDown);
        imgPlay=(ImageView) findViewById(R.id.ibtnPlay);
        imgPause=(ImageView) findViewById(R.id.ibtnPause);
        imgStop=(ImageView) findViewById(R.id.ibtnStop);
        imgOk=(ImageView) findViewById(R.id.ibtnOk);
        imgCancel=(ImageView) findViewById(R.id.ibtnCancel);
        pnlNumberPad=(GridLayout) findViewById(R.id.pnlNumberPad);
        btn0=(Button) findViewById(R.id.btn0);
        btn1=(Button) findViewById(R.id.btn1);
        btn2=(Button) findViewById(R.id.btn2);
        btn3=(Button) findViewById(R.id.btn3);
        btn4=(Button) findViewById(R.id.btn4);
        btn5=(Button) findViewById(R.id.btn5);
        btn6=(Button) findViewById(R.id.btn6);
        btn7=(Button) findViewById(R.id.btn7);
        btn8=(Button) findViewById(R.id.btn8);
        btn9=(Button) findViewById(R.id.btn9);

        lblActivityHeading.setText(Globals.currentIRDevice.DeviceName+" ("+Globals.connectionMode+")");

        imgPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Power",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        togMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Mute",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgVolumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("VolumeUp",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgVolumeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("VolumeDown",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgChannelUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("ChannelUp",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgChannelDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("ChannelDown",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Play",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Pause",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Stop",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Ok",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Cancel",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",3);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",4);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",5);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",6);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",7);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",8);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Number",9);
                } catch (Exception ex) {
                    ex.printStackTrace();
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
        if(rb.equals("Number")) {
            for(IRIndex iri:IRIndices) {
                if(iri.RemoteButton.equals(rb) && iri.Value==t) {
                    return iri;
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
        } else Toast.makeText(getApplicationContext(), "\n"+rb+" command NOT found\n", Toast.LENGTH_SHORT).show();
    }

    private void sendToGateway(String msg) {
        if(gatewayConnectThread==null) {
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
    }

    private void showPopupMessage(final String msg) {
        SetTopBoxActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SetTopBoxActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
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
                    showToast("Command Sent to Server",Toast.LENGTH_SHORT);
                } else {
                    showToast("Could not connect to Remote Server, pls check your internet.",Toast.LENGTH_SHORT);
                }
            } else {
                if(message.equals("1")) {
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

    private void showToast(final String msg,final int len) {
        SetTopBoxActivity.this.runOnUiThread(new Runnable() {
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