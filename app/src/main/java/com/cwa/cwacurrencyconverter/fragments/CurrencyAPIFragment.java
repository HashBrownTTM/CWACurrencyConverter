package com.cwa.cwacurrencyconverter.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cwa.cwacurrencyconverter.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link  CurrencyAPIFragment} factory method to
 * create an instance of this fragment.
 */
public class CurrencyAPIFragment extends Fragment {
    Spinner spToCurrency, spFromCurrency;
    EditText txtFromCurrency;
    TextView txtToCurrency;
    TextView lblToCurrency, lblFromCurrency, lblLastUpdated, lblCurrencyConversionRate;

    //progressDialog
    private ProgressDialog progressDialog;

    private ArrayList<String> currencyArrayList;
    ArrayList<String> currencyCodeList, currencyNameList;

    ArrayAdapter<String> spinnerCurrencyAdapter;

    String fromCurrency, toCurrency;
    String currencyJson = "";

    int fromCountryPosition = 0, toCountryPosition = 0;
    private double exchangeRate = 0;

    private String dateString = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_currency_converter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spFromCurrency = view.findViewById(R.id.spFromCurrency);
        spToCurrency = view.findViewById(R.id.spToCurrency);
        txtFromCurrency = view.findViewById(R.id.txtFromCurrency);
        txtToCurrency = view.findViewById(R.id.txtToCurrency);
        lblFromCurrency = view.findViewById(R.id.lblFromCurrency);
        lblToCurrency = view.findViewById(R.id.lblToCurrency);
        lblCurrencyConversionRate = view.findViewById(R.id.lblCurrencyConversionRate);
        lblLastUpdated = view.findViewById(R.id.lblLastUpdated);

        txtFromCurrency.requestFocus();
        //sets the character limit in txtFromCurrency to 15
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(15);
        txtFromCurrency.setFilters(filterArray);

        currencyCodeList = new ArrayList<>();
        currencyNameList = new ArrayList<>();
        currencyArrayList = new ArrayList<>();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Getting currency conversion data");
        progressDialog.setCanceledOnTouchOutside(false);

        //TODO: TODO: Initialising arrays for currency names and currency codes
        if (isInternetConnected()) {
            progressDialog.show();
            InitialiseStringArrays();
        }
        else {
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }

        //TODO: ON_SPINNER_SELECTED_ITEM
        spFromCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromCountryPosition = position;
                toCountryPosition = spToCurrency.getSelectedItemPosition();;

                fromCurrency = currencyCodeList.get(position);
                toCurrency = currencyCodeList.get(toCountryPosition);

                lblFromCurrency.setText(fromCurrency);
                lblToCurrency.setText(toCurrency);

