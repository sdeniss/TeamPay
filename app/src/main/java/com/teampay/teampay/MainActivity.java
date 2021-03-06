package com.teampay.teampay;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    HostTeamFragment hostTeamFragment;
    NearbyTeamsFragment nearbyTeamsFragment;

    FragmentTransaction fragTransaction;

    //Please note: InTeamFragment:Fragment was replaced by InGroupActivity:Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSharedPreferences(Const.FILE_PREF, 0).getString(Const.PREF_USER_ID, "").equals("")){
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        }

        final FragmentManager fragMan = getSupportFragmentManager();
        fragTransaction  = fragMan.beginTransaction();

        hostTeamFragment = new HostTeamFragment(new HostTeamFragment.OnStartTeamListener() {
            @Override
            public void onStartTeam(String teamId) {
                /*
                inTeamFragment = new InTeamFragment(teamId);
                fragTransaction = fragMan.beginTransaction();
                fragTransaction.remove(hostTeamFragment);
                fragTransaction.remove(nearbyTeamsFragment);
                fragTransaction.add(R.id.root_layout, inTeamFragment);
                fragTransaction.commit();*/
                Intent i = new Intent(MainActivity.this, InGroupActivity.class);
                i.putExtra("teamId", teamId);
                i.putExtra("isHost", true);
                startActivity(i);
            }
        });

        nearbyTeamsFragment = new NearbyTeamsFragment(new NearbyTeamsFragment.OnJoinTeamListener() {
            @Override
            public void onJoinTeam(String teamId) {
                Intent i = new Intent(MainActivity.this, InGroupActivity.class);
                i.putExtra("teamId", teamId);
                startActivity(i);
            /*
                inTeamFragment = new InTeamFragment(teamId);
                fragTransaction = fragMan.beginTransaction();
                fragTransaction.remove(hostTeamFragment);
                fragTransaction.remove(nearbyTeamsFragment);
                fragTransaction.add(R.id.root_layout, inTeamFragment);
                fragTransaction.commit();*/
            }
        });



        //Insert frags
        fragTransaction.add(R.id.root_layout, hostTeamFragment).commit();
        fragTransaction.add(R.id.root_layout, nearbyTeamsFragment).commit();


    }





}
