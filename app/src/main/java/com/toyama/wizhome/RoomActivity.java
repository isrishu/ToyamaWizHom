package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.toyama.includes.model.IRDevice;
import com.toyama.includes.model.NodeSwitch;
import com.toyama.includes.model.Room;
import com.toyama.includes.model.RoomNode;
import com.toyama.includes.model.SceneSwitch;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.ServiceLayer;
import com.toyama.includes.utilities.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

@SuppressLint("InflateParams")
public class RoomActivity extends Activity {
    TextView lblRoomName;
    ImageView btnRefresh;
    LinearLayout pnlOnOffSwitches,pnlDimmerSwitches,pnlCurtainSwitches,pnlMasterSwitches;
    RelativeLayout pnlOnOffHeader,pnlDimmerHeader,pnlCurtainHeader,pnlMasterHeader;
    ImageView imgMastersToggle,imgOnOffToggle,imgDimmerToggle,imgCurtainToggle;
    Spinner cmbSwitchCategories,cmbIRDevices;
    EditText txtSwitchName;

    Room ThisRoom;
    String receivedMessage = "";
    String newSwitchName="",newSwitchCategory="";
    int newIrdId=0;

    ArrayList<String> querymsgs=new ArrayList<String>();
    int currentQueryIndex=0;
    private boolean isRemoteTaskRunning=false;

