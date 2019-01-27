package dev.medicare;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ListView;

import java.util.ArrayList;

import dev.medicare.models.Dosage;

public class HistoryActivity extends Activity {

    TinyDB tinyDB;
    HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_history);

        tinyDB = new TinyDB(getApplicationContext());

        ListView listView = findViewById(R.id.med_list);

        ArrayList<Object> history = tinyDB.getListObject("history", Dosage.class);
        ArrayList<Dosage> dosageHistory = new ArrayList<>();

        for (Object o : history) {
            dosageHistory.add((Dosage) o);
        }

        adapter = new HistoryAdapter(this, dosageHistory);
        listView.setAdapter(adapter);
    }
}
