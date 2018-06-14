package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.model.Scene;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SceneActivity extends Activity {
    EditText txtSceneName;
    Button btnSaveScene;

    String receivedMessage = "";
    ProgressDialog pDialog;

    RemoteAsyncTask remoteTask=null;
    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scene);

        txtSceneName=(EditText) findViewById(R.id.txtSceneName);

        if(Globals.CurrentScene!=null) {
            txtSceneName.setText(Globals.CurrentScene.SceneName);
        }
        Button btn=(Button) findViewById(R.id.btnProceed);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try	{
                    Globals.isSceneSaved=false;
                    String sName=txtSceneName.getText().toString();
                    if(sName.trim().equals("")) {
                        showPopupMessage("Please give a Scene Name","");
                        return;
                    }
                    sName=sName.replace("\n","");
                    if(Globals.CurrentScene==null) { // new scene
                        Globals.CurrentScene=new Scene();
                        Globals.CurrentScene.SceneName=sName;
                    } else { //edit scene
                        Globals.CurrentScene.SceneName=sName;
                    }
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    Intent myInt=new Intent(getApplicationContext(),RoomsActivity.class);
                    SceneActivity.this.startActivity(myInt);
                }
                catch(Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSaveScene=(Button) findViewById(R.id.btnSaveScene);
        btnSaveScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try	{
                    if(Globals.CurrentScene==null) { // new scene
                        showPopupMessage("No scene to save","Please select a scene");
                        return;
                    } else { // save the scene
                        pDialog = ProgressDialog.show(SceneActivity.this, "Saving Scene", "Please wait...", true);
                        pDialog.setCanceledOnTouchOutside(true);
                        pDialog.setOwnerActivity(SceneActivity.this);
                        Globals.isSceneSaved=false;
                        String ss=Utilities.buildSceneString(Globals.CurrentScene);
                        sendToGateway("SaveScene_"+ss);
                    }
                }
                catch(Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendToGateway(String msg) {
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
                    if (Globals.dataInputStream.available() > 0) {
                        receivedMessage = Globals.dataInputStream.readUTF().trim();
                        if(receivedMessage.equals("")) {
                            break;
                        }
                        //showMessage(receivedMessage);
                        if(receivedMessage.contains("SaveSceneResponse")) {
                            String x[]=receivedMessage.split("_");
                            if(Integer.valueOf(x[1])==1) {
                                Globals.isSceneSaved=true;
                                if(Globals.isSceneEditing) {
                                    Globals.isSceneEditing=false;
                                } else {
                                    Globals.CurrentScene.SceneId=Integer.valueOf(x[2]);
                                    if(!Globals.AllScenes.contains(Globals.CurrentScene))
                                        Globals.AllScenes.add(Globals.CurrentScene);
                                }
                                Globals.scenesString=Utilities.generateAllScenesString();
                                showPopupMessage("Scene Saved","Touch outside to dismiss this dialog.");
                                Utilities.writeDataToFile(getApplicationContext());
                                SceneActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btnSaveScene.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                showPopupMessage("Scene not Saved. Please try again.","Touch outside to dismiss this dialog.");
                            }
                        }
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
    protected void onDestroy() {
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onDestroy();
    }

    @Override
    public boolean isFinishing() {
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        return true;
    }

    @Override
    protected void onPause() {
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(!Globals.isSceneSaved) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SceneActivity.this);
            alertDialog.setTitle("Scene not saved");
            alertDialog.setMessage("Current working scene is not saved. If you proceed you will lose this data. Are you sure?");

            alertDialog.setPositiveButton("Lose Data", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    //dialog.cancel();
                    Globals.isSceneSaved=true;
                }
            });

            alertDialog.setNegativeButton("Stay Here", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //dialog.cancel();
                    return;
                }
            });
            alertDialog.show();
        }
        if(Globals.isSceneSaved)
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Globals.isSceneSaved) {
            btnSaveScene.setVisibility(View.GONE);
        } else {
            btnSaveScene.setVisibility(View.VISIBLE);
        }
        if(Globals.connectionMode.equals("Local")) {
            connectToGateway();
        }
    }

    public class RemoteAsyncTask extends AsyncTask<String, Integer, String> {
        ServiceLayer svc;

        public RemoteAsyncTask() {
            //svc=new ServiceLayer();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                svc=new ServiceLayer();
                String mode=params[0];
                String paramstr=params[1];
                switch(mode) {
                    case "applyScene" :

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
                    showPopupMessage("Command Sent to Server","Touch outside to dismiss this dialog.");
                } else {
                    showPopupMessage("Could not connect to Remote Server, pls check your internet.","Touch outside to dismiss this dialog.");
                }
            } else {
                if(message.equals("1")) {
                    showPopupMessage("Command Sent to Server","Touch outside to dismiss this dialog.");
                } else {
                    showPopupMessage("Could not connect to Remote Server, pls check your internet.","Touch outside to dismiss this dialog.");
                }
            }
            svc.close();
        }

        @Override
        protected void onCancelled() {
            remoteTask = null;
        }
    }

    private void showPopupMessage(final String msg,final String title) {
        SceneActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(pDialog!=null && pDialog.isShowing()) {
                    pDialog.setTitle(msg);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SceneActivity.this);
                    builder.setMessage(msg);
                    builder.setTitle(title);
                    builder.setCancelable(true);

                    builder.setPositiveButton("Ok",null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    private void showToast(final String msg,final int len) {
        SceneActivity.this.runOnUiThread(new Runnable() {
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