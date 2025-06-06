package Lop48K14_1.group2.brainnote.sync;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
import java.util.UUID;

import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.models.Notification;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class JsonSyncManager {
    private static final String TAG = "JsonSyncManager";
    private static final String FILE_NAME = "notebooks_backup.json";

    public interface OnDataImported {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static String exportDataAsJson() {
        try {
            JSONArray notebooksArray = new JSONArray();
            for (Notebook notebook : DataProvider.getNotebooks()) {
                JSONObject notebookObj = new JSONObject();
                notebookObj.put("id", notebook.getId());
                notebookObj.put("name", notebook.getName());
                notebookObj.put("Default", notebook.getDefault());

                JSONArray notesArray = new JSONArray();
                for (Note note : notebook.getNotes()) {
                    JSONObject noteObj = new JSONObject();
                    noteObj.put("id", note.getId());
                    noteObj.put("title", note.getTitle());
                    noteObj.put("content", note.getContent());
                    noteObj.put("date", note.getDate());
                    notesArray.put(noteObj);
                }

                notebookObj.put("notes", notesArray);
                notebooksArray.put(notebookObj);
            }

            JSONObject rootObj = new JSONObject();
            rootObj.put("notebooks", notebooksArray);
            return rootObj.toString(2);
        } catch (JSONException e) {
            Log.e(TAG, "JSON export error", e);
            return "{}";
        }
    }

    public static void uploadNotebooksToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot upload to Firebase: User not logged in");
            return;
        }

        String userId = user.getUid();
        String email = user.getEmail();  // <-- lấy email người dùng
        String username;

        username = user.getDisplayName();
        if(username == null ||username.isEmpty()){
            if (email != null && email.contains("@")) {
                username = email.substring(0, email.indexOf("@"));
            }
        }
            String jsonData = exportDataAsJson();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(userId);

        // Upload cả email và backup_json vào cùng cấp
        userRef.child("email").setValue(email);
        userRef.child("username").setValue(username);
        userRef.child("backup_json").setValue(jsonData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Upload to Firebase successful for user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Upload to Firebase failed for user: " + userId + ": " + e.getMessage(), e));
    }


    public static void saveNotebooksToFile(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(exportDataAsJson().getBytes());
            fos.close();
            Log.d(TAG, "File saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving file: " + e.getMessage(), e);
        }
    }

    public static void importFromFile(Context context, OnDataImported callback) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            fis.close();
            parseJsonData(builder.toString());
            callback.onSuccess();
        } catch (IOException e) {
            Log.e(TAG, "Import from file failed: " + e.getMessage(), e);
            callback.onFailure(e);
        }
    }

    public static void importFromFirebase(OnDataImported callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot import from Firebase: User not logged in");
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + userId + "/backup_json");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String jsonData = snapshot.getValue(String.class);
                if (jsonData != null && !jsonData.isEmpty()) {
                    parseJsonData(jsonData);
                    callback.onSuccess();
                } else {
                    Log.w(TAG, "No data found on Firebase for user: " + userId);
                    callback.onFailure(new Exception("No data available on Firebase"));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase import cancelled for user: " + userId + ": " + error.getMessage(), error.toException());
                callback.onFailure(new Exception("Permission denied or Firebase error: " + error.getMessage()));
            }
        });
    }

    public static void importDataWithFallback(Context context, OnDataImported callback) {
        importFromFirebase(new OnDataImported() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Data imported from Firebase");
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Firebase import failed: " + e.getMessage() + ", trying local file...");
                importFromFile(context, new OnDataImported() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Data imported from local file");
                        callback.onSuccess();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Import failed from both Firebase and local file: " + e.getMessage());
                        callback.onFailure(e);
                    }
                });
            }
        });
    }

    public static void parseJsonData(String json) {
        List<Notebook> notebookList = new ArrayList<>();

        try {
            if (json == null || json.trim().isEmpty()) {
                Log.w(TAG, "Empty or null JSON data");
                return;
            }

            JSONObject rootObj = new JSONObject(json);
            JSONArray notebooksArray = rootObj.getJSONArray("notebooks");

            for (int i = 0; i < notebooksArray.length(); i++) {
                JSONObject notebookObj = notebooksArray.getJSONObject(i);

                String id = notebookObj.getString("id");
                String name = notebookObj.getString("name");
                boolean Default = notebookObj.getBoolean("Default");

                List<Note> notes = new ArrayList<>();
                JSONArray notesArray = notebookObj.getJSONArray("notes");

                for (int j = 0; j < notesArray.length(); j++) {
                    JSONObject noteObj = notesArray.getJSONObject(j);

                    String noteId = noteObj.getString("id");
                    String title = noteObj.getString("title");
                    String content = noteObj.getString("content");
                    String date = noteObj.getString("date");

                    notes.add(new Note(noteId,id , title, content, date));
                }

                Notebook notebook = new Notebook(id, name, notes,Default);
                notebookList.add(notebook);
            }

            // Cập nhật DataProvider với dữ liệu nhập
            DataProvider.updateNotebooks(notebookList);
            Log.d(TAG, "DataProvider updated with " + notebookList.size() + " notebooks");
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
        }
    }

    public static void moveNotebookToTrash(Context context, Notebook notebook) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);


        userRef.child("trash").child("notebooks").child(notebook.getId()).setValue(notebook);
        userRef.child("notebooks").child(notebook.getId()).removeValue();
    }
    public static void moveNoteToTrash(Context context, Note note, Runnable onSuccessCallback, Runnable onFailureCallback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            runOnUiThread(context, () -> Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show());
            if (onFailureCallback != null) onFailureCallback.run();
            return;
        }

        String userId = user.getUid();
        String notebookId = note.getNotebookId();
        String noteId = note.getId();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        DatabaseReference noteRef = userRef.child("notebooks").child(notebookId).child("notes").child(noteId);
        DatabaseReference trashRef = userRef.child("trash").child("notes").child(noteId);

        // Di chuyển ghi chú vào thùng rác
        trashRef.setValue(note)
                .addOnSuccessListener(aVoid -> {
                    // Xóa ghi chú khỏi danh sách chính
                    noteRef.removeValue()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Note moved to trash and removed from notebook successfully.");

                                // Cập nhật DataProvider
                                DataProvider.removeNote(note);

                                // Lưu dữ liệu cục bộ
                                saveNotebooksToFile(context);

                                // Cập nhật backup_json trên Firebase
                                uploadNotebooksToFirebase();

                                // Thông báo thành công trên UI thread
                                runOnUiThread(context, () -> {
                                    Toast.makeText(context, "Ghi chú đã được chuyển vào thùng rác", Toast.LENGTH_SHORT).show();
                                    if (onSuccessCallback != null) onSuccessCallback.run();
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error removing note from notebook: ", e);
                                runOnUiThread(context, () -> {
                                    Toast.makeText(context, "Không thể xóa ghi chú khỏi notebook: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    if (onFailureCallback != null) onFailureCallback.run();
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error moving note to trash: ", e);
                    runOnUiThread(context, () -> {
                        Toast.makeText(context, "Không thể chuyển ghi chú vào thùng rác: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        if (onFailureCallback != null) onFailureCallback.run();
                    });
                });
    }

    // Helper method to run code on UI thread
    private static void runOnUiThread(Context context, Runnable action) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(action);
    }

    public static void saveNotesToFile(Context context) {
        try {
            String jsonData = exportDataAsJson();  // Lấy dữ liệu ghi chú dưới dạng JSON

            // Lưu dữ liệu vào tệp cục bộ
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonData.getBytes());
            fos.close();
            Log.d(TAG, "File saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving file: " + e.getMessage(), e);
        }
    }


    public static void addNotification(String title, String content) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference("notifications");

        String notificationId = notificationsRef.push().getKey();

        if (notificationId == null) {
            Log.e("NotificationManager", "Không thể tạo ID thông báo");
            return;
        }

        Notification notification = new Notification(title, content, System.currentTimeMillis());

        notificationsRef.child(notificationId).setValue(notification)
                .addOnSuccessListener(aVoid ->
                        Log.d("NotificationManager", "Thông báo đã được thêm"))
                .addOnFailureListener(e ->
                        Log.e("NotificationManager", "Lỗi khi thêm thông báo: " + e.getMessage()));
    }

    public static void uploadaccountcreatedAtToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot upload to Firebase: User not logged in");
            return;
        }

        String userId = user.getUid();

        String jsonData = exportDataAsJson();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(userId);

        // Upload cả email và backup_json vào cùng cấp
        long createdAt = System.currentTimeMillis();
        userRef.child("createdAt").setValue(createdAt);
        userRef.child("backup_json").setValue(jsonData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Upload to Firebase successful for user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Upload to Firebase failed for user: " + userId + ": " + e.getMessage(), e));
    }

}