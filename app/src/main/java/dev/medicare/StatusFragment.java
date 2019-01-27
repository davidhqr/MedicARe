package dev.medicare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.HashMap;

import dev.medicare.models.Dosage;

public class StatusFragment extends Fragment implements View.OnClickListener {

    TinyDB tinyDB;

    private FragmentActivity mainActivity;
    private View mainView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivity = getActivity();
        mainView = inflater.inflate(R.layout.fragment_status, container, false);

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
        HashMap<String, Integer> amounts = getAmounts();

        TextView med1Name = mainView.findViewById(R.id.med1_name);
        TextView med1Percent = mainView.findViewById(R.id.med1_progress_text);
        CircularProgressBar med1Progress = mainView.findViewById(R.id.med1_progress);

        TextView med2Name = mainView.findViewById(R.id.med2_name);
        TextView med2Percent = mainView.findViewById(R.id.med2_progress_text);
        CircularProgressBar med2Progress = mainView.findViewById(R.id.med2_progress);

        TextView med3Name = mainView.findViewById(R.id.med3_name);
        TextView med3Percent = mainView.findViewById(R.id.med3_progress_text);
        CircularProgressBar med3Progress = mainView.findViewById(R.id.med3_progress);

        TextView med4Name = mainView.findViewById(R.id.med4_name);
        TextView med4Percent = mainView.findViewById(R.id.med4_progress_text);
        CircularProgressBar med4Progress = mainView.findViewById(R.id.med4_progress);

        TextView med5Name = mainView.findViewById(R.id.med5_name);
        TextView med5Percent = mainView.findViewById(R.id.med5_progress_text);
        CircularProgressBar med5Progress = mainView.findViewById(R.id.med5_progress);

        int count = 0;

        for (String key : amounts.keySet()) {
            ++count;
            if (count == 1) {
                int percent = (int) (amounts.get(key) / 5.0) * 100;
                med1Name.setText(key);
                med1Percent.setText(amounts.get(key) + "%");
                med1Progress.setProgress(percent);
            } else if (count == 2) {
                int percent = (int) (amounts.get(key) / 5.0) * 100;
                med2Name.setText(key);
                med2Percent.setText(percent + "%");
                med2Progress.setProgress(percent);
            } else if (count == 3) {
                int percent = (int) (amounts.get(key) / 5.0) * 100;
                med3Name.setText(key);
                med3Percent.setText(percent + "%");
                med3Progress.setProgress(percent);
            }
        }

        if (count == 0) {
            med1Name.setVisibility(View.INVISIBLE);
            med1Percent.setVisibility(View.INVISIBLE);
            med1Progress.setVisibility(View.INVISIBLE);
        }
        if (count <= 1) {
            med2Name.setVisibility(View.INVISIBLE);
            med2Percent.setVisibility(View.INVISIBLE);
            med2Progress.setVisibility(View.INVISIBLE);
        }
        if (count <= 2) {
            med3Name.setVisibility(View.INVISIBLE);
            med3Percent.setVisibility(View.INVISIBLE);
            med3Progress.setVisibility(View.INVISIBLE);
        }
        if (count <= 3) {
            med4Name.setVisibility(View.INVISIBLE);
            med4Percent.setVisibility(View.INVISIBLE);
            med4Progress.setVisibility(View.INVISIBLE);
        }
        if (count <= 4) {
            med5Name.setVisibility(View.INVISIBLE);
            med5Percent.setVisibility(View.INVISIBLE);
            med5Progress.setVisibility(View.INVISIBLE);
        }

    }

    private HashMap<String, Integer> getAmounts() {
        HashMap<String, Integer> map = new HashMap<>();
        ArrayList<Object> history = tinyDB.getListObject("history", Dosage.class);

        for (Object o : history) {
            Dosage dosage = (Dosage) o;
            int amount = dosage.getNumPills();
            if (map.containsKey(dosage.getName())) {
                amount += map.get(dosage.getName());
            }
            map.put(dosage.getName(), amount);
        }
        return map;
    }

    @Override
    public void onClick(View view) {

    }
}
