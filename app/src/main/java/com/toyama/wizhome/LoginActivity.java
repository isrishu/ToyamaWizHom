package com.toyama.wizhome;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v4.app.ActivityCompat;

public class LoginActivity extends Activity {

    String receivedMessage = "";
    private RemoteLoginTask remoteLoginTask = null;
    private String username,password;
    private boolean isLoggingIn=false;
    String destinationAddress="";

    // UI references.
    private EditText txtUsername,txtPassword;
    private TextView lblStatus,lblIsEthernet;
    private ToggleButton chkRemote,chkIsEthernet;
    private Button btnLogin;
    //ProgressDialog progressDialog=null;
    ProgressBar pbLoginProgress=null;
    LinearLayout pnlIsEthernet;

    GatewayTalkerThread gatewayTalkerThread = null;
    GatewayListenerThread gatewayListenerThread = null;

    private static final int multiplePermissionRequestCode=123;
    private final ExecutorService loginExecutor = Executors.newFixedThreadPool(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        try {
            txtUsername = (EditText) findViewById(R.id.txtUsername);
            txtPassword = (EditText) findViewById(R.id.txtPassword);
            chkRemote=(ToggleButton) findViewById(R.id.chkRemote);
            chkIsEthernet=(ToggleButton) findViewById(R.id.chkIsEthernet);
            lblStatus = (TextView) findViewById(R.id.lblStatus);
            lblIsEthernet = (TextView) findViewById(R.id.lblIsEthernet);
            btnLogin=(Button) findViewById(R.id.btnLogin);
            pbLoginProgress=(ProgressBar) findViewById(R.id.pbLoginProgress);

            lblStatus.setMovementMethod(new ScrollingMovementMethod());

            String[] multiplePermissions = new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE};

            if (Build.VERSION.SDK_INT >= 23) {
//                if(checkSelfPermission(multiplePermissions) == PackageManager.PERMISSION_GRANTED) {
//                    Log.v(TAG,"Permission is granted");
//                } else {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,multiplePermissions)) {
//                        Toast.makeText(LoginActivity.this, "These permissions are required for optimal experience. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
//                    } else {
                ActivityCompat.requestPermissions(LoginActivity.this, multiplePermissions, multiplePermissionRequestCode);
                //}
                //}
            }

//            if(progressDialog==null) {
//                progressDialog=new ProgressDialog(LoginActivity.this);
//                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                progressDialog.setTitle("Logging in");
//                progressDialog.setMessage("Please wait...");
//                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFD4D9D0")));
//                progressDialog.setOwnerActivity(LoginActivity.this);
//            }

            if(Utilities.isDeviceTablet(LoginActivity.this)) {
                chkIsEthernet.setVisibility(View.VISIBLE);
                lblIsEthernet.setVisibility(View.VISIBLE);
            } else {
                chkIsEthernet.setVisibility(View.GONE);
                lblIsEthernet.setVisibility(View.GONE);
            }

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startLoginProcess();
                }
            });

            if(!Globals.didUserLogout) { // meaning user didn't log out, so app is starting
                Globals.socket = null;
                Globals.dataOutputStream = null;
                Globals.dataInputStream = null;
                Globals.serverSocketAddress=null;
                Globals.isGatewayFound=false;

                Globals.gatewayId=0;
                Globals.customerId=0;
                Globals.customerUsername="";
                Globals.password="";
                Globals.networkCableConnectionMode="WiFi";
                Globals.ipList.clear();
                Globals.potentialIPs.clear();

                FileInputStream fis=null;
                InputStreamReader isr=null;
                BufferedReader bufferedReader=null;

                try {
                    fis = openFileInput(Globals.ipFilename);
                    isr = new InputStreamReader(fis);
                    bufferedReader = new BufferedReader(isr);

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if(!line.trim().equals(""))
                            Globals.ipList.add(line);
                    }
                    if(bufferedReader!=null) bufferedReader.close();
                    if(isr!=null) isr.close();
                    if(fis!=null) fis.close();
                } catch(FileNotFoundException ex) {
                    //showException(ex);
                } catch(Exception ex) {
                    //showException(ex);
                } finally {
                    if(bufferedReader!=null) bufferedReader.close();
                    if(isr!=null) isr.close();
                    if(fis!=null) fis.close();
                }
                Globals.sharedPreferences = getApplicationContext().getSharedPreferences(Globals.credsFilename, Context.MODE_PRIVATE);
                Globals.gatewayId=Globals.sharedPreferences.getInt("GatewayId",0);
                Globals.customerId=Globals.sharedPreferences.getInt("CustomerId",0);
                Globals.customerUsername=Globals.sharedPreferences.getString("Username","");
                Globals.password=Globals.sharedPreferences.getString("Password","");
                Globals.networkCableConnectionMode=Globals.sharedPreferences.getString("NetworkConnectionMode","");
                Globals.dbVersion=Double.parseDouble(Globals.sharedPreferences.getString("DBVersion","0.00"));
                Globals.gatewayVersion=Globals.sharedPreferences.getString("GatewayVersion","0");
                Utilities.initializeValuesForGateway();
            }
        } catch (IOException e) {
            e.printStackTrace();
            lblStatus.setText(e.getMessage());
        } catch(Exception ex) {
            ex.printStackTrace();
            lblStatus.setText(ex.getMessage());
        }
    }

    private void sendToGateway(String msg) {
        if(gatewayTalkerThread==null){
            return;
        }
        gatewayTalkerThread.sendMessage(msg);
    }

    private void connectToGateway() {
        gatewayListenerThread = new GatewayListenerThread();
        gatewayTalkerThread = new GatewayTalkerThread();

        loginExecutor.submit(gatewayListenerThread);
        loginExecutor.submit(gatewayTalkerThread);
    }

    private boolean isIPInTheSameNetwork(String ip) {
        String [] IPParts=ip.split("\\.");
        // this is 3rd part of this device's ip address
        if(!IPParts[2].equals(Globals.IPParts[2])) return false;
        // this is last part of this device's ip address,
        // checking if equal to this device's IP address
        if(Integer.valueOf(IPParts[3])==Globals.ipPart4) return false;
        return true;
    }

    private class GatewayTalkerThread extends Thread {
        ArrayList<String> ips=new ArrayList<String>();

        String messageToSend = "";
        boolean goOut = false;

        GatewayTalkerThread() {
            this.messageToSend="";
            this.goOut=false;
            this.ips.clear();
        }

        private void sendLoginMessage(String ipAddress) {
            try {
                // inducing sleep because somehow the listener isnt starting up fast enough or picking up a reply fast enough
                //Thread.sleep(2000);
                int timeoutMs = 800; // 200 ms
                Globals.serverSocketAddress = new InetSocketAddress(ipAddress,Globals.serverSocketPORT);
                Globals.socket = new Socket();
                Globals.socket.connect(Globals.serverSocketAddress,timeoutMs);
                Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
                Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
                Globals.dataOutputStream.writeUTF(Globals.loginMessageToSend);
                Globals.dataOutputStream.flush();
            } catch (IOException e) {
            }
//            catch (InterruptedException e) {
//            }
        }

        @Override
        public void run() {
            if(Globals.isUserJustLoggedIn) return;
            InetAddress ipaddr=null;
            Globals.loginMessageToSend="PermissionToConnect_"+Globals.customerUsername+"_"+Globals.password+"_"+Globals.dataTransferMode;
            showMessage("Connecting to gateway...");
            try {
                if(Globals.gatewayIPAddress.equals("")) Globals.isGatewayFound=false;
                if(Globals.isGatewayFound) {
                    if(!this.ips.contains(Globals.gatewayIPAddress)) {
                        this.ips.add(Globals.gatewayIPAddress);
                    }
                    //meaning user has logged out and is logging in, also means gateway has been found already
                    this.sendLoginMessage(Globals.gatewayIPAddress);
                } else {
                    showMessage("Scanning for gateway, Scanning in known devices...");
                    // checking recently connected IP Addresses
                    for(String ip:Globals.ipList) {
                        if(ip.equals("") || ip.equals(null) || ip.equals("null")) continue;
                        if(isIPInTheSameNetwork(ip)) {
                            if(!this.ips.contains(ip)) {
                                ipaddr=InetAddress.getByName(ip);
                                if(ipaddr.isReachable(100)) {
                                    this.ips.add(ip);
                                }
                            }
                        }
                    }
                    if(!Globals.isGatewayFound) {
                        for(int i=0;i<this.ips.size();i++) {
                            setLoginProgress(i*5);
                            if(Globals.isGatewayFound) {
                                break;
                            }
                            destinationAddress=this.ips.get(i);
                            sendLoginMessage(destinationAddress);
                        }
                    }
                    if(!Globals.isGatewayFound) {
                        showMessage("Scanning for gateway, Scanning All devices...");
                        for(int j=0;j<=255;j++) {
                            // this is last part of this device's ip address
                            if(j==Globals.ipPart4) continue;
                            destinationAddress=Globals.IPParts[0]+"."+Globals.IPParts[1]+"."+Globals.IPParts[2]+"."+String.valueOf(j);
                            //showMessage(destinationAddress);
                            if(!this.ips.contains(destinationAddress)) {
                                ipaddr=InetAddress.getByName(destinationAddress);
                                if(ipaddr.isReachable(100)) {
                                    this.ips.add(destinationAddress);
                                }
                            }
                        }
                        for(int k=0;k<this.ips.size();k++) {
                            setLoginProgress(k*5%100);
                            if(Globals.isGatewayFound) {
                                break;
                            }
                            destinationAddress=this.ips.get(k);
                            sendLoginMessage(destinationAddress);
                        }
                    }
                }
            } catch (IOException e) {
//                if(Globals.connectionMode.equals("Local") && gatewayTalkerThread!=null)
//                    gatewayTalkerThread.disconnect();
//                return;
            }
            if(Globals.connectionMode.equals("Local") && gatewayTalkerThread!=null) {
                setLoginProgress(1);
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnLogin.setEnabled(true);
                        Globals.isUserJustLoggedIn=false;
                        isLoggingIn=false;
                    }
                });
                gatewayTalkerThread.disconnect();
                showMessage("Scanning done, gateway not found. Please try again.");
            }
        }

        private void sendMessage(String msg){
            this.messageToSend = msg;
        }

        private void disconnect(){
            this.goOut = true;
        }
    }

    private class GatewayListenerThread extends Thread {
        boolean goOut = false;
        int responseCode=0;
        int i=0;

        GatewayListenerThread() {
            this.goOut=false;
        }

        @Override
        public void run() {
            while (!this.goOut) {
                try {
                    //showMessage("Listening..."+String.valueOf(++i));
                    if (Globals.dataInputStream!=null && Globals.dataInputStream.available() > 0) {
                        receivedMessage = Globals.dataInputStream.readUTF();
                        if(receivedMessage.contains("PermissionResponse")) {
                            gatewayTalkerThread.disconnect();
                            this.disconnect();
                            Globals.isGatewayFound=true;
                            setLoginProgress(99);
                            String[] x=receivedMessage.split("_");
                            if(x.length>1) {
                                responseCode=Integer.parseInt(x[1]);
                                switch(responseCode) {
                                    case 1: // success
                                        try {
                                            if(x.length>3) {
                                                Globals.gatewayId = Integer.parseInt(x[2]);
                                                Globals.customerId = Integer.parseInt(x[3]);
                                            }
                                            if(x.length>4) {
                                                Globals.isUserMaster=(Integer.parseInt(x[4])==1);
                                                Globals.dbVersion=Double.parseDouble(x[5].trim());
                                                Globals.gatewayVersion=x[6];
                                            }
                                            if(x.length>7) {
                                                Globals.gatewayIPAddress=x[7];
                                            }
                                            showMessage("Logged In, please wait...");
                                            Globals.isUserJustLoggedIn=true;
                                            isLoggingIn=false;
                                            Globals.didUserLogout=false;
                                            LoginActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                                                    Intent myInt=new Intent(getApplicationContext(),MainActivity.class);
                                                    LoginActivity.this.startActivity(myInt);
                                                }
                                            });
                                        } catch(Exception ex) {
                                            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case 2: // invalid credentials
                                        LoginActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                btnLogin.setEnabled(true);
                                                isLoggingIn=false;
                                                showMessage("Invalid Credentials. Please try again.");
                                                setLoginProgress(0);
                                            }
                                        });
                                        break;
                                    case 3: default: // gateway busy or other error
                                        LoginActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                btnLogin.setEnabled(true);
                                                isLoggingIn=false;
                                                showMessage("Please check if Gateway and Wifi are running. "
                                                        + "If yes, restart them. If the problem persists, contact Sys Admin. 3");
                                                setLoginProgress(0);
                                            }
                                        });
                                        break;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
