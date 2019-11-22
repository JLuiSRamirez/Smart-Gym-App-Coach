package com.example.mxinfornetsgcoach;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText te_correo, te_password;
    private MaterialButton button_login;
    private RequestQueue queue;
    private Integer res;
    private StringRequest request;
    private ProgressBar progressBar;
    private TextView forget_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        forget_pass = findViewById(R.id.forget_pass);
        forget_pass.setMovementMethod(LinkMovementMethod.getInstance());

        te_correo = findViewById(R.id.correo);
        te_password = findViewById(R.id.password);
        button_login = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        queue = Volley.newRequestQueue(getApplicationContext());

        ConexionSQLiteHelper conexion = new ConexionSQLiteHelper(getApplicationContext(), "coaches", null, 2);
        SQLiteDatabase db = conexion.getWritableDatabase();

        //Busca el registro de un usuario si ya ha iniciado sesion
        try {
            String query = "SELECT * FROM coaches";
            Cursor cursor = db.rawQuery(query, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                res = cursor.getCount();
            }

            if(res > 0){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

        }catch (Exception e){
            e.getStackTrace();
        }

        forget_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);

                LoginActivity.this.finish();
            }
        });

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailPattern = "^[a-zA-Z0-9\\._-]+@[a-zA-Z0-9-]{2,}[.][a-zA-Z]{2,4}$";

                final String correo = te_correo.getText().toString();
                final String password = te_password.getText().toString();

                //se validan los campos
                if(TextUtils.isEmpty(correo)){
                    te_correo.setError("Ingresa una direccion de correo electronico");
                    te_correo.requestFocus();
                    return;
                }else if ( !correo.matches(emailPattern)) {
                    te_correo.setError("Ingresa un correo valido. Ej. example@mail.com");
                    te_correo.requestFocus();
                }else if(TextUtils.isEmpty(password)){
                    te_password.setError("Por favor introduzca una contraseña válida");
                    te_password.requestFocus();
                    return;
                } else{

                    //final ProgressBar progressBar = new ProgressBar(getApplicationContext());
                    progressBar.setVisibility(View.VISIBLE);


                    request = new StringRequest(Request.Method.POST, Config.LOGIN_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);
                            te_correo.setText("");
                            te_password.setText("");
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                String status = jsonObject.getString("status");
                                String postToken = jsonObject.getString("access_token");
                                JSONObject usuario = jsonObject.getJSONObject("usuario");

                                Log.d("ESTATUS", status);

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

                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(i);
                                    LoginActivity.this.finish();

                                } else if (status.equals("401")) {
                                    String error = jsonObject.getString("message");
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                                    te_correo.setText("");
                                    te_password.setText("");
                                    te_correo.requestFocus();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                String err = e.toString();
                                Toast.makeText(getApplicationContext(), "Error " + err, Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressBar.setVisibility(View.GONE);

                            if (error.networkResponse.statusCode == 500) {
                                Toast.makeText(getApplicationContext(), "La contraseña debe tener entre 8 y 10 caracteres", Toast.LENGTH_LONG).show();
                            } else {
                                NetworkResponse networkResponse = error.networkResponse;

                                if(networkResponse != null && networkResponse.data != null){
                                    String jsonError = new String(networkResponse.data);
                                    try {
                                        JSONObject jsonObjectError = new JSONObject(jsonError);
                                        String status = jsonObjectError.getString("status");
                                        String err = jsonObjectError.getString("message");

                                        if (status.equals("401")) {
                                            te_correo.setText("");
                                            te_password.setText("");
                                            te_correo.requestFocus();
                                            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();

                                        } else {
                                            Toast.makeText(getApplicationContext(), "No se puedo conectar con el servidor. Compruba que tienes acceso a la red.", Toast.LENGTH_LONG).show();
                                        }

                                    }catch (JSONException e){
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("email", correo);
                            hashMap.put("password", password);
                            return hashMap;
                        }
                    };

                    queue.add(request);
                }
            }
        });
    }
}