    //GatewayConnectThread gatewayConnectThread = null;
    GatewayListenerThread gatewayListenerThread = null;
    GatewayTalkerThread gatewayTalkerThread = null;
    private final ExecutorService communicationExecutor = Executors.newFixedThreadPool(5);
    RemoteAsyncTask remoteTask=null;
    Toast toast=null;
    boolean isReloggingIn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_room);

        lblRoomName = (TextView) findViewById(R.id.lblActivityHeading);
        btnRefresh = (ImageView) findViewById(R.id.btnReconnect);
        btnRefresh.setVisibility(View.GONE);
        pnlOnOffSwitches = (LinearLayout) findViewById(R.id.pnlOnOffSwitches);
        pnlDimmerSwitches = (LinearLayout) findViewById(R.id.pnlDimmerSwitches);
        pnlCurtainSwitches = (LinearLayout) findViewById(R.id.pnlCurtainSwitches);
        pnlMasterSwitches = (LinearLayout) findViewById(R.id.pnlMasterSwitches);

        pnlOnOffHeader = (RelativeLayout) findViewById(R.id.pnlOnOffHeader);
        pnlDimmerHeader = (RelativeLayout) findViewById(R.id.pnlDimmerHeader);
        pnlCurtainHeader = (RelativeLayout) findViewById(R.id.pnlCurtainHeader);
        pnlMasterHeader = (RelativeLayout) findViewById(R.id.pnlMasterHeader);

        //pnlOnOffSwitches.setVisibility(View.GONE);
        pnlOnOffHeader.setVisibility(View.GONE);

        //pnlDimmerSwitches.setVisibility(View.GONE);
        pnlDimmerHeader.setVisibility(View.GONE);

        //pnlCurtainSwitches.setVisibility(View.GONE);
        pnlCurtainHeader.setVisibility(View.GONE);

        //pnlMasterSwitches.setVisibility(View.GONE);
        pnlMasterHeader.setVisibility(View.GONE);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToGateway();
            }
        });

        imgMastersToggle = (ImageView) findViewById(R.id.imgMastersToggle);
        imgMastersToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pnlMasterSwitches.getVisibility() == View.VISIBLE) {
                    pnlMasterSwitches.setVisibility(View.GONE);
                    imgMastersToggle.setImageResource(R.drawable.btn_down);
                } else {
                    pnlMasterSwitches.setVisibility(View.VISIBLE);
                    imgMastersToggle.setImageResource(R.drawable.btn_up);
                }
            }
        });

        imgOnOffToggle = (ImageView) findViewById(R.id.imgOnOffToggle);
        imgOnOffToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pnlOnOffSwitches.getVisibility() == View.VISIBLE) {
                    pnlOnOffSwitches.setVisibility(View.GONE);
                    imgOnOffToggle.setImageResource(R.drawable.btn_down);
                } else {
                    pnlOnOffSwitches.setVisibility(View.VISIBLE);
                    imgOnOffToggle.setImageResource(R.drawable.btn_up);
                }
            }
        });

        imgDimmerToggle = (ImageView) findViewById(R.id.imgDimmerToggle);
        imgDimmerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pnlDimmerSwitches.getVisibility() == View.VISIBLE) {
                    pnlDimmerSwitches.setVisibility(View.GONE);
                    imgDimmerToggle.setImageResource(R.drawable.btn_down);
                } else {
                    pnlDimmerSwitches.setVisibility(View.VISIBLE);
                    imgDimmerToggle.setImageResource(R.drawable.btn_up);
                }
            }
        });

        imgCurtainToggle = (ImageView) findViewById(R.id.imgCurtainToggle);
        imgCurtainToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pnlCurtainSwitches.getVisibility() == View.VISIBLE) {
                    pnlCurtainSwitches.setVisibility(View.GONE);
                    imgCurtainToggle.setImageResource(R.drawable.btn_down);
                } else {
                    pnlCurtainSwitches.setVisibility(View.VISIBLE);
                    imgCurtainToggle.setImageResource(R.drawable.btn_up);
                }
            }
        });

        ThisRoom=Utilities.getThisRoom(Globals.currentRoomId);
        //lblRoomName.setText(String.valueOf(Globals.currentRoomId)+" ("+Globals.connectionMode+")");
        if(ThisRoom!=null) {
            generateRoomView();
            showHeaderMessage(Utilities.toTitleCase(ThisRoom.RoomName)+" ("+Globals.connectionMode+")");
        } else {
            //finish();
            showPopupMessage("Error in Room Generation. Please go back and try again!");
        }
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    // function to enable/disable the switches in a room if master is toggled
    private void toggleSwitchEnable(int nsid,boolean status) {
        RoomNode rn=getRoomNode(nsid);
        String idstr="";
        int viewid=0;
        ToggleButton tog=null;
        SeekBar sb=null;
        if(rn!=null) {
            boolean hasMaster=hasMaster(rn);
            if(rn.Version==1) {
                if (hasMaster) {
                    for (NodeSwitch ns : rn.NodeSwitches) {
                        if (ns.SwitchNumber != 0) {
                            idstr = String.valueOf(ns.NodeSwitchId) + "1";
                            viewid = Integer.valueOf(idstr);
                            tog = (ToggleButton) findViewById(viewid);
                            if (tog != null)
                                tog.setEnabled(status);
                            if (ns.Type.equals("Dimmer")) {
                                idstr = String.valueOf(ns.NodeSwitchId) + "2";
                                viewid = Integer.valueOf(idstr);
                                sb = (SeekBar) findViewById(viewid);
                                if (sb != null)
                                    sb.setEnabled(status);
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateRoomView() {
        RelativeLayout.LayoutParams params;
        String idstr="";
        String tag="";
        View v;
        // ids as follows --- toggle-1,seekbar-2,curtain stop-3,label-4,image-5,level label -6
        try {
            pnlMasterSwitches.removeAllViews();
            pnlOnOffSwitches.removeAllViews();
            pnlDimmerSwitches.removeAllViews();
            pnlCurtainSwitches.removeAllViews();
            for(final RoomNode rn:ThisRoom.RoomNodes) {
                if(rn.NodeType.equals("Bell")) continue; // bell switch need not be shown
                if(rn.NodeType.equals("AHU")) continue; // thermostat is shown as a separate interface
                if(rn.NodeType.equals("Door")) continue; // door is shown as a separate interface
                for(final NodeSwitch ns:rn.NodeSwitches) {
                    final String sn=Utilities.toTitleCase(ns.SwitchName);
                    final String nsid=String.valueOf(ns.NodeSwitchId);
                    final int irdid=ns.IRDeviceId;

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

                    tag=String.valueOf(ns.NodeSwitchId);

                    setSwitchImage(img,ns,true);
                    if(!Globals.isForScene && irdid>0) {
                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Globals.currentIRDeviceId=irdid;
                                Globals.currentIRDevice=Utilities.getIRDevice(Globals.currentIRDeviceId);
                                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                                //Globals.isForScene=false;
                                Intent myInt=null;
                                switch(ns.Category) {
                                    case "TV":
                                        myInt=new Intent(getApplicationContext(),TVActivity.class);
                                        break;
                                    case "Projector":
                                        myInt=new Intent(getApplicationContext(),ProjectorActivity.class);
                                        break;
                                    case "AC":
                                        myInt=new Intent(getApplicationContext(),ACActivity.class);
                                        break;
                                    case "Music System":
                                        myInt=new Intent(getApplicationContext(),MusicActivity.class);
                                        break;
                                    case "Set Top Box":
                                        myInt=new Intent(getApplicationContext(),SetTopBoxActivity.class);
                                        break;
                                }
                                if(myInt!=null)
                                    RoomActivity.this.startActivity(myInt);
                            }
                        });
                    }
                    img.setAdjustViewBounds(true);
                    img.setMaxHeight((int) getPixelValue(40));
                    img.setMaxWidth((int) getPixelValue(40));

                    idstr=String.valueOf(ns.NodeSwitchId)+"5";
                    img.setId(Integer.valueOf(idstr));
                    img.setTag(tag);

                    switch(ns.Type) {
                        case "Master":
                            img.setImageDrawable(getResources().getDrawable(R.drawable.master_on));
                            pnl.addView(img);

                            final ToggleButton mtog = (ToggleButton) getLayoutInflater().inflate(R.layout.switch_toggle_template, null);
                            params = new RelativeLayout.LayoutParams((int) getPixelValue(50),(int) getPixelValue(50));
                            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                            params.setMargins((int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10));
                            mtog.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"1";
                            mtog.setId(Integer.valueOf(idstr));

                            mtog.setTag(tag);

                            if(Globals.isForScene) {
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        mtog.setChecked(ss.IsOn);
                                        if(ss.IsOn) {
                                            img.setImageDrawable(getResources().getDrawable(R.drawable.master_on));
                                        } else {
                                            img.setImageDrawable(getResources().getDrawable(R.drawable.master_off));
                                        }
                                    }
                                }

                                mtog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            ToggleButton tb=(ToggleButton)v;
                                            if(tb.isChecked()) {
                                                img.setImageDrawable(getResources().getDrawable(R.drawable.master_on));
                                            } else {
                                                img.setImageDrawable(getResources().getDrawable(R.drawable.master_off));
                                            }
                                            SceneSwitch ss=new SceneSwitch();
                                            ss.NodeSwitchId=Integer.valueOf(String.valueOf(v.getTag()));
                                            TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                            lbl.setTextColor(getResources().getColor(R.color.green));
                                            ss.Level=0;
                                            ss.IsOn=tb.isChecked();
                                            addSceneSwitch(ss,"tog");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });

                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        try {
                                            SceneSwitch ss=new SceneSwitch();
                                            int nsid=Integer.valueOf(String.valueOf(v.getTag()));
                                            ss=getSceneSwitch(nsid);
                                            if(ss!=null) {
                                                TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                                lbl.setTextColor(getResources().getColor(R.color.red));
                                                removeSceneSwitch(ss,"tog");
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        return true;
                                    }
                                });
                            } else {
                                // Set click listener for toggle button
                                mtog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int switchState=0;
                                        try {
                                            ToggleButton tb=(ToggleButton)v;
                                            if (tb.isChecked()) switchState=1;
                                            else switchState=0;
                                            String toggleString="ToggleSwitch_"+String.valueOf(v.getTag()
                                                    +"_"+String.valueOf(switchState));
                                            if(tb.isChecked()) {
                                                img.setImageDrawable(getResources().getDrawable(R.drawable.master_on));
                                            } else {
                                                img.setImageDrawable(getResources().getDrawable(R.drawable.master_off));
                                            }
                                            if(Globals.connectionMode.equals("Local")) {
                                                sendToGateway(toggleString);
                                            } else {
                                                // commented below code because master toggle is causing switch to toggle always
                                                //remoteTask=new RemoteAsyncTask();
                                                //String[] params={"toggleSwitch",String.valueOf(v.getTag())+"_"+String.valueOf(switchState)};
                                                //remoteTask.execute(params);
                                            }
                                            toggleSwitchEnable(Integer.parseInt(String.valueOf(v.getTag())), tb.isChecked());
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            //showException(ex);
                                        }
                                    }
                                });
                            }
                            pnl.addView(mtog);

                            final TextView mlbl=(TextView) getLayoutInflater().inflate(R.layout.switch_label_template, null);
                            params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.RIGHT_OF,img.getId());
                            //params.addRule(RelativeLayout.END_OF,img.getId());
                            params.addRule(RelativeLayout.LEFT_OF,mtog.getId());
                            //params.addRule(RelativeLayout.START_OF,otog.getId());
                            params.addRule(RelativeLayout.ALIGN_BASELINE,mtog.getId());
                            params.addRule(RelativeLayout.ALIGN_BOTTOM,mtog.getId());
                            mlbl.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"4"; // 4 for label
                            mlbl.setId(Integer.valueOf(idstr));

                            mlbl.setText(rn.NodeName);

                            if(Globals.isForScene) {
                                boolean found=false;
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        found=true;
                                    }
                                }
                                mlbl.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
                                if(found) mlbl.setTextColor(getResources().getColor(R.color.green));
                                else mlbl.setTextColor(getResources().getColor(R.color.red));
                            }
                            pnl.addView(mlbl);

                            if (pnlMasterHeader.getVisibility() != View.VISIBLE) {
                                pnlMasterHeader.setVisibility(View.VISIBLE);
                            }
                            pnlMasterSwitches.addView(pnl);
                            break;
                        case "OnOff":
                            pnl.addView(img);

                            final ToggleButton otog = (ToggleButton) getLayoutInflater().inflate(R.layout.switch_toggle_template, null);
                            params = new RelativeLayout.LayoutParams((int) getPixelValue(50),(int) getPixelValue(50));
                            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                            params.setMargins((int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10));
                            otog.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"1"; // 1 for toggle button
                            otog.setId(Integer.valueOf(idstr));

                            otog.setTag(tag);

                            if(Globals.isForScene) {
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        otog.setChecked(ss.IsOn);
                                        if(ns!=null) {
                                            setSwitchImage(img,ns,ss.IsOn);
                                        }
                                    }
                                }

                                otog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            ToggleButton tb=(ToggleButton)v;
                                            NodeSwitch ns=Utilities.getNodeSwitch(Integer.valueOf((String) v.getTag()));
                                            if(ns!=null) {
                                                setSwitchImage(img,ns,tb.isChecked());
                                            }
                                            SceneSwitch ss=new SceneSwitch();
                                            ss.NodeSwitchId=Integer.valueOf(String.valueOf(v.getTag()));
                                            TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                            lbl.setTextColor(getResources().getColor(R.color.green));
                                            ss.Level=0;
                                            ss.IsOn=tb.isChecked();
                                            addSceneSwitch(ss,"tog");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });

                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        try {
                                            SceneSwitch ss=new SceneSwitch();
                                            int nsid=Integer.valueOf(String.valueOf(v.getTag()));
                                            ss=getSceneSwitch(nsid);
                                            if(ss!=null) {
                                                TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                                lbl.setTextColor(getResources().getColor(R.color.red));
                                                removeSceneSwitch(ss,"tog");
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        return true;
                                    }
                                });
                            } else {
                                // Set click listener for toggle button
                                otog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int switchState=0;
                                        try {
                                            ToggleButton tb=(ToggleButton)v;
                                            if (tb.isChecked()) switchState=1;
                                            else switchState=0;
                                            String toggleString="ToggleSwitch_"+String.valueOf(v.getTag())
                                                    +"_"+String.valueOf(switchState);
                                            NodeSwitch ns=Utilities.getNodeSwitch(Integer.valueOf((String) v.getTag()));
                                            if(ns!=null) {
                                                setSwitchImage(img,ns,tb.isChecked());
                                            }
                                            if(Globals.connectionMode.equals("Local")) {
                                                sendToGateway(toggleString);
                                            } else {
                                                remoteTask=new RemoteAsyncTask();
                                                String[] params={"toggleSwitch",String.valueOf(v.getTag())+"_"+String.valueOf(switchState)};
                                                remoteTask.execute(params);
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });

                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        final ArrayList<IRDevice> irDevices=new ArrayList<IRDevice>();
                                        AlertDialog.Builder switchOptionsDialog=new AlertDialog.Builder(RoomActivity.this);

                                        View sodView=(View) getLayoutInflater().inflate(R.layout.dialog_switch_options, null);
                                        switchOptionsDialog.setView(sodView);
                                        txtSwitchName=(EditText) sodView.findViewById(R.id.txtSwitchName);
                                        cmbSwitchCategories=(Spinner) sodView.findViewById(R.id.cmbSwitchCategories);
                                        cmbSwitchCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                                String sc=(String) cmbSwitchCategories.getAdapter().getItem(pos);
                                                if(!sc.equals("")) {
                                                    for(IRDevice ird:Globals.AllIRDevices) {
                                                        if(ird.RoomId==ThisRoom.RoomId && ird.Category.equals(sc) && !Utilities.isIRDeviceLinkedToNodeSwitch(ird))
                                                            irDevices.add(ird);
                                                    }
                                                    cmbIRDevices.setAdapter(new ArrayAdapter<IRDevice>(getApplicationContext(),R.layout.spinner_item,irDevices));
                                                }
                                            }
                                            @Override
                                            public void onNothingSelected(AdapterView<?> arg0) {
                                                // TODO Auto-generated method stub
                                            }
                                        });
                                        cmbIRDevices=(Spinner) sodView.findViewById(R.id.cmbIRDevices);
                                        cmbIRDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                                //ird=(IRDevice) cmbSwitchCategories.getAdapter().getItem(pos);
                                            }
                                            @Override
                                            public void onNothingSelected(AdapterView<?> arg0) {
                                                // TODO Auto-generated method stub
                                            }
                                        });

                                        txtSwitchName.setText(sn);
                                        switchOptionsDialog
                                                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        String usn=txtSwitchName.getText().toString().trim().replace("\n","");
                                                        String usc=cmbSwitchCategories.getSelectedItem().toString();
                                                        IRDevice ird=(IRDevice) cmbIRDevices.getSelectedItem();
                                                        int irdid=0;
                                                        if(Globals.IRDeviceCategories.contains(usc)) {
                                                            if(ird==null) {
                                                                showPopupMessage("Please Select a Device to link");
                                                                return;
                                                            }
                                                            irdid=ird.IRDeviceId;
                                                        }
                                                        newSwitchName=usn;
                                                        newSwitchCategory=usc;
                                                        newIrdId=irdid;
                                                        Globals.currentNodeSwitchId=Integer.valueOf(nsid);
                                                        sendToGateway("UpdateNodeSwitch_"+nsid+"_"+usn+"_"+usc+"_"+String.valueOf(irdid)); // Update Node Switch
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        return;
                                                    }
                                                });
                                        switchOptionsDialog.show();
                                        return true;
                                    }
                                });
                            }

                            pnl.addView(otog);

                            final TextView olbl=(TextView) getLayoutInflater().inflate(R.layout.switch_label_template, null);
                            params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.RIGHT_OF,img.getId());
                            params.addRule(RelativeLayout.LEFT_OF,otog.getId());
                            params.addRule(RelativeLayout.ALIGN_BASELINE,otog.getId());
                            params.addRule(RelativeLayout.ALIGN_BOTTOM,otog.getId());
                            olbl.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"4"; // 4 for label
                            olbl.setId(Integer.valueOf(idstr));

                            olbl.setText(sn);

                            if(Globals.isForScene) {
                                boolean found=false;
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        found=true;
                                    }
                                }
                                olbl.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
                                if(found) olbl.setTextColor(getResources().getColor(R.color.green));
                                else olbl.setTextColor(getResources().getColor(R.color.red));
                            }
                            pnl.addView(olbl);

                            if (pnlOnOffHeader.getVisibility() != View.VISIBLE) {
                                pnlOnOffHeader.setVisibility(View.VISIBLE);
                            }

                            pnlOnOffSwitches.addView(pnl);
                            break;
                        case "Dimmer":
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                            params.setMargins((int) getPixelValue(10),(int) getPixelValue(5),(int) getPixelValue(10),(int) getPixelValue(2));
                            img.setLayoutParams(params);
                            img.setMaxHeight((int) getPixelValue(38));
                            img.setMaxWidth((int) getPixelValue(38));
                            pnl.addView(img);

                            final ToggleButton dtog = (ToggleButton) getLayoutInflater().inflate(R.layout.switch_toggle_template, null);
                            params = new RelativeLayout.LayoutParams((int) getPixelValue(50),(int) getPixelValue(50));
                            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                            params.setMargins((int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10));
                            dtog.setLayoutParams(params);

                            dtog.setTag(tag);

                            idstr=String.valueOf(ns.NodeSwitchId)+"1"; // 1 for toggle button
                            dtog.setId(Integer.valueOf(idstr));

                            if(Globals.isForScene) {
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        dtog.setChecked(ss.IsOn);
                                        if(ns!=null) {
                                            setSwitchImage(img,ns,ss.IsOn);
                                        }
                                    }
                                }

                                // Set click listener for button
                                dtog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int switchState=0;
                                        try {
                                            ToggleButton tb=(ToggleButton)v;
                                            NodeSwitch ns=Utilities.getNodeSwitch(Integer.valueOf((String) v.getTag()));
                                            if(ns!=null) {
                                                setSwitchImage(img,ns,tb.isChecked());
                                            }
                                            SceneSwitch ss=new SceneSwitch();
                                            ss.NodeSwitchId=Integer.valueOf(String.valueOf(v.getTag()));
                                            TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                            lbl.setTextColor(getResources().getColor(R.color.green));
                                            SeekBar sb=(SeekBar) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"2"));

                                            boolean isChecked=tb.isChecked();
                                            sb.setEnabled(isChecked);

                                            if(rn.Version==1) {
                                                if(sb.getProgress()==0) sb.setProgress(30);
                                                if (isChecked) {
                                                    int q=sb.getProgress()/10;
                                                    switch(q) {
                                                        case 1: case 2: case 3:
                                                            ss.Level=30;
                                                            break;
                                                        case 4: case 5: case 6: case 7: case 8: case 9: case 10:
                                                            ss.Level=q*10;
                                                            break;
                                                    }
                                                } else {
                                                    ss.Level=0;
                                                }
                                            }
                                            if(rn.Version==2) {
                                                if(sb.getProgress()==0) sb.setProgress(35);
                                                if (isChecked) {
                                                    int q=sb.getProgress()/10;
                                                    switch(q) {
                                                        case 1: case 2: case 3:
                                                            ss.Level=35;
                                                            break;
                                                        case 4:
                                                            ss.Level=45;
                                                            break;
                                                        case 5:
                                                            ss.Level=55;
                                                            break;
                                                        case 6: case 7: case 8: case 9: case 10:
                                                            ss.Level=100;
                                                            break;
                                                    }
                                                } else {
                                                    ss.Level=0;
                                                }
                                            }
                                            ss.IsOn=isChecked;
                                            addSceneSwitch(ss,"tog");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });

                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        try {
                                            SceneSwitch ss=new SceneSwitch();
                                            int nsid=Integer.valueOf(String.valueOf(v.getTag()));
                                            ss=getSceneSwitch(nsid);
                                            if(ss!=null) {
                                                TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                                lbl.setTextColor(getResources().getColor(R.color.red));
                                                removeSceneSwitch(ss,"tog");
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        return true;
                                    }
                                });
                            } else {
                                // Set click listener for button
                                dtog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int switchState=0;
                                        boolean isChecked=false;
                                        try {
                                            ToggleButton tb=(ToggleButton)v;
                                            SeekBar sb=(SeekBar) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"2"));
                                            isChecked=tb.isChecked();
                                            sb.setEnabled(isChecked);
                                            if (isChecked) {
                                                //if(sb.getProgress()==0) sb.setProgress(30);
                                                if(rn.Version==1) {
                                                    if(sb.getProgress()==0) sb.setProgress(30);
                                                    if (isChecked) {
                                                        int q=sb.getProgress()/10;
                                                        switch(q) {
                                                            case 1: case 2: case 3:
                                                                switchState=30;
                                                                break;
                                                            case 4: case 5: case 6: case 7: case 8: case 9: case 10:
                                                                switchState=q*10;
                                                                break;
                                                        }
                                                    } else {
                                                        switchState=0;
                                                    }
                                                }
                                                if(rn.Version==2) {
                                                    if(sb.getProgress()==0) sb.setProgress(35);
                                                    if (isChecked) {
                                                        int q=sb.getProgress()/10;
                                                        switch(q) {
                                                            case 1: case 2: case 3:
                                                                switchState=35;
                                                                break;
                                                            case 4:
                                                                switchState=45;
                                                                break;
                                                            case 5:
                                                                switchState=55;
                                                                break;
                                                            case 6: case 7: case 8: case 9: case 10:
                                                                switchState=100;
                                                                break;
                                                        }
                                                    } else {
                                                        switchState=0;
                                                    }
                                                }
                                            } else {
                                                switchState=0;
                                            }
                                            NodeSwitch ns=Utilities.getNodeSwitch(Integer.valueOf((String) v.getTag()));
                                            if(ns!=null) {
                                                setSwitchImage(img,ns,tb.isChecked());
                                            }
                                            String toggleString="ToggleSwitch_"+String.valueOf(v.getTag()
                                                    +"_"+String.valueOf(switchState));
                                            if(Globals.connectionMode.equals("Local")) {
                                                sendToGateway(toggleString);
                                            } else {
                                                remoteTask=new RemoteAsyncTask();
                                                String[] params={"toggleSwitch",String.valueOf(v.getTag())+"_"+String.valueOf(switchState)};
                                                remoteTask.execute(params);
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });

                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        final String nsid=String.valueOf(v.getTag());
                                        AlertDialog.Builder switchOptionsDialog=new AlertDialog.Builder(RoomActivity.this);

                                        View sodView=(View) getLayoutInflater().inflate(R.layout.dialog_switch_options, null);
                                        switchOptionsDialog.setView(sodView);
                                        txtSwitchName=(EditText) sodView.findViewById(R.id.txtSwitchName);
                                        cmbSwitchCategories=(Spinner) sodView.findViewById(R.id.cmbSwitchCategories);

                                        txtSwitchName.setText(sn);
                                        switchOptionsDialog
                                                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        String sn=txtSwitchName.getText().toString().trim().replace("\n","");
                                                        String sc=cmbSwitchCategories.getSelectedItem().toString();
                                                        newSwitchName=sn;
                                                        newSwitchCategory=sc;
                                                        newIrdId=0;
                                                        Globals.currentNodeSwitchId=Integer.valueOf(nsid);
                                                        switch(sc) {
                                                            case "Light":
                                                            case "Fan":
                                                                sendToGateway("UpdateNodeSwitch_"+nsid+"_"+sn+"_"+sc+"_0"); // Update Node Switch
                                                                break;
                                                            default:
                                                                dialog.cancel();
                                                                showPopupMessage("This category is not valid for a Dimmer Switch");
                                                                break;
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        switchOptionsDialog.show();
                                        return true;
                                    }
                                });
                            }

                            pnl.addView(dtog);

                            final TextView dlbl=(TextView) getLayoutInflater().inflate(R.layout.switch_label_template, null);
                            params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.RIGHT_OF,img.getId());

                            dlbl.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"4"; // 4 for label
                            dlbl.setId(Integer.valueOf(idstr));

                            dlbl.setText(sn);

                            if(Globals.isForScene) {
                                boolean found=false;
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        found=true;
                                    }
                                }
                                dlbl.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
                                if(found) dlbl.setTextColor(getResources().getColor(R.color.green));
                                else dlbl.setTextColor(getResources().getColor(R.color.red));
                            }

                            pnl.addView(dlbl);

                            final TextView levellbl=(TextView) getLayoutInflater().inflate(R.layout.switch_percent_label_template, null);
                            params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.LEFT_OF,dtog.getId());
                            params.addRule(RelativeLayout.ALIGN_BOTTOM,dlbl.getId());

                            levellbl.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"6"; // 6 for level label
                            levellbl.setId(Integer.valueOf(idstr));

                            if(rn.Version==1)
                                levellbl.setText("30%");
                            if(rn.Version==2)
                                levellbl.setText("35%");

                            pnl.addView(levellbl);

                            final SeekBar sb=new SeekBar(this);
                            params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.ALIGN_LEFT,img.getId());
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                            params.addRule(RelativeLayout.LEFT_OF,dtog.getId());
                            params.addRule(RelativeLayout.BELOW,img.getId());
                            sb.setLayoutParams(params);

                            sb.setTag(tag);

                            idstr=String.valueOf(ns.NodeSwitchId)+"2"; // 2 for seekbar
                            sb.setId(Integer.valueOf(idstr));

                            if(Globals.isForScene) {
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        sb.setProgress(ss.Level);
                                        levellbl.setText(String.valueOf(ss.Level)+"%");
                                    }
                                }

                                sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
                                    int progressValue = 0;
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                                        progressValue=progress;
                                    }
                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) { }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                        int q=progressValue/10;
                                        SceneSwitch ss=new SceneSwitch();
                                        ss.NodeSwitchId=Integer.valueOf(String.valueOf(seekBar.getTag()));
                                        TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(seekBar.getTag())+"4"));
                                        lbl.setTextColor(getResources().getColor(R.color.green));
                                        if(rn.Version==1) {
                                            switch(q) {
                                                case 1: case 2: case 3:
                                                    ss.Level=30;
                                                    break;
                                                case 4: case 5: case 6: case 7: case 8: case 9: case 10:
                                                    ss.Level=q*10;
                                                    break;
                                            }
                                        }
                                        if(rn.Version==2) {
                                            switch(q) {
                                                case 1: case 2: case 3:
                                                    ss.Level=35;
                                                    break;
                                                case 4:
                                                    ss.Level=45;
                                                    break;
                                                case 5:
                                                    ss.Level=55;
                                                    break;
                                                case 6: case 7: case 8: case 9: case 10:
                                                    ss.Level=100;
                                                    break;
                                            }
                                        }
                                        ss.IsOn=true;
                                        levellbl.setText(String.valueOf(ss.Level)+"%");
                                        addSceneSwitch(ss,"sb");
                                    }
                                });
                            } else {
                                sb.setEnabled(false);
                                sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
                                    int progressValue = 0;
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                                        progressValue=progress;
                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) { }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar v) {
                                        int q=progressValue/10;
                                        int percent=0;
                                        if(rn.Version==1) {
                                            percent=30;
                                            switch(q) {
                                                case 1: case 2: case 3:
                                                    percent=30;
                                                    break;
                                                case 4: case 5: case 6: case 7: case 8: case 9: case 10:
                                                    percent=q*10;
                                                    break;
                                            }
                                        }
                                        if(rn.Version==2) {
                                            percent=35;
                                            switch(q) {
                                                case 1: case 2: case 3:
                                                    percent=35;
                                                    break;
                                                case 4:
                                                    percent=45;
                                                    break;
                                                case 5:
                                                    percent=55;
                                                    break;
                                                case 6: case 7: case 8: case 9: case 10:
                                                    percent=100;
                                                    break;
                                            }
                                        }
                                        levellbl.setText(String.valueOf(percent)+"%");
                                        String toggleString="ToggleSwitch_"+String.valueOf(v.getTag()+"_"+String.valueOf(percent));
                                        if(Globals.connectionMode.equals("Local")) {
                                            sendToGateway(toggleString);
                                        } else {
                                            remoteTask=new RemoteAsyncTask();
                                            String[] params={"toggleSwitch",String.valueOf(v.getTag())+"_"+String.valueOf(percent)};
                                            remoteTask.execute(params);
                                        }
                                    }
                                });
                            }
                            pnl.addView(sb);

                            if (pnlDimmerHeader.getVisibility() != View.VISIBLE) {
                                pnlDimmerHeader.setVisibility(View.VISIBLE);
                            }

                            pnlDimmerSwitches.addView(pnl);
                            break;
                        case "Curtain":
                            pnl.addView(img);

                            final ToggleButton ctog = (ToggleButton) getLayoutInflater().inflate(R.layout.switch_ctoggle_template, null);
                            params = new RelativeLayout.LayoutParams((int) getPixelValue(50),(int) getPixelValue(50));
                            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                            params.setMargins((int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10),(int) getPixelValue(10));
                            ctog.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"1"; // 1 for toggle button
                            ctog.setId(Integer.valueOf(idstr));

                            ctog.setTag(tag);
                            // creating this here because we need to enable/disable stop button when open/close is pressed
                            final Button ibtn=(Button) getLayoutInflater().inflate(R.layout.switch_cbutton_template, null);

                            if(Globals.isForScene) {
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        ctog.setChecked(ss.IsOn);
                                        if(ns!=null) {
                                            setSwitchImage(img,ns,ss.IsOn);
                                        }
                                    }
                                }

                                // Set click listener for button
                                ctog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int switchState=0;
                                        try {
                                            ToggleButton tb=(ToggleButton)v;
                                            NodeSwitch ns=Utilities.getNodeSwitch(Integer.valueOf((String) v.getTag()));
                                            if(ns!=null) {
                                                setSwitchImage(img,ns,tb.isChecked());
                                            }
                                            SceneSwitch ss=new SceneSwitch();
                                            TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                            lbl.setTextColor(getResources().getColor(R.color.green));
                                            ss.NodeSwitchId=Integer.valueOf(String.valueOf(v.getTag()));
                                            ss.Level=0;
                                            ss.IsOn=tb.isChecked();
                                            addSceneSwitch(ss,"tog");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });

                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        try {
                                            SceneSwitch ss=new SceneSwitch();
                                            int nsid=Integer.valueOf(String.valueOf(v.getTag()));
                                            ss=getSceneSwitch(nsid);
                                            if(ss!=null) {
                                                TextView lbl=(TextView) findViewById(Integer.valueOf(String.valueOf(v.getTag())+"4"));
                                                lbl.setTextColor(getResources().getColor(R.color.red));
                                                removeSceneSwitch(ss,"tog");
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        return true;
                                    }
                                });
                            } else {
                                // Set click listener for button
                                ctog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int switchState=0;
                                        try {
                                            ToggleButton tb=(ToggleButton)v;
                                            if (tb.isChecked()) switchState=1;
                                            else switchState=0;
                                            String toggleString="ToggleSwitch_"+String.valueOf(v.getTag()
                                                    +"_"+String.valueOf(switchState));
                                            NodeSwitch ns=Utilities.getNodeSwitch(Integer.valueOf((String) v.getTag()));
                                            if(ns!=null) {
                                                setSwitchImage(img,ns,tb.isChecked());
                                            }
                                            if(Globals.connectionMode.equals("Local")) {
                                                sendToGateway(toggleString);
                                            } else {
                                                remoteTask=new RemoteAsyncTask();
                                                String[] params={"toggleSwitch",String.valueOf(v.getTag())+"_"+String.valueOf(switchState)};
                                                remoteTask.execute(params);
                                            }
                                            ibtn.setEnabled(true);
                                            ibtn.setAlpha(1f);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });

                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        final String nsid=String.valueOf(v.getTag());
                                        AlertDialog.Builder switchOptionsDialog=new AlertDialog.Builder(RoomActivity.this);

                                        View sodView=(View) getLayoutInflater().inflate(R.layout.dialog_switch_options, null);
                                        switchOptionsDialog.setView(sodView);
                                        txtSwitchName=(EditText) sodView.findViewById(R.id.txtSwitchName);
                                        cmbSwitchCategories=(Spinner) sodView.findViewById(R.id.cmbSwitchCategories);

                                        txtSwitchName.setText(sn);
                                        switchOptionsDialog
                                                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        String sn=txtSwitchName.getText().toString().trim().replace("\n","");
                                                        String sc="Curtain";
                                                        newSwitchName=sn;
                                                        newSwitchCategory=sc;
                                                        newIrdId=0;
                                                        Globals.currentNodeSwitchId=Integer.valueOf(nsid);
                                                        sendToGateway("UpdateNodeSwitch_"+nsid+"_"+sn+"_"+sc+"_0"); // Update Node Switch
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        switchOptionsDialog.show();
                                        return true;
                                    }
                                });
                            }

                            pnl.addView(ctog);

                            // ibtn for stop is initialized above because it needs to be enabled/disabled in open/close code
                            params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                            params = new RelativeLayout.LayoutParams((int) getPixelValue(50),(int) getPixelValue(50));
                            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                            params.addRule(RelativeLayout.LEFT_OF,ctog.getId());
                            ibtn.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"3"; // 3 for curtain stop button
                            ibtn.setId(Integer.valueOf(idstr));

                            ibtn.setTag(tag);

                            if(Globals.isForScene) {
                                // no click event for scene setting here
                            } else {
                                ibtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int switchState=2;
                                        try {
                                            String toggleString="ToggleSwitch_"+String.valueOf(v.getTag()
                                                    +"_"+String.valueOf(switchState));
                                            if(Globals.connectionMode.equals("Local")) {
                                                sendToGateway(toggleString);
                                            } else {
                                                remoteTask=new RemoteAsyncTask();
                                                String[] params={"toggleSwitch",String.valueOf(v.getTag())+"_"+String.valueOf(switchState)};
                                                remoteTask.execute(params);
                                            }
                                            ibtn.setEnabled(false);
                                            ibtn.setAlpha(.5f);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });
                            }

                            pnl.addView(ibtn);

                            final TextView clbl=(TextView) getLayoutInflater().inflate(R.layout.switch_clabel_template, null);
                            params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.RIGHT_OF,img.getId());
                            params.addRule(RelativeLayout.LEFT_OF,ibtn.getId());
                            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                            clbl.setLayoutParams(params);

                            idstr=String.valueOf(ns.NodeSwitchId)+"4"; // 4 for label
                            clbl.setId(Integer.valueOf(idstr));
                            clbl.setTag(tag);
                            clbl.setText(sn);

                            if(Globals.isForScene) {
                                boolean found=false;
                                for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
                                    if(ss.NodeSwitchId==ns.NodeSwitchId) {
                                        found=true;
                                    }
                                }
                                clbl.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
                                if(found) clbl.setTextColor(getResources().getColor(R.color.green));
                                else clbl.setTextColor(getResources().getColor(R.color.red));
                            }

                            pnl.addView(clbl);

                            if (pnlCurtainHeader.getVisibility() != View.VISIBLE) {
                                pnlCurtainHeader.setVisibility(View.VISIBLE);
                            }

                            pnlCurtainSwitches.addView(pnl);
                            break;
                    }
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void querySwitchStates() {
        String msg="";
        try {
            if(Globals.connectionMode.equals("Local")) {
                for(RoomNode rn:ThisRoom.RoomNodes) {
                    if(rn.NodeType.equals("Bell")) continue; // dont ask for the status of a bell switch
                    msg="QuerySwitch_"+String.valueOf(rn.RoomNodeId);
                    querymsgs.add(msg);
                }
                if(currentQueryIndex<querymsgs.size()) sendToGateway(querymsgs.get(currentQueryIndex++));
            } else {
                for(RoomNode rn:ThisRoom.RoomNodes) {
                    remoteTask=new RemoteAsyncTask();
                    String[] params={"querySwitch",String.valueOf(rn.RoomNodeId)};
                    remoteTask.execute(params);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addSceneSwitch(SceneSwitch sSwitch,String from) {
        boolean found=false;
        for(SceneSwitch ss: Globals.CurrentScene.SceneSwitches) {
            if(ss.NodeSwitchId==sSwitch.NodeSwitchId) {
                // means this switch state is already in the scene, so edit it
				/*if(from.equals("sb")) {

				} else {
					ss.Level=50;
				}*/
                ss.Level=sSwitch.Level;
                ss.IsOn=sSwitch.IsOn;
                found =true;
                break;
            }
        }
        if(!found) {
            Globals.CurrentScene.SceneSwitches.add(sSwitch);
        }
    }

    private void removeSceneSwitch(SceneSwitch sSwitch,String from) {
        //boolean found=false;
        for(SceneSwitch ss: Globals.CurrentScene.SceneSwitches) {
            if(ss.NodeSwitchId==sSwitch.NodeSwitchId) {
                Globals.CurrentScene.SceneSwitches.remove(sSwitch);
                //found =true;
                break;
            }
        }
    }

    private void sendToGateway(String msg) {
        if(gatewayTalkerThread==null) {
            return;
        }
        gatewayTalkerThread.sendMessage(msg);
    }

    private void connectToGateway() {
        gatewayListenerThread = new GatewayListenerThread();
        gatewayTalkerThread = new GatewayTalkerThread();
        communicationExecutor.submit(gatewayListenerThread);
        communicationExecutor.submit(gatewayTalkerThread);
    }

    private RoomNode getRoomNode(byte nh,byte nl) {
        try {
            for(RoomNode rn:ThisRoom.RoomNodes) {
                if(rn.NodeIdHigher==nh && rn.NodeIdLower==nl)
                    return rn;
            }
            return null;
        } catch (Exception ex) {
            throw ex;
        } finally {

        }
    }

    private RoomNode getRoomNode(int nsid) {
        try {
            for(RoomNode rn:ThisRoom.RoomNodes) {
                for(NodeSwitch ns:rn.NodeSwitches) {
                    if(ns.NodeSwitchId==nsid) return rn;
                }
            }
            return null;
        } catch (Exception ex) {
            throw ex;
        } finally {

        }
    }

    private RoomNode getRoomNodeInThisRoom(int rnid) {
        //RoomNode rn=null;
        try {
            //rn = new RoomNode();
            for(Room r:Globals.AllRooms) {
                for(RoomNode rn: r.RoomNodes) {
                    if(rn.RoomNodeId==rnid) {
                        // dont return rn yet. because a nodeswitch can be added in multiple rooms, this nodeid combination
                        // could have returned another room's roomnode from DB
                        // so now match to see which roomnodes in this room have node id equal to this room's nodes
                        for(RoomNode rn1:ThisRoom.RoomNodes) {
                            if(rn1.NodeIdHigher==rn.NodeIdHigher && rn1.NodeIdLower==rn.NodeIdLower)
                                return rn1;
                        }
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            throw ex;
        } finally {

        }
    }

    private NodeSwitch getNodeSwitch(RoomNode rn,int sNumber) {
        try {
            for(NodeSwitch ns: rn.NodeSwitches) {
                if(ns.SwitchNumber==sNumber) {
                    return ns;
                }
            }
            return null;
        } catch (Exception ex) {
            throw ex;
        } finally {

        }
    }

    private boolean hasMaster(RoomNode rn) {
        try {
            for(NodeSwitch ns: rn.NodeSwitches) {
                if(ns.SwitchNumber==0) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            throw ex;
        } finally {

        }
    }

    private SceneSwitch getSceneSwitch(int nsid) {
        for(SceneSwitch ss:Globals.CurrentScene.SceneSwitches) {
            if(ss.NodeSwitchId==nsid) {
                return ss;
            }
        }
        return null;
    }

    private void processSwitchStates(String recdstr) {
        // if connMode==local
        //updated sample recdmsg ToggleSwitch_nodeidhigh_nodeidlower_1_1_0_1_0_0_1_1_<End>
        // if connMode==remote
        //sample recdmsg ToggleSwitch_rnid_1_1_0_1_0_0_1_1_<End>
        int rnid,cDeviceId=0;
        byte nh,nl;
        ArrayList<Integer> swStates=new ArrayList<Integer>();
        boolean swStatus=false;
        @SuppressWarnings("unused")
        String mode="O";
        int percent=30;
        String idstr="";
        int viewid=0;
        NodeSwitch ns=null;
        RoomNode rn=null;
        String[] x=recdstr.split("_");
        int beginIndex=0;
        try {
            if(Globals.connectionMode.equals("Local")) {
                nh=Byte.parseByte(x[1].trim());
                nl=Byte.parseByte(x[2].trim());
                rn=getRoomNode(nh,nl);
                if(rn==null) return;
                //cDeviceId=Integer.parseInt(x[3].trim());
                if(rn.NodeType.equals("Curtain")) {
                    cDeviceId=2;
                } else {
                    cDeviceId=1;
                }
                beginIndex=3;
            } else {
                rnid=Integer.parseInt(x[1].trim());
                rn=getRoomNodeInThisRoom(rnid);
                if(rn==null) return;
                if(rn.NodeType.equals("Curtain")) {
                    cDeviceId=2;
                } else {
                    cDeviceId=1;
                }
                beginIndex=2;
            }
            for(int i=beginIndex;i<x.length;i++) {
                if(x[i].equals("<End>")) break;
                swStates.add(Integer.valueOf(x[i]));
            }
            if(rn!=null) {
                boolean isMasterOn=false;
                //boolean hasMaster=hasMaster(rn);

                for(int i=0;i<swStates.size();i++) {
                    // process the state of the master switch
                    percent=swStates.get(i);
                    if(percent>=1) isMasterOn=true; // meaning if atleast one on or percent is present
                }
                //if(hasMaster) {
                // master NodeSwitch
                ns=getNodeSwitch(rn,0);
                if(ns!=null) {
                    ns.isOn=isMasterOn;
                    idstr=String.valueOf(ns.NodeSwitchId)+"1";
                    viewid=Integer.valueOf(idstr);
                    ToggleButton tog=(ToggleButton) findViewById(viewid);
                    if(tog!=null) {
                        tog.setChecked(isMasterOn);
                    }
                    idstr=String.valueOf(ns.NodeSwitchId)+"5";
                    viewid=Integer.valueOf(idstr);
                    ImageView img=(ImageView) findViewById(viewid);
                    if(img!=null) {
                        setSwitchImage(img,ns,isMasterOn);
                    }
                }
                //}
                for(int i=0;i<swStates.size();i++) {
                    ns=getNodeSwitch(rn,i+1);
                    if(ns!=null) {
                        if(cDeviceId==1) {
                            percent=swStates.get(i);
                            if(percent>1) {
                                // percentage value recd
                                // meaning seekbar value and toggle has to be set
                                mode="P";
                                swStatus=true;

                                idstr=String.valueOf(ns.NodeSwitchId)+"1";
                                viewid=Integer.valueOf(idstr);
                                ToggleButton tog=(ToggleButton) findViewById(viewid);

                                if(tog!=null) {
                                    tog.setChecked(swStatus);
                                }
                                // constructing seekbar id from values
                                idstr=String.valueOf(ns.NodeSwitchId)+"2";
                                viewid=Integer.valueOf(idstr);
                                SeekBar sb=(SeekBar) findViewById(viewid);
                                if(sb!=null) {
                                    sb.setProgress(percent);
                                    sb.setEnabled(swStatus);
                                }
                                idstr=String.valueOf(ns.NodeSwitchId)+"5";
                                viewid=Integer.valueOf(idstr);
                                ImageView img=(ImageView) findViewById(viewid);
                                if(img!=null) {
                                    setSwitchImage(img,ns,swStatus);
                                }
                                idstr=String.valueOf(ns.NodeSwitchId)+"6";
                                viewid=Integer.valueOf(idstr);
                                TextView plabel=(TextView) findViewById(viewid);
                                if(plabel!=null) {
                                    plabel.setText(String.valueOf(percent)+"%");
                                }
                            } else { // on off value recd
                                // meaning switch status has to be toggled
                                mode="O";
                                swStatus=(swStates.get(i).equals(1));
                                idstr=String.valueOf(ns.NodeSwitchId)+"1";
                                viewid=Integer.valueOf(idstr);
                                ToggleButton tog=(ToggleButton) findViewById(viewid);

                                if(tog!=null) {
                                    tog.setChecked(swStatus);
                                }
                                if(ns.Type.equals("Dimmer")) {
                                    idstr=String.valueOf(ns.NodeSwitchId)+"2";
                                    viewid=Integer.valueOf(idstr);
                                    SeekBar sb=(SeekBar) findViewById(viewid);
                                    if(sb!=null) {
                                        if(rn.Version==1)
                                            sb.setProgress(30);
                                        if(rn.Version==2)
                                            sb.setProgress(35);
                                        sb.setEnabled(swStatus);
                                    }
                                }
                                idstr=String.valueOf(ns.NodeSwitchId)+"6";
                                viewid=Integer.valueOf(idstr);
                                TextView plabel=(TextView) findViewById(viewid);
                                if(plabel!=null) {
                                    if(rn.Version==1)
                                        plabel.setText("30%");
                                    if(rn.Version==2)
                                        plabel.setText("35%");
                                }
                                idstr=String.valueOf(ns.NodeSwitchId)+"5";
                                viewid=Integer.valueOf(idstr);
                                ImageView img=(ImageView) findViewById(viewid);
                                if(img!=null) {
                                    setSwitchImage(img,ns,swStatus);
                                }
                            }
                        }

                        if(cDeviceId==2) {
                            percent=swStates.get(i);
                            if(percent>1) { // curtain stop button has been pressed
                                swStatus=true;
                                idstr=String.valueOf(ns.NodeSwitchId)+"3";
                                viewid=Integer.valueOf(idstr);
                            } else { // meaning open/close
                                mode="O";
                                swStatus=(swStates.get(i).equals(1));
                                idstr=String.valueOf(ns.NodeSwitchId)+"1";
                                viewid=Integer.valueOf(idstr);
                                ToggleButton tog=(ToggleButton) findViewById(viewid);

                                if(tog!=null) {
                                    tog.setChecked(swStatus);
                                }
                                idstr=String.valueOf(ns.NodeSwitchId)+"5";
                                viewid=Integer.valueOf(idstr);
                                ImageView img=(ImageView) findViewById(viewid);
                                if(img!=null) {
                                    setSwitchImage(img,ns,swStatus);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //Log.d("Process sw states",ex.getMessage());
            //lblStatus.setText("line 1317: "+ex.getMessage());
        }
    }

    private void setSwitchImage(ImageView img,NodeSwitch ns,boolean swStatus) {
        switch(ns.Category) {
            case "Master":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.master_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.master_off));
                }
                break;
            case "Light":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
                }
                break;
            case "Fan":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.fan_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.fan_off));
                }
                break;
            case "TV":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.tv_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.tv_off));
                }
                break;
            case "Projector":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.projector_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.projector_off));
                }
                break;
            case "AC":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.ac_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.ac_off));
                }
                break;
            case "Music System":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.music_player_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.music_player_off));
                }
                break;
            case "Set Top Box":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.set_top_box_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.set_top_box_off));
                }
                break;
            case "Game Console":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.game_console_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.game_console_off));
                }
                break;
            case "Curtain":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.curtain_open));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.curtain_closed));
                }
                break;
            case "Chandelier":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.chandelier_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.chandelier_off));
                }
                break;
            case "Geyser":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.geyser_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.geyser_off));
                }
                break;
            case "Fridge":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.fridge_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.fridge_off));
                }
                break;
            case "Microwave":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.microwave_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.microwave_off));
                }
                break;
            case "Appliance":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.appliance_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.appliance_off));
                }
                break;
            case "Door":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.door_open));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.door_closed));
                }
                break;
            case "Washing Machine":
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.washing_machine_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.washing_machine_off));
                }
                break;
            default:
                if(swStatus) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
                } else {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
                }
                break;
        }
    }

    private class GatewayListenerThread extends Thread {
        boolean goOut = false;

        GatewayListenerThread() {
            this.goOut=false;
        }

        @Override
        public void run() {
            while (!this.goOut) {
                try {
//                    if(Globals.dataInputStream==null || Globals.dataOutputStream==null) {
//                        gatewayTalkerThread.sendLoginMessage();
//                    }
                    if (Globals.dataInputStream!=null && Globals.dataInputStream.available() > 0) {
                        receivedMessage = Globals.dataInputStream.readUTF();
                        RoomActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //ToggleSwitch_1_1_1_1_0_1_0_0_1_1_<End>
                                if(receivedMessage.startsWith("ToggleSwitch")) {
                                    if(!Globals.isForScene) {
                                        processSwitchStates(receivedMessage);
                                    }
                                    if(currentQueryIndex<querymsgs.size())
                                        gatewayTalkerThread.sendMessage(querymsgs.get(currentQueryIndex++));
                                }
                                if(receivedMessage.contains("UpdateNodeSwitchResponse")) {
                                    String[] x=receivedMessage.split("_");
                                    if(Integer.valueOf(x[1])==1) {
                                        NodeSwitch ns=Utilities.getNodeSwitch(Globals.currentNodeSwitchId);
                                        if(ns!=null) {
                                            ns.SwitchName=newSwitchName;
                                            ns.Category=newSwitchCategory;
                                            ns.IRDeviceId=newIrdId;
                                        }
                                        Globals.roomsString=Utilities.generateAllRoomsString();
                                        if(Utilities.writeDataToFile(getApplicationContext())) showPopupMessage("Done");
                                        else showPopupMessage("Not Done");
                                        ThisRoom=Utilities.getThisRoom(Globals.currentRoomId);
                                        generateRoomView();
                                    } else {
                                        showPopupMessage("Error, Please try again.");
                                    }
                                }
                                if(receivedMessage.contains("PermissionResponse")) {
                                    String[] x=receivedMessage.split("_");
                                    if(x.length>1) {
                                        int responseCode=Integer.parseInt(x[1]);
                                        if(responseCode==1) {
                                            isReloggingIn=false;
                                            showHeaderMessage(Utilities.toTitleCase(ThisRoom.RoomName)+" ("+Globals.connectionMode+")");
                                            showPopupMessage("Connection re-established");
                                        }
                                    }
                                }
                            }
                        });
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch(Exception e) {
                    showToast(Globals.unexpectedErrorMessage,Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            }
        }

        private void disconnect(){
            this.goOut = true;
        }
    }

    private class GatewayTalkerThread extends Thread {
        String messageToSend = "";
        boolean goOut = false;

        GatewayTalkerThread() {
            this.messageToSend="";
            this.goOut=false;
        }

        public void sendLoginMessage() {
            try {
                this.messageToSend="";
                showHeaderMessage("Connection lost, Trying to reconnect...");
                Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
                Globals.socket = new Socket();
                Globals.socket.connect(Globals.serverSocketAddress,200);
                Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
                Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
                Globals.dataOutputStream.writeUTF(Globals.loginMessageToSend);
                Globals.dataOutputStream.flush();
                isReloggingIn=true;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            WifiManager wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            while(!this.goOut) {
                try {
                    if (wifiMgr.isWifiEnabled()) {
                        if(Globals.dataInputStream==null || Globals.dataOutputStream==null) {
                            sendLoginMessage();
                        }
                        if(!messageToSend.equals("") && !isReloggingIn){
                            Globals.dataOutputStream.writeUTF(messageToSend);
                            Globals.dataOutputStream.flush();
                            messageToSend = "";
                        }
                    } else {
                        showHeaderMessage("WiFi disconnected, pls. reconnect to WiFi.");
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    //showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    sendLoginMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    //showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    sendLoginMessage();
                } catch(Exception e) {
                    showToast(Globals.unexpectedErrorMessage,Toast.LENGTH_SHORT);
                    e.printStackTrace();
                    sendLoginMessage();
                }
            }
        }

        private void sendMessage(String msg){
            this.messageToSend=msg;
        }

        private void disconnect(){
            this.goOut = true;
        }
    }

    private class GatewayConnectThread extends Thread {
        String messageToSend = "";
        boolean goOut = false;

        GatewayConnectThread() {
            this.messageToSend="";
            this.goOut=false;
        }

        @Override
        public void run() {
            while (!goOut) {
                try {
                    if(Globals.dataInputStream==null || Globals.dataOutputStream==null) {
                        // app probably crashed
                        isReloggingIn=true;
                        Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
                        Globals.socket = new Socket();
                        Globals.socket.connect(Globals.serverSocketAddress,200);
                        Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
                        Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
                        Globals.dataOutputStream.writeUTF(Globals.loginMessageToSend);
                        Globals.dataOutputStream.flush();
                        messageToSend="";
                    }
                    if (Globals.dataInputStream.available() > 0) {
                        receivedMessage = Globals.dataInputStream.readUTF();
                        RoomActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //ToggleSwitch_1_1_1_1_0_1_0_0_1_1_<End>
                                if(receivedMessage.startsWith("ToggleSwitch")) {
                                    if(!Globals.isForScene) {
                                        processSwitchStates(receivedMessage);
                                    }
                                    if(currentQueryIndex<querymsgs.size())
                                        sendMessage(querymsgs.get(currentQueryIndex++));
                                }
                                if(receivedMessage.contains("UpdateNodeSwitchResponse")) {
                                    String[] x=receivedMessage.split("_");
                                    if(Integer.valueOf(x[1])==1) {
                                        NodeSwitch ns=Utilities.getNodeSwitch(Globals.currentNodeSwitchId);
                                        if(ns!=null) {
                                            ns.SwitchName=newSwitchName;
                                            ns.Category=newSwitchCategory;
                                            ns.IRDeviceId=newIrdId;
                                        }
                                        Globals.roomsString=Utilities.generateAllRoomsString();
                                        if(Utilities.writeDataToFile(getApplicationContext())) showPopupMessage("Done");
                                        else showPopupMessage("Not Done");
                                        ThisRoom=Utilities.getThisRoom(Globals.currentRoomId);
                                        generateRoomView();
                                    } else {
                                        showPopupMessage("Error, Please try again.");
                                    }
                                }
                                if(receivedMessage.contains("PermissionResponse")) {
                                    String[] x=receivedMessage.split("_");
                                    if(x.length>1) {
                                        int responseCode=Integer.parseInt(x[1]);
                                        if(responseCode==1) {
                                            isReloggingIn=false;
                                            showPopupMessage("Connection re-established");
                                            toggleReconnectButton(false);
                                        }
                                    }
                                }
                            }
                        });
                    }
                    if(!messageToSend.equals("") && !isReloggingIn){
                        Globals.dataOutputStream.writeUTF(messageToSend);
                        Globals.dataOutputStream.flush();
                        messageToSend = "";
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    toggleReconnectButton(true);
                    disconnect();
//                    showToast("Reconnecting to gateway...",Toast.LENGTH_LONG);
//                    try {
//                        isReloggingIn=true;
//                        Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
//                        Globals.socket = new Socket();
//                        Globals.socket.connect(Globals.serverSocketAddress,200);
//                        Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
//                        Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
//                        Globals.dataOutputStream.writeUTF(Globals.loginMessageToSend);
//                        Globals.dataOutputStream.flush();
//                    } catch (IOException ex) {
//                        showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
                    toggleReconnectButton(true);
                    disconnect();
//                    showToast("Reconnecting to gateway...",Toast.LENGTH_LONG);
//                    try {
//                        isReloggingIn=true;
//                        Globals.serverSocketAddress = new InetSocketAddress(Globals.gatewayIPAddress,Globals.serverSocketPORT);
//                        Globals.socket = new Socket();
//                        Globals.socket.connect(Globals.serverSocketAddress,200);
//                        Globals.dataOutputStream = new DataOutputStream(Globals.socket.getOutputStream());
//                        Globals.dataInputStream = new DataInputStream(Globals.socket.getInputStream());
//                        Globals.dataOutputStream.writeUTF(Globals.loginMessageToSend);
//                        Globals.dataOutputStream.flush();
//                    } catch (IOException ex) {
//                        showToast(Globals.connectionLostMessage,Toast.LENGTH_SHORT);
//                    }
                } catch(Exception e) {
                    showToast(Globals.unexpectedErrorMessage,Toast.LENGTH_SHORT);
                    e.printStackTrace();
                    toggleReconnectButton(true);
                    disconnect();
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
        if(Globals.connectionMode.equals("Local")) {
            if(gatewayListenerThread!=null)
                gatewayListenerThread.disconnect();
            if(gatewayTalkerThread!=null)
                gatewayTalkerThread.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(Globals.connectionMode.equals("Local")) {
            if(gatewayListenerThread!=null)
                gatewayListenerThread.disconnect();
            if(gatewayTalkerThread!=null)
                gatewayTalkerThread.disconnect();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Globals.connectionMode.equals("Local")) {
            connectToGateway();
        }
        // after connecting to gateway, get the status of switches in the room only if,
        //we are not showing scenes view
        if(!Globals.isForScene) {
            querySwitchStates();
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
                String remoteParamString=params[1];
                while(isRemoteTaskRunning) {
                    Thread.sleep(500);
                }
                switch(mode) {
                    case "querySwitch":
                        if(svc.querySwitch(remoteParamString)) {
                            return svc.getResponseString();
                        }
                        break;
                    case "toggleSwitch":
                        if(svc.toggleSwitch(remoteParamString)) {
                            return svc.getResponseString();
                        }
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
                    if(responses[1].startsWith("ToggleSwitch")) {
                        if(!Globals.isForScene) {
                            processSwitchStates(responses[1]);
                        }
                    } else {
                        showToast("Command Sent to Server",Toast.LENGTH_SHORT);
                    }
                } else {
                    //showToast("Could not connect to Remote Server, pls check your internet.",Toast.LENGTH_SHORT);
                    showToast(responses[1],Toast.LENGTH_SHORT);
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
        RoomActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
                builder.setMessage(msg);
                builder.setCancelable(true);

                builder.setPositiveButton("Ok",null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void showHeaderMessage(final String msg) {
        RoomActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
               lblRoomName.setText(msg);
            }
        });
    }

    private void showException(Exception ex) {
        if (BuildConfig.DEBUG) {
            String ex1="";
            for(StackTraceElement a1:ex.getStackTrace()) {
                ex1+=a1.toString();
            }
            showPopupMessage("Err "+ex1);
        }
    }

    private void showToast(final String msg,final int len) {
        RoomActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(toast==null) {
                    toast=Toast.makeText(getApplicationContext(),msg,len);
                    toast.show();
                }
            }
        });
    }

    private void toggleReconnectButton(final boolean toShow) {
        RoomActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(toShow) {
                    btnRefresh.setVisibility(View.VISIBLE);
                } else {
                    btnRefresh.setVisibility(View.GONE);
                }
            }
        });
    }
}