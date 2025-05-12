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

public class NewNoteFragment extends Fragment {
    private EditText titleEditText, contentEditText;
    private TextView noteBookDefault;
    private ImageButton backButton;
    private String notebookId;
    private Notebook notebook;
    private List<Notebook> notebooks;


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup c, Bundle b) {
        View view = inflater.inflate(R.layout.fragment_new_note, c, false);

        titleEditText   = view.findViewById(R.id.titleNewNote);
        contentEditText = view.findViewById(R.id.contentNewNote);
        backButton      = view.findViewById(R.id.backButtonNewNote);
        noteBookDefault = view.findViewById(R.id.default_newNote_notebook);

        // 1. Lấy danh sách sổ
        notebooks = DataProvider.getNotebooks();

        // 2. Khởi tạo giá trị mặc định
        if (getArguments() != null && getArguments().containsKey("NOTEBOOK_ID")) {
            notebookId = getArguments().getString("NOTEBOOK_ID");
        } else {
            notebookId = notebooks.get(0).getId();
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

        backButton.setOnClickListener(v ->{
            saveNote();
        });

        return view;
    }
    private void saveNote() {
        String title   = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            title = "Tài liệu không có tiêu đề";
        }
        if (content.isEmpty()) {
            content = "";
        }

        // Tạo Note mới
        Note note = new Note(
                UUID.randomUUID().toString(),
                notebookId,
                title,
                content,
                new SimpleDateFormat("HH:mm:ss, dd/MM/yyyy", Locale.getDefault()).format(new Date())
        );
        DataProvider.addNoteToNotebook(notebookId, note);
        if (notebook!=null) {
            DataProvider.addNoteToNotebook(notebookId, note);
            // Sync ra file + Firebase
            JsonSyncManager.saveNotebooksToFile(getContext());
            JsonSyncManager.uploadNotebooksToFirebase();
        }

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
