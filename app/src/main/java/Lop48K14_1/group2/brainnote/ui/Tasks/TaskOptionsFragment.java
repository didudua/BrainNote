package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.adapters.TaskAdapter;

public class TaskOptionsFragment extends DialogFragment {

    private TaskAdapter taskAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_BrainNote);
        // Initialize taskAdapter only if parent fragment is TasksFragment
        if (getParentFragment() instanceof TasksFragment) {
            TasksFragment tasksFragment = (TasksFragment) getParentFragment();
            taskAdapter = tasksFragment.getTaskAdapter();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_options, container, false);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView viewModeOption = view.findViewById(R.id.view_mode_option);
        TextView sortOption = view.findViewById(R.id.sort_option);

        viewModeOption.setOnClickListener(v -> {
            Toast.makeText(getContext(), "View mode clicked", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        sortOption.setOnClickListener(v -> showSortOptions());

        return view;
    }

    private void showSortOptions() {
        String[] sortOptions = {"By Creation Date", "By Priority"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sort By")
                .setItems(sortOptions, (dialog, which) -> {
                    String selectedOption = sortOptions[which];
                    // Check if parent fragment is still available
                    if (getParentFragment() instanceof TasksFragment) {
                        TasksFragment tasksFragment = (TasksFragment) getParentFragment();
                        tasksFragment.setSortOption(selectedOption);
                    } else {
                        Toast.makeText(getContext(), "Cannot apply sort: Parent fragment not found", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                    dismiss();
                })
                .show();
    }
}