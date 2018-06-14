package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.toyama.includes.model.CameraModel;
import com.toyama.includes.model.Room;
import com.toyama.includes.model.Camera;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;
import com.toyama.wizhome.hikvision.HikVisionCameraManager;
import com.toyama.wizhome.hikvision.HikVisionGlobals;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CameraActivity extends Activity {

    EditText txtCameraName;
    AutoCompleteTextView cmbRooms,cmbModels;
    Button btnSaveCamera;

    String receivedMessage = "";
    ProgressDialog pDialog;

//    GatewayConnectThread gatewayConnectThread = null;
    Room ThisRoom;
    CameraModel ThisModel;
    ArrayList<CameraModel> Models=new ArrayList<CameraModel>();
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);

//        txtCameraName=(EditText) findViewById(R.id.txtCameraName);
//        cmbRooms=(AutoCompleteTextView) findViewById(R.id.cmbRooms);
//        cmbModels=(AutoCompleteTextView) findViewById(R.id.cmbModels);
//
//        if(Globals.CurrentCamera!=null) {
//            txtCameraName.setText(Globals.CurrentCamera.Name);
//        }
//
//        HikVisionGlobals.hikVisionCameraManager=HikVisionCameraManager.getInstance();
//
//        this.Models.clear();
//        CameraModel cm=new CameraModel(1,"DLink","dlink");
//        this.Models.add(cm);
//        cm=new CameraModel(2,"Motorola Monitor","motorolamonitor");
//        this.Models.add(cm);
//
//        cmbModels.setAdapter(new ArrayAdapter<CameraModel>(getApplicationContext(),R.layout.spinner_item,Models));
//        cmbRooms.setAdapter(new ArrayAdapter<Room>(getApplicationContext(),R.layout.spinner_item,Globals.AllRooms));
//
//        Button btn=(Button) findViewById(R.id.btnSaveCamera);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try	{
//                    CameraLoginTask cameraLoginTask=new CameraLoginTask();
//                    cameraLoginTask.execute("");
////                    String cn=txtCameraName.getText().toString().trim().replace("\n","");
////                    if(cn.equals("")) {
////                        showPopupMessage("Please give the camera a name.");
////                        return;
////                    }
////                    if(ThisRoom==null || ThisModel==null) {
////                        showPopupMessage("Please select room and model properly");
////                        return;
////                    }
////                    if(Globals.CurrentCamera==null) { // new scene
////                        Globals.CurrentCamera=new Camera();//new Camera(0,ThisRoom.RoomId,cn,ThisModel.Model,1);
////                    } else { // save
////                        Globals.CurrentCamera.Name=cn;
////                        Globals.CurrentCamera.RoomId=ThisRoom.RoomId;
////                        Globals.CurrentCamera.Model=ThisModel.Model;
////                    }
////                    pDialog = ProgressDialog.show(CameraActivity.this, "Saving Camera", "Please wait...", true);
////                    pDialog.setCanceledOnTouchOutside(true);
////                    pDialog.setOwnerActivity(CameraActivity.this);
////                    String str="";
////                    str+=Globals.CurrentCamera.CameraId+"_";
////                    str+=Globals.CurrentCamera.RoomId+"_";
////                    str+=Globals.CurrentCamera.Name+"_";
////                    str+=Globals.CurrentCamera.Model;
////                    sendToGateway("SaveCamera_"+str);
//                }
//                catch(Exception ex) {
//                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//
//        cmbRooms.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
//                ThisRoom=(Room) cmbRooms.getAdapter().getItem(pos);
//                if(ThisRoom==null) {
//                    showPopupMessage("Invalid Room. Please try again!");
//                }
//            }
//        });
//
//        cmbModels.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
//                ThisModel=(CameraModel) cmbModels.getAdapter().getItem(pos);
//                if(ThisModel==null) {
//                    showPopupMessage("Invalid Model. Please try again!");
//                }
//            }
//        });
    }

