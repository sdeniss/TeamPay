package com.teampay.teampay;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by denis on 23/10/14.
 */
public class InGroupActivity extends FragmentActivity{

    InTeamFragment inTeamFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_group);
        if(savedInstanceState != null)
            return;
        inTeamFragment = new InTeamFragment(getIntent().getExtras().getString("teamId"), getIntent().getExtras().getBoolean("isHost", false));
        getSupportFragmentManager().beginTransaction().add(R.id.container, inTeamFragment).commit();
    }

    @Override
    public void onBackPressed() {
        inTeamFragment.leaveTeam(new InTeamFragment.TeamLeaveCallback() {
            @Override
            public void onTeamLeave(boolean success) {
                if(success)
                    pressBack();
            }
        });
    }

    void pressBack(){
        super.onBackPressed();
    }
}