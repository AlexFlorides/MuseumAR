package com.example.museumar;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ArchitectActivity extends AppCompatActivity {

    protected ArchitectView architectView;

    // free licence key generated by Wikitude
    final String licenceKey = "aarGR0sJUBFZ2ywpvbeMVAf09nl3q/vdY/O0pxGkw/RBgO7mWtWDz/UwGuuqLCnBA7bzHACHkeuOsSZodjIqWr2JSTwTj2pRLQbP8KzN5Vf4ks+3o9enXlyb6O4SuoSQ6ApgufDTce1H/dNRjLAFNDK/MD4DLhF6TLqiuIzbcFhTYWx0ZWRfX7gzV+Q8mGBM0fbOch4ihcEd5XERSEpWz5b1NEtnJEzj4cBBpKck1ZSfTVQFRy3HiO0RVLGsmWZP+/AVZ9u59fl6R0Bt+LMZLVS7hrhE0cJ4K1A/Bgv9wqp1EpSjmVUopC+9ehY1aKrXgNLRAcZxyEe4RgA9qKfqUuu2CCRi23cW/AQMKJqOXStJWZg1/XboeihLR6jtUUkzye7bY3c6Td68GYZcPq/xlWbVDN9Ct1Gv6Du5e7BDWpltQbSyKMc12gvVkLw5oG6jJc7mTirNykIL/Jl+7+7gASsAsdEGiIqVLp7aGDPgvkfih7R6gFtuo7MD6rq4eTM+3pY87bgCt5fH3lMTZjI6PpUgp1aOalZ+7Q9NC8ipyMmHoyRZpFUzyzeiryZAIfubavQVdl7g/lrBL3tNzgWor1/sAU/4KmvmW7zSRn4gYd7YhX67HBssBpkSfX/geTPUNBj3RFDi3bJLvaaLi2FQudAn+jiX5V0Hr/w0EbNCntE2zgJYN60wI19rX/fsZ58Ph0ggPcjJYXyiSxovm7iFpEezkmcDcnV5wUhxj6f19EVPSszT9qxUO1aeMFPHBMVW";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView.setWebContentsDebuggingEnabled(true);

        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration(); // Creates a config with its default values.
        config.setLicenseKey(licenceKey); // Has to be set, to get a trial license key visit http://www.wikitude.com/developer/licenses.

        architectView = new ArchitectView(this);
        architectView.onCreate(config); // create ArchitectView with configuration

        architectView.addArchitectJavaScriptInterfaceListener(new ArchitectJavaScriptInterfaceListener() {
            @Override
            public void onJSONObjectReceived(JSONObject jsonObject) {
                try {
                    Intent intent = new Intent(ArchitectActivity.this, ImagesActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("targetName", jsonObject.getString("targetName1"));
                    intent.putExtras(bundle);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        setContentView(architectView);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        this.architectView.onPostCreate();

        try {
            this.architectView.load("index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.architectView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.architectView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.architectView.clearCache();
        this.architectView.onDestroy();
    }
}
