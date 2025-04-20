package Lop48K14_1.group2.brainnote.ui.Notes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup c, Bundle b) {
        View view = inflater.inflate(R.layout.fragment_note_detail, c, false);

        titleEditText   = view.findViewById(R.id.titleNote);
        contentEditText = view.findViewById(R.id.contentNote);
        backButton      = view.findViewById(R.id.backButtonNote);
        noteBookDefault = view.findViewById(R.id.default_notebook_note);

        // 1. Lấy danh sách sổ
        notebooks = DataProvider.getNotebooks();

        // 2. Khởi tạo giá trị mặc định
        if (getArguments()!=null) {
            originalNotebookId = getArguments().getString("NOTEBOOK_ID");
            notebookId         = originalNotebookId;
            noteId             = getArguments().getString("NOTE_ID");
        } else {
            originalNotebookId = notebooks.get(0).getId();
            notebookId         = originalNotebookId;
            noteId             = "";
        }
        // Hiển thị tên sổ hiện tại
        notebook = DataProvider.getNotebookById(notebookId);
        noteBookDefault.setText(notebook.getName());

        // 3. Khi nhấn vào TextView thì bật dialog
        noteBookDefault.setOnClickListener(v -> {
            // Chuẩn bị mảng tên để show
            String[] names = new String[notebooks.size()];
            for (int i = 0; i < notebooks.size(); i++) {
                names[i] = notebooks.get(i).getName();
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn sổ để thêm ghi chú")
                    .setItems(names, (dialog, which) -> {
                        // Cập nhật notebookId và tên hiển thị
                        notebookId = notebooks.get(which).getId();
                        noteBookDefault.setText(names[which]);
                    })
                    .show();
        });

        // 2. Tìm Notebook
        notebook = DataProvider.getNotebookById(notebookId);
        if (notebook != null) {
            noteBookDefault.setText(notebook.getName());

            // 3. Tìm Note trong danh sách của notebook này
            for (Note n : notebook.getNotes()) {
                if (n.getId().equals(noteId)) {
                    note = n;
                    break;
                }
            }

            // 4. Bind dữ liệu lên UI nếu tìm thấy note
            if (note != null) {
                titleEditText.setText(note.getTitle());
                contentEditText.setText(note.getContent());
            }
        }

        backButton.setOnClickListener(v -> saveNote());

        return view;
    }

    private void saveNote() {
        String newTitle   = titleEditText.getText().toString().trim();
        String newContent = contentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle)) {
            newTitle = "Tài liệu không có tiêu đề";
        }

        // 1. Cập nhật trực tiếp lên object Note (đã lấy ở onCreateView)
        if (note != null) {
            note.setTitle(newTitle);
            note.setContent(newContent);
            note.setDate(dateFormat.format(new Date()));
        }
        if (!notebookId.equals(originalNotebookId)) {
            // xóa note khỏi sổ cũ
            DataProvider.removeNoteFromNotebook(originalNotebookId, noteId);
            // thêm note vào sổ mới
            DataProvider.addNoteToNotebook(notebookId, note);
        }

        // 2. Ghi lại toàn bộ notebooks xuống file và Firebase
        JsonSyncManager.saveNotebooksToFile(requireContext());
        JsonSyncManager.uploadNotebooksToFirebase();

        requireActivity().getSupportFragmentManager().popBackStack();
    }
}