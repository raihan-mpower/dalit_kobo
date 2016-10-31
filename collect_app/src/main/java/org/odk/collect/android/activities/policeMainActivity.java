package org.koboc.collect.android.activities;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.koboc.collect.android.R;
import org.odk.collect.android.model.PoliceStation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class policeMainActivity extends Activity implements AdapterView.OnItemSelectedListener {
    public Spinner divisionSpinner;

    public ListView listView;

    public StationAdapter stationAdapter;

    private EditText editText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.police_activity_main);


        divisionSpinner = (Spinner) findViewById(R.id.spinnerDiv);

        listView = (ListView) findViewById(R.id.listView);



        ArrayAdapter<CharSequence> divAdapter = ArrayAdapter.createFromResource(this,
                R.array.devision_array, android.R.layout.simple_spinner_item);

        divAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        divisionSpinner.setAdapter(divAdapter);
        divisionSpinner.setOnItemSelectedListener(this);





    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        loadJSONFromAsset();
        switch (position) {
            case 0:
                stationAdapter.getFilter().filter("ঢাকা");

                break;
            case 1:
                stationAdapter.getFilter().filter("চট্টগ্রাম");

                break;
            case 2:
                stationAdapter.getFilter().filter("রাজশাহী");

                break;
            case 3:
                stationAdapter.getFilter().filter("রংপুর");

                break;
            case 4:
                stationAdapter.getFilter().filter("খুলনা");

                break;
            case 5:
                stationAdapter.getFilter().filter("বরিশাল");

                break;
            case 6:
                stationAdapter.getFilter().filter("সিলেট");

                break;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }




    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("allstations.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");

            loadJson(json);
            // Log.d("LOG","GOT JSON"+json.toString());

        } catch (IOException ex) {
            ex.printStackTrace();
            // return null;
        }
        return json;

    }


    public void loadJson(String json) {
        ArrayList<PoliceStation> policeStationsList;

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("stations");
            PoliceStation policeStation = null;
            policeStationsList = new ArrayList<>();
            Gson gson = new Gson();


            for (int i = 0; i < jsonArray.length(); i++)

            {
                //policeStation=new PoliceStation();
                policeStation = gson.fromJson(jsonArray.getJSONObject(i).toString(), PoliceStation.class);
                policeStationsList.add(policeStation);

            }


            stationAdapter = new StationAdapter(getApplicationContext(), R.layout.row_item, policeStationsList);
            listView.setAdapter(stationAdapter);
            stationAdapter.notifyDataSetChanged();


            //Log.d("LOG","GOT THE ARRAY"+policeStation.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public class StationAdapter extends ArrayAdapter implements Filterable

    {
        private LayoutInflater layoutInflater;
        ArrayList<PoliceStation> stationList;
        private ArrayList<PoliceStation> mPoliceStations=new ArrayList<>();
        private int resource;


        public StationAdapter(Context context, int resource, ArrayList<PoliceStation> objects) {
            super(context, resource, objects);
            this.resource = resource;
            stationList = objects;
            mPoliceStations=objects;

            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //View view=convertView;

            ViewHolder holder = null;
            if (convertView == null) {

                holder = new ViewHolder();

                convertView = layoutInflater.inflate(resource, null);


                holder.tvSerial = (TextView) convertView.findViewById(R.id.tvSerial);
                holder.tvMobile = (TextView) convertView.findViewById(R.id.tvMobile);
                holder.tvPosition = (TextView) convertView.findViewById(R.id.tvPosition);
                holder.tvThana = (TextView) convertView.findViewById(R.id.tvThana);
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }


            holder.tvThana.setTextColor(Color.RED);
            holder.tvPosition.setTextColor(Color.BLACK);
            holder.tvMobile.setTextColor(Color.BLUE);

            holder.tvSerial.setText(stationList.get(position).getSerialNo());
            holder.tvMobile.setText(stationList.get(position).getMobileNo());
            holder.tvPosition.setText(stationList.get(position).getPosition());
            holder.tvThana.setText(stationList.get(position).getThana());



                     return convertView;
                 }

        @Override
        public int getCount() {
            return stationList.size();
        }

       @Override
        public Filter getFilter() {
            Filter filter=new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                    ArrayList<PoliceStation> FilteredArrList = new ArrayList<>();

                    if (stationList == null) {
                        stationList = new ArrayList<>(mPoliceStations); // saves the original data in mOriginalValues
                    }

                    /********
                     *
                     *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                     *  else does the Filtering and returns FilteredArrList(Filtered)
                     *
                     ********/
                    if (constraint == null || constraint.length() == 0) {

                        // set the Original result to return

                        results.count = stationList.size();
                        results.values = stationList;
                    } else {
                        constraint = constraint.toString().toLowerCase();
                       // Log.i("TAG",constraint.toString());
                        for (int i = 0; i < stationList.size(); i++) {
                            String data = stationList.get(i).getThana();
                            //Log.i("TAG-ata",data);
                            //Log.d("Tag-Const",constraint.toString());
                            if (data.toLowerCase().contains(constraint)) {

                                //Log.d("TAG-BeforeAdding","is Here");
                                FilteredArrList.add(new PoliceStation(stationList.get(i).getThana(),stationList.get(i).getPosition(),stationList.get(i).getMobileNo(),stationList.get(i).getSerialNo()));
                            }
                        }
                        // set the Filtered result to return
                        results.count = FilteredArrList.size();
                        results.values = FilteredArrList;
                    }
                    return results;
                }





                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    stationList = (ArrayList<PoliceStation>) results.values;
                    stationAdapter.notifyDataSetChanged();

                }


            };
                    return filter;

        }

    }



     class ViewHolder{
         private TextView tvThana,tvPosition,tvMobile,tvSerial;


    }
}
