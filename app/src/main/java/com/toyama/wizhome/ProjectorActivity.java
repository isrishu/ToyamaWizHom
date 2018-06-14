package com.toyama.wizhome;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ProjectorActivity extends Activity {

    ToggleButton togMute;
    TextView lblActivityHeading;
    ImageView imgPower,imgVolumeUp,imgVolumeDown,imgCommit,imgInput,imgMenu;
    ImageView imgKeystoneUp,imgKeystoneDown,imgAutoS,imgFreeze,imgResizeUp,imgResizeDown;

    ArrayList<IRIndex> IRIndices=new ArrayList<IRIndex>();
    String receivedMessage = "";

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_projector);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        togMute=(ToggleButton) findViewById(R.id.togMute);
        imgPower=(ImageView) findViewById(R.id.ibtnPower);
        imgVolumeUp=(ImageView) findViewById(R.id.ibtnVolumeUp);
        imgVolumeDown=(ImageView) findViewById(R.id.ibtnVolumeDown);
        imgCommit=(ImageView) findViewById(R.id.ibtnOk);
        imgInput=(ImageView) findViewById(R.id.ibtnInput);
        imgMenu=(ImageView) findViewById(R.id.ibtnMenu);
        imgKeystoneUp=(ImageView) findViewById(R.id.ibtnKeyStoneUp);
        imgKeystoneDown=(ImageView) findViewById(R.id.ibtnKeyStoneDown);
        imgAutoS=(ImageView) findViewById(R.id.ibtnAutoS);
        imgFreeze=(ImageView) findViewById(R.id.ibtnFreeze);
        imgResizeUp=(ImageView) findViewById(R.id.ibtnResizeUp);
        imgResizeDown=(ImageView) findViewById(R.id.ibtnResizeDown);

        lblActivityHeading.setText(Globals.currentIRDevice.DeviceName+" ("+Globals.connectionMode+")");

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

        imgCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Commit",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Input",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Menu",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgKeystoneUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("KeyStoneUp",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgKeystoneDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("KeyStoneDown",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgAutoS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("AutoS",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgFreeze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("Freeze",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgResizeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("ResizeUp",0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        imgResizeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trySendIRCommand("ResizeDown",0);
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
        ProjectorActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProjectorActivity.this);
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
        ProjectorActivity.this.runOnUiThread(new Runnable() {
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