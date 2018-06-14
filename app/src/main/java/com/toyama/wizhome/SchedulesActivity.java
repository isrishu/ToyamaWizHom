package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.model.Room;
import com.toyama.includes.model.Scene;
import com.toyama.includes.model.Schedule;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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

public class SchedulesActivity extends Activity {

    TextView lblActivityHeading;
    LinearLayout pnlSchedules;
    ImageButton ibtnEditSchedule,ibtnDeleteSchedule;
    Room ThisRoom;
    String receivedMessage = "";
    ProgressDialog pDialog;

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_schedules);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        pnlSchedules= (LinearLayout) findViewById(R.id.pnlSchedules);
        ibtnEditSchedule=(ImageButton) findViewById(R.id.ibtnEditSchedule);
        ibtnDeleteSchedule=(ImageButton) findViewById(R.id.ibtnDeleteSchedule);

        ibtnEditSchedule.setEnabled(false);
        ibtnEditSchedule.setAlpha(.5f);
        ibtnDeleteSchedule.setEnabled(false);
        ibtnDeleteSchedule.setAlpha(.5f);

        lblActivityHeading.setText("Schedules ("+Globals.connectionMode+")");

        ibtnEditSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(Globals.connectionMode.equals("Local")) {
                    Globals.isScheduleSaved=false;
                    Globals.isScheduleEditing=true;
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    Intent myInt=new Intent(getApplicationContext(),ScheduleActivity.class);
                    SchedulesActivity.this.startActivity(myInt);
                    ibtnEditSchedule.setEnabled(false);
                    ibtnEditSchedule.setAlpha(.5f);
                    ibtnDeleteSchedule.setEnabled(false);
                    ibtnDeleteSchedule.setAlpha(.5f);
                } else {
                    showPopupMessage("Feature not enabled in remote mode.");
                }
            }
        });

        ibtnDeleteSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.connectionMode.equals("Local")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SchedulesActivity.this);
                        alertDialog.setTitle("Delete Schedule");

                        alertDialog.setMessage("Are you sure you want to delete this Schedule?");

                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                sendToGateway("DeleteSchedule_"+String.valueOf(Globals.CurrentSchedule.ScheduleId));
                                ibtnEditSchedule.setEnabled(false);
                                ibtnEditSchedule.setAlpha(.5f);
                                ibtnDeleteSchedule.setEnabled(false);
                                ibtnDeleteSchedule.setAlpha(.5f);
                            }
                        });

                        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ibtnEditSchedule.setEnabled(false);
                                ibtnEditSchedule.setAlpha(.5f);
                                ibtnDeleteSchedule.setEnabled(false);
                                ibtnDeleteSchedule.setAlpha(.5f);
                                dialog.cancel();
                            }
                        });

                        alertDialog.show();
                    } else {
                        showPopupMessage("Feature not enabled in remote mode.");
                    }
                } catch(Exception ex) {
                    showPopupMessage("Error in delete Schedule. Please try again.");
                }
            }
        });
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void generateSchedules() {
        pnlSchedules.removeAllViews();
        // create the layout params that will be used to define how your button will be displayed
        LinearLayout.LayoutParams btnparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(50));
        LinearLayout.LayoutParams vparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(3));
        try {
            for(Schedule sch:Globals.AllSchedules) {
                final Button btn = new Button(this);
                // Give button an ID
                btn.setId(sch.ScheduleId);
                btn.setTag(sch.ScheduleId);
                Scene s=Utilities.getCurrentScene(sch.SceneId);
                btn.setText(s.SceneName+" on "+sch.toString());

                btn.setLayoutParams(btnparams);
                btn.setBackgroundColor(getResources().getColor(R.color.transparent));
                btn.setTextColor(getResources().getColor(R.color.green));
                btn.setTextSize(16);
                btn.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                btn.setPadding((int) getPixelValue(15), 0, 0, 0);
                btn.setTypeface(Typeface.MONOSPACE,Typeface.NORMAL);

                final int index = sch.ScheduleId;

                btn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Log.i("TAG", "index/rc :" + String.valueOf(index)+"/"+v.getTag());
                        Globals.currentScheduleId=Integer.parseInt(String.valueOf(v.getTag()));
                        //Globals.isForScene=true;
                        Globals.CurrentSchedule=Utilities.getCurrentSchedule(Globals.currentScheduleId);
                        Globals.CurrentScene=Utilities.getCurrentScene(Globals.CurrentSchedule.SceneId);
                        ibtnEditSchedule.setEnabled(true);
                        ibtnEditSchedule.setAlpha(1f);
                        ibtnDeleteSchedule.setEnabled(true);
                        ibtnDeleteSchedule.setAlpha(1f);
                        return true;
                    }
                });

                //Add button to LinearLayout
                pnlSchedules.addView(btn);

                final View v=new View(this);
                vparams.setMargins((int) getPixelValue(7), 0,(int) getPixelValue(7), 0);
                v.setLayoutParams(vparams);
                v.setBackground(getResources().getDrawable(R.drawable.line_lightgray));

                pnlSchedules.addView(v);
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
                        receivedMessage = Globals.dataInputStream.readUTF();
                        SchedulesActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //lblStatus.setText(recdmsg);
                                if(receivedMessage.contains("DeleteScheduleResponse")) {
                                    String x[]=receivedMessage.split("_");
                                    if(Integer.valueOf(x[1])==1) {
                                        showPopupMessage("Saved, Loading Schedules Data...");
                                        Globals.AllSchedules.remove(Globals.CurrentSchedule);
                                        Globals.schedulesString=Utilities.generateAllSchedulesString();
                                        generateSchedules();
                                        Utilities.writeDataToFile(getApplicationContext());
                                    } else {
                                        showPopupMessage("Not Saved, Please try again");
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
    public void onBackPressed() {
        if(ibtnEditSchedule.isEnabled()) {
            Globals.CurrentSchedule=null;
            Globals.currentScheduleId=0;
            Globals.CurrentScene=null;
            ibtnEditSchedule.setEnabled(false);
            ibtnEditSchedule.setAlpha(.5f);
            ibtnDeleteSchedule.setEnabled(false);
            ibtnDeleteSchedule.setAlpha(.5f);
        } else {
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
    protected void onDestroy() {
        //Globals.isForScene=false;
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Globals.AllSchedules.size()==0)
            showPopupMessage("No schedules found. Please click Refresh button in Settings page.");
        else {
            if(Globals.connectionMode.equals("Local")) {
                connectToGateway();
            }
            generateSchedules();
        }
    }

    private void showPopupMessage(final String msg) {
        SchedulesActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SchedulesActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showToast(final String msg,final int len) {
        SchedulesActivity.this.runOnUiThread(new Runnable() {
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