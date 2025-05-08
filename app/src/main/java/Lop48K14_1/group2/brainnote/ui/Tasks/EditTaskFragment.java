package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Task;

public class EditTaskFragment extends Fragment {

    private static final String ARG_TASK_ID = "task_id";

    private String taskId;
    private Task currentTask;

    private EditText taskTitleEditText, taskDescriptionEditText;
    private ChipGroup dateChipGroup, priorityChipGroup, reminderChipGroup;
    private Button saveButton;
    private ImageButton closeButton;
    private ImageView flagButton;
    private TextView headerTitle;
    private RadioButton taskRadioButton;
    private DatabaseReference taskRef;
    private FirebaseUser currentUser;

    public static EditTaskFragment newInstance(String taskId) {
        EditTaskFragment fragment = new EditTaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getString(ARG_TASK_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && taskId != null) {
            taskRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("tasks")
                    .child(taskId);
        } else {
            Log.e("EditTaskFragment", "User not logged in or task ID is null");
            Toast.makeText(getContext(), "Error loading task", Toast.LENGTH_SHORT).show();
            navigateBack();
            return view;
        }

        taskTitleEditText = view.findViewById(R.id.task_title_edit_text);
        taskDescriptionEditText = view.findViewById(R.id.task_description_edit_text);
        dateChipGroup = view.findViewById(R.id.date_chip_group);
        priorityChipGroup = view.findViewById(R.id.priority_chip_group);
        reminderChipGroup = view.findViewById(R.id.reminder_chip_group);
        saveButton = view.findViewById(R.id.save_button);
        closeButton = view.findViewById(R.id.close_button);
        flagButton = view.findViewById(R.id.flag_button);
        headerTitle = view.findViewById(R.id.header_title);
        taskRadioButton = view.findViewById(R.id.task_radio_button);

        headerTitle.setText(R.string.edit_task);
        closeButton.setOnClickListener(v -> navigateBack());
        saveButton.setOnClickListener(v -> saveTask());
        flagButton.setOnClickListener(v -> toggleFlag());
        taskRadioButton.setOnClickListener(v -> taskRadioButton.setChecked(!taskRadioButton.isChecked()));

        priorityChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chipLow = view.findViewById(R.id.chip_low);
            Chip chipMedium = view.findViewById(R.id.chip_medium);
            Chip chipHigh = view.findViewById(R.id.chip_high);
            if (chipLow != null && chipMedium != null && chipHigh != null) {
                chipLow.setChipBackgroundColorResource(android.R.color.transparent);
                chipMedium.setChipBackgroundColorResource(android.R.color.transparent);
                chipHigh.setChipBackgroundColorResource(android.R.color.transparent);
                if (checkedId == R.id.chip_medium) {
                    chipMedium.setChipBackgroundColorResource(R.color.yellow);
                } else if (checkedId == R.id.chip_high) {
                    chipHigh.setChipBackgroundColorResource(R.color.red);
                }
            }
        });

        loadTaskData();

        return view;
    }

    private void loadTaskData() {
        if (taskRef == null) return;

        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentTask = snapshot.getValue(Task.class);
                if (currentTask != null) {
                    populateTaskData();
                } else {
                    Toast.makeText(getContext(), R.string.error_loading_task, Toast.LENGTH_SHORT).show();
                    navigateBack();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditTaskFragment", "Database error: " + error.getMessage());
                Toast.makeText(getContext(), R.string.error_loading_task, Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }

    private void populateTaskData() {
        taskTitleEditText.setText(currentTask.getTitle());
        taskDescriptionEditText.setText(currentTask.getDescription());
        taskRadioButton.setChecked(currentTask.isCompleted());
        updateFlagButtonState();

        String dueDate = currentTask.getDueDate();
        if (dueDate != null && !dueDate.isEmpty()) {
            for (int i = 0; i < dateChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) dateChipGroup.getChildAt(i);
                if (chip.getText().toString().equals(dueDate)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        int priority = currentTask.getPriority();
        int chipId;
        switch (priority) {
            case 1:
                chipId = R.id.chip_medium;
                break;
            case 2:
                chipId = R.id.chip_high;
                break;
            default:
                chipId = R.id.chip_low;
                break;
        }
        Chip priorityChip = priorityChipGroup.findViewById(chipId);
        if (priorityChip != null) {
            priorityChip.setChecked(true);
            if (chipId == R.id.chip_medium) {
                priorityChip.setChipBackgroundColorResource(R.color.yellow);
            } else if (chipId == R.id.chip_high) {
                priorityChip.setChipBackgroundColorResource(R.color.red);
            }
        }
    }

    private void updateFlagButtonState() {
        if (currentTask != null) {
            flagButton.setImageResource(currentTask.isFlagged() ? R.drawable.ic_flag_filled : R.drawable.ic_flag_outline);
        }
    }

    private void toggleFlag() {
        if (currentTask != null) {
            currentTask.setFlagged(!currentTask.isFlagged());
            updateFlagButtonState();
        }
    }

    private void saveTask() {
        if (currentTask == null || taskRef == null) {
            Log.e("EditTaskFragment", "Task or taskRef is null");
            Toast.makeText(getContext(), "Error saving task", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = taskTitleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_title, Toast.LENGTH_SHORT).show();
            return;
        }

        String description = taskDescriptionEditText.getText().toString().trim();
        String dueDate = "";
        int dateChipId = dateChipGroup.getCheckedChipId();
        if (dateChipId != -1) {
            Chip selectedDateChip = dateChipGroup.findViewById(dateChipId);
            if (selectedDateChip != null) {
                dueDate = selectedDateChip.getText().toString();
            }
        }

        int priority = 0;
        int priorityChipId = priorityChipGroup.getCheckedChipId();
        if (priorityChipId != -1) {
            Chip selectedPriorityChip = priorityChipGroup.findViewById(priorityChipId);
            if (selectedPriorityChip != null) {
                String priorityText = selectedPriorityChip.getText().toString();
                if (priorityText.equals(getString(R.string.medium_priority))) {
                    priority = 1;
                } else if (priorityText.equals(getString(R.string.high_priority))) {
                    priority = 2;
                }
            }
        }

        String reminder = "";
        int reminderChipId = reminderChipGroup.getCheckedChipId();
        if (reminderChipId != -1) {
            Chip selectedReminderChip = reminderChipGroup.findViewById(reminderChipId);
            if (selectedReminderChip != null) {
                reminder = selectedReminderChip.getText().toString();
            }
        }

        currentTask.setTitle(title);
        currentTask.setDescription(description);
        currentTask.setDueDate(dueDate);
        currentTask.setPriority(priority);
        currentTask.setCompleted(taskRadioButton.isChecked());

        taskRef.setValue(currentTask)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), R.string.task_updated_success, Toast.LENGTH_SHORT).show();
                    navigateBack();
                })
                .addOnFailureListener(e -> {
                    Log.e("EditTaskFragment", "Failed to update task: " + e.getMessage());
                    Toast.makeText(getContext(), R.string.task_updated_failure, Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateBack() {
        getParentFragmentManager().popBackStack();
    }
}