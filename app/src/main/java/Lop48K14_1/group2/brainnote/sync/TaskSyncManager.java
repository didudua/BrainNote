package Lop48K14_1.group2.brainnote.sync;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.ui.models.Task;


public class TaskSyncManager {
    private static final String TAG = "TaskSyncManager";
    private static final String FILE_NAME = "tasks_backup.json";

    public interface OnDataImported {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static String exportDataAsJson(List<Task> tasks) {
        try {
            JSONArray tasksArray = new JSONArray();
            for (Task task : tasks) {
                JSONObject taskObj = new JSONObject();
                taskObj.put("id", task.getId());
                taskObj.put("title", task.getTitle());
                taskObj.put("description", task.getDescription());
                taskObj.put("dueDate", task.getDueDate());
                taskObj.put("isCompleted", task.isCompleted());
                taskObj.put("isFlagged", task.isFlagged());
                taskObj.put("priority", task.getPriority());
                tasksArray.put(taskObj);
            }

            JSONObject rootObj = new JSONObject();
            rootObj.put("tasks", tasksArray);
            return rootObj.toString(2);
        } catch (JSONException e) {
            Log.e(TAG, "JSON export error", e);
            return "{}";
        }
    }

    public static void uploadTasksToFirebase(List<Task> tasks) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot upload to Firebase: User not logged in");
            return;
        }

        String userId = user.getUid();
        String email = user.getEmail();
        String username = user.getDisplayName();
        
        if (username == null || username.isEmpty()) {
            if (email != null && email.contains("@")) {
                username = email.substring(0, email.indexOf("@"));
            } else {
                username = "User";
            }
        }
        
        String jsonData = exportDataAsJson(tasks);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(userId);

        // Upload user data and tasks
        userRef.child("email").setValue(email);
        userRef.child("username").setValue(username);
        userRef.child("tasks_json").setValue(jsonData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Upload to Firebase successful for user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Upload to Firebase failed for user: " + userId + ": " + e.getMessage(), e));
    }

    public static void saveTasksToFile(Context context, List<Task> tasks) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(exportDataAsJson(tasks).getBytes());
            fos.close();
            Log.d(TAG, "File saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving file: " + e.getMessage(), e);
        }
    }

    public static List<Task> importFromFile(Context context, OnDataImported callback) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            fis.close();
            List<Task> tasks = parseJsonData(builder.toString());
            callback.onSuccess();
            return tasks;
        } catch (IOException e) {
            Log.e(TAG, "Import from file failed: " + e.getMessage(), e);
            callback.onFailure(e);
            return new ArrayList<>();
        }
    }

    public static List<Task> importFromFirebase(OnDataImported callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot import from Firebase: User not logged in");
            callback.onFailure(new Exception("User not logged in"));
            return new ArrayList<>();
        }

        String userId = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + userId + "/tasks_json");
        
        final List<Task>[] tasksList = new List[1];
        
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String jsonData = snapshot.getValue(String.class);
                if (jsonData != null && !jsonData.isEmpty()) {
                    tasksList[0] = parseJsonData(jsonData);
                    callback.onSuccess();
                } else {
                    Log.w(TAG, "No data found on Firebase for user: " + userId);
                    tasksList[0] = new ArrayList<>();
                    callback.onFailure(new Exception("No data available on Firebase"));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase import cancelled for user: " + userId + ": " + error.getMessage(), error.toException());
                tasksList[0] = new ArrayList<>();
                callback.onFailure(new Exception("Permission denied or Firebase error: " + error.getMessage()));
            }
        });
        
        return tasksList[0];
    }

    public static List<Task> parseJsonData(String json) {
        List<Task> taskList = new ArrayList<>();

        try {
            if (json == null || json.trim().isEmpty()) {
                Log.w(TAG, "Empty or null JSON data");
                return taskList;
            }

            JSONObject rootObj = new JSONObject(json);
            JSONArray tasksArray = rootObj.getJSONArray("tasks");

            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject taskObj = tasksArray.getJSONObject(i);

                String id = taskObj.getString("id");
                String title = taskObj.getString("title");
                String description = taskObj.getString("description");
                String dueDate = taskObj.getString("dueDate");
                boolean isCompleted = taskObj.getBoolean("isCompleted");
                boolean isFlagged = taskObj.getBoolean("isFlagged");
                int priority = taskObj.getInt("priority");

                Task task = new Task(title, description, dueDate, isFlagged, priority);
                task.setId(id);
                task.setCompleted(isCompleted);
                taskList.add(task);
            }

            Log.d(TAG, "Parsed " + taskList.size() + " tasks from JSON");
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
        }
        
        return taskList;
    }
}
