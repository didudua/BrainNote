package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import Lop48K14_1.group2.brainnote.R;


public class TaskOptionsFragment extends DialogFragment {

    private TextView viewModeOption, sortOption, filterOption, settingsOption;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_options, container, false);
        
        // Initialize views
        viewModeOption = view.findViewById(R.id.view_mode_option);
        sortOption = view.findViewById(R.id.sort_option);
        filterOption = view.findViewById(R.id.filter_option);
        settingsOption = view.findViewById(R.id.settings_option);
        
        // Set up click listeners
        viewModeOption.setOnClickListener(v -> {
            dismiss();
            // Handle view mode option
        });
        
        sortOption.setOnClickListener(v -> {
            dismiss();
            // Handle sort option
        });
        
        filterOption.setOnClickListener(v -> {
            dismiss();
            openFilterFragment();
        });
        
        settingsOption.setOnClickListener(v -> {
            dismiss();
            // Handle settings option
        });
        
        return view;
    }
    
    private void openFilterFragment() {
        FilterTasksFragment filterFragment = new FilterTasksFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, filterFragment)
                .addToBackStack(null)
                .commit();
    }
}
