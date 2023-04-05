# CWA Currency Converter

- [`Introduction`](#introduction)
- [`Json Format for Both API`](#json-format-for-both-api)
- [`Getting The Exhange Rates`](#getting-the-exchange-rates)


## Introduction

A simple currency conversion application built using Java and Android studio, which allows users to convert between different currencies in real-time;
currently the app uses 2 currrency conversion APIs, namely:

1. FrankfurterAPI:
- <https://www.frankfurter.app/docs/>
- <https://github.com/hakanensari/frankfurter>

2. Currency-Api: 
- <https://github.com/fawazahmed0/currency-api#readme>

| Using Frankfurter | Using Currency-Api  |
|---|---|
|   |   |
|   |   |
|   |   |

## Json format for both API

## Getting the exchange rates

For both API  usages, GETTING results uses this function:

~~~
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
~~~

and simply calling the function in your code with:

~~~
getExchangeRateData(); 
~~~

Alternatively, you can use AsyncTask<>

- NOTE: The default constructor in android.os.AsyncTask is deprecated, so it's not really recommended

~~~
public class GetExchangeRateData extends AsyncTask<Void, Void, Double> {
    protected Double doInBackground(Void... voids) {
        double exchangeRate = 0;

        try {
            //you may replace this with the URL of you desired API
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
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return exchangeRate;
    }

    protected void onPostExecute(Double exchangeRate) {

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
}
~~~

and calling this function with:

simply calling the function in your code with:

~~~
GetExchangeRateData getExchangeRateData = new GetExchangeRateData();
getExchangeRateData.execute();
~~~

or just simply with:

~~~
new GetExchangeRateData().execute();
~~~
