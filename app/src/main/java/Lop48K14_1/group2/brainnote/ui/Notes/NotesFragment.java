package Lop48K14_1.group2.brainnote.ui.Notes;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

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

public class NotesFragment extends Fragment implements NoteAdapter.OnNoteClickListener {
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

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.rvNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize search EditText
        searchEditText = view.findViewById(R.id.searchNote);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (noteAdapter != null) {
                    noteAdapter.getFilter().filter(s);
                    updateNoteCount();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Initialize note count TextView
        noteCount = view.findViewById(R.id.tvNoteCount);

        // Floating action button to add new note
        addButtonNote = view.findViewById(R.id.addButtonNote);
        addButtonNote.setOnClickListener(v -> {
            Notebook defaultNb = DataProvider.getNotebooks().get(0);
            Bundle args = new Bundle();
            args.putString("NOTEBOOK_ID", defaultNb.getId());
            NavController nav = NavHostFragment.findNavController(NotesFragment.this);
            nav.navigate(R.id.action_nav_notes_to_nav_new_note, args);
        });

        // Load notes
        loadNotes();

        return view;
    }

    // Update the note count
    private void updateNoteCount() {
        int count = noteAdapter != null ? noteAdapter.getItemCount() : 0;
        noteCount.setText(count + " ghi chú");
    }

    // Load notes from Firebase or local file
    private void loadNotes() {
        JsonSyncManager.importDataWithFallback(getContext(), new JsonSyncManager.OnDataImported() {
            @Override
            public void onSuccess() {
                List<Notebook> notebooks = DataProvider.getNotebooks();
                notes = new ArrayList<>();
                for (Notebook notebook : notebooks) {
                    notes.addAll(notebook.getNotes());
                }

                noteAdapter = new NoteAdapter(notes, NotesFragment.this);
                recyclerView.setAdapter(noteAdapter);
                updateNoteCount();
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure
            }
        });
    }

    // Handle note click event
    @Override
    public void onNoteClick(int position) {
        Note clickedNote = notes.get(position);
        Bundle args = new Bundle();
        args.putString("NOTEBOOK_ID", clickedNote.getNotebookId());
        args.putString("NOTE_ID", clickedNote.getId());
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.action_nav_notes_to_nav_note_detail, args);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }
}
