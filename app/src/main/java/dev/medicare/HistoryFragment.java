package dev.medicare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import dev.medicare.models.Dosage;

public class HistoryFragment extends Fragment implements View.OnClickListener {

    TinyDB tinyDB;
    HistoryAdapter adapter;

    private FragmentActivity mainActivity;
    private View mainView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivity = getActivity();
        mainView = inflater.inflate(R.layout.fragment_history, container, false);

        tinyDB = new TinyDB(mainActivity);

        initViews();

        return mainView;
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible) {
            initViews();
        }
    }

    private void initViews() {
        ListView listView = mainView.findViewById(R.id.med_list);

        ArrayList<Object> history = tinyDB.getListObject("history", Dosage.class);
        ArrayList<Dosage> dosageHistory = new ArrayList<>();

        for (Object o : history) {
            dosageHistory.add((Dosage) o);
        }

        adapter = new HistoryAdapter(mainActivity, dosageHistory);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {

    }
}
