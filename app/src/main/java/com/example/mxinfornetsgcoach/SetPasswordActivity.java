package com.example.mxinfornetsgcoach;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SetPasswordActivity extends AppCompatActivity {

    private TextInputEditText password, password_confirm;
    private MaterialButton bt_set;
    private ProgressBar progressBar;
    private StringRequest request;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set_password);

        password = findViewById(R.id.set_password);
        password_confirm = findViewById(R.id.set_password_confirm);
        bt_set = findViewById(R.id.btn_set_pass);
        progressBar = findViewById(R.id.prog_bar_set_pass);

        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        Intent intent = getIntent();
        Uri uri = Uri.parse(intent.getData().toString());

        final String email = uri.getQueryParameter("email");
        final String gimnasio = uri.getQueryParameter("gimnasio");

        queue = Volley.newRequestQueue(this);


        bt_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String contra = password.getText().toString();
                final String contra_confirm = password_confirm.getText().toString();

                //se validan los campos
                if(TextUtils.isEmpty(contra)){
                    password.setError("Por favor introduzca una contraseña");
                    password.requestFocus();
                    return;
                }else if(TextUtils.isEmpty(contra_confirm)){
                    password_confirm.setError("Por favor introduzca la misma contraseña");
                    password_confirm.requestFocus();
                    return;
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    request = new StringRequest(Request.Method.POST, Config.SET_PASSWORD_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                Log.d("RES_SET_PASS", jsonObject.toString());

                                if (jsonObject.has("error")){

                                    String error = jsonObject.getString("error");
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();

                                } else if(jsonObject.has("status")){

                                    String status = jsonObject.getString("status");
                                    String postToken = jsonObject.getString("access_token");
                                    JSONObject usuario = jsonObject.getJSONObject("usuario");
                                    String error = jsonObject.getString("error");

                                    if (status.equals("200")) {
                                        //Log.d("JSONUSUARIO", usuario.toString());
                                        String mensaje = jsonObject.getString("message");

                                        String postId = usuario.getString("id");
                                        String postNombre = usuario.getString("nombre");
                                        String postBiografia = usuario.getString("biografia");
                                        String postEmail = usuario.getString("email");
                                        String postHorarios = usuario.getString("horarios");
                                        String postGimnasio = usuario.getString("id_gimnasio");

                                        ConexionSQLiteHelper con = new ConexionSQLiteHelper(getApplicationContext(), "coaches", null, 2);
                                        SQLiteDatabase db = con.getWritableDatabase();
                                        ContentValues values = new ContentValues();

                                        values.put("idCoach", postId);
                                        values.put("nombre", postNombre);
                                        values.put("biografia", postBiografia);
                                        values.put("email", postEmail);
                                        values.put("horarios", postHorarios);
                                        values.put("gimnasio", postGimnasio);
                                        values.put("token", postToken);

                                        db.insert("coaches", null, values);
                                        db.close();

                                        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();

                                        Intent i = new Intent(SetPasswordActivity.this, LoginActivity.class);
                                        startActivity(i);
                                        SetPasswordActivity.this.finish();

                                    }else if (status.equals("404")) {
                                        String msg = jsonObject.getString("message");
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                    } else if (status.equals("401")) {
                                        String msg = jsonObject.getString("message");
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                    }else if ( status.equals("500")) {
                                        String msg = jsonObject.getString("message");
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                        Toast.makeText(getApplicationContext(), "Tostada de response", Toast.LENGTH_LONG).show();
                                    }
                                }

                                Intent i = new Intent(SetPasswordActivity.this, LoginActivity.class);
                                startActivity(i);

                                finish();

                            } catch (JSONException e){
                                e.printStackTrace();
                                String err = e.toString();
                                Toast.makeText(getApplicationContext(), "Error " + err, Toast.LENGTH_LONG).show();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressBar.setVisibility(View.GONE);

                            NetworkResponse networkResponse = error.networkResponse;

                                String jsonError = new String(networkResponse.data);
                                try {
                                    JSONObject jsonObjectError = new JSONObject(jsonError);
                                    String msg = jsonObjectError.getString("message");

                                    if (error.networkResponse.statusCode == 500  && !msg.equals("Ya se ha establecido una contraseña")) {
                                        String errs = jsonObjectError.getString("errors");
                                        errs = errs.replaceAll("password","");
                                        String errors[] = errs.split(",");
                                        for (int i = 0; i < errors.length; i++) {
                                            errors[i] = errors[i].replaceAll("[^a-zA-Z\\d\\s\u00f1\u00D1\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA]", "");

                                            Toast.makeText(getApplicationContext(), errors[i], Toast.LENGTH_SHORT).show();
                                        }
                                    }else {
                                        String status = jsonObjectError.getString("status");
                                        if (status.equals("500")) {
                                            String err = jsonObjectError.getString("message");
                                            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
                                        } else if (status.equals("404")) {
                                            String err = jsonObjectError.getString("message");
                                            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No se puedo conectar con el servidor. Compreuba que tienes acceso a la red.", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                }catch (JSONException e){
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                    }){
                        @Override
                        protected Map<String, String> getParams() {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("gimnasio", gimnasio);
                            hashMap.put("password", contra);
                            hashMap.put("password_confirmation", contra_confirm);
                            return hashMap;
                        }
                    };
                    queue.add(request);
                }
            }
        });

    }
}