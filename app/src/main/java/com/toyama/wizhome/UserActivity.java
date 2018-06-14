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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class UserActivity extends Activity {

    TextView lblActivityHeading;
    String receivedMessage = "",newUsername="",newPassword="";
    EditText txtUsername,txtPassword;//,txtFirstname,txtLastname;
    ToggleButton chkShowPassword;

    RemoteAsyncTask remoteTask=null;
    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user);

        txtUsername=(EditText) findViewById(R.id.txtUsername);
        txtPassword=(EditText) findViewById(R.id.txtPassword);
        chkShowPassword=(ToggleButton) findViewById(R.id.chkShowPassword);

        txtUsername.setText(Globals.CurrentUser.Username);
        txtPassword.setText(Globals.CurrentUser.Password);

        if(Globals.CurrentUser.UserId==0) { // new user
            txtUsername.setEnabled(true);
            txtPassword.setEnabled(true);
        } else {
            txtUsername.setEnabled(false);
            txtPassword.setEnabled(false);
        }

        chkShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkShowPassword.isChecked()) {
                    txtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                txtPassword.setSelection(txtPassword.length());
            }
        });

        Button btn=(Button) findViewById(R.id.btnSaveUser);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String un=txtUsername.getText().toString().trim().replace("\n","");
                String pwd=txtPassword.getText().toString().trim().replace("\n","");
                Globals.CurrentUser.Username=un;
                Globals.CurrentUser.Password=pwd;
                Globals.CurrentUser.Role=2;
                if(un.equals("") || pwd.equals("")) {
                    Toast.makeText(getApplicationContext(), "All fields are required.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(Globals.CurrentUser.UserId==0) { // new user
                    if(Utilities.isUsernameExists(un)) {
                        showPopupMessage("Username exists. Please provide a different one.");
                        return;
                    } else {
                        sendToGateway("AddUser_"+un+"_"+pwd+"_Firstname_Lastname");
                    }
                } else {
                    sendToGateway("UpdateUser_"+String.valueOf(Globals.CurrentUser.UserId)+"_Firstname_Lastname");
                }
                return;
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
                        if(receivedMessage.startsWith("AddUserResponse")) {
                            String[] x=receivedMessage.split("_");
                            int response=Integer.valueOf(x[1]);
                            switch(response) {
                                case 1:
                                    if(Globals.CurrentUser.UserId==0) { // new user
                                        Globals.CurrentUser.UserId=Integer.valueOf(x[2]);
                                        if(!Globals.AllUsers.contains(Globals.CurrentUser))
                                            Globals.AllUsers.add(Globals.CurrentUser);
                                    }
                                    Globals.usersString=Utilities.generateAllUsersString();
                                    showPopupMessage("User Saved");
                                    Utilities.writeDataToFile(getApplicationContext());
                                    break;
                                case 2:
                                    showPopupMessage("Username already Exists.");
                                    break;
                                case 3:
                                    showPopupMessage("Error in adding User, please try again.");
                                    break;
                                case 4:
                                    showPopupMessage("You have to be logged in as the master user.");
                                    break;
                                default:
                                    break;
                            }
                        }
                        if(receivedMessage.startsWith("UpdateUserResponse")) {
                            String[] x=receivedMessage.split("_");
                            int response=Integer.valueOf(x[1]);
                            switch(response) {
                                case 1:
                                    Globals.CurrentUser.Firstname=x[2].trim();
                                    Globals.CurrentUser.Lastname=x[3].trim();
                                    Globals.usersString=Utilities.generateAllUsersString();
                                    showPopupMessage("User Saved");
                                    Utilities.writeDataToFile(getApplicationContext());
                                    break;
                                default:
                                    showPopupMessage("Error in editing User, please try again.");
                                    break;
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
    public void onBackPressed() {
        super.onBackPressed();
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

    private void showPopupMessage(final String msg) {
        UserActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showToast(final String msg,final int len) {
        UserActivity.this.runOnUiThread(new Runnable() {
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