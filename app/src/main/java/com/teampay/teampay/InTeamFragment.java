package com.teampay.teampay;

import android.app.ProgressDialog;
import android.content.Entity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by denis on 22/10/14.
 */
public class InTeamFragment extends Fragment {

    private String teamId;
    Double price = 0.0;
    ListView listView;
    SharedPreferences preferences;
    TextView priceTv, myPriceTv;
    ArrayList<User> users;

    public InTeamFragment(String teamId){
        this.teamId = teamId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_in_team, container, false);
        users = new ArrayList<User>();
        priceTv = (TextView) rootView.findViewById(R.id.price_tv);
        myPriceTv = (TextView) rootView.findViewById(R.id.my_price_tv);
        preferences = getActivity().getSharedPreferences(Const.FILE_PREF, 0);
        listView = (ListView) rootView.findViewById(R.id.list_view);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new TeamInfoGetterTask(teamId).execute();
            }
        }, 0, 2000);

        myPriceTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    myPriceTv.setTextColor(Color.parseColor("#848484"));
                    Double myPrice = Algorithm.PaymentCost(users, preferences.getString(Const.PREF_USER_ID,""), price);
                    myPriceTv.setText(myPrice.toString() + "$");
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    myPriceTv.setTextColor(Color.parseColor("#000000"));
                    myPriceTv.setText("Hold to show how much you'll pay");
                }
                return true;
            }
        });

        return rootView;
    }




    public void leaveTeam(TeamLeaveCallback teamLeaveCallback){
        new LeaveTeamTask(teamLeaveCallback).execute();
    }

    class LeaveTeamTask extends AsyncTask<Void,Void,Boolean>{

        ProgressDialog progressDialog;
        TeamLeaveCallback teamLeaveCallback;

        public LeaveTeamTask(TeamLeaveCallback teamLeaveCallback){
            this.teamLeaveCallback = teamLeaveCallback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), null, "Leaving team...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String url = "http://teampay.esy.es/leave-team.php";
            HttpPost httpPost = new HttpPost(url);
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", preferences.getString(Const.PREF_USER_ID, ""));
                jsonObject.put("teamId", teamId);
                httpPost.setEntity(new StringEntity(httpPost.toString()));
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(httpPost);
                String response = EntityUtils.toString(httpResponse.getEntity());
                return new JSONObject(response).getBoolean("success");
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            progressDialog.dismiss();
            if(teamLeaveCallback != null)
                teamLeaveCallback.onTeamLeave(success);
        }


    }



    interface TeamLeaveCallback{
        public void onTeamLeave(boolean success);
    }





    class TeamInfoGetterTask extends AsyncTask<Void,Void,JSONObject>{

        String teamId;

        public TeamInfoGetterTask(String teamId){
            this.teamId = teamId;
        }


        @Override
        protected JSONObject doInBackground(Void... voids) {
            String request_url = "http://teampay.esy.es/get-team.php";
            HttpPost httpPost = new HttpPost(request_url);
            try{
                JSONObject requestJson = new JSONObject();
                requestJson.put("teamId", teamId);
                httpPost.setEntity(new StringEntity(requestJson.toString()));
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                JSONObject responseJson = new JSONObject(EntityUtils.toString(response.getEntity()));
                return responseJson;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if(jsonObject == null)
                return;
            try {
                price = jsonObject.getDouble("price");
                priceTv.setText(jsonObject.getString("price") + "$");
                JSONArray jsonArray = jsonObject.getJSONArray("participants");
                users.clear();
                for(int i = 0; i < jsonArray.length(); i++){
                    users.add(User.fromJson(jsonArray.getJSONObject(i)));
                }
                ArrayList<String> names = new ArrayList<String>();
                for(User user : users){
                    names.add(user.name);
                }
                listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, names));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}
