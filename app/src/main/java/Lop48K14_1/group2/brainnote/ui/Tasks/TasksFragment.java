package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.Home.HeaderFragment;
import Lop48K14_1.group2.brainnote.ui.adapters.TaskAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Task;

public class TasksFragment extends Fragment {

    private View emptyStateView;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> incompleteTasks = new ArrayList<>();
    private List<Task> completedTasks = new ArrayList<>();
    private EditText searchEditText;
    private TextView incompleteTasksHeader, completedTasksHeader;
    private ConstraintLayout taskListContainer;
    private FloatingActionButton addTaskButton;
    private ImageButton filterButton, moreButton;
    private ImageView avatarImageView;
    private DatabaseReference tasksRef;
    private FirebaseUser currentUser;
    private ValueEventListener tasksListener;
    private NavController navController;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tasksRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("tasks");
        } else {
            Log.e("TasksFragment", "No user logged in");
            Toast.makeText(getContext(), "Please log in to view tasks", Toast.LENGTH_SHORT).show();
        }

        // Initialize NavController
        navController = NavHostFragment.findNavController(this);

        // Initialize views
        emptyStateView = view.findViewById(R.id.empty_state_view);
        taskListContainer = view.findViewById(R.id.task_list_container);
        recyclerView = view.findViewById(R.id.tasks_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        incompleteTasksHeader = view.findViewById(R.id.incomplete_tasks_header);
        completedTasksHeader = view.findViewById(R.id.completed_tasks_header);
        addTaskButton = view.findViewById(R.id.add_task_button);
        filterButton = view.findViewById(R.id.btn_filter);
        moreButton = view.findViewById(R.id.btn_more);

        // Check for null views
        if (incompleteTasksHeader == null || completedTasksHeader == null) {
            Log.e("TasksFragment", "Header TextViews are null");
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getContext(), incompleteTasks, completedTasks, this::onTaskStatusChanged, navController);
        recyclerView.setAdapter(taskAdapter);

        // Set up click listeners
        filterButton.setOnClickListener(v -> openFilterTasksFragment());
        moreButton.setOnClickListener(v -> showMoreOptions());

        // Load tasks from Firebase
        loadTasks();
        addTaskButton.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_tasksFragment_to_addTaskFragment);
            } catch (Exception e) {
                Log.e("TasksFragment", "Navigation error: " + e.getMessage());
                Toast.makeText(getContext(), "Cannot navigate to Add Task", Toast.LENGTH_SHORT).show();
            }
        });
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.headerContainer, new HeaderFragment())
                    .commit();
        }
        return view;
    }

    private void loadTasks() {
        if (tasksRef == null) return;

        tasksListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    Log.w("TasksFragment", "Fragment is not attached, skipping UI update");
                    return;
                }

                incompleteTasks.clear();
                completedTasks.clear();

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        if (task.isCompleted()) {
                            completedTasks.add(task);
                        } else {
                            incompleteTasks.add(task);
                        }
                    }
                }

                updateUI();
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TasksFragment", "Database error: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
            }
        };
        tasksRef.addValueEventListener(tasksListener);
    }

    private void updateUI() {
        if (incompleteTasksHeader == null || completedTasksHeader == null) {
            Log.e("TasksFragment", "One or both TextView headers are null");
            return;
        }

        if (incompleteTasks.isEmpty() && completedTasks.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            taskListContainer.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            taskListContainer.setVisibility(View.VISIBLE);

            incompleteTasksHeader.setText(getString(R.string.incomplete_tasks_format, incompleteTasks.size()));
            completedTasksHeader.setText(getString(R.string.completed_tasks_format, completedTasks.size()));
        }
    }

    private void onTaskStatusChanged(Task task, boolean isCompleted) {
        if (tasksRef == null || task == null || task.getId() == null) {
            Log.e("TasksFragment", "Invalid task or database reference");
            Toast.makeText(getContext(), "Cannot update task status", Toast.LENGTH_SHORT).show();
            return;
        }

        task.setCompleted(isCompleted);
        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TasksFragment", "Task status updated: " + task.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("TasksFragment", "Failed to update task status: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                });
    }

    private void openFilterTasksFragment() {
        try {
            navController.navigate(R.id.action_tasksFragment_to_filterTasksFragment);
        } catch (Exception e) {
            Log.e("TasksFragment", "Navigation error: " + e.getMessage());
            Toast.makeText(getContext(), "Cannot navigate to Filter Tasks", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMoreOptions() {
        try {
            TaskOptionsFragment optionsFragment = new TaskOptionsFragment();
            optionsFragment.show(getParentFragmentManager(), "task_options");
        } catch (Exception e) {
            Log.e("TasksFragment", "Error showing TaskOptionsFragment: " + e.getMessage());
            Toast.makeText(getContext(), "Cannot show options", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tasksRef != null && tasksListener != null) {
            tasksRef.removeEventListener(tasksListener);
        }
    }

    public TaskAdapter getTaskAdapter() {
        return taskAdapter;
    }

    public List<Task> getIncompleteTasks() {
        return incompleteTasks;
    }

    public List<Task> getCompletedTasks() {
        return completedTasks;
    }
}