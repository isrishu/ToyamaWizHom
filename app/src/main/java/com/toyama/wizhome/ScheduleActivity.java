package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ScheduleActivity extends Activity {
    EditText txtSceneName;
    TimePicker tpScheduleStartTime;
    CheckBox chkSunday,chkMonday,chkTuesday,chkWednesday,chkThursday,chkFriday,chkSaturday,chkSelectAll;
    Button btnMakeSchedule;
    String receivedMessage = "";

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_schedule);

        try {
            txtSceneName=(EditText) findViewById(R.id.txtSceneName);

            tpScheduleStartTime=(TimePicker) findViewById(R.id.tpScheduleStartTime);
            chkSunday=(CheckBox) findViewById(R.id.chkSunday);
            chkMonday=(CheckBox) findViewById(R.id.chkMonday);
            chkTuesday=(CheckBox) findViewById(R.id.chkTuesday);
            chkWednesday=(CheckBox) findViewById(R.id.chkWednesday);
            chkThursday=(CheckBox) findViewById(R.id.chkThursday);
            chkFriday=(CheckBox) findViewById(R.id.chkFriday);
            chkSaturday=(CheckBox) findViewById(R.id.chkSaturday);
            chkSelectAll=(CheckBox) findViewById(R.id.chkSelectAll);

            btnMakeSchedule=(Button) findViewById(R.id.btnMakeSchedule);

            txtSceneName.setText(Globals.CurrentScene.SceneName);
            txtSceneName.setEnabled(false);

            tpScheduleStartTime.setIs24HourView(true);

            loadSchedule();

            chkSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    chkSunday.setChecked(chkSelectAll.isChecked());
                    chkMonday.setChecked(chkSelectAll.isChecked());
                    chkTuesday.setChecked(chkSelectAll.isChecked());
                    chkWednesday.setChecked(chkSelectAll.isChecked());
                    chkThursday.setChecked(chkSelectAll.isChecked());
                    chkFriday.setChecked(chkSelectAll.isChecked());
                    chkSaturday.setChecked(chkSelectAll.isChecked());
                }
            });

            btnMakeSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    boolean isOneChecked=false;
                    int st=tpScheduleStartTime.getCurrentHour()*60*60;
                    st+=tpScheduleStartTime.getCurrentMinute()*60;
                    // getting total time in secs from 12:00 am that day
                    Globals.CurrentSchedule.StartTime=st;
                    //Tools.CurrentSchedule.DatesApplicable[0]=chkSunday.isChecked()?1:0;
                    if(chkSunday.isChecked()) {
                        isOneChecked=true;
                        Globals.CurrentSchedule.DatesApplicable[0]=1;
                    } else {
                        Globals.CurrentSchedule.DatesApplicable[0]=0;
                    }
                    if(chkMonday.isChecked()) {
                        isOneChecked=true;
                        Globals.CurrentSchedule.DatesApplicable[1]=1;
                    } else {
                        Globals.CurrentSchedule.DatesApplicable[1]=0;
                    }
                    if(chkTuesday.isChecked()) {
                        isOneChecked=true;
                        Globals.CurrentSchedule.DatesApplicable[2]=1;
                    } else {
                        Globals.CurrentSchedule.DatesApplicable[2]=0;
                    }
                    if(chkWednesday.isChecked()) {
                        isOneChecked=true;
                        Globals.CurrentSchedule.DatesApplicable[3]=1;
                    } else {
                        Globals.CurrentSchedule.DatesApplicable[3]=0;
                    }
                    if(chkThursday.isChecked()) {
                        isOneChecked=true;
                        Globals.CurrentSchedule.DatesApplicable[4]=1;
                    } else {
                        Globals.CurrentSchedule.DatesApplicable[4]=0;
                    }
                    if(chkFriday.isChecked()) {
                        isOneChecked=true;
                        Globals.CurrentSchedule.DatesApplicable[5]=1;
                    } else {
                        Globals.CurrentSchedule.DatesApplicable[5]=0;
                    }
                    if(chkSaturday.isChecked()) {
                        isOneChecked=true;
                        Globals.CurrentSchedule.DatesApplicable[6]=1;
                    } else {
                        Globals.CurrentSchedule.DatesApplicable[6]=0;
                    }
                    if(!isOneChecked) {
                        showPopupMessage("Please select atleast one day.");
                        return;
                    }
                    String str="";
                    str+=Globals.CurrentSchedule.ScheduleId+"_";
                    str+=Globals.CurrentSchedule.SceneId+"_";
                    str+=Globals.CurrentSchedule.StartTime+"_";
                    String da="";
                    for(int i=0;i<Globals.CurrentSchedule.DatesApplicable.length;i++) {
                        da+=String.valueOf(Globals.CurrentSchedule.DatesApplicable[i])+",";
                    }
                    if(!da.equals("")) da=da.substring(0,da.lastIndexOf(","));
                    str+=da;
                    sendToGateway("SaveSchedule_"+str);
                }
            });
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadSchedule() {
        int sTime=Globals.CurrentSchedule.StartTime; // value of secs in day after 00:00
        sTime=sTime/60; // first convert to minutes after 00:00 in the day

        int sHours=sTime/60;
        int sMins=sTime%60;

        tpScheduleStartTime.setCurrentHour(sHours);
        tpScheduleStartTime.setCurrentMinute(sMins);

        chkSunday.setChecked(Globals.CurrentSchedule.DatesApplicable[0]==1);
        chkMonday.setChecked(Globals.CurrentSchedule.DatesApplicable[1]==1);
        chkTuesday.setChecked(Globals.CurrentSchedule.DatesApplicable[2]==1);
        chkWednesday.setChecked(Globals.CurrentSchedule.DatesApplicable[3]==1);
        chkThursday.setChecked(Globals.CurrentSchedule.DatesApplicable[4]==1);
        chkFriday.setChecked(Globals.CurrentSchedule.DatesApplicable[5]==1);
        chkSaturday.setChecked(Globals.CurrentSchedule.DatesApplicable[6]==1);
        chkSelectAll.setChecked(chkSunday.isChecked() && chkMonday.isChecked() && chkTuesday.isChecked() && chkWednesday.isChecked() && chkThursday.isChecked() && chkFriday.isChecked() && chkSaturday.isChecked());
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
                        ScheduleActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(receivedMessage.contains("SaveScheduleResponse")) {
                                    String x[]=receivedMessage.split("_");
                                    if(Integer.valueOf(x[1])==1) {
                                        Globals.isScheduleSaved=true;
                                        if(Globals.isScheduleEditing) {
                                            Globals.isScheduleEditing=false;
                                        } else {
                                            Globals.CurrentSchedule.ScheduleId=Integer.valueOf(x[2]);
                                            Globals.AllSchedules.add(Globals.CurrentSchedule);
                                        }
                                        Globals.schedulesString=Utilities.generateAllSchedulesString();
                                        Utilities.writeDataToFile(getApplicationContext());
                                        showPopupMessage("Schedule Saved");
                                    } else {
                                        showPopupMessage("Schedule NOT Saved");
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
    }

    private void showPopupMessage(final String msg) {
        ScheduleActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showToast(final String msg,final int len) {
        ScheduleActivity.this.runOnUiThread(new Runnable() {
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