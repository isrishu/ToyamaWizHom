package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.toyama.includes.model.SceneSensor;
import com.toyama.includes.model.Sensor;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ToggleButton;

@SuppressLint("InflateParams")
public class SensorsActivity extends Activity {

    TextView lblActivityHeading;
    LinearLayout pnlSensors;
    String receivedMessage = "";

    GatewayConnectThread gatewayConnectThread = null;
    RemoteAsyncTask remoteTask=null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sensors);

        lblActivityHeading = (TextView) findViewById(R.id.lblActivityHeading);
        pnlSensors = (LinearLayout) findViewById(R.id.pnlSensors);

        lblActivityHeading.setText("Sensors ("+Globals.connectionMode+")");
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    @SuppressWarnings("deprecation")
    private void generateSensorsView() {
        RelativeLayout.LayoutParams params;
        String idstr="";
        String tag="";
        View v;
        // ids as follows
        // 1 for toggle button
        // 4 for label
        // 5 for image
        try {
            pnlSensors.removeAllViews();
            for(Sensor s: Globals.AllSensors) {
                if(s.SensorId==0) {
                    return;
                } else {
                    final String sn=Utilities.toTitleCase(s.toString());
                    //final String sc=s.Type;
                    final String sid=String.valueOf(s.SensorId);
                    final RelativeLayout pnl=new RelativeLayout(this);

                    params= new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                    pnl.setLayoutParams(params);
                    pnl.setBackgroundColor(getResources().getColor(R.color.transparent));
                    pnl.setFocusableInTouchMode(false);

                    final ImageView img = (ImageView) getLayoutInflater().inflate(R.layout.switch_image_template, null);
                    params= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                    params.setMargins((int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10));
                    img.setLayoutParams(params);

                    tag=String.valueOf(s.SensorId);

                    setSensorImage(img,s);

                    img.setAdjustViewBounds(true);
                    img.setMaxHeight((int) getPixelValue(42));
                    img.setMaxWidth((int) getPixelValue(42));

                    idstr=String.valueOf(sid)+"5"; // 5 for image
                    img.setId(Integer.valueOf(idstr));
                    img.setTag(tag);

                    pnl.addView(img);

                    final ToggleButton otog = (ToggleButton) getLayoutInflater().inflate(R.layout.sensor_toggle_template, null);
                    params = new RelativeLayout.LayoutParams((int) getPixelValue(50),(int) getPixelValue(50));
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                    params.setMargins((int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10));
                    otog.setLayoutParams(params);

                    idstr=String.valueOf(sid)+"1"; // 1 for toggle button
                    otog.setId(Integer.valueOf(idstr));

                    otog.setTag(tag);

                    if(Globals.isForScene) {
                        for(SceneSensor ss:Globals.CurrentScene.SceneSensors) {
                            if(ss.SensorId==s.SensorId) {
                                otog.setChecked(ss.IsArmed);
                            }
                        }

                        otog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    ToggleButton tb=(ToggleButton)v;
                                    SceneSensor ss=new SceneSensor();
                                    ss.SensorId=Integer.valueOf(String.valueOf(v.getTag()));
                                    TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                    lbl.setTextColor(getResources().getColor(R.color.green));
                                    ss.IsArmed=tb.isChecked();
                                    addSceneSensor(ss,"tog");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                        img.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                try {
                                    SceneSensor ss=new SceneSensor();
                                    int sid=Integer.valueOf(String.valueOf(v.getTag()));
                                    ss=getSceneSensor(sid);
                                    if(ss!=null) {
                                        TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                        lbl.setTextColor(getResources().getColor(R.color.red));
                                        removeSceneSensor(ss,"tog");
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                return true;
                            }
                        });
                    } else {
                        otog.setChecked(s.IsArmed);
                        // Set click listener for toggle button
                        otog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int switchState=0;
                                try {
                                    ToggleButton tb=(ToggleButton)v;
                                    Sensor s=Utilities.getSensor(Integer.valueOf(sid));
                                    if (tb.isChecked()) switchState=1;
                                    else switchState=0;
                                    if(s!=null) {
                                        s.IsArmed=tb.isChecked();
                                        if(Globals.connectionMode.equals("Local")) {
                                            sendToGateway("ToggleSensor_"+sid+"_"+String.valueOf(switchState));
                                        } else {
                                            remoteTask=new RemoteAsyncTask();
                                            String [] params={"toggleSensor",sid+"_"+String.valueOf(switchState)};
                                            remoteTask.execute(params);
                                        }
                                        setSensorImage(img,s);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }

                    pnl.addView(otog);

                    final TextView olbl=(TextView) getLayoutInflater().inflate(R.layout.switch_label_template, null);
                    params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.RIGHT_OF,img.getId());
                    //params.addRule(RelativeLayout.END_OF,img.getId());
                    params.addRule(RelativeLayout.LEFT_OF,otog.getId());
                    //params.addRule(RelativeLayout.START_OF,otog.getId());
                    params.addRule(RelativeLayout.ALIGN_BASELINE,otog.getId());
                    params.addRule(RelativeLayout.ALIGN_BOTTOM,otog.getId());
                    olbl.setLayoutParams(params);

                    idstr=String.valueOf(sid)+"4"; // 4 for label
                    olbl.setId(Integer.valueOf(idstr));

                    olbl.setText(sn);

                    if(Globals.isForScene) {
                        boolean found=false;
                        for(SceneSensor ss:Globals.CurrentScene.SceneSensors) {
                            if(ss.SensorId==Integer.valueOf(sid)) {
                                found=true;
                            }
                        }
                        olbl.setTypeface(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
                        if(found) olbl.setTextColor(getResources().getColor(R.color.green));
                        else olbl.setTextColor(getResources().getColor(R.color.red));
                    }
                    pnl.addView(olbl);

                    v=new View(this);
                    params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) getPixelValue(3));
                    params.setMargins((int) getPixelValue(7), 0,(int) getPixelValue(7), 0);
                    v.setLayoutParams(params);
                    v.setBackground(getResources().getDrawable(R.drawable.line_lightgray));
                    pnl.addView(v);

                    pnlSensors.addView(pnl);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void setSensorImage(ImageView img,Sensor s) {
        switch(s.Type) {
            case "Motion":
                if(s.IsArmed) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.sensor_motion_armed));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.sensor_motion_disarmed));
                }
                break;
            case "Gas":
                if(s.IsArmed) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.sensor_gas_armed));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.sensor_gas_disarmed));
                }
                break;
            case "Smoke":
                if(s.IsArmed) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.sensor_smoke_armed));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.sensor_smoke_disarmed));
                }
                break;
            case "Magnetic":
                if(s.IsArmed) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.sensor_magnetic_armed));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.sensor_magnetic_disarmed));
                }
                break;
        }
    }

    private void addSceneSensor(SceneSensor sSwitch,String from) {
        boolean found=false;
        for(SceneSensor ss: Globals.CurrentScene.SceneSensors) {
            if(ss.SensorId==sSwitch.SensorId) {
                ss.IsArmed=sSwitch.IsArmed;
                found =true;
                break;
            }
        }
        if(!found) {
            Globals.CurrentScene.SceneSensors.add(sSwitch);
        }
    }

    private void removeSceneSensor(SceneSensor sSwitch,String from) {
        //boolean found=false;
        for(SceneSensor ss: Globals.CurrentScene.SceneSensors) {
            if(ss.SensorId==sSwitch.SensorId) {
                Globals.CurrentScene.SceneSensors.remove(sSwitch);
                //found =true;
                break;
            }
        }
    }

    private SceneSensor getSceneSensor(int sid) {
        for(SceneSensor ss:Globals.CurrentScene.SceneSensors) {
            if(ss.SensorId==sid) {
                return ss;
            }
        }
        return null;
    }

    private void sendToGateway(String msg) {
        if(gatewayConnectThread==null) {
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
                        SensorsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //showMessage(receivedMessage);
                                if(receivedMessage.contains("ToggleSensors_")) {
                                    receivedMessage=receivedMessage.replace("ToggleSensors_","");
                                    String sensorStates[]=receivedMessage.split("\\*");
                                    for(String sensorState:sensorStates) {
                                        if(sensorState.isEmpty()) continue;
                                        String x[]=sensorState.split("_");
//										int si=Utilities.getSensorIndex(Integer.valueOf(x[0]));
//										if(si>-1) {
//											Globals.AllSensors.get(si).IsArmed=(Integer.valueOf(x[0])==1);
//										}
                                        Sensor s=Utilities.getSensor(Integer.valueOf(x[0]));
                                        if(s!=null)
                                            s.IsArmed=(Integer.valueOf(x[1])==1);
                                    }
                                    Globals.sensorsString=Utilities.generateAllSensorsString();
                                    Utilities.writeDataToFile(getApplicationContext());
                                    generateSensorsView();
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
        if(Globals.AllSensors.size()==0) {
            showPopupMessage("No Sensors found. Please Refresh in Settings page.");
        } else {
            if(Globals.connectionMode.equals("Local")) {
                connectToGateway();
            }
            generateSensorsView();
            if(!Globals.isForScene) {
                querySensors();
            }
        }
    }

    private void querySensors() {
        try {
            if(Globals.connectionMode.equals("Local")) {
                sendToGateway("QuerySensors");
            } else {
                remoteTask=new RemoteAsyncTask();
                String[] params={"querySensors"};
                remoteTask.execute(params);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showPopupMessage(final String msg) {
        SensorsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SensorsActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
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
                    case "querySensors" :
                        if(svc.querySensors())
                            return svc.getResponseString();
                        else
                            return svc.getResponseString();
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
                    if(responses[1].startsWith("ToggleSensors_")) {
                        String sensorStates[]=responses[1].replace("ToggleSensors_","").split("*");
                        for(String sensorState:sensorStates) {
                            if(sensorState.isEmpty()) continue;
                            String x[]=sensorState.split("_");
                            Sensor s=Utilities.getSensor(Integer.valueOf(x[0]));
                            s.IsArmed=(Integer.valueOf(x[0])==1);
                        }
                        Globals.sensorsString=Utilities.generateAllSensorsString();
                        Utilities.writeDataToFile(getApplicationContext());
                        SensorsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                generateSensorsView();
                            }
                        });
                    } else {
                        showToast("Command Sent to Server",Toast.LENGTH_SHORT);
                    }
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

    private void showToast(final String msg,final int len) {
        SensorsActivity.this.runOnUiThread(new Runnable() {
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