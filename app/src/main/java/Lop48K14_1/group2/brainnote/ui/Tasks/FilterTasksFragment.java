package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.R;

public class FilterTasksFragment extends Fragment {

    private static final String PREFS_NAME = "TaskFilterPrefs";
    private static final String KEY_FLAGGED = "show_flagged";
    private static final String KEY_DUE_DATE = "show_due_date";
    private static final String KEY_COMPLETED = "show_completed";
    private static final String KEY_PRIORITY_LOW = "priority_low";
    private static final String KEY_PRIORITY_MEDIUM = "priority_medium";
    private static final String KEY_PRIORITY_HIGH = "priority_high";

    private ImageButton closeButton;
    private Button resetButton;
    private Switch flaggedSwitch, dueDateSwitch, completedSwitch;
    private ChipGroup priorityChipGroup;
    private TextView headerTitle;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_tasks, container, false);

        // Initialize views
        closeButton = view.findViewById(R.id.close_button);
        resetButton = view.findViewById(R.id.reset_button);
        flaggedSwitch = view.findViewById(R.id.flagged_switch);
        dueDateSwitch = view.findViewById(R.id.due_date_switch);
        completedSwitch = view.findViewById(R.id.completed_switch);
        priorityChipGroup = view.findViewById(R.id.priority_chip_group);
        headerTitle = view.findViewById(R.id.header_title);

        // Set up header
        headerTitle.setText(R.string.filter_tasks);

        // Initialize preferences
        preferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load saved preferences
        loadPreferences();

        // Set up click listeners
        closeButton.setOnClickListener(v -> navigateBack());
        resetButton.setOnClickListener(v -> resetFilters());

        // Set up switch listeners
        flaggedSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                savePreference(KEY_FLAGGED, isChecked));
        dueDateSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                savePreference(KEY_DUE_DATE, isChecked));
        completedSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                savePreference(KEY_COMPLETED, isChecked));

        // Set up priority chip listeners
        priorityChipGroup.setOnCheckedChangeListener((group, checkedId) -> savePriorityPreferences());

        return view;
    }

    private void loadPreferences() {
        flaggedSwitch.setChecked(preferences.getBoolean(KEY_FLAGGED, false));
        dueDateSwitch.setChecked(preferences.getBoolean(KEY_DUE_DATE, false));
        completedSwitch.setChecked(preferences.getBoolean(KEY_COMPLETED, true));

        // Load priority preferences
        ((Chip) priorityChipGroup.findViewById(R.id.chip_low)).setChecked(
                preferences.getBoolean(KEY_PRIORITY_LOW, false));
        ((Chip) priorityChipGroup.findViewById(R.id.chip_medium)).setChecked(
                preferences.getBoolean(KEY_PRIORITY_MEDIUM, false));
        ((Chip) priorityChipGroup.findViewById(R.id.chip_high)).setChecked(
                preferences.getBoolean(KEY_PRIORITY_HIGH, false));
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void savePriorityPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_PRIORITY_LOW,
                ((Chip) priorityChipGroup.findViewById(R.id.chip_low)).isChecked());
        editor.putBoolean(KEY_PRIORITY_MEDIUM,
                ((Chip) priorityChipGroup.findViewById(R.id.chip_medium)).isChecked());
        editor.putBoolean(KEY_PRIORITY_HIGH,
                ((Chip) priorityChipGroup.findViewById(R.id.chip_high)).isChecked());
        editor.apply();
    }

    private void resetFilters() {
        flaggedSwitch.setChecked(false);
        dueDateSwitch.setChecked(false);
        completedSwitch.setChecked(true);

        // Reset priority chips
        ((Chip) priorityChipGroup.findViewById(R.id.chip_low)).setChecked(false);
        ((Chip) priorityChipGroup.findViewById(R.id.chip_medium)).setChecked(false);
        ((Chip) priorityChipGroup.findViewById(R.id.chip_high)).setChecked(false);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_FLAGGED, false);
        editor.putBoolean(KEY_DUE_DATE, false);
        editor.putBoolean(KEY_COMPLETED, true);
        editor.putBoolean(KEY_PRIORITY_LOW, false);
        editor.putBoolean(KEY_PRIORITY_MEDIUM, false);
        editor.putBoolean(KEY_PRIORITY_HIGH, false);
        editor.apply();
    }

    private void navigateBack() {
        getParentFragmentManager().popBackStack();
    }

    // Helper method to get selected priorities
    public List<Integer> getSelectedPriorities() {
        List<Integer> priorities = new ArrayList<>();
        if (((Chip) priorityChipGroup.findViewById(R.id.chip_low)).isChecked()) {
            priorities.add(0);
        }
        if (((Chip) priorityChipGroup.findViewById(R.id.chip_medium)).isChecked()) {
            priorities.add(1);
        }
        if (((Chip) priorityChipGroup.findViewById(R.id.chip_high)).isChecked()) {
            priorities.add(2);
        }
        return priorities;
    }
}