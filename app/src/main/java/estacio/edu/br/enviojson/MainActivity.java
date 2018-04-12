package estacio.edu.br.enviojson;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private Button botao;
    private EditText nome;
    private EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.botao = (Button) findViewById(R.id.button);
        this.nome = (EditText) findViewById(R.id.nome);
        this.email = (EditText) findViewById(R.id.email);

        botao.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enviarDados();
            }
        });
    }

    private void enviarDados() {
        JSONObject postData = new JSONObject();
        try {
            postData.put("name",nome.getText().toString());
            postData.put("email",email.getText().toString());

            SendDeviceDetails t = new SendDeviceDetails();
            t.execute("http://172.16.1.18/json_server/service.php", postData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private class SendDeviceDetails extends AsyncTask<String, Void, String> {
        private ProgressDialog progress = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            //display progress dialog.
            this.progress.setMessage("Aguarde...");
            this.progress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

                httpURLConnection.setReadTimeout(15000 /* milliseconds */);
                httpURLConnection.setConnectTimeout(15000 /* milliseconds */);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();


                //pega o codigo da requisicao http
                int responseCode=httpURLConnection.getResponseCode();

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
            if (progress.isShowing()) {
                progress.dismiss();
            }


            JSONObject json = null;
            Long codigo = null;
            String msg = null;
            try {
                json = new JSONObject(result);
                codigo = json.getLong("status");
                msg = json.getString("msg");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String titulo = "Sucesso";
            if( codigo != 1L){
                titulo = "Erro";
            }



            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage(msg)
                    .setTitle(titulo);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();

            dialog.show();
        }

    }
}