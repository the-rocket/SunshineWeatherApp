package daniyar.com.sunshineweatherapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


/**
 * Created by yernar on 03/11/16.
 */

public class WeatherFragment extends Fragment {

    final static String TAG = WeatherFragment.class.getSimpleName();

    private ArrayAdapter<String> adapter;

    public WeatherFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        //creating fake data
        String[] fakeData = {"Today - Foggy - 20/30", "Tomorr - Sunny - 30/35", "Weds - Soft Rain - 15/30", "Thurs - Sunny - 30/35", "Friday - Sunny - 30/35", "Sat - Sunny - 30/35", "Sun - Sunny - 30/35"};
        final ListView listView = (ListView) fragmentView.findViewById(R.id.listView_forecast);
        List<String> listArray = new ArrayList<>(Arrays.asList(fakeData));
        adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, listArray);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(parent.getContext(), "clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(parent.getContext(),DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, adapter.getItem(position));
                startActivity(intent);
            }
        });
        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.weatherfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            new FetchWeatherTask().execute("77477");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        final String Base_URL = "http://api.openweathermap.org/data/2.5/forecast/daily/";
        final String query_param = "q";
        final String format_param = "mode";
        final String unit_param = "units";
        final String days_param = "cnt";
        final String key_param = "appid";
        final String format = "json";
        final String units = "metric";
        final String password = "Your Password";
        final int numDays = 7;
        private StringBuffer jsonBuilder = new StringBuffer();
        private HttpURLConnection httpsURLConnection = null;
        private BufferedReader bufferedReader = null;
        private String JSONString = null;
        private InputStream inputStream = null;
        private Calendar calendar = null;


        @Override
        protected String[] doInBackground(String... params) {
            if (params == null)
                return null;
            httpsURLConnection = null;
            bufferedReader = null;
            JSONString = null;
            inputStream = null;

            try {
                //URL url = new URL(build(params).toString());
                //httpsURLConnection = (HttpsURLConnection) url.openConnection();
                Log.d(LOG_TAG, build(params).toString());
                httpsURLConnection = (HttpURLConnection) new URL(build(params).toString()).openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.connect();

                inputStream = httpsURLConnection.getInputStream();
                if (inputStream == null)
                    return null;

                extract_json();

                if (jsonBuilder == null)
                    return null;

                JSONString = jsonBuilder.toString();

                Log.d(LOG_TAG, JSONString);

            }catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
            } finally {
                if (httpsURLConnection != null)
                httpsURLConnection.disconnect();

                try {
                    return getWeatherDataFromJson(JSONString, numDays);
                }catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
            return null;
        }

        private String[] getWeatherDataFromJson(String jsonString, int numDays) throws Exception{

            JSONObject weather = new JSONObject(jsonString);
            JSONArray days = weather.getJSONArray("list");
            String[] Result = new String[numDays];
            calendar = new GregorianCalendar();
            //weather.getLong("dt") -> current day incorrect if it has changed city
            String day, description, main;

            for(int index = 0; index < days.length(); ++index) {

                weather = days.getJSONObject(index);

                day = NormalDate();
                description = getInfo(weather, "description").toString();
                main = getInfo(weather, "main").toString();

                int min = Integer.valueOf(getInfo(weather, "min").toString());
                int max = Integer.valueOf(getInfo(weather, "max").toString());

                Result[index] = day + "-" + main + "-" + min + "/" + max;
            }
            return Result;
        }

        private String NormalDate() {
            String week_day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)+"-"
                            + calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);

            calendar.add(Calendar.DAY_OF_WEEK, 7);

            return week_day;
        }

        private Object getInfo(JSONObject weather, final String key) throws JSONException{
            switch (key) {
                case "main": return weather.getJSONArray("weather").getJSONObject(0).getString("main");
                case "description": return weather.getJSONArray("weather").getJSONObject(0).getString("description");
                case "min": return weather.getJSONObject("temp").getInt("min");
                case "max": return weather.getJSONObject("temp").getInt("max");
            }
            return null;
        }

        private void extract_json() throws IOException {
            jsonBuilder.delete(0, jsonBuilder.length());
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonBuilder.append(line).append("/n");
            }
            bufferedReader.close();
        }

        private Uri build(String... params) {
            return Uri.parse(Base_URL).buildUpon()
                .appendQueryParameter(query_param, params[0])
                .appendQueryParameter(format_param, format)
                .appendQueryParameter(unit_param, units)
                .appendQueryParameter(days_param, Integer.toString(numDays))
                .appendQueryParameter(key_param, password).build();
        }


        @Override
        protected void onPostExecute(String[] Result) {
            super.onPostExecute(Result);
            if (Result != null) {
                adapter.clear();
                for (String s : Result)
                    adapter.add(s);
            }
        }
    }
}

