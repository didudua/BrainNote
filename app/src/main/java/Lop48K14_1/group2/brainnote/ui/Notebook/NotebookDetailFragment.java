package Lop48K14_1.group2.brainnote.ui.Notebook;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import Lop48K14_1.group2.brainnote.ui.Home.NewNoteFragment;
import Lop48K14_1.group2.brainnote.ui.adapters.NoteAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class NotebookDetailFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> notes;
    private List<Note> filteredNotes;
    private EditText searchEditText;
    private TextView titleTextView;
    private TextView noteCountTextView;
    private ImageButton backButton;
    private FloatingActionButton addButton;
    private Notebook notebook;
    private String notebookId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notebook_detail, container, false);

        // Lấy ID sổ tay từ arguments
        Bundle args = getArguments();
        if (args != null) {
            notebookId = args.getString("NOTEBOOK_ID");
        }

        notebook = DataProvider.getNotebookById(notebookId);

        if (notebook == null) {
            Log.e("NotebookDetailFragment", "Notebook not found for ID: " + notebookId);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return view;
        }

        // Khởi tạo views
        recyclerView = view.findViewById(R.id.recyclerView);
        titleTextView = view.findViewById(R.id.titleTextView);
        noteCountTextView = view.findViewById(R.id.noteCountTextView);
        backButton = view.findViewById(R.id.backButton);
        addButton = view.findViewById(R.id.addButton);
        searchEditText = view.findViewById(R.id.searchEditText);

        // Thiết lập tiêu đề
        titleTextView.setText(notebook.getName());

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy dữ liệu
        notes = notebook.getNotes();
        filteredNotes = new ArrayList<>(notes);
        Log.d("NotebookDetailFragment", "Number of notes: " + notes.size());

        // Thiết lập adapter
        adapter = new NoteAdapter(filteredNotes, this);
        recyclerView.setAdapter(adapter);

        // Cập nhật số lượng ghi chú
        updateNoteCount();

        // Thiết lập tìm kiếm
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterNotes(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } else {
            Log.w("NotebookDetailFragment", "searchEditText is null, search functionality disabled");
        }

        // Thiết lập nút quay lại
        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(NotebookDetailFragment.this);
            navController.navigateUp();
        });

        // Thiết lập nút thêm mới
        addButton.setOnClickListener(v -> {
            Bundle newArgs = new Bundle();
            newArgs.putString("NOTEBOOK_ID", notebookId);
            NavController navController = NavHostFragment.findNavController(NotebookDetailFragment.this);

        });

        return view;
    }

    private void filterNotes(String query) {
        filteredNotes.clear();

        if (query.isEmpty()) {
            filteredNotes.addAll(notes);
        } else {
            query = query.toLowerCase();
            for (Note note : notes) {
                if (note.getTitle().toLowerCase().contains(query) ||
                        note.getContent().toLowerCase().contains(query)) {
                    filteredNotes.add(note);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateNoteCount();
    }

    private void updateNoteCount() {
        if (filteredNotes.isEmpty()) {
            noteCountTextView.setText("Không có ghi chú nào");
        } else {
            noteCountTextView.setText(filteredNotes.size() + " ghi chú");
        }
    }

    @Override
    public void onNoteClick(int position) {
        if (filteredNotes == null || position >= filteredNotes.size()) {
            Log.e("NotebookDetailFragment", "Invalid note position: " + position);
            return;
        }

        Note selectedNote = filteredNotes.get(position);
        if (selectedNote == null) {
            Log.e("NotebookDetailFragment", "Selected note is null at position: " + position);
            return;
        }

        Bundle args = new Bundle();
        args.putString("NOTEBOOK_ID", notebookId);
        args.putString("NOTE_ID", selectedNote.getId());

        NavController navController = NavHostFragment.findNavController(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại dữ liệu khi quay lại fragment
        notebook = DataProvider.getNotebookById(notebookId);
        if (notebook != null) {
            notes = notebook.getNotes();
            if (searchEditText != null && searchEditText.getText() != null) {
                filterNotes(searchEditText.getText().toString());
            } else {
                filteredNotes.clear();
                filteredNotes.addAll(notes);
                adapter.notifyDataSetChanged();
                updateNoteCount();
            }
        }
    }
}