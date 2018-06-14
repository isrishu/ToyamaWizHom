package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.toyama.includes.model.Room;
import com.toyama.includes.model.Camera;
import com.toyama.includes.model.Schedule;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class CamerasActivity extends Activity {

    TextView lblActivityHeading;
    LinearLayout pnlCameras;
    ImageButton ibtnNewCamera,ibtnEditCamera,ibtnDeleteCamera;
    Room ThisRoom;
    String receivedMessage = "";
    ProgressDialog pDialog;

    //GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cameras);

//        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
//        pnlCameras= (LinearLayout) findViewById(R.id.pnlCameras);
//        ibtnNewCamera=(ImageButton) findViewById(R.id.ibtnNewCamera);
//        ibtnEditCamera=(ImageButton) findViewById(R.id.ibtnEditCamera);
//        ibtnDeleteCamera=(ImageButton) findViewById(R.id.ibtnDeleteCamera);
//
//        ibtnEditCamera.setEnabled(false);
//        ibtnDeleteCamera.setEnabled(false);
//
//        lblActivityHeading.setText("Cameras");
//
//        ibtnNewCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Globals.CurrentCamera=null;
//                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
//                Intent myInt=new Intent(getApplicationContext(),CameraActivity.class);
//                CamerasActivity.this.startActivity(myInt);
//                ibtnEditCamera.setEnabled(false);
//                ibtnDeleteCamera.setEnabled(false);
//            }
//        });
//
//        ibtnEditCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Feature coming soon...",Toast.LENGTH_LONG).show();
//            }
//        });
//
//        ibtnDeleteCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    if(Globals.connectionMode.equals("Local")) {
//                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CamerasActivity.this);
//                        alertDialog.setTitle("Delete Camera");
//
//                        alertDialog.setMessage("Are you sure you want to delete "+ Globals.CurrentCamera.Name +" Camera?");
//
//                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,int which) {
//                                int sid=Globals.CurrentCamera.CameraId;
//                                sendToGateway("DeleteCamera_"+String.valueOf(sid));
//                                ibtnEditCamera.setEnabled(false);
//                                ibtnDeleteCamera.setEnabled(false);
//                            }
//                        });
//
//                        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                ibtnEditCamera.setEnabled(false);
//                                ibtnDeleteCamera.setEnabled(false);
//                                dialog.cancel();
//                            }
//                        });
//
//                        alertDialog.show();
//                    } else {
//                        showPopupMessage("Feature not enabled in remote mode");
//                    }
//                } catch(Exception ex) {
//                    showPopupMessage("Error in Camera delete. Please try again.");
//                }
//            }
//        });
    }

