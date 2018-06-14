package com.toyama.wizhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

import com.toyama.includes.model.IRDevice;
import com.toyama.includes.model.STBChannel;
import com.toyama.includes.model.SetTopBox;
import com.toyama.includes.utilities.Globals;
import com.toyama.includes.utilities.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.GridLayout;

@SuppressLint("InflateParams")
public class STBChannelsActivity extends Activity {

    TextView lblStatus,lblModeName;
    GridLayout pnlMovieChannels,pnlEntertainmentChannels,pnlSportsChannels,pnlNewsChannels;
    RelativeLayout pnlMoviesHeader,pnlEntertainmentHeader,pnlSportsHeader,pnlNewsHeader;
    ImageView imgMoviesToggle,imgEntertainmentToggle,imgSportsToggle,imgNewsToggle;

    SetTopBox ThisSetTopBox;
    IRDevice ThisIRDevice;
    String receivedMessage = "";

    GatewayConnectThread gatewayConnectThread = null;
    Toast toast=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_stbchannels);

        ThisIRDevice=Utilities.getIRDevice(Globals.currentIRDeviceId);

        lblStatus = (TextView) findViewById(R.id.lblStatus);
        lblModeName = (TextView) findViewById(R.id.lblActivityHeading);
        pnlMovieChannels = (GridLayout) findViewById(R.id.pnlMovieChannels);
        pnlEntertainmentChannels = (GridLayout) findViewById(R.id.pnlEntertainmentChannels);
        pnlSportsChannels = (GridLayout) findViewById(R.id.pnlSportsChannels);
        pnlNewsChannels = (GridLayout) findViewById(R.id.pnlNewsChannels);

        pnlMoviesHeader = (RelativeLayout) findViewById(R.id.pnlMoviesHeader);
        pnlEntertainmentHeader = (RelativeLayout) findViewById(R.id.pnlEntertainmentHeader);
        pnlSportsHeader = (RelativeLayout) findViewById(R.id.pnlSportsHeader);
        pnlNewsHeader = (RelativeLayout) findViewById(R.id.pnlNewsHeader);

        pnlMovieChannels.setVisibility(View.GONE);
        //pnlMoviesHeader.setVisibility(View.GONE);

        pnlEntertainmentChannels.setVisibility(View.GONE);
        //pnlEntertainmentHeader.setVisibility(View.GONE);

        pnlSportsChannels.setVisibility(View.GONE);
        //pnlSportsHeader.setVisibility(View.GONE);

        pnlNewsChannels.setVisibility(View.GONE);
        //pnlNewsHeader.setVisibility(View.GONE);

        imgMoviesToggle = (ImageView) findViewById(R.id.imgMoviesToggle);
        imgMoviesToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pnlMovieChannels.getVisibility() == View.VISIBLE) {
                    pnlMovieChannels.setVisibility(View.GONE);
                    imgMoviesToggle.setImageResource(R.drawable.btn_down);
                } else {
                    pnlMovieChannels.setVisibility(View.VISIBLE);
                    imgMoviesToggle.setImageResource(R.drawable.btn_up);
                }
            }
        });

        imgEntertainmentToggle = (ImageView) findViewById(R.id.imgEntertainmentToggle);
        imgEntertainmentToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pnlEntertainmentChannels.getVisibility() == View.VISIBLE) {
                    pnlEntertainmentChannels.setVisibility(View.GONE);
                    imgEntertainmentToggle.setImageResource(R.drawable.btn_down);
                } else {
                    pnlEntertainmentChannels.setVisibility(View.VISIBLE);
                    imgEntertainmentToggle.setImageResource(R.drawable.btn_up);
                }
            }
        });

        imgSportsToggle = (ImageView) findViewById(R.id.imgSportsToggle);
        imgSportsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pnlSportsChannels.getVisibility() == View.VISIBLE) {
                    pnlSportsChannels.setVisibility(View.GONE);
                    imgSportsToggle.setImageResource(R.drawable.btn_down);
                } else {
                    pnlSportsChannels.setVisibility(View.VISIBLE);
                    imgSportsToggle.setImageResource(R.drawable.btn_up);
                }
            }
        });

        imgNewsToggle = (ImageView) findViewById(R.id.imgNewsToggle);
        imgNewsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pnlNewsChannels.getVisibility() == View.VISIBLE) {
                    pnlNewsChannels.setVisibility(View.GONE);
                    imgNewsToggle.setImageResource(R.drawable.btn_down);
                } else {
                    pnlNewsChannels.setVisibility(View.VISIBLE);
                    imgNewsToggle.setImageResource(R.drawable.btn_up);
                }
            }
        });
    }

    private float getPixelValue(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void generateChannelsView() {
        RelativeLayout.LayoutParams params;
        String idstr="";
        String tag="";
        View v;
        // ids as follows
        // 5 for image
        try {
            for(STBChannel ch:ThisSetTopBox.Channels) {
                final String chid=String.valueOf(ch.STBChannelId);
                final ImageView img =(ImageView) getLayoutInflater().inflate(R.layout.switch_image_template, null);
                //(ImageView) getLayoutInflater().inflate(R.drawable.ac, null);

                tag=String.valueOf(ch.STBChannelId+"_"+Globals.currentIRDeviceId);
                int resid=getResources().getIdentifier(ch.Filename, "drawable", getPackageName());
                if(resid<=0) continue;
                img.setImageDrawable(getResources().getDrawable(resid));
                img.setAdjustViewBounds(true);

                idstr=String.valueOf(ch.STBChannelId)+"5"; // 1 for toggle button
                img.setId(Integer.valueOf(idstr));
                img.setTag(tag);

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        sendToGateway("SendIR_"+String.valueOf(v.getTag())+"_CH");
                        // channel here is not ir channel but STB channel like star sports etc
                    }
                });

                switch(ch.Category) {
                    case "Movies":
                        pnlMovieChannels.addView(img);
                        break;
                    case "Entertainment":
                        pnlEntertainmentChannels.addView(img);
                        break;
                    case "Sports":
                        pnlSportsChannels.addView(img);
                        break;
                    case "News":
                        pnlNewsChannels.addView(img);
                        break;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            lblStatus.setText(ex.getMessage());
        }
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

    private boolean fillSetTopBoxes(String sString) {
        boolean isDone=false;
        String sprop="";
        String[] sprops;
        String ssprop="";
        String[] ssprops;
        SetTopBox s=new SetTopBox();
        STBChannel ch=new STBChannel();

        Globals.AllSetTopBoxes.clear();
        try {
            if(sString.startsWith("<SetTopBoxes>")) {
                sString=sString.replace("<SetTopBoxes>","");
                if(sString.endsWith("</SetTopBoxes>")) {
                    sString=sString.replace("</SetTopBoxes>","");
                }
                if(!sString.equals("")) {
                    String[] rArray=sString.split("<ETXSetTopBox>");
                    for(String scenestr:rArray) {
                        sprop=scenestr.substring(0,scenestr.indexOf("<STBChannels>"));
                        sprops=sprop.split("\\*");
                        s=new SetTopBox();
                        if(!sprop.isEmpty()) {
                            s.SetTopBoxId=Integer.valueOf(sprops[0].trim());
                            s.Name=sprops[1].trim();
                        } else {
                            s.Name="Error in STB";
                        }

                        String sceneswistr=scenestr.substring(scenestr.indexOf("<STBChannels>")); // removing room props from this room string

                        if(sceneswistr.startsWith("<STBChannels>")) {
                            sceneswistr=sceneswistr.replace("<STBChannels>","");
                            if(sceneswistr.endsWith("</STBChannels>")) {
                                sceneswistr=sceneswistr.replace("</STBChannels>","");
                            }
                            String[] ssArray=sceneswistr.split("<ETXSTBChannel>");
                            for(String sswitchstr:ssArray) {
                                ssprop=sswitchstr;
                                ssprops=sswitchstr.split("\\*");
                                ch=new STBChannel();
                                if(!ssprop.isEmpty()) {
                                    ch.STBChannelId=Integer.valueOf(ssprops[0]);
                                    ch.Number=Integer.valueOf(ssprops[1]);
                                    ch.Name=ssprops[2];
                                    ch.Filename=ssprops[3];
                                    ch.Category=ssprops[4];
                                    ch.Language=ssprops[5];
                                }
                                s.Channels.add(ch);
                            }
                        }
                        Globals.AllSetTopBoxes.add(s);
                    }
                } else {
                    // no scenes are available
                }
                isDone=true;
            }
        } catch(Exception ex) {
            String ex1 = "";
            for (StackTraceElement a1 : ex.getStackTrace()) {
                ex1 += a1.toString();
            }
            lblStatus.setText(ex1);
        } finally {

        }
        return isDone;
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
        if(Globals.connectionMode.equals("Local")) {
            connectToGateway();
        }
        lblStatus.setText("Loading Buttons...");
        lblModeName.setText(Globals.currentChannelsMode.toUpperCase(Locale.ENGLISH));
        generateChannelsView();
        lblStatus.setText("");
        Globals.isSetTopBoxesLoaded=true;
    }

    private void showMessage(final String msg) {
        STBChannelsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lblStatus.append(msg+"\n");
            }
        });
    }

    private void showToast(final String msg,final int len) {
        STBChannelsActivity.this.runOnUiThread(new Runnable() {
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