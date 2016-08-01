package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.internal.message.Message;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

public class MainActivity extends Activity {
    Handler handler;
    Runnable goOver = new Runnable() {
        @Override
        public void run() {
            Intent selectRobot = new Intent(MainActivity.this, SelectRobot.class);
            selectRobot.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
            startActivity(selectRobot);
            finish();
        }
    };

    public MainActivity(){

        //super("Mobile Controller", "Mobile Controller");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView logo = (ImageView) findViewById(R.id.woniklogo);
        ImageView robotics = (ImageView) findViewById(R.id.robotics);
        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        robotics.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        handler = new Handler();
        handler.postDelayed(goOver, 3000);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


//    @Override
//    public void init(NodeMainExecutor nodeMainExecutor){
//
//        NodeConfiguration nodeConfiguration =
//                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
//        nodeConfiguration.setMasterUri(getMasterUri());
//
//        PublishingSet pSet = new PublishingSet("mobile_base/commands/velocity",
//                geometry_msgs.Twist._TYPE, 100) {
//            @Override
//            public void publishingRoutine(ConnectedNode connectedNode) {
//
//            }
//
//            @Override
//            public void onLoopFinished() {
//
//            }
//
//        };
//
//        SubscribingSet sSet = new SubscribingSet("p1_sonar", sensor_msgs.Range._TYPE){
//            @Override
//            public void subscribingRoutine(Message message) {
//
//                sensor_msgs.Range msg = (sensor_msgs.Range)message;
//
//                System.out.println(msg.getRange());
//
//            }
//
//        };
//
//
//        AndroidNode androidNode = new AndroidNode();
//
//        androidNode.addSubscriber(sSet);
//
//        nodeMainExecutor.execute(androidNode, nodeConfiguration);
//
//    }


}