//    private float getPixelValue(float dpValue) {
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
//    }
//
//    private void generateCameras() {
//        pnlCameras.removeAllViews();
//        // create the layout params that will be used to define how your button will be displayed
//        LinearLayout.LayoutParams btnparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(50));
//        LinearLayout.LayoutParams vparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(3));
//        try {
//            for(Camera s:Globals.AllCameras) {
//                String sn=Utilities.toTitleCase(s.Name)+" in "+Utilities.getThisRoom(s.RoomId).RoomName;
//                final Button btn = new Button(this);
//                // Give button an ID
//                btn.setId(s.CameraId);
//                btn.setTag(s.CameraId);
//                btn.setText(sn);
//
//                btn.setLayoutParams(btnparams);
//                btn.setBackgroundColor(getResources().getColor(R.color.transparent));
//                btn.setTextColor(getResources().getColor(R.color.black));
//                btn.setTextSize(20);
//                btn.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
//                btn.setPadding((int) getPixelValue(15), 0, 0, 0);
//                btn.setTypeface(Typeface.MONOSPACE,Typeface.NORMAL);
//
//                final String sName=sn;
//                // Set click listener for button
//                btn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String packageName="";
//                        Camera c=Utilities.getCamera((Integer) v.getTag());
//                        switch (c.Model) {
//                            case "dlink":
//                                packageName="com.dlink.mydlink";
//                                break;
//                            case "motorolamonitor":
//                                packageName="com.blinkhd";
//                                break;
//                        }
//                        if(packageName.equals("")) {
//                            Toast.makeText(getApplicationContext(), "Camera Model not Found",Toast.LENGTH_LONG).show();
//                        } else {
//                            startExternalActivity(packageName);
//                        }
//                    }
//                });
//
//                btn.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        Globals.currentCameraId=Integer.parseInt(String.valueOf(v.getTag()));
//                        Globals.CurrentCamera=Utilities.getCamera(Globals.currentCameraId);
//
//                        ibtnEditCamera.setEnabled(true);
//                        ibtnEditCamera.setClickable(true);
//                        ibtnDeleteCamera.setEnabled(true);
//                        ibtnDeleteCamera.setClickable(true);
//                        return true;
//                    }
//                });
//
//                //Add button to LinearLayout
//                pnlCameras.addView(btn);
//
//                final View v=new View(this);
//                vparams.setMargins((int) getPixelValue(7), 0,(int) getPixelValue(7), 0);
//                v.setLayoutParams(vparams);
//                v.setBackground(getResources().getDrawable(R.drawable.line_lightgray));
//
//                pnlCameras.addView(v);
//            }
//        } catch(Exception ex) {
//            ex.printStackTrace();
//            showPopupMessage("Error in listing cameras. Please try again.");
//        }
//    }
//
//    private void startExternalActivity(String packageName) {
//        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
//        if (intent == null) {
//            // Bring user to the market or let them choose an app?
//            intent = new Intent(Intent.ACTION_VIEW);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setData(Uri.parse("market://details?id=" + packageName));
//        }
//        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getApplicationContext().startActivity(intent);
//    }
//
//    private void sendToGateway(String msg) {
//        if(gatewayConnectThread==null){
//            return;
//        }
//        gatewayConnectThread.sendMessage(msg);
//    }
//
//    private void connectToGateway() {
//        receivedMessage = "";
//
//        gatewayConnectThread = new GatewayConnectThread();
//        gatewayConnectThread.start();
//    }
//
//    private class GatewayConnectThread extends Thread {
//        String messageToSend = "";
//        boolean goOut = false;
//
//        GatewayConnectThread() {
//        }
//
//        @Override
//        public void run() {
//            while (!goOut) {
//                try {
//                    if(Globals.dataInputStream==null || Globals.dataOutputStream==null) {
//                        // app probably crashed
//                        messageToSend="PermissionToConnect_"+Globals.customerUsername+"_"+Globals.password+"_"+Globals.dataTransferMode;
//                        Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
//                        Globals.socket = new Socket();
//                        Globals.socket.connect(Globals.serverSocketAddress,200);
//                        Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
//                        Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
//                        Globals.dataOutputStream.writeUTF(messageToSend);
//                        Globals.dataOutputStream.flush();
//                        messageToSend="";
//                    }
//                    if (Globals.dataInputStream.available() > 0) {
//                        receivedMessage = Globals.dataInputStream.readUTF().trim();
//                        if(receivedMessage.equals("")) {
//                            break;
//                        }
//                        CamerasActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(receivedMessage.contains("DeleteCameraResponse")) {
//                                    String[] x=receivedMessage.split("_");
//                                    if(Integer.valueOf(x[1])==1) {
//                                        showPopupMessage("Deleted, Loading Cameras Data...");
//                                        Globals.AllCameras.remove(Globals.CurrentCamera);
//                                        Globals.camerasString=Utilities.generateAllCamerasString();
//                                        generateCameras();
//                                        Utilities.writeDataToFile(getApplicationContext());
//                                    } else {
//                                        showPopupMessage("Error in delete, please try again");
//                                    }
//                                }
//                            }
//                        });
//                    }
//                    if(!messageToSend.equals("")){
//                        //msgToSend+="_"+Tools.username;
//                        Globals.dataOutputStream.writeUTF(messageToSend);
//						/*byte[] buffer=messageToSend.getBytes();
//						Tools.dataOutputStream.write(buffer);*/
//                        Globals.dataOutputStream.flush();
//                        messageToSend = "";
//                    }
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
//                } catch(Exception e) {
//                    showToast(Globals.unexpectedErrorMessage,Toast.LENGTH_SHORT);
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        private void sendMessage(String msg){
//            messageToSend = msg;
//        }
//
//        private void disconnect(){
//            goOut = true;
//        }
//    }

    @Override
    protected void onDestroy() {
//        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
//            gatewayConnectThread.disconnect();
        super.onDestroy();
    }

    @Override
    public boolean isFinishing() {
//        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
//            gatewayConnectThread.disconnect();
        return true;
    }

    protected void onPause() {
//        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
//            gatewayConnectThread.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(Globals.connectionMode.equals("Local")) {
//            connectToGateway();
//        }
//        if(Globals.AllCameras.size()==0) {
//            //showPopupMessage("No cameras found. If you are sure you have some, please click Refresh button in Settings page.");
//        } else generateCameras();
    }

//    private void showPopupMessage(final String msg) {
//        CamerasActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                AlertDialog.Builder builder = new AlertDialog.Builder(CamerasActivity.this);
//                builder.setMessage(msg);
//                builder.setCancelable(true);
//
//                builder.setPositiveButton("Ok",null);
//                AlertDialog alert = builder.create();
//                alert.show();
//            }
//        });
//    }
//
//    private void showToast(final String msg,final int len) {
//        CamerasActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(toast==null) {
//                    toast=Toast.makeText(getApplicationContext(),msg,len);
//                    toast.show();
//                }
//            }
//        });
//    }
}
