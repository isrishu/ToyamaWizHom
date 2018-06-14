package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    String receivedMessage = "";
    String currentMode="";
    String currentRemoteMode="";
    byte[] fileContents;
    private ServiceTask backupRefreshTask = null;
    ProgressDialog pDialog;

    private Button btnRefresh,btnLogout,btnUsers,btnVersion,btnBackup;
    TextView lblActivityHeading;

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        lblActivityHeading.setText("Settings ("+Globals.connectionMode+")");

        btnRefresh=(Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Globals.connectionMode.equals("Local")) {
                    showPopupMessage("Alert","This feature is only available when you are connected to the gateway locally.");
                    return;
                }
                pDialog = ProgressDialog.show(SettingsActivity.this, "Refreshing", "Please wait...", true);
                pDialog.setOwnerActivity(SettingsActivity.this);

                // starts sending messages so we get data one after the other in sequence
                Globals.usersString="";
                Globals.roomsString="";
                Globals.scenesString="";
                Globals.schedulesString="";
                Globals.sensorsString="";
                Globals.irDevicesString="";
                Globals.irBlastersString="";
                Globals.camerasString="";
                currentMode="GetUsers";
                sendToGateway("GetUsers");
                showPopupMessage("","Getting Users...");
            }
        });

        btnLogout=(Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(Globals.connectionMode.equals("Local")) {
                        if (Globals.socket==null || Globals.socket.isClosed()) {
                            try {
                                Globals.didUserLogout=true;
                                if(Globals.socket != null) Globals.socket.close();
                                if (Globals.dataOutputStream != null) Globals.dataOutputStream.close();
                                if (Globals.dataInputStream != null) Globals.dataInputStream.close();
                                if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
                                    gatewayConnectThread.disconnect();
                                overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                                Intent myInt=new Intent(getApplicationContext(),LoginActivity.class);
                                myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                SettingsActivity.this.startActivity(myInt);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            sendToGateway("UserDisconnect_"+Globals.customerUsername);
                            currentMode="Logout";
                        }
                    } else {
                        Globals.didUserLogout=true;
                        overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                        Intent myInt=new Intent(getApplicationContext(),LoginActivity.class);
                        myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        SettingsActivity.this.startActivity(myInt);
                    }
                } catch(Exception ex) {
                    Globals.didUserLogout=true;
                    if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
                        gatewayConnectThread.disconnect();
                    overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                    Intent myInt=new Intent(getApplicationContext(),LoginActivity.class);
                    myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    SettingsActivity.this.startActivity(myInt);
                }
                return;
            }
        });

        btnUsers=(Button) findViewById(R.id.btnUsers);
        btnUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                Intent myInt=new Intent(getApplicationContext(),UsersActivity.class);
                myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                SettingsActivity.this.startActivity(myInt);
                return;
            }
        });

        btnVersion=(Button) findViewById(R.id.btnVersion);
        btnVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String versionString = "WizHom Version: "+getPackageManager().getPackageInfo(getPackageName(), 0).versionName+"\n"
                            +"Gateway Version: "+String.valueOf(Globals.gatewayVersion)+"\n"
                            +"Database Version: "+String.valueOf(Globals.dbVersion)+"\n";
                    showPopupMessage("Version",versionString);
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }
        });

        btnBackup=(Button) findViewById(R.id.btnBackup);
        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog = ProgressDialog.show(SettingsActivity.this, "Backup in progress", "Please wait...", true);
                pDialog.setOwnerActivity(SettingsActivity.this);
                pDialog.setCanceledOnTouchOutside(true);

                if (pDialog!=null && pDialog.isShowing()) {
                    pDialog.setMessage("Uploading Users...");
                }
                currentRemoteMode="uploadUsers";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                return;
            }
        });

        if(Globals.isUserMaster) {
            btnUsers.setVisibility(View.VISIBLE);
        } else {
            btnUsers.setVisibility(View.GONE);
        }
    }

    private void processServiceResponse() {
        switch(currentRemoteMode) {
            case "uploadUsers":
                showPopupMessage("","Uploading Rooms...");
                currentRemoteMode="uploadRooms";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                break;
            case "uploadRooms":
                showPopupMessage("","Uploading Scenes...");
                currentRemoteMode="uploadScenes";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                break;
            case "uploadScenes":
                showPopupMessage("","Uploading Schedules...");
                currentRemoteMode="uploadSchedules";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                break;
            case "uploadSchedules":
                showPopupMessage("","Uploading Sensors...");
                currentRemoteMode="uploadSensors";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                break;
            case "uploadSensors":
                showPopupMessage("","Uploading IR Blasters...");
                currentRemoteMode="uploadIRBlasters";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                break;
            case "uploadIRBlasters":
                showPopupMessage("","Uploading IR Devices...");
                currentRemoteMode="uploadIRDevices";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                break;
            case "uploadIRDevices":
                showPopupMessage("","Uploading Set Top Boxes...");
                currentRemoteMode="uploadSetTopBoxes";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                break;
            case "uploadSetTopBoxes":
                showPopupMessage("","Uploading Cameras...");
                currentRemoteMode="uploadCameras";
                backupRefreshTask=new ServiceTask();
                backupRefreshTask.execute(currentRemoteMode);
                break;
            case "uploadCameras":
                showPopupMessage("","Backup Done.\n");
                if(pDialog!=null) {
                    pDialog.setCanceledOnTouchOutside(true);
                }
                break;
            default:
                break;
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
                        switch(currentMode) {
                            case "GetUsers":
                                if(receivedMessage.startsWith("<Users>") && receivedMessage.endsWith("</Users>")) {
                                    Globals.usersString=receivedMessage.trim().replace("\n","");
                                    receivedMessage="";
                                    Thread.sleep(1000);
                                    currentMode="GetRooms";
                                    sendToGateway("GetRooms");
                                    showPopupMessage("","Getting Rooms...");
                                } else {
                                    showPopupMessage("","Error in Users.");
                                }
                                break;
                            case "GetRooms":
                                if(receivedMessage.startsWith("<Rooms>") && receivedMessage.endsWith("</Rooms>")) {
                                    Globals.roomsString=receivedMessage.trim().replace("\n","");
                                    receivedMessage="";
                                    Thread.sleep(1000);
                                    currentMode="GetScenes";
                                    sendToGateway("GetScenes");
                                    showPopupMessage("","Getting Scenes...");
                                } else {
                                    showPopupMessage("","Error in rooms.");
                                }
                                break;
                            case "GetScenes":
                                if(receivedMessage.startsWith("<Scenes>") && receivedMessage.endsWith("</Scenes>")) {
                                    Globals.scenesString=receivedMessage.trim().replace("\n","");
                                    receivedMessage="";
                                    Thread.sleep(1000);
                                    currentMode="GetSchedules";
                                    sendToGateway("GetSchedules");
                                    showPopupMessage("","Getting Schedules...");
                                } else {
                                    showPopupMessage("","Error in scenes.");
                                }
                                break;
                            case "GetSchedules":
                                if(receivedMessage.startsWith("<Schedules>") && receivedMessage.endsWith("</Schedules>")) {
                                    Globals.schedulesString=receivedMessage.trim().replace("\n","");
                                    receivedMessage="";
                                    Thread.sleep(1000);
                                    currentMode="GetSensors";
                                    sendToGateway("GetSensors");
                                    showPopupMessage("","Getting Sensors...");
                                } else {
                                    showPopupMessage("","Error in schedules.");
                                }
                                break;
                            case "GetSensors":
                                if(receivedMessage.startsWith("<Sensors>") && receivedMessage.endsWith("</Sensors>")) {
                                    Globals.sensorsString=receivedMessage.trim().replace("\n","");
                                    receivedMessage="";
                                    Thread.sleep(1000);
                                    currentMode="GetIRDevices";
                                    sendToGateway("GetIRDevices");
                                    showPopupMessage("","Getting IR Data...");
                                } else {
                                    showPopupMessage("","Error in Sensors.");
                                }
                                break;
                            case "GetIRDevices":
                                if(receivedMessage.startsWith("<IRDevices>") && receivedMessage.endsWith("</IRDevices>")) {
                                    Globals.irDevicesString=receivedMessage.trim().replace("\n","");
                                    receivedMessage="";
                                    Thread.sleep(1000);
                                    currentMode="GetIRBlasters";
                                    sendToGateway("GetIRBlasters");
                                } else {
                                    showPopupMessage("","Error in IR Devices.");
                                }
                                break;
                            case "GetIRBlasters":
                                if(receivedMessage.startsWith("<IRBlasters>") && receivedMessage.endsWith("</IRBlasters>")) {
                                    Globals.irBlastersString=receivedMessage.trim().replace("\n","");
                                    receivedMessage="";
                                    Thread.sleep(1000);
                                    currentMode="GetCameras";
                                    sendToGateway("GetCameras");
                                    showPopupMessage("","Getting Cameras...");
                                } else {
                                    showPopupMessage("","Error in IR Data.");
                                }
                                break;
                            case "GetCameras":
                                if(receivedMessage.startsWith("<Cameras>") && receivedMessage.endsWith("</Cameras>")) {
                                    Globals.camerasString=receivedMessage.trim().replace("\n","");
                                    receivedMessage="";
                                    Thread.sleep(500);
                                    //if(writeDataToFile()) {
                                    if(Utilities.writeDataToFile(getApplicationContext())) {
                                        showPopupMessage("","Done refreshing.");
                                        currentMode="";
                                    } else {
                                        showPopupMessage("","Error in refreshing.");
                                    }
                                } else {
                                    showPopupMessage("","Error in Cameras Data.");
                                }
                                if(pDialog!=null)
                                    pDialog.setCanceledOnTouchOutside(true);
                                break;
                            case "Logout":
                                if(receivedMessage.contains("UserDisconnect")) {
                                    Globals.didUserLogout=true;
                                    goOut=true;
                                    if (Globals.dataOutputStream != null) Globals.dataOutputStream.close();
                                    if (Globals.dataInputStream != null) Globals.dataInputStream.close();
                                    if (Globals.socket != null) Globals.socket.close();
                                    overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                                    Intent myInt=new Intent(getApplicationContext(),LoginActivity.class);
                                    myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    SettingsActivity.this.startActivity(myInt);
                                    return;
                                }
                                break;
                            default: break;
                        }
                    }
                    if(!messageToSend.equals("")) {
                        Globals.dataOutputStream.writeUTF(messageToSend);
                        Globals.dataOutputStream.flush();
                        messageToSend = "";
                    }
                } catch(UnknownHostException ex) {
                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    if(currentMode.equals("Logout")) {
                        Globals.didUserLogout=true;
                        goOut=true;
                        overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                        Intent myInt=new Intent(getApplicationContext(),LoginActivity.class);
                        myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        SettingsActivity.this.startActivity(myInt);
                        return;
                    }
                } catch (IOException ex) {
                    if(currentMode.equals("Logout")) {
                        Globals.didUserLogout=true;
                        goOut=true;
                        overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                        Intent myInt=new Intent(getApplicationContext(),LoginActivity.class);
                        myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        SettingsActivity.this.startActivity(myInt);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    if(currentMode.equals("Logout")) {
                        Globals.didUserLogout=true;
                        goOut=true;
                        overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                        Intent myInt=new Intent(getApplicationContext(),LoginActivity.class);
                        myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        SettingsActivity.this.startActivity(myInt);
                        return;
                    }
                } catch(Exception e) {
                    showToast(Globals.unexpectedErrorMessage,Toast.LENGTH_SHORT);
                    e.printStackTrace();
                    if(currentMode.equals("Logout")) {
                        Globals.didUserLogout=true;
                        goOut=true;
                        overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                        Intent myInt=new Intent(getApplicationContext(),LoginActivity.class);
                        myInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        SettingsActivity.this.startActivity(myInt);
                        return;
                    }
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
        //if(Tools.isLoggedOut) finish();
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
    public boolean isFinishing() {
        if(Globals.connectionMode.equals("Local") && gatewayConnectThread!=null)
            gatewayConnectThread.disconnect();
        return true;
    }

    private void showToast(final String msg) {
        SettingsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        if(Globals.connectionMode.equals("Local")) {
            connectToGateway();
        }
        super.onResume();
    }

    private void showException(Exception ex) {
        if (BuildConfig.DEBUG) {
            showPopupMessage("Error",ex.getMessage());
        }
    }

    public class ServiceTask extends AsyncTask<String, Integer, String> {
        ServiceLayer svc;

        public ServiceTask() {
            //svc=new ServiceLayer();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                svc=new ServiceLayer();
                String mode=params[0];
                String status="";
                switch(mode) {
                    case "uploadUsers":
                        if(svc.uploadUsers()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "uploadRooms":
                        if(svc.uploadRooms()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "uploadScenes":
                        if(svc.uploadScenes()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "uploadSchedules":
                        if(svc.uploadSchedules()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "uploadSensors":
                        if(svc.uploadSensors()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "uploadIRBlasters":
                        if(svc.uploadIRBlasters()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "uploadIRDevices":
                        if(svc.uploadIRDevices()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "uploadSetTopBoxes":
                        if(svc.uploadSetTopBoxes()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "uploadCameras":
                        if(svc.uploadCameras()) {
                            return svc.getResponseString();
                        } else {
                            return svc.getResponseString();
                        }
                    case "Restore":
                        break;
                }
                return status;
            } catch(Exception ex) {
                return "In Service Task "+ex.getStackTrace()[0].toString();
            }
        }

        @Override
        protected void onPostExecute(String message) {
            backupRefreshTask = null;
            if(message.contains("#")) {
                String[] responses=message.split("#");
                if(responses[0].equals("1")) {
                    processServiceResponse();
                } else {
                    showPopupMessage("Info",responses[1]);
                }
            } else {
                if(message.equals("1")) {
                    processServiceResponse();
                } else {
                    showPopupMessage("Info",message);
                }
            }
        }

        @Override
        protected void onCancelled() {
            backupRefreshTask = null;
        }
    }

    private void showPopupMessage(final String title,final String msg) {
        SettingsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pDialog!=null && pDialog.isShowing()) {
                    if(!title.equals(""))
                        pDialog.setTitle(title);
                    pDialog.setMessage(msg);
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
                    if(!title.equals(""))
                        dialogBuilder.setTitle(title);
                    dialogBuilder.setMessage(msg);
                    dialogBuilder.setCancelable(true);

                    dialogBuilder.setPositiveButton("Ok",null);
                    dialogBuilder.show();
                }
            }
        });
    }

    private void showToast(final String msg,final int len) {
        SettingsActivity.this.runOnUiThread(new Runnable() {
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