//    private void sendToGateway(String msg) {
//        if(gatewayConnectThread==null){
//            return;
//        }
//        gatewayConnectThread.sendMessage(msg);
//    }
//
//    private void connectToGateway() {
//        receivedMessage = "";
//        gatewayConnectThread = new GatewayConnectThread();
//        gatewayConnectThread.start();
//    }
//
//    private class GatewayConnectThread extends Thread {
//        String messageToSend = "";
//        boolean goOut = false;
//
//        GatewayConnectThread() {
//
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
//                        //showMessage(receivedMessage);
//                        if(receivedMessage.contains("SaveCameraResponse")) {
//                            String x[]=receivedMessage.split("_");
//                            if(Integer.valueOf(x[1])==1) {
//                                Globals.CurrentCamera.CameraId=Integer.valueOf(x[2]);
//                                if(!Globals.AllCameras.contains(Globals.CurrentCamera))
//                                    Globals.AllCameras.add(Globals.CurrentCamera);
//                                Globals.camerasString=Utilities.generateAllCamerasString();
//                                showPopupMessage("Camera Saved");
//                                Utilities.writeDataToFile(getApplicationContext());
//                                if(pDialog.isShowing()) {
//                                    pDialog.dismiss();
//                                }
//                            } else {
//                                showPopupMessage("Camera not Saved. Please try again.");
//                            }
//                        }
//                    }
//                    if(!messageToSend.equals("")){
//                        Globals.dataOutputStream.writeUTF(messageToSend);
//                        Globals.dataOutputStream.flush();
//                        messageToSend = "";
//                    }
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
//                } catch (IOException e) {
//                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
//                    e.printStackTrace();
//                } catch (Exception e) {
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
    }

//    private void showException(Exception ex) {
//        if (BuildConfig.DEBUG) {
//            String ex1="";
//            for(StackTraceElement a1:ex.getStackTrace()) {
//                ex1+=a1.toString();
//            }
//            showPopupMessage("Error: "+ex.getMessage());
//        }
//    }
//
//    private void showText(final String text) {
//        CameraActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                txtCameraName.setText(text);
//            }
//        });
//    }
//
//    private void showPopupMessage(final String msg) {
//        CameraActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
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
//        CameraActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(toast==null) {
//                    toast=Toast.makeText(getApplicationContext(),msg,len);
//                    toast.show();
//                }
//            }
//        });
//    }
//
//    public class CameraLoginTask extends AsyncTask<String, Integer, String> {
//        ArrayList<String> ips=new ArrayList<String>();
//        InetAddress ipaddr=null;
//
//        public CameraLoginTask() {
//
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            try {
//                // Initialise Network SDK
//                String errorMessage = HikVisionGlobals.hikVisionCameraManager.init();
//                if (errorMessage != null)
//                    return "Error: "+errorMessage;
//                // try hikvision camera login
//                showToast("Scanning for camera, Scanning All devices...",Toast.LENGTH_SHORT);
//                for(int j=0;j<=255;j++) {
//                    if(j==Globals.ipPart4) continue;
//                    String destinationAddress=Globals.IPParts[0]+"."+Globals.IPParts[1]+"."+Globals.IPParts[2]+"."+String.valueOf(j);
//                    showText(destinationAddress);
//                    if(!this.ips.contains(destinationAddress)) {
//                        this.ipaddr= InetAddress.getByName(destinationAddress);
//                        if(this.ipaddr.isReachable(50)) {
//                            this.ips.add(destinationAddress);
//                        }
//                    }
//                }
//                for(int k=0;k<this.ips.size();k++) {
//                    HikVisionGlobals.hikvisionCameraIP=this.ips.get(k);
//                    showText(HikVisionGlobals.hikvisionCameraIP);
//                    errorMessage = HikVisionGlobals.hikVisionCameraManager.login(HikVisionGlobals.hikvisionCameraIP,
//                            HikVisionGlobals.hikvisionPort,HikVisionGlobals.hikvisionUsername,
//                            HikVisionGlobals.hikvisionPassword);
//                    Thread.sleep(1000);
//                    //if (errorMessage != null) return errorMessage;
//                    if (errorMessage != null) continue;
//                    HikVisionGlobals.hikVisionCameraManager.dumpUsefulInfo();
//                    return "Logged In";
//                }
//                return "Error: Could not find camera";
//            } catch(Exception ex) {
//                ex.printStackTrace();
//                return "Error: "+ex.getStackTrace()[0].toString();
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String message) {
//            try {
//                if(message.trim().equals("") || message.trim().startsWith("Error")) {
//                    showToast(message,Toast.LENGTH_LONG);
//                    return;
//                }
//                showToast(message,Toast.LENGTH_LONG);
//            } catch(Exception ex) {
//                ex.printStackTrace();
//                showToast(ex.getMessage(),Toast.LENGTH_LONG);
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//
//        }
//    }

}