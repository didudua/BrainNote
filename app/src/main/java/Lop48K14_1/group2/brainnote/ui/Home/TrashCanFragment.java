package Lop48K14_1.group2.brainnote.ui.Home;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.adapters.TrashNoteAdapter;
import Lop48K14_1.group2.brainnote.ui.adapters.TrashNotebookAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;

public class TrashCanFragment extends Fragment implements TrashNotebookAdapter.OnTrashItemClickListener, TrashNoteAdapter.OnTrashNoteClickListener {

    private RecyclerView recyclerView, noterecyclerView;
    private TrashNotebookAdapter adapter;
    private TrashNoteAdapter noteadapter;
    private List<Notebook> deletedNotebooks = new ArrayList<>();
    private List<Note> deletedNotes = new ArrayList<>();
    private DatabaseReference trashRef;
    private DatabaseReference notebooksRef, trashNotesRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_trash_can, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerViewTrashNotebooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        noterecyclerView = rootView.findViewById(R.id.recyclerViewTrashNotes);
        noterecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Set up the adapter with the listener
        adapter = new TrashNotebookAdapter(getContext(), deletedNotebooks, this);
        recyclerView.setAdapter(adapter);

        noteadapter = new TrashNoteAdapter(getContext(), deletedNotes, this);
        noterecyclerView.setAdapter(noteadapter);

        // Get Firebase reference for trash and notebooks
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        trashRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("trash").child("notebooks");
        notebooksRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notebooks");
        trashNotesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("trash").child("notes");

        ImageButton btnBack = rootView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_trashFragment_to_homeFragment);
        });

        // Load deleted notebooks from Firebase
        loadDeletedNotebooks();

        loadDeletedNotes();

        return rootView;
    }

    private void loadDeletedNotebooks() {
        // Add a listener to load deleted notebooks from Firebase
        trashRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deletedNotebooks.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notebook notebook = snapshot.getValue(Notebook.class);
                    if (notebook != null) {
                        deletedNotebooks.add(notebook);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void loadDeletedNotes() {
        // Lấy ID người dùng hiện tại từ Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Tham chiếu tới Firebase nơi chứa các ghi chú đã xóa
        trashNotesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("trash").child("notes");

        // Thêm ValueEventListener để theo dõi sự thay đổi của dữ liệu
        trashNotesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Xóa danh sách cũ trước khi thêm dữ liệu mới
                deletedNotes.clear();

                // Duyệt qua các ghi chú trong Firebase
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Chuyển dữ liệu từ Firebase thành đối tượng Note
                    Note note = snapshot.getValue(Note.class);

                    // Kiểm tra nếu ghi chú hợp lệ và thêm vào danh sách deletedNotes
                    if (note != null) {
                        deletedNotes.add(note);
                    }
                }

                // Cập nhật RecyclerView sau khi dữ liệu đã được tải xong
                noteadapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi khi lấy dữ liệu
                Toast.makeText(getContext(), "Lỗi khi tải ghi chú", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRestore(Notebook notebook) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // 1. Lấy backup_json hiện tại
        userRef.child("backup_json").get().addOnSuccessListener(snapshot -> {
            try {
                String jsonString = snapshot.getValue(String.class);
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray notebooksArray = jsonObject.getJSONArray("notebooks");

                // 2. Tạo JSONObject từ notebook
                JSONObject restoredNotebook = new JSONObject();
                restoredNotebook.put("id", notebook.getId());
                restoredNotebook.put("name", notebook.getName());

                JSONArray notesArray = new JSONArray();
                if (notebook.getNotes() != null) {
                    for (Note note : notebook.getNotes()) {
                        JSONObject noteJson = new JSONObject();
                        noteJson.put("id", note.getId());
                        noteJson.put("title", note.getTitle());
                        noteJson.put("content", note.getContent());
                        noteJson.put("date", note.getDate());
                        noteJson.put("notebookId", note.getNotebookId());
                        notesArray.put(noteJson);
                    }
                }

                restoredNotebook.put("notes", notesArray);
                notebooksArray.put(restoredNotebook);

                // 3. Ghi lại backup_json mới
                userRef.child("backup_json").setValue(jsonObject.toString());

                // 4. Xóa khỏi trash
                userRef.child("trash").child("notebooks").child(notebook.getId()).removeValue();

                // 5. Thông báo
                Toast.makeText(getContext(), "Sổ tay đã được khôi phục", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi khi khôi phục sổ tay", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeletePermanently(Notebook notebook) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa vĩnh viễn")
                .setMessage("Bạn có chắc muốn xóa vĩnh viễn sổ tay \"" + notebook.getName() + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                    // Xóa khỏi trash
                    userRef.child("trash").child("notebooks").child(notebook.getId()).removeValue();

                    Toast.makeText(getContext(), "Sổ tay đã bị xóa vĩnh viễn", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    public void onRestore(Note note) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.child("backup_json").get().addOnSuccessListener(snapshot -> {
            try {
                String jsonString = snapshot.getValue(String.class);
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray notebooksArray = jsonObject.getJSONArray("notebooks");

                boolean notebookFound = false;

                // Tìm đúng notebook để thêm ghi chú vào
                for (int i = 0; i < notebooksArray.length(); i++) {
                    JSONObject notebookObj = notebooksArray.getJSONObject(i);
                    if (notebookObj.getString("id").equals(note.getNotebookId())) {
                        JSONArray notesArray = notebookObj.getJSONArray("notes");

                        // Tạo object note mới
                        JSONObject newNote = new JSONObject();
                        newNote.put("id", note.getId());
                        newNote.put("title", note.getTitle());
                        newNote.put("content", note.getContent());
                        newNote.put("date", note.getDate());
                        newNote.put("notebookId", note.getNotebookId());

                        notesArray.put(newNote);
                        notebookFound = true;
                        break;
                    }
                }

                if (!notebookFound) {
                    Toast.makeText(getContext(), "Không tìm thấy sổ tay tương ứng", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cập nhật lại dữ liệu vào backup_json
                userRef.child("backup_json").setValue(jsonObject.toString());

                // Xóa ghi chú khỏi trash
                userRef.child("trash").child("notes").child(note.getId()).removeValue();

                Toast.makeText(getContext(), "Ghi chú đã được khôi phục", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi khi khôi phục ghi chú", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeletePermanently(Note note) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa vĩnh viễn")
                .setMessage("Bạn có chắc muốn xóa vĩnh viễn ghi chú \"" + note.getTitle() + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                    userRef.child("trash").child("notes").child(note.getId()).removeValue();

                    Toast.makeText(getContext(), "Ghi chú đã bị xóa vĩnh viễn", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}