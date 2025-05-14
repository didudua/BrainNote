package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
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

public class TasksFragment extends Fragment implements TaskAdapter.TaskStatusChangeListener {

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
    private int swipedPosition = -1;

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

        // Kiểm tra ánh xạ
        if (incompleteTasksHeader == null) {
            Log.e("TasksFragment", "incompleteTasksHeader is null. Check fragment_tasks.xml for ID: incomplete_tasks_header");
        }
        if (completedTasksHeader == null) {
            Log.e("TasksFragment", "completedTasksHeader is null. Check fragment_tasks.xml for ID: completed_tasks_header");
        }

        // Đặt lại SharedPreferences để đảm bảo show_completed=true
        SharedPreferences preferences = getContext().getSharedPreferences("TaskFilterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("show_completed", true);
        editor.apply();

        // Khởi tạo taskAdapter
        taskAdapter = new TaskAdapter(getContext(), incompleteTasks, completedTasks, this, navController);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);

        // Set up click listeners
        filterButton.setOnClickListener(v -> openFilterTasksFragment());
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        preferences.registerOnSharedPreferenceChangeListener((sharedPrefs, key) -> applyFilters(searchEditText.getText().toString()));

        loadTasks();
        setupSwipeToDelete();

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

        // Đóng mục khi chạm ngoài
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && swipedPosition != -1) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(swipedPosition);
                if (holder != null) {
                    float x = event.getX();
                    float y = event.getY();
                    View itemView = holder.itemView;

                    // Kiểm tra chạm vào nút Xóa
                    float buttonWidth = itemView.getWidth() * 0.15f;
                    float deleteLeft = itemView.getRight() - buttonWidth;
                    if (x >= deleteLeft && x <= itemView.getRight() && y >= itemView.getTop() && y <= itemView.getBottom()) {
                        int position = swipedPosition;
                        Task task;
                        boolean isCompleted;

                        // Xác định task và trạng thái dựa trên vị trí
                        int incompleteCount = taskAdapter.getIncompleteTasks().size();
                        if (taskAdapter.getItemViewType(position) == 2) { // TYPE_TASK_INCOMPLETE
                            task = taskAdapter.getIncompleteTasks().get(position - 1);
                            isCompleted = false;
                        } else {
                            task = taskAdapter.getCompletedTasks().get(position - incompleteCount - 3);
                            isCompleted = true;
                        }

                        onDeleteTask(task, position, isCompleted);
                        swipedPosition = -1;
                        return true;
                    }

                    // Đóng mục nếu chạm ngoài
                    itemView.animate().translationX(0).setDuration(100).start();
                    swipedPosition = -1;
                }
            }
            return false;
        });

        return view;
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // Lấy vị trí của viewHolder
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return 0;

                // Kiểm tra loại item dựa trên getItemViewType
                int viewType = taskAdapter.getItemViewType(position);
                // Chỉ cho phép vuốt sang trái trên các item task
                return (viewType == 2 || viewType == 3) ? ItemTouchHelper.LEFT : 0; // TYPE_TASK_INCOMPLETE = 2, TYPE_TASK_COMPLETED = 3
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Không xử lý onSwiped để giữ mục mở
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                float buttonWidth = itemView.getWidth() * 0.15f; // Nút chiếm 15% chiều rộng
                float maxDx = buttonWidth; // Giới hạn dịch chuyển tối đa

                // Xác định translationX (chỉ cho phép vuốt trái)
                float translationX = Math.max(dX, -maxDx);

                // Dịch chuyển itemView
                if (isCurrentlyActive) {
                    itemView.setTranslationX(translationX);
                } else {
                    itemView.animate().translationX(translationX).setDuration(100).start();
                }

                // Vẽ nút Xóa nếu đang vuốt trái
                if (translationX < 0) {
                    // Vẽ nền trắng cho vùng hành động
                    ColorDrawable background = new ColorDrawable(Color.WHITE);
                    background.setBounds((int) (itemView.getRight() - maxDx), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    // Vẽ nút Xóa (màu đỏ)
                    Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                    ColorDrawable deleteBackground = new ColorDrawable(Color.parseColor("#FF0000"));
                    int deleteIntrinsicWidth = deleteIcon.getIntrinsicWidth();
                    int deleteIntrinsicHeight = deleteIcon.getIntrinsicHeight();

                    int deleteLeft = (int) (itemView.getRight() - buttonWidth);
                    int deleteRight = itemView.getRight();
                    int deleteTop = itemView.getTop();
                    int deleteBottom = itemView.getBottom();

                    deleteBackground.setBounds(deleteLeft, deleteTop, deleteRight, deleteBottom);
                    deleteBackground.draw(c);

                    // Căn giữa biểu tượng thùng rác
                    int deleteIconMarginVertical = (deleteBottom - deleteTop - deleteIntrinsicHeight) / 2;
                    int deleteIconTop = deleteTop + deleteIconMarginVertical;
                    int deleteIconBottom = deleteIconTop + deleteIntrinsicHeight;
                    int deleteIconMarginHorizontal = (deleteRight - deleteLeft - deleteIntrinsicWidth) / 2;
                    int deleteIconLeft = deleteLeft + deleteIconMarginHorizontal;
                    int deleteIconRight = deleteIconLeft + deleteIntrinsicWidth;

                    deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                    deleteIcon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);

                // Lưu vị trí mục đang mở
                if (translationX < 0 && !isCurrentlyActive) {
                    swipedPosition = viewHolder.getAdapterPosition();
                } else if (translationX == 0) {
                    swipedPosition = -1;
                }
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.5f;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return defaultValue * 0.5f;
            }

            @Override
            public float getSwipeVelocityThreshold(float defaultValue) {
                return defaultValue * 0.5f;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void onDeleteTask(Task task, int position, boolean isCompleted) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa task \"" + task.getTitle() + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Lưu task để hoàn tác
                    Task deletedTask = task;
                    int deletedPosition = position;
                    boolean wasCompleted = isCompleted;

                    // Xóa task khỏi danh sách
                    if (isCompleted) {
                        completedTasks.remove(task);
                    } else {
                        incompleteTasks.remove(task);
                    }
                    taskAdapter.notifyItemRemoved(position);

                    // Xóa task khỏi Firebase
                    tasksRef.child(task.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Task đã bị xóa", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Nếu xóa thất bại, khôi phục task
                                if (wasCompleted) {
                                    completedTasks.add(deletedTask);
                                } else {
                                    incompleteTasks.add(deletedTask);
                                }
                                taskAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Không thể xóa task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    // Cập nhật UI
                    applyFilters(searchEditText != null ? searchEditText.getText().toString() : "");
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    // Đóng mục
                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                    if (holder != null) {
                        holder.itemView.animate().translationX(0).setDuration(100).start();
                    }
                    swipedPosition = -1;
                })
                .show();
    }

    private void applyFilters(String searchQuery) {
        SharedPreferences preferences = getContext().getSharedPreferences("TaskFilterPrefs", Context.MODE_PRIVATE);
        boolean showFlagged = preferences.getBoolean("show_flagged", false);
        boolean showCompleted = preferences.getBoolean("show_completed", true);
        boolean priorityLow = preferences.getBoolean("priority_low", false);
        boolean priorityMedium = preferences.getBoolean("priority_medium", false);
        boolean priorityHigh = preferences.getBoolean("priority_high", false);

        List<Task> filteredIncompleteTasks = new ArrayList<>();
        List<Task> filteredCompletedTasks = new ArrayList<>();

        Log.d("TasksFragment", "Before filtering: incompleteTasks=" + (incompleteTasks != null ? incompleteTasks.size() : "null") +
                ", completedTasks=" + (completedTasks != null ? completedTasks.size() : "null"));
        Log.d("TasksFragment", "Filter settings: showFlagged=" + showFlagged + ", showCompleted=" + showCompleted +
                ", priorityLow=" + priorityLow + ", priorityMedium=" + priorityMedium + ", priorityHigh=" + priorityHigh);

        if (incompleteTasks != null) {
            for (Task task : incompleteTasks) {
                if (task.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                    boolean matchesFilter = true;

                    if (showFlagged && !task.isFlagged()) {
                        matchesFilter = false;
                    }

                    if (priorityLow || priorityMedium || priorityHigh) {
                        int priority = task.getPriority();
                        if (!((priorityLow && priority == 0) ||
                                (priorityMedium && priority == 1) ||
                                (priorityHigh && priority == 2))) {
                            matchesFilter = false;
                        }
                    }

                    if (matchesFilter) {
                        filteredIncompleteTasks.add(task);
                    }
                }
            }
        }

        if (completedTasks != null) {
            for (Task task : completedTasks) {
                if (task.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                    boolean matchesFilter = true;

                    if (showFlagged && !task.isFlagged()) {
                        matchesFilter = false;
                    }

                    if (priorityLow || priorityMedium || priorityHigh) {
                        int priority = task.getPriority();
                        if (!((priorityLow && priority == 0) ||
                                (priorityMedium && priority == 1) ||
                                (priorityHigh && priority == 2))) {
                            matchesFilter = false;
                        }
                    }

                    if (matchesFilter && showCompleted) {
                        filteredCompletedTasks.add(task);
                    }
                }
            }
        }

        Log.d("TasksFragment", "After filtering: filteredIncompleteTasks=" + filteredIncompleteTasks.size() +
                ", filteredCompletedTasks=" + filteredCompletedTasks.size());
        if (taskAdapter != null) {
            taskAdapter.updateTasks(filteredIncompleteTasks, filteredCompletedTasks);
        }
        updateUI();
    }

    private void loadTasks() {
        if (tasksRef == null) {
            Log.e("TasksFragment", "tasksRef is null");
            return;
        }

        tasksListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    Log.w("TasksFragment", "Fragment is not attached, skipping UI update");
                    return;
                }

                // Khởi tạo lại danh sách nếu null
                if (incompleteTasks == null) {
                    incompleteTasks = new ArrayList<>();
                }
                if (completedTasks == null) {
                    completedTasks = new ArrayList<>();
                }

                incompleteTasks.clear();
                completedTasks.clear();

                Log.d("TasksFragment", "Loading tasks from Firebase");
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        task.setId(taskSnapshot.getKey());
                        Log.d("TasksFragment", "Task loaded: id=" + task.getId() + ", title=" + task.getTitle() +
                                ", completed=" + task.isCompleted() + ", priority=" + task.getPriority() +
                                ", flagged=" + task.isFlagged() + ", dueDate=" + task.getDueDate());
                        if (task.isCompleted()) {
                            completedTasks.add(task);
                        } else {
                            incompleteTasks.add(task);
                        }
                    } else {
                        Log.w("TasksFragment", "Task is null for snapshot: " + taskSnapshot.getKey());
                    }
                }

                Log.d("TasksFragment", "Tasks loaded: incomplete=" + incompleteTasks.size() + ", completed=" + completedTasks.size());
                applyFilters(searchEditText != null ? searchEditText.getText().toString() : "");
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
        List<Task> currentIncomplete = taskAdapter != null ? taskAdapter.getIncompleteTasks() : new ArrayList<>();
        List<Task> currentCompleted = taskAdapter != null ? taskAdapter.getCompletedTasks() : new ArrayList<>();

        Log.d("TasksFragment", "Updating UI: incomplete=" + currentIncomplete.size() + ", completed=" + currentCompleted.size());

        if (currentIncomplete.isEmpty() && currentCompleted.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            taskListContainer.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            taskListContainer.setVisibility(View.VISIBLE);

            if (incompleteTasksHeader != null) {
                incompleteTasksHeader.setText(getString(R.string.incomplete_tasks_format, currentIncomplete.size()));
            } else {
                Log.w("TasksFragment", "incompleteTasksHeader is null, cannot update text");
            }
            if (completedTasksHeader != null) {
                completedTasksHeader.setText(getString(R.string.completed_tasks_format, currentCompleted.size()));
            } else {
                Log.w("TasksFragment", "completedTasksHeader is null, cannot update text");
            }
        }
    }

    private void openFilterTasksFragment() {
        try {
            navController.navigate(R.id.action_tasksFragment_to_filterTasksFragment);
        } catch (Exception e) {
            Log.e("TasksFragment", "Navigation error: " + e.getMessage());
            Toast.makeText(getContext(), "Cannot navigate to Filter Tasks", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskStatusChanged(Task task, boolean isCompleted) {
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
        return incompleteTasks != null ? incompleteTasks : new ArrayList<>();
    }

    public List<Task> getCompletedTasks() {
        return completedTasks != null ? completedTasks : new ArrayList<>();
    }
}