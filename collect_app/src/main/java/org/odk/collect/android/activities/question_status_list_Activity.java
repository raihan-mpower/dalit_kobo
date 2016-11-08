package org.koboc.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.koboc.collect.android.R;
import org.odk.collect.android.model.Case_status;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.StatusLine;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.impl.client.DefaultHttpClient;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;
import org.koboc.collect.android.application.Collect;
import org.koboc.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.util.EntityUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;


/**
 * Created by raihan on 9/29/16.
 */

//Modified by Sabbir

public class question_status_list_Activity extends Activity {
    private static final String CASE_STATUS_URL="/get/caid_rprt_list";
    private static final String SERVER_URL="http://ca.mpower-social.com:8003/";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.case_status_list);
        final ListView caselist = (ListView)findViewById(R.id.list_cases);

        (new AsyncTask(){
            ArrayList<Case_status> case_statuses;
            @Override
            protected Object doInBackground(Object[] params) {
                SharedPreferences settings =
                        PreferenceManager.getDefaultSharedPreferences(Collect.getInstance().getBaseContext());
                HttpClient httpclient = new DefaultHttpClient();
                try {
                    String storedUsername = settings.getString(org.koboc.collect.android.preferences.PreferencesActivity.KEY_USERNAME, null);
                    String URL=SERVER_URL+storedUsername+CASE_STATUS_URL;
                    Log.d("LOG",URL);
                    HttpResponse response = httpclient.execute(new HttpGet(URL));
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        InputStream inputStream;
                        HttpEntity httpEntity=response.getEntity();
                        inputStream=httpEntity.getContent();
                        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder=new StringBuilder();
                        String line=null;
                        while ((line=bufferedReader.readLine())!=null)
                        {
                            stringBuilder.append(line);
                        }

                        String jsonString=stringBuilder.toString();
                      //  Log.d("LOG",jsonString);
                        JSONArray jsonArray=new JSONArray(jsonString);
                        Gson gson=new Gson();
                        case_statuses=new ArrayList<Case_status>();
                        Case_status case_status=null;
                        for (int i=0;i<jsonArray.length();i++)
                        {
                            JSONObject jsonObject=jsonArray.getJSONObject(i);
                            case_status=gson.fromJson(jsonArray.getJSONObject(i).toString(),Case_status.class);
                           // Log.d("LOG",case_status.toString());
                            case_statuses.add(case_status);
                        }

                        /*ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        String responseString = out.toString();
                        Log.v("far cry from hell",responseString);
                        out.close();*/
                       // case_statuses =Case_status.getCaseStatusList(responseString);

                        //case_statuses=Case_status.getCaseStatusList(jsonString);
                        //..more logic
                    } else {
                        //Closes the connection.
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                }catch (Exception e){

                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                    UsersAdapter adapter = new UsersAdapter(question_status_list_Activity.this,R.layout.case_status_row, case_statuses);
                    caselist.setAdapter(adapter);
                    adapter.notifyDataSetChanged();


            }
        }).execute();
          // now it will not fail here
    }
    public class UsersAdapter extends ArrayAdapter{
        ArrayList<Case_status> statuses;
        private LayoutInflater layoutInflater;
        int resource;

        public UsersAdapter(Context context,int resource, ArrayList<Case_status> users) {
            super(context, resource, users);
            this.resource=resource;
            statuses=users;
            layoutInflater=(LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            Log.d("LOG",statuses.toString());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            //final Case_status user =getItem(position);
            ViewHolder holder=null;
          /*  if (statuses.size()==0)
            {
                Log.d("LOG","ArrayLIst SIze is"+statuses.size());
                finish();
            }else {
                Log.d("LOG","size"+statuses.size());

            }*/
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                holder=new ViewHolder();
                convertView = layoutInflater.inflate(resource,null);


                holder.tvCaseID=(TextView) convertView.findViewById(R.id.caseID);
                holder.tvCaseName=(TextView) convertView.findViewById(R.id.casename);
                holder.tvCaseStatus=(TextView) convertView.findViewById(R.id.casestatus);

                convertView.setTag(holder);



            }else {
                holder=(ViewHolder) convertView.getTag();
            }

            /*holder.tvCaseID.setText("Nothing");
            holder.tvCaseStatus.setText("Nothing");
            holder.tvCaseName.setText("Nothing");*/
            /*// Lookup view for data population
            TextView caseID = (TextView) convertView.findViewById(R.id.caseID);
            TextView caseName = (TextView) convertView.findViewById(R.id.casename);
            TextView caseStatus = (TextView) convertView.findViewById(R.id.casestatus);
            // Populate the data into the template view using the data object*/

            //Alternate way added by Sabbir
            holder.tvCaseID.setText(statuses.get(position).getCase_id());
            holder.tvCaseName.setText(statuses.get(position).getCase_name());
            holder.tvCaseStatus.setText(statuses.get(position).getCase_status());


            /*
            holder.tvCaseID.setText(user.getCase_id());
            holder.tvCaseName.setText(user.getCase_name());
            holder.tvCaseStatus.setText(user.getCase_status());
            holder.tvCaseStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(question_status_list_Activity.this);
                    builder.setMessage(statuses.get(position).getCase_status_details())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.setTitle("Case Status");
                    alert.show();
                }
            });*/
            // Return the completed view to render on screen
            return convertView;

        }

        @Override
        public int getCount() {
            return statuses.size();
        }

        //Added by Sabbir for Holding the views
        class ViewHolder
        {
           public TextView tvCaseID,tvCaseName,tvCaseStatus;
        }
    }
}
