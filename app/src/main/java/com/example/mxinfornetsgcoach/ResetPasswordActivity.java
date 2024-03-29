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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText password, password_confirm, email;
    private MaterialButton bt_reset;
    private ProgressBar progressBar;
    private StringRequest request;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reset_password);

        password = findViewById(R.id.pass_reset);
        password_confirm = findViewById(R.id.pass_reset_confirm);
        email = findViewById(R.id.email_reset);
        bt_reset = findViewById(R.id.btn_reset_pass);
        progressBar = findViewById(R.id.prog_bar_reset_pass);

        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        Intent intent = getIntent();
        Uri uri = Uri.parse(intent.getData().toString());

        final String token = uri.getQueryParameter("token");

        //Log.d("TOKEN", token);

        queue = Volley.newRequestQueue(this);

        bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailPattern = "^[a-zA-Z0-9\\._-]+@[a-zA-Z0-9-]{2,}[.][a-zA-Z]{2,4}$";

                final String contra = password.getText().toString();
                final String contra_confirm = password_confirm.getText().toString();
                final String correo = email.getText().toString();


                //se validan los campos
                if (TextUtils.isEmpty(correo)){
                    email.setError("Ingresa un correo electrónico");
                    email.requestFocus();
                    return;
                }else if ( !correo.matches(emailPattern)) {
                    email.setError("Ingresa un correo valido. Ej. example@mail.com");
                    email.requestFocus();
                }else if(TextUtils.isEmpty(contra)){
                    password.setError("Por favor introduzca una contraseña");
                    password.requestFocus();
                    return;
                }else if(TextUtils.isEmpty(contra_confirm)){
                    password_confirm.setError("Por favor introduzca una contraseña");
                    password_confirm.requestFocus();
                    return;
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    request = new StringRequest(Request.Method.POST, Config.RESET_PASSWORD_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                if(jsonObject.has("error")){
                                    String error = jsonObject.getString("error");
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                                }else if(jsonObject.has("status")){
                                    String status = jsonObject.getString("status");
                                    String postToken = jsonObject.getString("access_token");
                                    JSONObject usuario = jsonObject.getJSONObject("usuario");

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

                                        Intent i = new Intent(ResetPasswordActivity.this, MainActivity.class);
                                        startActivity(i);
                                        ResetPasswordActivity.this.finish();


                                    } else if (status.equals("401")) {
                                        String error = jsonObject.getString("message");
                                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                                    }
                                }

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
                            if(networkResponse != null && networkResponse.data != null){

                                String jsonError = new String(networkResponse.data);
                                try {
                                    JSONObject jsonObjectError = new JSONObject(jsonError);
                                    if (jsonObjectError.has("error")) {
                                        String err = jsonObjectError.getString("error");
                                        Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
                                    }else if (jsonObjectError.has("password")) {
                                        String errs = jsonObjectError.getString("password");
                                        String errors[] = errs.split(",");
                                        for (int i = 0; i < errors.length; i++) {
                                            errors[i] = errors[i].replaceAll("[^a-zA-Z\\d\\s\u00f1\u00D1\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA]", "");
                                            Toast.makeText(getApplicationContext(), errors[i], Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }catch (JSONException e){
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("token", token);
                            hashMap.put("email", correo);
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