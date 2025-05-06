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

import java.util.Collections;
import java.util.Comparator;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.adapters.TaskAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Task;

public class TaskOptionsFragment extends DialogFragment {

    private TaskAdapter taskAdapter;
    private java.util.List<Task> incompleteTasks;
    private java.util.List<Task> completedTasks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_BrainNote);
        if (getParentFragment() instanceof TasksFragment) {
            TasksFragment tasksFragment = (TasksFragment) getParentFragment();
            taskAdapter = tasksFragment.getTaskAdapter();
            incompleteTasks = tasksFragment.getIncompleteTasks();
            completedTasks = tasksFragment.getCompletedTasks();
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
                    if (which == 0) {
                        sortByCreationDate();
                    } else {
                        sortByPriority();
                    }
                    taskAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                    dismiss();
                })
                .show();
    }

    private void sortByCreationDate() {
        Comparator<Task> dateComparator = (task1, task2) -> task1.getId().compareTo(task2.getId());
        Collections.sort(incompleteTasks, dateComparator);
        Collections.sort(completedTasks, dateComparator);
    }

    private void sortByPriority() {
        Comparator<Task> priorityComparator = (task1, task2) -> Integer.compare(task2.getPriority(), task1.getPriority());
        Collections.sort(incompleteTasks, priorityComparator);
        Collections.sort(completedTasks, priorityComparator);
    }
}