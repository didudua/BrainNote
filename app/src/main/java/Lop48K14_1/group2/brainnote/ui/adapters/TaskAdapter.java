package Lop48K14_1.group2.brainnote.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Task;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER_INCOMPLETE = 0;
    private static final int TYPE_HEADER_COMPLETED = 1;
    private static final int TYPE_TASK_INCOMPLETE = 2;
    private static final int TYPE_TASK_COMPLETED = 3;
    private static final int TYPE_DIVIDER = 4;

    private Context context;
    private List<Task> incompleteTasks;
    private List<Task> completedTasks;
    private TaskStatusChangeListener statusChangeListener;
    private NavController navController;
    private boolean isIncompleteExpanded = true;
    private boolean isCompletedExpanded = true;

    public interface TaskStatusChangeListener {
        void onTaskStatusChanged(Task task, boolean isCompleted);
    }

    public TaskAdapter(Context context, List<Task> incompleteTasks, List<Task> completedTasks,
                       TaskStatusChangeListener listener, NavController navController) {
        this.context = context;
        this.incompleteTasks = incompleteTasks;
        this.completedTasks = completedTasks;
        this.statusChangeListener = listener;
        this.navController = navController;
    }

    @Override
    public int getItemViewType(int position) {
        int incompleteTasksCount = isIncompleteExpanded ? incompleteTasks.size() : 0;

        if (position == 0) {
            return TYPE_HEADER_INCOMPLETE;
        } else if (position <= incompleteTasksCount) {
            return TYPE_TASK_INCOMPLETE;
        } else if (position == incompleteTasksCount + 1) {
            return TYPE_DIVIDER;
        } else if (position == incompleteTasksCount + 2) {
            return TYPE_HEADER_COMPLETED;
        } else {
            return TYPE_TASK_COMPLETED;
        }
    }

    @Override
    public int getItemCount() {
        int count = 1; // Header incomplete
        if (isIncompleteExpanded) {
            count += incompleteTasks.size(); // Task incomplete
        }
        count += 1; // Divider
        count += 1; // Header completed
        if (isCompletedExpanded) {
            count += completedTasks.size(); // Task completed
        }
        return count;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case TYPE_HEADER_INCOMPLETE:
            case TYPE_HEADER_COMPLETED:
                View headerView = inflater.inflate(R.layout.item_task_header, parent, false);
                return new HeaderViewHolder(headerView);

            case TYPE_DIVIDER:
                View dividerView = inflater.inflate(R.layout.item_divider, parent, false);
                return new DividerViewHolder(dividerView);

            case TYPE_TASK_INCOMPLETE:
            case TYPE_TASK_COMPLETED:
            default:
                View taskView = inflater.inflate(R.layout.item_task, parent, false);
                return new TaskViewHolder(taskView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        int incompleteTasksCount = isIncompleteExpanded ? incompleteTasks.size() : 0;

        switch (viewType) {
            case TYPE_HEADER_INCOMPLETE:
                HeaderViewHolder incompleteHeader = (HeaderViewHolder) holder;
                incompleteHeader.headerText.setText(context.getString(R.string.incomplete_tasks_format, incompleteTasks.size()));
                incompleteHeader.arrowIcon.setRotation(isIncompleteExpanded ? 180 : 0);
                // Gán sự kiện nhấn cho arrowIcon thay vì itemView
                incompleteHeader.arrowIcon.setOnClickListener(v -> {
                    isIncompleteExpanded = !isIncompleteExpanded;
                    incompleteHeader.arrowIcon.setRotation(isIncompleteExpanded ? 180 : 0);
                    notifyDataSetChanged();
                });
                break;

            case TYPE_HEADER_COMPLETED:
                HeaderViewHolder completedHeader = (HeaderViewHolder) holder;
                completedHeader.headerText.setText(context.getString(R.string.completed_tasks_format, completedTasks.size()));
                completedHeader.arrowIcon.setRotation(isCompletedExpanded ? 180 : 0);
                // Gán sự kiện nhấn cho arrowIcon thay vì itemView
                completedHeader.arrowIcon.setOnClickListener(v -> {
                    isCompletedExpanded = !isCompletedExpanded;
                    completedHeader.arrowIcon.setRotation(isCompletedExpanded ? 180 : 0);
                    notifyDataSetChanged();
                });
                break;

            case TYPE_TASK_INCOMPLETE:
                TaskViewHolder incompleteHolder = (TaskViewHolder) holder;
                Task incompleteTask = incompleteTasks.get(position - 1);
                bindTaskViewHolder(incompleteHolder, incompleteTask, false);
                break;

            case TYPE_TASK_COMPLETED:
                TaskViewHolder completedHolder = (TaskViewHolder) holder;
                Task completedTask = completedTasks.get(position - incompleteTasksCount - 3);
                bindTaskViewHolder(completedHolder, completedTask, true);
                break;
        }
    }

    private void bindTaskViewHolder(TaskViewHolder holder, Task task, boolean isCompleted) {
        holder.taskTitle.setText(task.getTitle());

        if (isCompleted) {
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.taskCheckbox.setChecked(isCompleted);

        // Set background color based on priority
        int priority = task.getPriority();
        if (priority == 2) { // High priority
            holder.taskContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.high_priority_background));
        } else if (priority == 1) { // Medium priority
            holder.taskContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.medium_priority_background));
        } else { // Low priority
            holder.taskContainer.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.taskCheckbox.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                boolean newStatus = !isCompleted;
                holder.taskCheckbox.setChecked(newStatus);
                statusChangeListener.onTaskStatusChanged(task, newStatus);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (task.getId() == null) {
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString("task_id", task.getId());
            navController.navigate(R.id.action_tasksFragment_to_editTaskFragment, bundle);
        });
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        ImageView arrowIcon;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.header_text);
            arrowIcon = itemView.findViewById(R.id.header_toggle);
        }
    }

    static class DividerViewHolder extends RecyclerView.ViewHolder {
        DividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        RadioButton taskCheckbox;
        TextView taskTitle;
        ConstraintLayout taskContainer;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckbox = itemView.findViewById(R.id.task_checkbox);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskContainer = itemView.findViewById(R.id.task_container);
        }
    }
    public void updateTasks(List<Task> incompleteTasks, List<Task> completedTasks) {
        this.incompleteTasks = incompleteTasks;
        this.completedTasks = completedTasks;
        notifyDataSetChanged();
    }

}