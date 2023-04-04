package com.cwa.cwacurrencyconverter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class FragmentContainerActivity extends AppCompatActivity implements View.OnClickListener {
    FrameLayout flContainer;
    ImageButton btnBack;
    TextView lblHeader;

    String apiType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        flContainer = findViewById(R.id.flContainer);
        lblHeader = findViewById(R.id.lblHeader);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);


        Intent intent = getIntent();
        apiType = intent.getStringExtra("apiType");

        lblHeader.setText(apiType);

        switch (apiType){
            case "FrankfurterAPI":
                replaceFragment(new FrankfurterFragment());
                break;

            case "CurrencyAPI":
                replaceFragment(new CurrencyAPIFragment());
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(flContainer.getId(), fragment);

        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                finish();
                break;
        }
    }
}