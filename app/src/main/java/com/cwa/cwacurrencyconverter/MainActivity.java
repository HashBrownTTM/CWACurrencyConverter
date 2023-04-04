package com.cwa.cwacurrencyconverter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cwa.cwacurrencyconverter.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnFrankfurter, btnCurrencyAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFrankfurter = findViewById(R.id.btnFrankfurter);
        btnFrankfurter.setOnClickListener(this);
        btnCurrencyAPI = findViewById(R.id.btnCurrencyAPI);
        btnCurrencyAPI.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.btnFrankfurter:
                intent = new Intent(this, FragmentContainerActivity.class);
                intent.putExtra("apiType", "FrankfurterAPI");
                startActivity(intent);
                break;

            case R.id.btnCurrencyAPI:
                intent = new Intent(this, FragmentContainerActivity.class);
                intent.putExtra("apiType", "CurrencyAPI");
                startActivity(intent);
                break;
        }
    }
}