//                if(Globals.connectionMode.equals("Local") && gatewayListenerThread!=null)
//                    gatewayListenerThread.disconnect();
                }
            }// of while
            //showMessage("Stopped Listening...");
        }

        private void disconnect(){
            this.goOut = true;
        }
    }

//    private void showLoginProgress(final boolean show) {
//        LoginActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(show) {
//                    progressDialog.show();
//                } else {
//                    if(progressDialog !=null) {
//                        if(progressDialog.isShowing()) {
//                            progressDialog.dismiss();
//                        }
//                    }
//                }
//            }
//        });
//    }

    public class RemoteLoginTask extends AsyncTask<String, Integer, String> {
        ServiceLayer svc;

        public RemoteLoginTask() {

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                svc=new ServiceLayer();
                String mode=params[0];
                if(svc.login()) {
                    return svc.getResponseString();
                }
                else {
                    return svc.getResponseString();
                }
            } catch(Exception ex) {
                return ex.getStackTrace()[0].toString();
            }
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                remoteLoginTask = null;
                btnLogin.setEnabled(true);
                isLoggingIn=false;
                if(message.contains("#")) {
                    String[] responses=message.split("#");
                    if(responses[0].equals("1")) {
                        showMessage("Logged In...");
                        setLoginProgress(100);
                        //showLoginProgress(false);
                        overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                        Intent myInt=new Intent(getApplicationContext(),MainActivity.class);
                        LoginActivity.this.startActivity(myInt);
                        return;
                    } else {
                        showMessage(responses[1]);
                        setLoginProgress(0);
                    }
                } else {
                    if (message.equals("1")) {
                        showMessage("Logged In...");
                        setLoginProgress(100);
                        overridePendingTransition(R.anim.fadeout,R.anim.fadein);
                        Intent myInt=new Intent(getApplicationContext(),MainActivity.class);
                        LoginActivity.this.startActivity(myInt);
                        return;
                    } else {
                        showMessage(message);
                    }
                }
                svc.close();
            } catch(Exception ex) {

            }
        }

        @Override
        protected void onCancelled() {
            remoteLoginTask = null;
            setLoginProgress(0);
            btnLogin.setEnabled(true);
            isLoggingIn=false;
        }
    }

    @Override
    protected void onDestroy() {
        if(Globals.connectionMode.equals("Local")) {
            if(gatewayTalkerThread!=null) {
                gatewayTalkerThread.disconnect();
                gatewayTalkerThread=null;
            }
            if(gatewayListenerThread!=null) {
                gatewayListenerThread.disconnect();
                gatewayListenerThread=null;
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean isFinishing() {
        if(Globals.connectionMode.equals("Local")) {
            if(gatewayTalkerThread!=null) {
                gatewayTalkerThread.disconnect();
                gatewayTalkerThread=null;
            }
            if(gatewayListenerThread!=null) {
                gatewayListenerThread.disconnect();
                gatewayListenerThread=null;
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnLogin.requestFocus();
        if(!isLoggingIn) {
            btnLogin.setEnabled(true);
            Globals.isUserJustLoggedIn=false;
            isLoggingIn=false;
            Globals.remoteServiceCaller="wizhom";
            if(!Globals.customerUsername.equals("") && !Globals.password.equals("")) {
                // username and password found
                try {
                    txtUsername.setText(Globals.customerUsername);
                    txtPassword.setText(Globals.password);
                    if(Globals.networkCableConnectionMode.equals("Ethernet")) {
                        chkIsEthernet.setChecked(true);
                    }
                    Utilities.hideKeyboard(LoginActivity.this);
                    // because when user logs out variable is set to true, we dont want to login automatically
                    if(Globals.didUserLogout) {
                        if(Globals.connectionMode.equals("Remote"))
                            chkRemote.setChecked(true);
                        else
                            chkRemote.setChecked(false);
                    } else {
                        if(chkRemote.isChecked()) {
                            Globals.connectionMode="Remote";
                        } else {
                            Globals.connectionMode="Local";
                            WifiManager wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            if (!wifiMgr.isWifiEnabled()) {
                                // check if the device has an IP address, presumably from the local wifi router i.e. same logic as in gateway
                                if(Globals.networkCableConnectionMode.equals("Ethernet")) {
                                    ConnectivityManager cm= (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo activeNetwork=cm.getActiveNetworkInfo();
                                    boolean isConnected= (activeNetwork!=null && activeNetwork.isConnected());
                                    boolean isEthernet=(activeNetwork.getType()==ConnectivityManager.TYPE_ETHERNET);
                                    if(!isConnected) {
                                        lblStatus.setText("Please check if you are connected to a network and try again");
                                        return;
                                    }
                                } else {
                                    lblStatus.setText("Local WiFi not connected, switching to remote login mode.");
                                    Globals.connectionMode="Remote";
                                    //lblStatus.setText("Please check if you are connected to your local Wifi network and try again");
                                    //return;
                                }
                            }
                        }
                        Utilities.parseLocalIPAddress();
                        attemptLogin();
                    }
                } catch(Exception ex) {
                    lblStatus.setText("Please check if your device is connected to a WiFi network and try again");
                }
            } else {
                txtUsername.requestFocus();
            }
        }
    }

    private void startLoginProcess() {
        try {
            Globals.didUserLogout=false; // after logout this is an attempt to login again
            if(chkIsEthernet.isChecked()) {
                Globals.networkCableConnectionMode="Ethernet";
            } else {
                Globals.networkCableConnectionMode="WiFi";
            }
            if(chkRemote.isChecked()) {
                Globals.connectionMode="Remote";
            } else {
                Globals.connectionMode="Local";
                WifiManager wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (!wifiMgr.isWifiEnabled()) {
                    // check if the device has an IP address, presumably from the local wifi router i.e. same as gateway
                    if(Globals.networkCableConnectionMode.equals("Ethernet")) {
                        ConnectivityManager cm= (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork=cm.getActiveNetworkInfo();
                        boolean isConnected= (activeNetwork!=null && activeNetwork.isConnected());
                        boolean isEthernet=(activeNetwork.getType()==ConnectivityManager.TYPE_ETHERNET);
                        if(!isConnected) {
                            lblStatus.setText("Please check if you are connected to a network and try again");
                            return;
                        }
                    } else {
                        lblStatus.setText("Please check if you are connected to your local Wifi network and try again");
                        return;
                    }
                }
            }
            Utilities.parseLocalIPAddress();
            Utilities.hideKeyboard(LoginActivity.this);
            attemptLogin();
        } catch(Exception ex) {
            lblStatus.setText("Please check if your device is connected to a WiFi network and try again");
        }
    }

    public void attemptLogin() {
        if (remoteLoginTask != null) {
            return;
        }
        // another login is already in progress
        if(isLoggingIn || Globals.isUserJustLoggedIn) return;
        // Store values at the time of the login attempt.
        username = txtUsername.getText().toString().trim();
        password = txtPassword.getText().toString().trim();

        if(username.isEmpty()) {
            showMessage("Username is required");
            txtUsername.requestFocus();
            return;
        }
        if(password.isEmpty()) {
            showMessage("Password is required");
            txtPassword.requestFocus();
            return;
        }
        btnLogin.setEnabled(false); // to disable multiple clicks

        Globals.customerUsername=username;
        Globals.password=password;

        isLoggingIn=true;
        //showLoginProgress(true);

        if(Globals.connectionMode.equals("Local")) {
            connectToGateway();
        } else {
            if(Globals.gatewayId>0) {
                lblStatus.setText("Connecting to remote server...");
//                if (progressDialog !=null && progressDialog.isShowing()) {
//                    progressDialog.setMessage("Connecting to remote server, please wait...");
//                }
                remoteLoginTask = new RemoteLoginTask();
                remoteLoginTask.execute("");
            } else {
                lblStatus.setText("Your WizHom app is not initialized. "
                        + "Please sign in to your gateway in Local mode atleast once.");
//                if (progressDialog !=null && progressDialog.isShowing()) {
//                    progressDialog.setMessage("Your WizHom app is not initialized. Please sign in locally.");
//                    progressDialog.setCanceledOnTouchOutside(true);
//                }
                return;
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case storagePermissionRequestCode:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.e("value", "Permission Granted, Now you can use local drive .");
//                } else {
//                    Log.e("value", "Permission Denied, You cannot use local drive .");
//                }
//                break;
//        }
//    }

    private void showMessage(final String msg) {
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lblStatus.setText(msg);
            }
        });
    }

    private void appendMessage(final String msg) {
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lblStatus.append(msg+"\n");
            }
        });
    }

    private void setLoginProgress(final int value) {
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbLoginProgress.setProgress(value);
            }
        });
    }

    private void showException(Exception ex) {
        if (BuildConfig.DEBUG) {
            String ex1="";
            for(StackTraceElement a1:ex.getStackTrace()) {
                ex1+=a1.toString();
            }
            showMessage("Err "+ex.getMessage());
        }
    }
}