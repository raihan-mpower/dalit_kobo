package org.odk.collect.android.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by raihan on 10/2/16.
 */

public class Case_status
{
    private String case_status_details;
    //We will use case_name as Victim Name
    @SerializedName("victim_name")
    private String case_name;
@SerializedName("submission_id")
    private String case_id;
@SerializedName("valid_status")
    private String case_status;
@SerializedName("note")
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCase_status_details ()
    {
        return case_status_details;
    }

    public void setCase_status_details (String case_status_details)
    {
        this.case_status_details = case_status_details;
    }

    public String getCase_name ()
    {
        return case_name;
    }

    public void setCase_name (String case_name)
    {
        this.case_name = case_name;
    }

    public String getCase_id ()
    {
        return case_id;
    }

    public void setCase_id (String case_id)
    {
        this.case_id = case_id;
    }

    public String getCase_status ()
    {
        return case_status;
    }

    public void setCase_status (String case_status)
    {
        this.case_status = case_status;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [case_status_details = "+case_status_details+", case_name = "+case_name+", case_id = "+case_id+", case_status = "+case_status+"]";
    }

    public Case_status(String case_status_details, String case_name, String case_id, String case_status) {
        this.case_status_details = case_status_details;
        this.case_name = case_name;
        this.case_id = case_id;
        this.case_status = case_status;
    }

    public static Case_status getCaseStatusFromJson(String jsonFromCaseStatus){
        try {
            JSONObject caseObject = new JSONObject(jsonFromCaseStatus);
            Case_status case_status = new Case_status(caseObject.getString("case_status_details"),caseObject.getString("case_name"),caseObject.getString("case_id"),caseObject.getString("case_status"));
            return case_status;
        } catch (JSONException e) {
            return null;
        }
    }
    public static ArrayList<Case_status> getCaseStatusList(String jsonArraytoconvert){
        Log.d("LOG",jsonArraytoconvert.toString());
        ArrayList <Case_status> caseStatusList = new ArrayList<Case_status>();
        try {
            JSONArray jsonArray = new JSONArray(jsonArraytoconvert);
            for(int i = 0;i<jsonArray.length();i++){
                caseStatusList.add(getCaseStatusFromJson(jsonArray.getJSONObject(i).toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return caseStatusList;
    }
}