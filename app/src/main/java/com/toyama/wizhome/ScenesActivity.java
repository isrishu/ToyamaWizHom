package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.toyama.includes.model.Room;
import com.toyama.includes.model.Scene;
import com.toyama.includes.model.Schedule;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class ScenesActivity extends Activity {

    TextView lblActivityHeading;
    LinearLayout pnlScenes;
    ImageButton ibtnNewScene,ibtnEditScene,ibtnDeleteScene,ibtnMakeSchedule;
    Room ThisRoom;
    String receivedMessage = "";
    ProgressDialog pDialog;

    RemoteAsyncTask remoteTask=null;
    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scenes);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        pnlScenes= (LinearLayout) findViewById(R.id.pnlScenes);
        ibtnNewScene=(ImageButton) findViewById(R.id.ibtnNewScene);
        ibtnEditScene=(ImageButton) findViewById(R.id.ibtnEditScene);
        ibtnDeleteScene=(ImageButton) findViewById(R.id.ibtnDeleteScene);
        ibtnMakeSchedule=(ImageButton) findViewById(R.id.ibtnMakeSchedule);

        ibtnEditScene.setEnabled(false);
        ibtnEditScene.setAlpha(.5f);
        ibtnDeleteScene.setEnabled(false);
        ibtnDeleteScene.setAlpha(.5f);
        ibtnMakeSchedule.setEnabled(false);
        ibtnMakeSchedule.setAlpha(.5f);

        lblActivityHeading.setText("Scenes ("+Globals.connectionMode+")");

        ibtnNewScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.CurrentScene=null;
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                Intent myInt=new Intent(getApplicationContext(),SceneActivity.class);
                ScenesActivity.this.startActivity(myInt);
                ibtnEditScene.setEnabled(false);
                ibtnEditScene.setAlpha(.5f);
                ibtnDeleteScene.setEnabled(false);
                ibtnDeleteScene.setAlpha(.5f);
                ibtnMakeSchedule.setEnabled(false);
                ibtnMakeSchedule.setAlpha(.5f);
            }
        });

        ibtnNewScene.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sendToGateway("ClearForm");
                return true;
            }
        });

        ibtnEditScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Globals.connectionMode.equals("Local")) {
                    Globals.isSceneEditing=true;
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    Intent myInt=new Intent(getApplicationContext(),SceneActivity.class);
                    ScenesActivity.this.startActivity(myInt);
                    ibtnEditScene.setEnabled(false);
                    ibtnEditScene.setAlpha(.5f);
                    ibtnDeleteScene.setEnabled(false);
                    ibtnDeleteScene.setAlpha(.5f);
                    ibtnMakeSchedule.setEnabled(false);
                    ibtnMakeSchedule.setAlpha(.5f);
                } else {
                    showPopupMessage("Feature not enabled in remote mode","");
                }
            }
        });

        ibtnDeleteScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.connectionMode.equals("Local")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScenesActivity.this);
                        alertDialog.setTitle("Delete Scene");
                        alertDialog.setMessage("Are you sure you want to delete "+ Globals.CurrentScene.SceneName +" scene?");
                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                int sid=Globals.CurrentScene.SceneId;
                                sendToGateway("DeleteScene_"+String.valueOf(sid));
                                ibtnEditScene.setEnabled(false);
                                ibtnEditScene.setAlpha(.5f);
                                ibtnDeleteScene.setEnabled(false);
                                ibtnDeleteScene.setAlpha(.5f);
                                ibtnMakeSchedule.setEnabled(false);
                                ibtnMakeSchedule.setAlpha(.5f);
                            }
                        });

                        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ibtnEditScene.setEnabled(false);
                                ibtnEditScene.setAlpha(.5f);
                                ibtnDeleteScene.setEnabled(false);
                                ibtnDeleteScene.setAlpha(.5f);
                                ibtnMakeSchedule.setEnabled(false);
                                ibtnMakeSchedule.setAlpha(.5f);
                                dialog.cancel();
                            }
                        });

                        alertDialog.show();
                    } else {
                        showPopupMessage("Feature not enabled in remote mode","");
                    }
                } catch(Exception ex) {
                    showPopupMessage("Error in Scene delete. Please try again.","");
                }
            }
        });

        ibtnMakeSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Globals.CurrentSchedule=new Schedule();
                    Globals.CurrentSchedule.ScheduleId=0;
                    Globals.CurrentSchedule.SceneId=Globals.CurrentScene.SceneId;
                    Globals.isScheduleSaved=false;
                    Globals.isScheduleEditing=false;
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    Intent myInt=new Intent(getApplicationContext(),ScheduleActivity.class);
                    ScenesActivity.this.startActivity(myInt);
                    ibtnEditScene.setEnabled(false);
                    ibtnEditScene.setAlpha(.5f);
                    ibtnDeleteScene.setEnabled(false);
                    ibtnDeleteScene.setAlpha(.5f);
                    ibtnMakeSchedule.setEnabled(false);
                    ibtnMakeSchedule.setAlpha(.5f);
                } catch(Exception ex) {
                    showPopupMessage("Error in creating Schedule. Please try again.","");
                }
            }
        });
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void generateScenes() {
        pnlScenes.removeAllViews();
        // create the layout params that will be used to define how your button will be displayed
        LinearLayout.LayoutParams btnparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(50));
        LinearLayout.LayoutParams vparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(3));
        try {
            for(Scene s:Globals.AllScenes) {
                String sn=Utilities.toTitleCase(s.SceneName);
                final Button btn = new Button(this);
                // Give button an ID
                btn.setId(s.SceneId);
                btn.setTag(s.SceneId);
                btn.setText(sn);

                if(sn.contains("Morning")) {
                    //btn.setCompoundDrawables(getResources().getDrawable(R.drawable.good_morning),null,null,null);
                }

                if(sn.contains("Night")) {
                    //btn.setCompoundDrawables(getResources().getDrawable(R.drawable.good_evening),null,null,null);
                }
                btn.setLayoutParams(btnparams);
                btn.setBackgroundColor(getResources().getColor(R.color.transparent));
                btn.setTextColor(getResources().getColor(R.color.green));
                btn.setTextSize(20);
                btn.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                btn.setPadding((int) getPixelValue(15), 0, 0, 0);
                btn.setTypeface(Typeface.MONOSPACE,Typeface.NORMAL);

                final String sName=sn;
                // Set click listener for button
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pDialog = ProgressDialog.show(ScenesActivity.this, "Applying Scene "+sName, "Please wait...", true);
                        pDialog.setCanceledOnTouchOutside(true);
                        pDialog.setOwnerActivity(ScenesActivity.this);
                        if(Globals.connectionMode.equals("Local")) {
                            sendToGateway("ApplyScene_"+String.valueOf(v.getTag()));
                        } else {
                            remoteTask=new RemoteAsyncTask();
                            String [] params={"applyScene",String.valueOf(v.getTag())};
                            remoteTask.execute(params);
                        }
                    }
                });

                btn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Globals.currentSceneId=Integer.parseInt(String.valueOf(v.getTag()));
                        Globals.CurrentScene=Utilities.getCurrentScene(Globals.currentSceneId);

                        ibtnEditScene.setEnabled(true);
                        ibtnEditScene.setAlpha(1f);
                        ibtnDeleteScene.setEnabled(true);
                        ibtnDeleteScene.setAlpha(1f);
                        ibtnMakeSchedule.setEnabled(true);
                        ibtnMakeSchedule.setAlpha(1f);
                        return true;
                    }
                });

                //Add button to LinearLayout
                pnlScenes.addView(btn);

                final View v=new View(this);
                vparams.setMargins((int) getPixelValue(7), 0,(int) getPixelValue(7), 0);
                v.setLayoutParams(vparams);
                v.setBackground(getResources().getDrawable(R.drawable.line_lightgray));
                pnlScenes.addView(v);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
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
                        ScenesActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(receivedMessage.startsWith("ApplySceneResponse")) {
                                    String[] x=receivedMessage.split("_");
                                    if(Integer.valueOf(x[1])==1) {
                                        showPopupMessage("Scene Applied","Touch anywhere to dismiss.");
                                    } else {
                                        showPopupMessage("Error, please try again","");
                                    }
                                }
                                if(receivedMessage.contains("DeleteSceneResponse")) {
                                    String[] x=receivedMessage.split("_");
                                    if(Integer.valueOf(x[1])==1) {
                                        showPopupMessage("Deleted","Loading Scenes Data...");
                                        Globals.AllScenes.remove(Globals.CurrentScene);
                                        ArrayList<Schedule> toDeleteSchedules=new ArrayList<Schedule>();
                                        for(Schedule sch:Globals.AllSchedules) {
                                            if(sch.SceneId==Globals.CurrentScene.SceneId) {
                                                toDeleteSchedules.add(sch);
                                            }
                                        }
                                        Globals.AllSchedules.removeAll(toDeleteSchedules);
                                        Globals.scenesString=Utilities.generateAllScenesString();
                                        Globals.schedulesString=Utilities.generateAllSchedulesString();
                                        generateScenes();
                                        Utilities.writeDataToFile(getApplicationContext());
                                    } else {
                                        showPopupMessage("Error in delete, please try again","");
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
    protected void onDestroy() {
        Globals.isForScene=false;
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(ibtnEditScene.isEnabled()) {
            Globals.CurrentScene=null;
            Globals.currentSceneId=0;
            ibtnEditScene.setEnabled(false);
            ibtnEditScene.setAlpha(.5f);
            ibtnDeleteScene.setEnabled(false);
            ibtnDeleteScene.setAlpha(.5f);
            ibtnMakeSchedule.setEnabled(false);
            ibtnMakeSchedule.setAlpha(.5f);
        } else {
            //Globals.isForScene=false;
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Globals.isForScene=true;
        if(Globals.connectionMode.equals("Local")) {
            connectToGateway();
        }
        if(Globals.AllScenes.size()==0) {
            //showPopupMessage("No scenes found. If you are sure you have some, please click Refresh button in Settings page.");
        } else generateScenes();
    }

    public class RemoteAsyncTask extends AsyncTask<String, Integer, String> {
        ServiceLayer svc;

        public RemoteAsyncTask() {

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                svc=new ServiceLayer();
                String mode=params[0];
                String paramstr=params[1];
                switch(mode) {
                    case "applyScene" :
                        if(svc.applyScene(paramstr))
                            return svc.getResponseString();
                        else
                            return svc.getResponseString();
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
                    showPopupMessage("Command Sent","Touch anywhere outside this message to dismiss.");
                } else {
                    showPopupMessage("Command not Sent","Could not connect to Remote Server, pls check your internet..."+responses[1]);
                }
            } else {
                if(message.equals("1")) {
                    showPopupMessage("Command Sent","Touch anywhere outside this message to dismiss.");
                } else {
                    showPopupMessage("Command not Sent","Could not connect to Remote Server, pls check your internet..."+message);
                }
            }
            svc.close();
        }

        @Override
        protected void onCancelled() {
            remoteTask = null;
        }
    }

    private void showPopupMessage(final String title,final String msg) {
        ScenesActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(pDialog!=null && pDialog.isShowing()) {
                    pDialog.setTitle(title);
                    pDialog.setMessage(msg);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScenesActivity.this);
                    builder.setTitle(title);
                    builder.setMessage(msg);
                    builder.setCancelable(true);

                    builder.setPositiveButton("Ok",null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    private void showToast(final String msg,final int len) {
        ScenesActivity.this.runOnUiThread(new Runnable() {
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