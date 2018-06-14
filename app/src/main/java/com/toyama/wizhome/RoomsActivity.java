package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.model.Room;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.Utilities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class RoomsActivity extends Activity {
    TextView lblActivityHeading;
    LinearLayout pnlRooms;
    Room ThisRoom;
    String receivedMessage = "";
    ProgressDialog pDialog;

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_rooms);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        pnlRooms=(LinearLayout) findViewById(R.id.pnlRooms);

        lblActivityHeading.setText("Rooms ("+Globals.connectionMode+")");
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void generateRooms() {
        // create the layout params that will be used to define how your button will be displayed
        LinearLayout.LayoutParams btnparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(50));
        LinearLayout.LayoutParams vparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(3));
        try {
            pnlRooms.removeAllViews();
            for(Room r:Globals.AllRooms) {
                if(r.RoomId==0) {
                    return;
                } else {
                    final Button btn = new Button(this);
                    // Give button an ID
                    btn.setId(r.RoomId);
                    btn.setText(Utilities.toTitleCase(r.RoomName)+" ("+Utilities.getNodeSwitchesCount(r)+" Controls)");

                    btn.setLayoutParams(btnparams);
                    btn.setBackgroundColor(getResources().getColor(R.color.transparent));
                    btn.setTextColor(getResources().getColor(R.color.black));
                    btn.setTextSize(20);
                    btn.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                    btn.setPadding((int) getPixelValue(15), 0, 0, 0);
                    btn.setTypeface(Typeface.MONOSPACE,Typeface.NORMAL);

                    final int index = r.RoomId;
                    // Set click listener for button
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("TAG", "index/rc :" + String.valueOf(index)+"/"+v.getTag());
                            Globals.currentRoomId=v.getId();
                            if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
                                gatewayConnectThread.disconnect();
                            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                            Intent myInt=new Intent(getApplicationContext(),RoomActivity.class);
                            RoomsActivity.this.startActivity(myInt);
                        }
                    });

                    if(!Globals.isForScene) {
                        final int rid=r.RoomId;
                        final String prn=r.RoomName;
                        btn.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(RoomsActivity.this);
                                alertDialog.setTitle("Edit Room Name");

                                alertDialog.setMessage("Please enter/edit your room name");
                                final EditText input = new EditText(RoomsActivity.this);
                                input.setText(prn);
                                alertDialog.setView(input);
                                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int which) {
                                        Globals.isRoomSaved=false;
                                        String rn=input.getText().toString().trim().replace("\n","");
                                        pDialog = ProgressDialog.show(RoomsActivity.this, "Saving Room", "Please wait...", true);
                                        pDialog.setCanceledOnTouchOutside(true);
                                        pDialog.setOwnerActivity(RoomsActivity.this);
                                        sendToGateway("UpdateRoomName_"+String.valueOf(rid)+"_"+rn);
                                    }
                                });

                                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                alertDialog.show();
                                return true;
                            }
                        });
                    }
                    //Add button to LinearLayout
                    pnlRooms.addView(btn);

                    final View v=new View(this);
                    vparams.setMargins((int) getPixelValue(7), 0,(int) getPixelValue(7), 0);
                    vparams.height=(int) getPixelValue(1);
                    v.setLayoutParams(vparams);
                    v.setBackground(getResources().getDrawable(R.drawable.line_lightgray));
                    pnlRooms.addView(v);
                }
            }
            if(Globals.isForScene) {
                final Button btn = new Button(this);
                btn.setText("Sensors");

                btn.setLayoutParams(btnparams);
                btn.setBackgroundColor(getResources().getColor(R.color.transparent));
                btn.setTextColor(getResources().getColor(R.color.black));
                btn.setTextSize(20);
                btn.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                btn.setPadding((int) getPixelValue(15), 0, 0, 0);
                btn.setTypeface(Typeface.MONOSPACE,Typeface.NORMAL);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
                            gatewayConnectThread.disconnect();
                        Globals.isForScene=true;
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        Intent myInt=new Intent(getApplicationContext(),SensorsActivity.class);
                        RoomsActivity.this.startActivity(myInt);
                    }
                });

                //Add button to LinearLayout
                pnlRooms.addView(btn);

                final View v=new View(this);
                vparams.setMargins((int) getPixelValue(7), 0,(int) getPixelValue(7), 0);
                v.setLayoutParams(vparams);
                v.setBackground(getResources().getDrawable(R.drawable.line_lightgray));
                pnlRooms.addView(v);
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
                        RoomsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if(receivedMessage.contains("UpdateRoomNameResponse")) {
                                        String x[]=receivedMessage.split("_");
                                        if(Integer.valueOf(x[1])==1) {
                                            Globals.isRoomSaved=true;
                                            showPopupMessage("Room Updated");
                                            int rid=Integer.valueOf(x[2]);
                                            String rn=x[3];
                                            Room r=Utilities.getThisRoom(rid);
                                            r.RoomName=rn;
                                            generateRooms();
                                            Globals.roomsString=Utilities.generateAllRoomsString();
                                            if(Utilities.writeDataToFile(getApplicationContext())) showPopupMessage("Done");
                                            else showPopupMessage("Not Done");
                                        } else {
                                            showPopupMessage("Room Name not Updated");
                                        }
                                        if(pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }
                                    }
                                } catch(Exception ex) {
                                    if(pDialog.isShowing()) {
                                        pDialog.dismiss();
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
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        super.onBackPressed();
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
        if(Globals.AllRooms.size()==0) {
            //showPopupMessage("No Rooms found. Please click Refresh button in Settings page.");
        } else {
            if(Globals.connectionMode.equals("Local")) {
                connectToGateway();
            }
            generateRooms();
        }
    }

    private void showPopupMessage(final String msg) {
        RoomsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(RoomsActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showToast(final String msg,final int len) {
        RoomsActivity.this.runOnUiThread(new Runnable() {
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