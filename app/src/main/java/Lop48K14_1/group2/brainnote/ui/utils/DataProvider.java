package Lop48K14_1.group2.brainnote.ui.utils;

import static java.security.AccessController.getContext;

import static Lop48K14_1.group2.brainnote.sync.JsonSyncManager.saveNotebooksToFile;
import static Lop48K14_1.group2.brainnote.sync.JsonSyncManager.uploadNotebooksToFirebase;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;

public class DataProvider {

    private static List<Notebook> notebooks = new ArrayList<>();

    // Khởi tạo dữ liệu mẫu nếu cần
    public static void initializeSampleData() {
        // Can be used to initialize sample data if needed.
    }

    public static List<Notebook> getNotebooks() {
        return notebooks;
    }

    public static Notebook getNotebookById(String id) {
        for (Notebook notebook : notebooks) {
            if (notebook.getId().equals(id)) {
                return notebook;
            }
        }
        return null;
    }

    public static void addNotebook(Notebook notebook) {
        notebooks.add(notebook);
    }

    public static void addNoteToNotebook(String notebookId, Note note) {
        Notebook notebook = getNotebookById(notebookId);
        if (notebook != null) {
            List<Note> notes = notebook.getNotes();

            // Kiểm tra nếu ghi chú đã tồn tại trong danh sách
            boolean isExist = false;
            for (int i = 0; i < notes.size(); i++) {
                // Kiểm tra nếu ID của ghi chú trùng với ID của note cần thêm
                if (notes.get(i).getId().equals(note.getId())) {
                    System.out.println("Found note with ID: " + note.getId() + " at index " + i);
                    // Nếu ghi chú đã tồn tại, cập nhật lại ghi chú đó
                    notes.set(i, note);
                    isExist = true;
                    break;  // Dừng vòng lặp sau khi tìm thấy
                }
            }

            // Kiểm tra kết quả của việc tìm thấy ghi chú
            if (!isExist) {
                System.out.println("Note with ID " + note.getId() + " not found. Adding new note.");
                notes.add(note); // Thêm ghi chú mới vào danh sách
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(user.getUid())
                        .child("notebooks")
                        .child(notebookId)
                        .child("notes")
                        .child(note.getId());

                ref.setValue(note)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("DataProvider", "Ghi chú đã được lưu Firebase.");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("DataProvider", "Lỗi khi lưu ghi chú: " + e.getMessage());
                        });
            }
        }
    }

    // Cập nhật danh sách notebooks từ dữ liệu nhập
    public static void updateNotebooks(List<Notebook> newNotebooks) {
        notebooks.clear();
        notebooks.addAll(newNotebooks);
    }

    // Làm mới dữ liệu (xóa danh sách hiện tại)
    public static void clearData() {
        notebooks.clear();
    }

    // Chuyển đổi dữ liệu sang JSON
    public static String getDataAsJson() {
        try {
            JSONArray notebooksArray = new JSONArray();
            for (Notebook notebook : notebooks) {
                JSONObject notebookObj = new JSONObject();
                notebookObj.put("id", notebook.getId());
                notebookObj.put("name", notebook.getName());
                notebookObj.put("isDefault", notebook.getDefault());

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
            e.printStackTrace();
            return "{}";
        }
    }

    public static Note getNoteById(String notebookId, String noteId) {
        // Lấy sổ tay
        Notebook nb = getNotebookById(notebookId);
        if (nb == null) return null;

        // Duyệt qua các ghi chú
        for (Note n : nb.getNotes()) {
            if (n.getId().equals(noteId)) {
                return n;
            }
        }
        return null;
    }

    // Xóa ghi chú trong sổ tay
    public static void removeNoteFromNotebook(String notebookId, String noteId) {
        // Tìm notebook theo notebookId
        Notebook nb = getNotebookById(notebookId);
        if (nb == null) return;  // Nếu không tìm thấy notebook, không làm gì cả

            if (nb.getDefault()) {
                Log.w("DataProvider", "Không thể xóa sổ tay mặc định.");
                return;  // Dừng lại nếu là notebook mặc định
            }

        // Sử dụng Iterator để tránh ConcurrentModificationException khi xóa
        Iterator<Note> iter = nb.getNotes().iterator();
        while (iter.hasNext()) {
            Note note = iter.next();
            if (note.getId().equals(noteId)) {
                iter.remove();  // Xóa ghi chú khỏi danh sách
                break;  // Đã xóa thành công thì thoát khỏi vòng lặp
            }
        }
    }
    public static void setDefaultNotebook(String notebookId) {
        for (Notebook notebook : notebooks) {
            if (notebook.getId().equals(notebookId)) {
                notebook.setDefault(true);  // Đặt notebook làm mặc định
            } else {
                notebook.setDefault(false);  // Đặt tất cả notebook khác là không mặc định
            }
        }
    }


    // Cập nhật dữ liệu sau khi xóa
    public static void removeNoteFromDataProvider(String notebookId, String noteId, Context context) {
        Notebook notebook = getNotebookById(notebookId);
        if (notebook != null && notebook.getDefault()) {
            Toast.makeText(context, "Không thể xóa ghi chú từ sổ tay mặc định", Toast.LENGTH_SHORT).show();
            return;  // Không cho phép xóa nếu là sổ tay mặc định
        }
        removeNoteFromFirebase(notebookId, noteId, context);  // Xóa ghi chú từ Firebase
        removeNoteFromNotebook(notebookId, noteId);  // Xóa ghi chú trong notebook

        // Cập nhật lại dữ liệu JSON sau khi xóa
        String updatedJson = getDataAsJson();

        System.out.println("Updated Data: " + updatedJson);
    }

    private static void removeNoteFromFirebase(String notebookId, String noteId, Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Lấy tham chiếu đến Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())  // Ensure you are referencing the correct user
                .child("notebooks")
                .child(notebookId)
                .child("notes")
                .child(noteId);

        // Lấy dữ liệu trước khi xóa
        notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // In dữ liệu trước khi xóa
                if (dataSnapshot.exists()) {
                    Log.d("Firebase", "Dữ liệu trước khi xóa: " + dataSnapshot.getValue());
                } else {
                    Log.d("Firebase", "Ghi chú không tồn tại trước khi xóa.");
                    Log.d("Firebase", "Notebook ID: " + notebookId);
                    Log.d("Firebase", "Note ID: " + noteId);

                }

                // Xóa ghi chú từ Firebase
                notesRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Thành công, ghi chú đã được xóa
                            Toast.makeText(context, "Ghi chú đã được xóa thành công từ Firebase", Toast.LENGTH_SHORT).show();

                            // Kiểm tra lại dữ liệu sau khi xóa
                            notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // In dữ liệu sau khi xóa
                                    if (dataSnapshot.exists()) {
                                        Log.d("Firebase", "Dữ liệu sau khi xóa: " + dataSnapshot.getValue());
                                    } else {
                                        Log.d("Firebase", "Ghi chú đã bị xóa.");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("Firebase", "Lỗi khi đọc dữ liệu sau khi xóa: " + databaseError.getMessage());
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Xử lý lỗi nếu không xóa được ghi chú
                            Toast.makeText(context, "Lỗi khi xóa ghi chú từ Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu trước khi xóa: " + databaseError.getMessage());
            }
        });
    }

    public static void removeNote(Note note) {
    }

    public static void updateNotebook(Notebook updatedNotebook) {
        // Step 1: Update the local list of notebooks
        for (int i = 0; i < notebooks.size(); i++) {
            if (notebooks.get(i).getId().equals(updatedNotebook.getId())) {
                notebooks.set(i, updatedNotebook);  // Replace the old notebook with the updated one
                Log.d("DataProvider", "Updated local notebook with ID: " + updatedNotebook.getId());
                break;
            }
        }

        // Step 2: Update the notebook data in Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("backup_json");

        // Find the specific notebook and update its values in Firebase
        userRef.child("notebooks").child(updatedNotebook.getId()).setValue(updatedNotebook)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DataProvider", "Successfully updated notebook in Firebase.");
                })
                .addOnFailureListener(e -> {
                    Log.e("DataProvider", "Failed to update notebook in Firebase: " + e.getMessage());
                });
    }
    public static void updateNotebook(Notebook updatedNotebook, Context context) {
        // Step 1: Update the local list of notebooks
        for (int i = 0; i < notebooks.size(); i++) {
            if (notebooks.get(i).getId().equals(updatedNotebook.getId())) {
                // Replace the old notebook with the updated one
                notebooks.set(i, updatedNotebook);
                Log.d("DataProvider", "Updated local notebook with ID: " + updatedNotebook.getId());
                break;
            }
        }

        // Step 2: Update the notebook data in Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("backup_json");

        // Find the specific notebook and update its values in Firebase
        userRef.child("notebooks").child(updatedNotebook.getId()).setValue(updatedNotebook)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DataProvider", "Successfully updated notebook in Firebase.");

                    // Step 3: Save updated notebooks to a file
                    saveNotebooksToFile(context);  // Save the updated data locally

                    // Step 4: Upload the backup data to Firebase after local update
                    uploadNotebooksToFirebase();  // Upload the full updated notebook data to Firebase

                })
                .addOnFailureListener(e -> {
                    Log.e("DataProvider", "Failed to update notebook in Firebase: " + e.getMessage());
                    Toast.makeText(context, "Failed to update notebook. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


}