                if(position == spToCurrency.getSelectedItemPosition()){
                    txtToCurrency.setText(txtFromCurrency.getText().toString());
                    String output = "1 " + fromCurrency + " = 1 " + toCurrency;
                    lblCurrencyConversionRate.setText(output);
                }
                else{
                    if (isInternetConnected()) {
                        getExchangeRateData();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spToCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromCountryPosition = spFromCurrency.getSelectedItemPosition();
                toCountryPosition = position;

                toCurrency = currencyCodeList.get(toCountryPosition);
                fromCurrency = currencyCodeList.get(fromCountryPosition);

                lblFromCurrency.setText(fromCurrency);
                lblToCurrency.setText(toCurrency);

                if(position == spFromCurrency.getSelectedItemPosition()){
                    txtToCurrency.setText(txtFromCurrency.getText().toString());
                    String output = "1 " + fromCurrency + " = 1 " + toCurrency;
                    lblCurrencyConversionRate.setText(output);
                }
                else{
                    if (isInternetConnected()) {
                        getExchangeRateData();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //TODO: ON TEXT_CHANGE
        txtFromCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!txtFromCurrency.getText().toString().isEmpty()){
                    fromCountryPosition = spFromCurrency.getSelectedItemPosition();
                    toCountryPosition = spToCurrency.getSelectedItemPosition();

                    fromCurrency = currencyCodeList.get(fromCountryPosition);
                    toCurrency = currencyCodeList.get(toCountryPosition);

                    lblFromCurrency.setText(fromCurrency);
                    lblToCurrency.setText(toCurrency);

                    if(fromCountryPosition == toCountryPosition){
                        txtToCurrency.setText(txtFromCurrency.getText().toString());
                        String output = "1 " + fromCurrency + " = 1 " + toCurrency;
                        lblCurrencyConversionRate.setText(output);
                    }
                    else{
                        if (isInternetConnected()) {
                            getExchangeRateData();
                        }
                    }
                }
                else {
                    txtToCurrency.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    //requires ACCESS_NETWORK_STATE in AndroidManifest
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void InitialiseStringArrays() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        currencyCodeList.clear();
        currencyNameList.clear();
        currencyArrayList.clear();

        executorService.execute(new Runnable() {
            @Override
            public void run() { //like doInBackground in AsyncTask
                try {
                    String GET_URL = "https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies.json";
                    URL url = new URL(GET_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){ //successful
                        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while((inputLine = in.readLine()) != null){
                            response.append(inputLine);
                        }in.close();

                        JSONObject jsonObject = new JSONObject(response.toString());

                        Iterator<String> keys = jsonObject.keys();

                        while(keys.hasNext()){
                            String code = keys.next();
                            String currencyName = jsonObject.getString(code);

                            currencyCodeList.add(code);
                            currencyNameList.add(currencyName);
                        }
                    }
                }
                catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {//like onPostExecute in AsyncTask
                    @Override
                    public void run() {
                        for(int i = 0; i < currencyCodeList.size(); ++i){
                            currencyArrayList.add(currencyNameList.get(i) + " (" + currencyCodeList.get(i) + ")");
                        }

                        spinnerCurrencyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, currencyArrayList);

                        spFromCurrency.setAdapter(spinnerCurrencyAdapter);
                        spToCurrency.setAdapter(spinnerCurrencyAdapter);

                        spFromCurrency.setSelection(0);
                        spToCurrency.setSelection(1);

                        fromCountryPosition = spFromCurrency.getSelectedItemPosition();
                        toCountryPosition = spToCurrency.getSelectedItemPosition();

                        fromCurrency = currencyCodeList.get(fromCountryPosition);
                        toCurrency = currencyCodeList.get(toCountryPosition);

                        lblFromCurrency.setText(fromCurrency);
                        lblToCurrency.setText(toCurrency);

                        getExchangeRateData();
                        getUpdatedDate();
                    }
                });
            }
        });
    }

    //TODO: FOR CURRENCY CONVERSION
    public void getExchangeRateData(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        //performs a network request to retrieve the currency exchange rate
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String GET_URL = "https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies/" + fromCurrency + "/" + toCurrency + ".json";
                    URL url = new URL(GET_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){ //successful
                        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while((inputLine = in.readLine()) != null){
                            response.append(inputLine);
                        }
                        in.close();

                        currencyJson = response.toString();

                        JSONObject jsonObject = new JSONObject(response.toString());

                        exchangeRate = jsonObject.getDouble(toCurrency);
                        progressDialog.dismiss();
                    }
                }
                catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                //updates the UI
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (exchangeRate != 0 && !txtFromCurrency.getText().toString().equals("")) {
                            double input = Double.parseDouble(txtFromCurrency.getText().toString());
                            BigDecimal result = new BigDecimal(input * exchangeRate);

                            txtToCurrency.setText("" + result.setScale(3, RoundingMode.UP).toPlainString());

                            String currencyConversionRate = "1 " + fromCurrency + " = " + (1*exchangeRate) + " " + toCurrency;
                            lblCurrencyConversionRate.setText(currencyConversionRate);
                        }
                        else {
                            Toast.makeText(getContext(), "GET FAILED", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    //TODO:FOR GETTING THE UPDATED DATE
    public void getUpdatedDate(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() { //like doInBackground in AsyncTask
                try {
                    String GET_URL = "https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies/" + fromCurrency + "/" + toCurrency + ".json";
                    URL url = new URL(GET_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){ //successful
                        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while((inputLine = in.readLine()) != null){
                            response.append(inputLine);
                        }in.close();

                        JSONObject jsonObject = new JSONObject(response.toString());

                        dateString = jsonObject.getString("date");
                        progressDialog.dismiss();
                    }
                }
                catch (IOException | JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }

                handler.post(new Runnable() {//like onPostExecute
                    @Override
                    public void run() {
                        if (dateString != null || !dateString.equals("")) {

                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                            try {
                                Date date = inputFormat.parse(dateString);
                                String outputDate = outputFormat.format(date);
                                lblLastUpdated.setText("Rates as of " + outputDate);
                                progressDialog.dismiss();
                            }
                            catch (ParseException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                            }
                        }
                        else {
                            Toast.makeText(getContext(), "GET FAILED", Toast.LENGTH_SHORT).show();;
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        });
    }
}