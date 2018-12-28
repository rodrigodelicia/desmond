package com.app.deliciaefoco.deliciaefoco.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.app.deliciaefoco.deliciaefoco.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private final String baseUrl = "http://portal.deliciaefoco.com.br/api";
    private AlertDialog alerta;
    Context context = this;
    HashMap<String ,Integer> hmLang;
    String FILENAME = "enterprise_file.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });

        setContentView(R.layout.activity_main);



        File file = new File(this.FILENAME);
        if(file.exists()){
            Intent intent = new Intent(this, StoreActivity.class);
            startActivity(intent);
            finish();
        }else{
            this.getEnterprises();
        }

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Spinner spn = (Spinner) findViewById(R.id.spinnerEnterprises);
                String selected = spn.getSelectedItem().toString();
                int id_selected_enterprise = hmLang.get(selected);

                File file = new File(FILENAME);
                try {
                    FileOutputStream outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    outputStream.write((id_selected_enterprise+"").getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    @Override
    protected void onResume (){
        super.onResume();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
    }



    private void getEnterprises(){
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Carregando ...");
        progress.setMessage("Aguarde enquanto carregamos algumas informações");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        JsonArrayRequest jar = new JsonArrayRequest(Request.Method.GET, this.baseUrl + "/enterprises", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("DeliciaEFoco", "Request Finalizado");
                List<String> spinnerArray =  new ArrayList<String>();
                hmLang = new HashMap<String, Integer>();
                try{
                    for(int i=0;i<response.length();i++){
                        JSONObject enterprise = response.getJSONObject(i);
                        spinnerArray.add(enterprise.getString("nome_fantasia"));
                        hmLang.put(enterprise.getString("nome_fantasia"), enterprise.getInt("id"));
                    }


                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Spinner sItems = (Spinner) findViewById(R.id.spinnerEnterprises);
                    sItems.setAdapter(adapter);
                    progress.dismiss();
                }catch (JSONException e){
                    e.printStackTrace();
                    Log.d("DeliciaEFoco", "Falha no JSON");
                    progress.dismiss();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.d("DeliciaEFoco", "Falha ao buscar empresas");
                progress.dismiss();
            }
        });

        requestQueue.add(jar);
    }
}