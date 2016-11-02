package daniyar.com.sunshineweatherapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //return super.onCreateView(inflater, container, savedInstanceState);
            View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
            //creating fake data
            String[] fakeData = {"Today - Foggy - 20/30","Tomorr - Sunny - 30/35","Weds - Soft Rain - 15/30","Thurs - Sunny - 30/35","Friday - Sunny - 30/35","Sat - Sunny - 30/35", "Sun - Sunny - 30/35"};
            ListView listView = (ListView) fragmentView.findViewById(R.id.listView_forecast);
            List<String> listArray = new ArrayList<>(Arrays.asList(fakeData));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, listArray);
            listView.setAdapter(adapter);
            return fragmentView;
        }
    }
}
