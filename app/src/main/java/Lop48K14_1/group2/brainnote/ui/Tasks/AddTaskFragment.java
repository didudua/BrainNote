package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Task;


public class AddTaskFragment extends Fragment {

    private EditText taskTitleEditText, taskDescriptionEditText;
    private ChipGroup dateChipGroup, priorityChipGroup;
    private Button saveButton;
    private ImageButton closeButton;
    private TextView headerTitle;
    private RadioButton taskRadioButton;
    private DatabaseReference tasksRef;
    private FirebaseUser currentUser;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);
        
        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tasksRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("tasks");
        }
        
        // Initialize views
        taskTitleEditText = view.findViewById(R.id.task_title_edit_text);
        taskDescriptionEditText = view.findViewById(R.id.task_description_edit_text);
        dateChipGroup = view.findViewById(R.id.date_chip_group);
        priorityChipGroup = view.findViewById(R.id.priority_chip_group);
        saveButton = view.findViewById(R.id.save_button);
        closeButton = view.findViewById(R.id.close_button);
        headerTitle = view.findViewById(R.id.header_title);
        taskRadioButton = view.findViewById(R.id.task_radio_button);
        
        // Set up header
        headerTitle.setText(R.string.add_task);
        
        // Set up click listeners
        closeButton.setOnClickListener(v -> navigateBack());
        saveButton.setOnClickListener(v -> saveTask());
        
        return view;
    }

    private void saveTask() {
        // Kiểm tra views
        if (taskTitleEditText == null || taskDescriptionEditText == null || dateChipGroup == null || priorityChipGroup == null) {
            Log.e("AddTaskFragment", "One or more views are null");
            Toast.makeText(getContext(), "UI initialization error", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = taskTitleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_title, Toast.LENGTH_SHORT).show();
            return;
        }

        String description = taskDescriptionEditText.getText().toString().trim();

        // Get selected date
        String dueDate = "";
        int dateChipId = dateChipGroup.getCheckedChipId();
        if (dateChipId != -1) {
            Chip selectedDateChip = dateChipGroup.findViewById(dateChipId);
            if (selectedDateChip != null) {
                dueDate = selectedDateChip.getText().toString();
            } else {
                Log.w("AddTaskFragment", "Selected date chip is null");
            }
        }

        // Get selected priority
        int priority = 0; // Default: Low
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
            } else {
                Log.w("AddTaskFragment", "Selected priority chip is null");
            }
        }

        // Kiểm tra Firebase
        if (tasksRef == null) {
            Log.e("AddTaskFragment", "tasksRef is null, user may not be logged in");
            Toast.makeText(getContext(), "Please log in to add tasks", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo task
        Task newTask = new Task(title, description, dueDate, false, priority);
        if (newTask.getId() == null) {
            Log.e("AddTaskFragment", "Task ID is null");
            Toast.makeText(getContext(), "Error creating task", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu task
        tasksRef.child(newTask.getId()).setValue(newTask)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), R.string.task_added_success, Toast.LENGTH_SHORT).show();
                        navigateBack();
                    } else {
                        Log.w("AddTaskFragment", "Context is null in onSuccess");
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Log.e("AddTaskFragment", "Failed to save task: " + e.getMessage());
                        Toast.makeText(getContext(), R.string.task_added_failure, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void navigateBack() {
        getParentFragmentManager().popBackStack();
    }
}
