package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Task;

public class AddTaskFragment extends Fragment {

    private EditText taskTitleEditText, taskDescriptionEditText, taskReminderEditText, taskDueDateEditText;
    private ChipGroup dateChipGroup, priorityChipGroup, reminderChipGroup;
    private Button saveButton;
    private ImageButton closeButton;
    private TextView headerTitle;
    private RadioButton taskRadioButton;
    private ImageView flagButton;
    private DatabaseReference tasksRef;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tasksRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("tasks");
        } else {
            Log.e("AddTaskFragment", "No user logged in");
            Toast.makeText(getContext(), "Please log in to add tasks", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize views
        taskTitleEditText = view.findViewById(R.id.task_title_edit_text);
        taskDescriptionEditText = view.findViewById(R.id.task_description_edit_text);
        taskDueDateEditText = view.findViewById(R.id.task_due_date);
        taskDueDateEditText.setOnClickListener(v -> showDateTimePickerDialog(taskDueDateEditText));
        taskReminderEditText = view.findViewById(R.id.task_alarm_date);
        taskReminderEditText.setOnClickListener(v -> showDateTimePickerDialog(taskDueDateEditText));
        priorityChipGroup = view.findViewById(R.id.priority_chip_group);
        reminderChipGroup = view.findViewById(R.id.reminder_chip_group);
        saveButton = view.findViewById(R.id.save_button);
        closeButton = view.findViewById(R.id.close_button);
        headerTitle = view.findViewById(R.id.header_title);
        taskRadioButton = view.findViewById(R.id.task_radio_button);
        flagButton = view.findViewById(R.id.flag_button);

        // Set up header
        headerTitle.setText(R.string.add_task);

        // Set default priority (Low)
        Chip lowPriorityChip = view.findViewById(R.id.chip_low);
        if (lowPriorityChip != null) {
            lowPriorityChip.setChecked(true);
            lowPriorityChip.setChipBackgroundColorResource(android.R.color.transparent); // Thấp: không màu
        }

        // Set up click listeners
        closeButton.setOnClickListener(v -> navigateBack());
        saveButton.setOnClickListener(v -> saveTask());
        flagButton.setOnClickListener(v -> toggleFlag());
        taskRadioButton.setOnClickListener(v -> taskRadioButton.setChecked(!taskRadioButton.isChecked()));

        // Set up priority chip color
        priorityChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chipLow = view.findViewById(R.id.chip_low);
            Chip chipMedium = view.findViewById(R.id.chip_medium);
            Chip chipHigh = view.findViewById(R.id.chip_high);
            if (chipLow != null && chipMedium != null && chipHigh != null) {
                chipLow.setChipBackgroundColorResource(android.R.color.transparent);
                chipMedium.setChipBackgroundColorResource(android.R.color.transparent);
                chipHigh.setChipBackgroundColorResource(android.R.color.transparent);
                if (checkedId == R.id.chip_medium) {
                    chipMedium.setChipBackgroundColorResource(R.color.yellow); // Trung bình: vàng
                } else if (checkedId == R.id.chip_high) {
                    chipHigh.setChipBackgroundColorResource(R.color.red); // Cao: đỏ
                }
            }
        });

        return view;
    }
    private void showDateTimePickerDialog(EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Hiển thị DatePickerDialog để chọn ngày
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Sau khi người dùng chọn ngày, hiển thị TimePickerDialog để chọn giờ và phút
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                            (timeView, selectedHour, selectedMinute) -> {
                                // Xử lý khi người dùng chọn giờ và phút
                                String selectedDateTime =String.format("%02d", selectedHour) + ":" + String.format("%02d", selectedMinute) +" - " + selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear ;
                                dateEditText.setText(selectedDateTime);  // Gán ngày và giờ vào EditText
                            }, hour, minute, true);
                    timePickerDialog.show();
                }, year, month, day);
        datePickerDialog.show();
    }


    private void toggleFlag() {
        flagButton.setImageResource(flagButton.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_flag_outline).getConstantState()
                ? R.drawable.ic_flag_filled : R.drawable.ic_flag_outline);
    }

    private void saveTask() {
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
        String dueDate = taskReminderEditText.getText().toString().trim(); // Nhận giá trị từ EditText chọn ngày giờ

        if (dueDate.isEmpty()) {
            Toast.makeText(getContext(), "Please select a due date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi ngày giờ từ EditText thành đối tượng Calendar
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(dueDate); // Chuyển đổi giá trị vào Calendar
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Đặt báo thức
            setAlarm(calendar);
        }
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

        if (tasksRef == null) {
            Log.e("AddTaskFragment", "tasksRef is null, user may not be logged in");
            Toast.makeText(getContext(), "Please log in to add tasks", Toast.LENGTH_SHORT).show();
            return;
        }

        Task newTask = new Task(title, description, dueDate, taskRadioButton.isChecked(), priority);
        newTask.setId(UUID.randomUUID().toString());
        newTask.setFlagged(flagButton.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_flag_filled).getConstantState());

        tasksRef.child(newTask.getId()).setValue(newTask)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), R.string.task_added_success, Toast.LENGTH_SHORT).show();
                        navigateBack();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Log.e("AddTaskFragment", "Failed to save task: " + e.getMessage());
                        Toast.makeText(getContext(), R.string.task_added_failure, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void setAlarm(Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        // Tạo Intent và PendingIntent để gửi Broadcast khi đến giờ báo thức
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        intent.putExtra("task_title", taskTitleEditText.getText().toString());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Đặt báo thức
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void navigateBack() {
        getParentFragmentManager().popBackStack();
    }
}