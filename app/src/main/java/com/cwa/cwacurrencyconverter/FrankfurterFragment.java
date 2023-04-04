package com.cwa.cwacurrencyconverter;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FrankfurterFragment} factory method to
 * create an instance of this fragment.
 */
public class FrankfurterFragment extends Fragment {
    Spinner spToCurrency, spFromCurrency;
    EditText txtFromCurrency;
    TextView txtToCurrency;
    TextView lblToCurrency, lblFromCurrency, lblLastUpdated, lblCurrencyConversionRate;

    //progressDialog
    private ProgressDialog progressDialog;

    private String[] countryCodeList, countryNameList;
    ArrayAdapter<String> countryCodeAdapter;

    String fromCurrency, toCurrency;
    String currencyJson = "";

    int fromCountryPosition = 0, toCountryPosition = 0;
    private double exchangeRate = 0;

    private String dateString = "";
    private ArrayList<String> currencyArrayList;


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

        InitialiseStringArrays();
        currencyArrayList = new ArrayList<>();
        loadArrayList();

        //TODO: INITIAL
        countryCodeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, currencyArrayList);
        spFromCurrency.setAdapter(countryCodeAdapter);
        spToCurrency.setAdapter(countryCodeAdapter);

        spFromCurrency.setSelection(0);
        spToCurrency.setSelection(1);

        fromCountryPosition = spFromCurrency.getSelectedItemPosition();
        toCountryPosition = spToCurrency.getSelectedItemPosition();

        fromCurrency = countryCodeList[fromCountryPosition];
        toCurrency = countryCodeList[toCountryPosition];

        lblFromCurrency.setText(fromCurrency);
        lblToCurrency.setText(toCurrency);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Getting currency conversion data");
        progressDialog.setCanceledOnTouchOutside(false);

        if (isInternetConnected()) {
            progressDialog.show();

            getExchangeRateData();
            getUpdatedDate();
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

                fromCurrency = countryCodeList[position];
                toCurrency = countryCodeList[spToCurrency.getSelectedItemPosition()];

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

                toCurrency = countryCodeList[toCountryPosition];
                fromCurrency = countryCodeList[fromCountryPosition];

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

                    fromCurrency = countryCodeList[fromCountryPosition];
                    toCurrency = countryCodeList[toCountryPosition];

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

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void InitialiseStringArrays() {
        countryCodeList = new String[]{
                "AUD","BGN","BRL","CAD",
                "CHF","CNY","CZK","DKK",
                "EUR", "GBP","HKD","HUF",
                "IDR", "ILS","INR","ISK",
                "JPY", "KRW","MXN","MYR",
                "NOK", "NZD","PHP","PLN",
                "RON", "SEK","SGD","THB",
                "TRY", "USD","ZAR"
        };

        countryNameList = new String[]{
                "Australian Dollar", "Bulgarian Lev", "Brazilian Real", "Canadian Dollar",
                "Swiss Franc", "Chinese Yuan", "Czech Koruna", "Danish Krone",
                "Euro", "British Pound", "Hong Kong Dollar", "Hungarian Forint",
                "Indonesian Rupiah", "Israeli New Sheqel", "Indian Rupee", "Icelandic Króna",
                "Japanese Yen", "South Korean Won", "Mexican Peso", "Malaysian Ringgit",
                "Norwegian Krone", "New Zealand Dollar", "Philippine Peso", "Polish Zloty",
                "Romanian Leu", "Swedish Krona", "Singapore Dollar", "Thai Baht",
                "Turkish Lira", "US Dollar", "South African Rand"
        };

    }

    private void loadArrayList() {
        currencyArrayList.clear();

        for(int i = 0; i < countryCodeList.length; ++i){
            currencyArrayList.add(countryNameList[i] + " (" + countryCodeList[i] + ")");
        }
    }

    //TODO: FOR CURRENCY CONVERSION
    public void getExchangeRateData(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() { //like doInBackground in AsyncTask
                try {
                    String GET_URL = "https://api.frankfurter.app/latest?from=" + fromCurrency + "&to=" + toCurrency;
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

                        currencyJson = response.toString();

                        JSONObject jsonObject = new JSONObject(response.toString());

                        exchangeRate = jsonObject.getJSONObject("rates").getDouble(toCurrency);
                        progressDialog.dismiss();
                    }
                }
                catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {//like onPostExecute
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
                    String GET_URL = "https://api.frankfurter.app/latest";
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