package Lop48K14_1.group2.brainnote.ui.Notes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.sync.JsonSyncManager;
import Lop48K14_1.group2.brainnote.ui.adapters.NoteAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class NotesFragment extends Fragment implements NoteAdapter.OnNoteClickListener, NoteAdapter.OnNoteDeleteListener {

    private List<Note> notes;
    private FloatingActionButton addButtonNote;
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private TextView noteCount;
    private EditText searchEditText;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.rvNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchEditText = view.findViewById(R.id.searchNote);
        noteCount = view.findViewById(R.id.tvNoteCount);
        addButtonNote = view.findViewById(R.id.addButtonNote);

        addButtonNote.setOnClickListener(v -> {
            Notebook defaultNb = DataProvider.getNotebooks().get(0);
            Bundle args = new Bundle();
            args.putString("NOTEBOOK_ID", defaultNb.getId());
            NavController nav = NavHostFragment.findNavController(NotesFragment.this);
            nav.navigate(R.id.action_nav_notes_to_nav_new_note, args);
        });

        setupSearchFunctionality();
        loadNotes();

        return view;
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (noteAdapter != null) {
                    noteAdapter.getFilter().filter(s);
                    updateNoteCount(noteAdapter.getItemCount());
                }
            }
        });
    }

    private void updateNoteCount(int count) {
        if (count <= 0) {
            noteCount.setText("Không có ghi chú nào");
        } else {
            noteCount.setText(count + " ghi chú");
        }
    }


    private void loadNotes() {
        JsonSyncManager.importDataWithFallback(getContext(), new JsonSyncManager.OnDataImported() {
            @Override
            public void onSuccess() {
                List<Notebook> notebooks = DataProvider.getNotebooks();
                notes = new ArrayList<>();
                for (Notebook notebook : notebooks) {
                    notes.addAll(notebook.getNotes());
                }

                noteAdapter = new NoteAdapter(notes, NotesFragment.this, NotesFragment.this);
                recyclerView.setAdapter(noteAdapter);
                updateNoteCount(noteAdapter.getItemCount());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("NotesFragment", "Failed to load notes", e);
                Toast.makeText(getContext(), "Không thể tải dữ liệu ghi chú. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNoteClick(Note clickedNote) {
        Bundle args = new Bundle();
        args.putString("NOTEBOOK_ID", clickedNote.getNotebookId());
        args.putString("NOTE_ID", clickedNote.getId());
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.action_nav_notes_to_nav_note_detail, args);
    }

    @Override
    public void onNoteDelete(Note note, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa ghi chú \"" + note.getTitle() + "\"? Hành động này sẽ chuyển ghi chú vào thùng rác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Di chuyển ghi chú vào thùng rác
                    JsonSyncManager.moveNoteToTrash(
                            requireContext(),
                            note,
                            () -> {
                                // Thành công: Cập nhật UI
                                notes.remove(note);
                                noteAdapter.notifyItemRemoved(position);
                                updateNoteCount(noteAdapter.getItemCount());
                                loadNotes();
                            },
                            () -> {
                                // Thất bại: Làm mới UI để phản ánh dữ liệu thực tế
                                noteAdapter.notifyDataSetChanged();
                                Toast.makeText(requireContext(), "Không thể xóa ghi chú, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                            }
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }
}