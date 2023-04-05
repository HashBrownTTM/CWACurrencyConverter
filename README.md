# CWA Currency Converter

- [`Introduction`](#introduction)
    - [`1. Frankfurter`](#1-frankfurterapi) 
    - [`2. Currency-Api`](#2-currency-api)
- [`Json Format for Both API`](#json-format-for-both-api)
    - [`1. Frankfurter`](#1-frankfurther)
    - [`2. Currency-Api`](#2-currency-api-1)
- [`Getting The Exhange Rates`](#getting-the-exchange-rates)


## Introduction

A simple currency conversion application built using Java and Android studio, which allows users to convert between different currencies in real-time;
currently the app uses 2 currrency conversion APIs, namely:

### 1. FrankfurterAPI:

- <https://www.frankfurter.app/docs/>

- <https://github.com/hakanensari/frankfurter>

| Using Frankfurter | *[FrankfurterFragment.java](https://github.com/HashBrownTTM/CWACurrencyConverter/blob/master/app/src/main/java/com/cwa/cwacurrencyconverter/fragments/FrankfurterFragment.java)* |
| ----------- | ----------- |

https://user-images.githubusercontent.com/93540733/230078213-315602d0-57a8-4ae2-a802-d7bdff999d10.mp4

### 2. Currency-Api: 

- <https://github.com/fawazahmed0/currency-api#readme>

| Using Currency-Api | *[CurrencyAPIFragment.java](https://github.com/HashBrownTTM/CWACurrencyConverter/blob/master/app/src/main/java/com/cwa/cwacurrencyconverter/fragments/CurrencyAPIFragment.java)* |
| ----------- | ----------- |

https://user-images.githubusercontent.com/93540733/230078322-317cbcfa-9547-4487-bc1f-bc12f0d26079.mp4


## Json format for both API

You can read on about how the usage of the APIs in the links provided above, but here's the general idea for both:

### 1. Frankfurther

To get the latest exchange rates, use:

> https://api.frankfurter.app/latest

which will return:

~~~
{"amount":1.0,"base":"EUR","date":"2023-04-04",
"rates":
{"AUD":1.6154,"BGN":1.9558,"BRL":5.5121,"CAD":1.4641,
"CHF":0.9954,"CNY":7.5034,"CZK":23.418,"DKK":7.4513,"GBP":0.87333, and so on...}}
~~~

> - Note: You can replace latest with any past date, such as 2012-05-01

To get the latest exhange rates for a specific currency (e.g. ZAR), add ?from=(currency code):

> https://api.frankfurter.app/latest?from=ZAR

which will return:

~~~
{"amount":1.0,"base":"ZAR","date":"2023-04-04",
"rates":
{"AUD":0.08309,"BGN":0.1006,"BRL":0.28352,"CAD":0.07531,
"CHF":0.0512,"CNY":0.38595,"CZK":1.2045,"DKK":0.38327,"EUR":0.05144, and so on...}}
~~~

> - Note: When a specific currency isn't given, the default currency is EUR

To get the latest exhange rates from one currency to another (e.g. ZAR to USD), add ?from=(currency code)&to=(currency code):

> https://api.frankfurter.app/latest?from=ZAR&to=USD

which will return:

~~~
{"amount":1.0,"base":"ZAR","date":"2023-04-04","rates":{"USD":0.05607}}
~~~

> - Note: The from and to currencies cannot be the same, as it will result in no data being found:
>
> https://api.frankfurter.app/latest?from=ZAR&to=ZAR
>
> returns:

~~~
{"message":"not found"}
~~~
   
### 2. Currency-Api

Use the following to get the list of currency codes and names:

> https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies.json

which will return:

~~~
{
    "1inch": "1inch Network",
    "aave": "Aave",
    "ada": "Cardano",
    "aed": "United Arab Emirates Dirham",
    "afn": "Afghan afghani",
    "algo": "Algorand",
    "all": "Albanian lek",
    "amd": "Armenian dram",
    "amp": "Synereo",
    and so on...
}
~~~
   
To get the latest exhange rates for a specific currency (e.g. ZAR), add /(currency code) after /currencies:

> https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies/zar.json

which will return:

~~~
{
    "date": "2023-04-02",
    "zar": {
        "1inch": 0.104714,
        "aave": 0.000761,
        "ada": 0.144422,
        "aed": 0.206403,
        "afn": 4.875624,
        "algo": 0.253741,
        "all": 5.861798,
        "amd": 21.832279,
        "amp": 15.053744,
        and so on...
        }
}
~~~

> - Note: You can replace latest with any past date, such as 2012-05-01
   
To get the latest exhange rates from one currency to another (e.g. ZAR to USD), add /(from currency code)/(to currency code) after /currencies:

> https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies/zar/usd.json

which will return:

~~~
{
    "date": "2023-04-04",
    "usd": 0.05612
}
~~~

## Getting the exchange rates

For both API  usages, GETTING results uses this function:

~~~
public void getExchangeRateData(){
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    //performs a network request to retrieve the currency exchange rate
    executorService.execute(new Runnable() {
        @Override
        public void run() {
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

                    
                    //response.toString being the json of the api as a String
                    JSONObject jsonObject = new JSONObject(response.toString());

                    exchangeRate = jsonObject.getJSONObject("rates").getDouble(toCurrency);
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
~~~

and simply calling the function in your code with:

~~~
getExchangeRateData(); 
~~~

Alternatively, you can use AsyncTask<>

- NOTE: The default constructor in android.os.AsyncTask is deprecated, so it is not recommended

~~~
public class GetExchangeRateData extends AsyncTask<Void, Void, Double> {

    //performs a network request to retrieve the currency exchange rate
    protected Double doInBackground(Void... voids) {
        double exchangeRate = 0;
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
                }
                in.close();

                //response.toString being the json of the api as a String
                JSONObject jsonObject = new JSONObject(response.toString());

                exchangeRate = jsonObject.getJSONObject("rates").getDouble(toCurrency);
                progressDialog.dismiss();
            }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        
        //return the exchangeRate, so that it's used in onPostExecute() 
        return exchangeRate;
    }

    //updates the UI
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

~~~
GetExchangeRateData getExchangeRateData = new GetExchangeRateData();
getExchangeRateData.execute();
~~~

or just simply with:

~~~
new GetExchangeRateData().execute();
~~~
