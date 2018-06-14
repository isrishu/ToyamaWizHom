package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.model.User;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class UsersActivity extends Activity {

    TextView lblActivityHeading;
    LinearLayout pnlUsers;
    ImageButton ibtnNewUser,ibtnEditUser,ibtnDeleteUser;
    //User ThisUser;
    String receivedMessage = "";

    RemoteAsyncTask remoteTask=null;
    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_users);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        pnlUsers= (LinearLayout) findViewById(R.id.pnlUsers);
        ibtnNewUser=(ImageButton) findViewById(R.id.ibtnNewUser);
        ibtnEditUser=(ImageButton) findViewById(R.id.ibtnEditUser);
        ibtnDeleteUser=(ImageButton) findViewById(R.id.ibtnDeleteUser);

        ibtnEditUser.setEnabled(false);
        ibtnDeleteUser.setEnabled(false);
        ibtnEditUser.setAlpha(.5f);
        ibtnDeleteUser.setAlpha(.5f);

        lblActivityHeading.setText("Users ("+Globals.connectionMode+")");

        ibtnNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Globals.connectionMode.equals("Local")) {
                    Globals.CurrentUser=new User();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    Intent myInt=new Intent(getApplicationContext(),UserActivity.class);
                    UsersActivity.this.startActivity(myInt);
                    ibtnEditUser.setEnabled(false);
                    ibtnDeleteUser.setEnabled(false);
                    ibtnEditUser.setAlpha(.5f);
                    ibtnDeleteUser.setAlpha(.5f);
                } else {
                    showPopupMessage("Feature not enabled in remote mode");
                }
            }
        });

        ibtnEditUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Globals.connectionMode.equals("Local")) {
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    Intent myInt=new Intent(getApplicationContext(),UserActivity.class);
                    UsersActivity.this.startActivity(myInt);
                    ibtnEditUser.setEnabled(false);
                    ibtnDeleteUser.setEnabled(false);
                    ibtnEditUser.setAlpha(.5f);
                    ibtnDeleteUser.setAlpha(.5f);
                } else {
                    showPopupMessage("Feature not enabled in remote mode");
                }
            }
        });

        ibtnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Globals.connectionMode.equals("Local")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UsersActivity.this);
                        if(Globals.CurrentUser.Role==1) {
                            alertDialog.setTitle("Cannot Delete User");
                            alertDialog.setMessage("This user is the master user and cannot be deleted.");
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int which) {
                                    dialog.cancel();
                                }
                            });
                            alertDialog.show();
                        } else {
                            alertDialog.setTitle("Delete User");
                            alertDialog.setMessage("Are you sure you want to delete this user? "+ Globals.CurrentUser.Username);
                            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int which) {
                                    int sid=Globals.CurrentUser.UserId;
                                    sendToGateway("DeleteUser_"+String.valueOf(sid));
                                    ibtnEditUser.setEnabled(false);
                                    ibtnDeleteUser.setEnabled(false);
                                    ibtnEditUser.setAlpha(.5f);
                                    ibtnDeleteUser.setAlpha(.5f);
                                }
                            });
                            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ibtnEditUser.setEnabled(false);
                                    ibtnDeleteUser.setEnabled(false);
                                    ibtnEditUser.setAlpha(.5f);
                                    ibtnDeleteUser.setAlpha(.5f);
                                    dialog.cancel();
                                }
                            });
                            alertDialog.show();
                        }
                    } else {
                        showPopupMessage("Feature not enabled in remote mode");
                    }
                } catch(Exception ex) {
                    showPopupMessage("Error in User delete. Please try again.");
                }
            }
        });
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void generateUsers() {
        pnlUsers.removeAllViews();
        // create the layout params that will be used to define how your button will be displayed
        LinearLayout.LayoutParams btnparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(50));
        LinearLayout.LayoutParams vparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(3));
        try {
            for(User u:Globals.AllUsers) {
                final Button btn = new Button(this);
                // Give button an ID
                btn.setId(u.UserId);
                btn.setTag(u.UserId);
                btn.setText(Utilities.toTitleCase(u.Username));

                btn.setLayoutParams(btnparams);
                btn.setBackgroundColor(getResources().getColor(R.color.transparent));
                btn.setTextColor(getResources().getColor(R.color.black));
                btn.setTextSize(20);
                btn.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                btn.setPadding((int) getPixelValue(15), 0, 0, 0);
                btn.setTypeface(Typeface.MONOSPACE,Typeface.NORMAL);

                btn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Globals.CurrentUser=Utilities.getCurrentUser(Integer.parseInt(String.valueOf(v.getTag())));
                        if(Globals.CurrentUser!=null) {
                            ibtnEditUser.setEnabled(true);
                            ibtnDeleteUser.setEnabled(true);
                            ibtnEditUser.setAlpha(1f);
                            ibtnDeleteUser.setAlpha(1f);
                            return true;
                        } else {
                            Toast.makeText(getApplicationContext(), "Error occured, please try again", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                });

                //Add button to LinearLayout
                pnlUsers.addView(btn);
                // line
                final View v=new View(this);
                vparams.setMargins((int) getPixelValue(7), 0,(int) getPixelValue(7), 0);
                v.setLayoutParams(vparams);
                v.setBackground(getResources().getDrawable(R.drawable.line_lightgray));

                pnlUsers.addView(v);
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
                        UsersActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                                if(receivedMessage.contains("DeleteUserResponse")) {
                                    String[] x=receivedMessage.split("_");
                                    if(Integer.valueOf(x[1])==1) {
                                        Globals.AllUsers.remove(Globals.CurrentUser);
                                        Globals.usersString=Utilities.generateAllUsersString();
                                        generateUsers();
                                        Utilities.writeDataToFile(getApplicationContext());
                                    } else {
                                        showPopupMessage("Error in delete, please try again");
                                    }
                                }
                            }
                        });
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
        if(ibtnEditUser.isEnabled()) {
            Globals.CurrentScene=null;
            Globals.currentSceneId=0;
            ibtnEditUser.setEnabled(false);
            ibtnDeleteUser.setEnabled(false);
            ibtnEditUser.setAlpha(.5f);
            ibtnDeleteUser.setAlpha(.5f);
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
    protected void onResume() {
        super.onResume();
        if(Globals.connectionMode.equals("Local")) {
            connectToGateway();
        }
        if(Globals.AllUsers.size()==0) {
            //showPopupMessage("No Users found. If you are sure you have some, please click Refresh button in Settings page.");
        } else generateUsers();
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
        UsersActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showToast(final String msg,final int len) {
        UsersActivity.this.runOnUiThread(new Runnable() {
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
