package com.toyama.wizhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.Utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    TextView lblHeader;
    String recdmsg = "";
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        try {
            lblHeader=(TextView)findViewById(R.id.lblActivityHeading);
            lblHeader.setText("Home ("+ Globals.connectionMode+")");
            if(Globals.isUserJustLoggedIn && Globals.connectionMode.equals("Local")) {
                // to not write preferences everytime main form is loaded. they are being overwritten sometimes

                // writing last connected IP into a private file
                StringBuilder sb=new StringBuilder();
                if(!Globals.ipList.contains(Globals.gatewayIPAddress)) {
                    Globals.ipList.add(0,Globals.gatewayIPAddress);
                } else {
                    // bring to first
                    Globals.ipList.remove(Globals.gatewayIPAddress);
                    Globals.ipList.add(0,Globals.gatewayIPAddress);
                }
                // later write code to store only last 25 or 50 IPs???
                for(String s:Globals.ipList) {
                    sb.append(s).append("\n");
                }
                FileOutputStream fos;
                try {
                    fos = openFileOutput(Globals.ipFilename, Context.MODE_PRIVATE);
                    fos.write(sb.toString().getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Globals.sharedPreferences = getApplicationContext().getSharedPreferences(Globals.credsFilename, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = Globals.sharedPreferences.edit();
                editor.putInt("GatewayId",Globals.gatewayId);
                editor.putInt("CustomerId",Globals.customerId);
                editor.putString("Username",Globals.customerUsername);
                editor.putString("Password",Globals.password);
                editor.putString("NetworkConnectionMode",Globals.networkCableConnectionMode);
                editor.putString("DBVersion",String.valueOf(Globals.dbVersion));
                editor.putString("GatewayVersion",Globals.gatewayVersion);
                if(editor.commit())
                    Toast.makeText(getApplicationContext(),"Prefs Saved",Toast.LENGTH_SHORT).show();
            }

            Button btn=(Button) findViewById(R.id.btnControl);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try	{
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        Globals.isForScene=false;
                        Intent myInt=new Intent(getApplicationContext(),RoomsActivity.class);
                        MainActivity.this.startActivity(myInt);
                    } catch(Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            btn=(Button) findViewById(R.id.btnClimate);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        Intent myInt=new Intent(getApplicationContext(),ClimateActivity.class);
                        MainActivity.this.startActivity(myInt);
                    } catch(Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            btn=(Button) findViewById(R.id.btnMedia);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        Intent myInt=new Intent(getApplicationContext(),MediaActivity.class);
                        MainActivity.this.startActivity(myInt);
                    } catch(Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            btn=(Button) findViewById(R.id.btnScenes);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        Globals.isForScene=true;
                        Intent myIntent=new Intent(MainActivity.this,ScenesActivity.class);
                        MainActivity.this.startActivity(myIntent);
                    }
                    catch(Exception ex)	{
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            btn=(Button) findViewById(R.id.btnCameras);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
//                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
//                        Intent myInt=new Intent(MainActivity.this,CamerasActivity.class);
//                        MainActivity.this.startActivity(myInt);
                        Toast.makeText(getApplicationContext(),"Feature coming soon in a great update.", Toast.LENGTH_LONG).show();
                    } catch(Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            btn=(Button) findViewById(R.id.btnSecurity);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        Intent myInt=new Intent(MainActivity.this,SensorsActivity.class);
                        MainActivity.this.startActivity(myInt);
                    } catch(Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            btn=(Button) findViewById(R.id.btnSchedules);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        Intent myIntent=new Intent(MainActivity.this,SchedulesActivity.class);
                        MainActivity.this.startActivity(myIntent);
                    } catch(Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            btn=(Button) findViewById(R.id.btnSettings);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        Intent myIntent=new Intent(MainActivity.this,SettingsActivity.class);
                        MainActivity.this.startActivity(myIntent);
                    } catch(Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch(Exception ex) {
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
            //showMessage(ex.toString());
            ex.printStackTrace();
        }
    }

    private void showPopupMessage(final String title,final String msg) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(title);
                builder.setMessage(msg);
                builder.setCancelable(true);
                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void loadData() throws Exception {
        try {
            if(Globals.AllRooms.size()==0) {
                FileInputStream fis = openFileInput(Globals.dataFilename);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);

                String line="";
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.startsWith("<Users>") && line.endsWith("</Users>")) {
                        Globals.usersString=line;
                        continue;
                    }
                    if(line.startsWith("<Rooms>") && line.endsWith("</Rooms>")) {
                        Globals.roomsString=line;
                        continue;
                    }
                    if(line.startsWith("<Scenes>") && line.endsWith("</Scenes>")) {
                        Globals.scenesString=line;
                        continue;
                    }
                    if(line.startsWith("<Schedules>") && line.endsWith("</Schedules>")) {
                        Globals.schedulesString=line;
                        continue;
                    }
                    if(line.startsWith("<Sensors>") && line.endsWith("</Sensors>")) {
                        Globals.sensorsString=line;
                        continue;
                    }
                    if(line.startsWith("<IRDevices>") && line.endsWith("</IRDevices>")) {
                        Globals.irDevicesString=line;
                        continue;
                    }
                    if(line.startsWith("<IRBlasters>") && line.endsWith("</IRBlasters>")) {
                        Globals.irBlastersString=line;
                        continue;
                    }
                    if(line.startsWith("<Cameras>") && line.endsWith("</Cameras>")) {
                        Globals.camerasString=line;
                        continue;
                    }
                }
                bufferedReader.close();
                isr.close();
                fis.close();
                if(!Utilities.fillData()) throw new Exception("Unable to load data...");
            }
        } catch(Exception ex) {
            //showException(ex);
            throw ex;
        }
    }

    @Override
    protected void onResume() {
        try {
            loadData();
        } catch(Exception ex) {
            showPopupMessage("Alert","Your app has NOT been initialized. Please click Refresh button in Settings page.");
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void showException(Exception ex) {
        if (BuildConfig.DEBUG) {
            String ex1="";
            for(StackTraceElement a1:ex.getStackTrace()) {
                ex1+=a1.toString();
            }
            showPopupMessage("Error",ex.toString());
        }
    }

    private void showToast(final String msg,final int len) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(toast==null) {
                    toast= Toast.makeText(getApplicationContext(),msg,len);
                    toast.show();
                }
            }
        });
    }
}
