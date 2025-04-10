package Lop48K14_1.group2.brainnote.ui.Home;

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
import java.util.Locale;
import java.util.UUID;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class NewNoteFragment extends Fragment {

    private EditText titleEditText;
    private EditText contentEditText;
    private TextView infoTextView;
    private ImageButton backButton;
    private ImageButton saveButton;
    private Notebook notebook;
    private String notebookId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_note, container, false);

        // Lấy ID sổ tay từ arguments
        Bundle args = getArguments();
        if (args != null) {
            notebookId = args.getString("NOTEBOOK_ID");
        }

        notebook = DataProvider.getNotebookById(notebookId);

        if (notebook == null) {
            // Quay lại fragment trước đó nếu không tìm thấy sổ tay
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return view;
        }

        // Khởi tạo views
        titleEditText = view.findViewById(R.id.titleEditText);
        contentEditText = view.findViewById(R.id.contentEditText);
        infoTextView = view.findViewById(R.id.infoTextView);
        backButton = view.findViewById(R.id.backButton);
        saveButton = view.findViewById(R.id.saveButton);

        // Thiết lập thông tin
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        infoTextView.setText(notebook.getName() + " • " + currentDate);

        // Thiết lập nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        // Thiết lập nút lưu
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        return view;
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo ghi chú mới
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        Note newNote = new Note(
                UUID.randomUUID().toString(),
                title,
                content,
                currentDate
        );

        // Thêm ghi chú vào sổ tay
        DataProvider.addNoteToNotebook(notebook.getId(), newNote);

        Toast.makeText(getContext(), "Đã lưu ghi chú", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
