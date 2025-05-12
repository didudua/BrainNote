package Lop48K14_1.group2.brainnote.ui.Notes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.sync.JsonSyncManager;
import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class NoteDetailFragment extends Fragment {
    private EditText titleEditText, contentEditText;
    private TextView noteBookDefault;
    private ImageButton backButton;
    private String notebookId, noteId, originalNotebookId;
    private Notebook notebook;
    private Note note;
    private List<Notebook> notebooks;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("HH:mm:ss, dd/MM/yyyy", Locale.getDefault());

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_detail, container, false);

        // Bind views
        titleEditText   = view.findViewById(R.id.titleNote);
        contentEditText = view.findViewById(R.id.contentNote);
        backButton      = view.findViewById(R.id.backButtonNote);
        noteBookDefault = view.findViewById(R.id.default_notebook_note);

        // Load notebooks
        notebooks = DataProvider.getNotebooks();

        // Get args
        Bundle args = getArguments();
        if (args == null) {
            Log.e("NoteDetailFragment", "Arguments are null!");
        } else {
            Log.d("NoteDetailFragment", "NOTE_ID in args = " + args.getString("NOTEBOOK_ID"));
        }
        if (args != null) {
            originalNotebookId = args.getString("NOTEBOOK_ID");
            noteId             = args.getString("NOTE_ID", "");

            Log.d("NoteDetailFragment", "NOTE_ID: " + noteId);
            Log.d("NoteDetailFragment", "NOTEBOOK_ID: " + originalNotebookId);
        } else {
            // Default to first notebook for new note
            originalNotebookId = notebooks.get(0).getId();
            noteId             = "";
        }
        notebookId = originalNotebookId;

        // Display current notebook
        notebook = DataProvider.getNotebookById(notebookId);
        noteBookDefault.setText(notebook.getName());

        // Choose another notebook
        noteBookDefault.setOnClickListener(v -> {
            String[] names = new String[notebooks.size()];
            for (int i = 0; i < notebooks.size(); i++) {
                names[i] = notebooks.get(i).getName();
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn sổ để lưu ghi chú")
                    .setItems(names, (dialog, which) -> {
                        notebookId = notebooks.get(which).getId();
                        noteBookDefault.setText(names[which]);
                    })
                    .show();
        });

        Notebook debugNotebook = DataProvider.getNotebookById(originalNotebookId);

        if (debugNotebook == null) {
            Log.e("NoteDetailFragment", "Notebook is NULL! ID = " + originalNotebookId);
        } else {
            Log.d("NoteDetailFragment", "Notebook found. ID = " + originalNotebookId);
            Log.d("NoteDetailFragment", "Notebook name: " + debugNotebook.getName());
            Log.d("NoteDetailFragment", "Notebook contains " + debugNotebook.getNotes().size() + " notes");

            for (Note n : debugNotebook.getNotes()) {
                Log.d("NoteDetailFragment", "Note ID = " + n.getId() + ", Title = " + n.getTitle());
            }
        }
        // Load existing note if editing
        if (!TextUtils.isEmpty(noteId)) {
            note = DataProvider.getNotebookById(originalNotebookId)
                    .getNotes().stream()
                    .filter(n -> n.getId().equals(noteId))
                    .findFirst().orElse(null);
        }
        if (note != null) {
            titleEditText.setText(note.getTitle());
            contentEditText.setText(note.getContent());
        }

        // Save or create
        backButton.setOnClickListener(v -> saveNote());

        return view;
    }

    private void saveNote() {
        String newTitle   = titleEditText.getText().toString().trim();
        String newContent = contentEditText.getText().toString().trim();
        if (TextUtils.isEmpty(newTitle)) {
            newTitle = "Tài liệu không có tiêu đề";
        }
        String now = dateFormat.format(new Date());

        if (note == null) {
            // Create new note
            String newId = UUID.randomUUID().toString();
            Note newNote = new Note(newId, notebookId, newTitle, newContent, now);
            DataProvider.addNoteToNotebook(notebookId, newNote);
        } else {
            // Update existing
            note.setTitle(newTitle);
            note.setContent(newContent);
            note.setDate(now);
            // Move if notebook changed
            if (!notebookId.equals(originalNotebookId)) {
                // Notebook changed → move note
                DataProvider.removeNoteFromNotebook(originalNotebookId, note.getId());
                DataProvider.addNoteToNotebook(notebookId, note);
            } else {
                // Notebook not changed → update using addNoteToNotebook (handles overwrite)
                DataProvider.addNoteToNotebook(notebookId, note);
            }
        }

        // Sync
        JsonSyncManager.saveNotebooksToFile(requireContext());
        JsonSyncManager.uploadNotebooksToFirebase();

        requireActivity().getSupportFragmentManager().popBackStack();
    }

}