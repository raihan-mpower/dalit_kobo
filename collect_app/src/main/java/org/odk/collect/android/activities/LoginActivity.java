package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.koboc.collect.android.R;

/**
 * Created by Sabbir on 26,October,2016
 * mPower
 * Dhaka
 */
public class LoginActivity extends Activity {
    public EditText etUserName,etPassword;
    public Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUserName=(EditText) findViewById(R.id.editTextUsername);
        etPassword=(EditText) findViewById(R.id.edittextPassword);
        btnLogin=(Button) findViewById(R.id.btnLogIn);


    }

    public void logInClicked(View view)
    {
        String userName=etUserName.getText().toString();
        String password=etPassword.getText().toString();
        if (!userName.equals("") && !password.equals(""))
        {
            //Needs to be done
            new LoginTask().execute(userName,password);
        }
        else {
            Toast.makeText(getApplicationContext(),"Please Enter UserName/Password",Toast.LENGTH_SHORT).show();
        }
    }

    class LoginTask extends AsyncTask<String,String,String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
           /* progressDialog.setMessage("Loading..");
            progressDialog.setCancelable(false);
            progressDialog.show();*/
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String userName=params[1];
            String password=params[2];

            if (userName.toLowerCase()=="himel" && password.toLowerCase()=="himel")
            {
                String s="success";
                return s;
            }else {
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            //progressDialog.dismiss();
            super.onPostExecute(s);
            if (s.equals("success"))
            {
                Intent intent=new Intent(LoginActivity.this, org.koboc.collect.android.activities.MainMenuActivity.class);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
            }



      }

    }
}
