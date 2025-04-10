package Lop48K14_1.group2.brainnote.ui.Home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.UUID;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class NewNotebookFragment extends Fragment {

    private EditText nameEditText;
    private ImageButton backButton;
    private ImageButton saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_notebook, container, false);

        // Khởi tạo views
        nameEditText = view.findViewById(R.id.nameEditText);
        backButton = view.findViewById(R.id.backButton);
        saveButton = view.findViewById(R.id.saveButton);

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
                saveNotebook();
            }
        });

        return view;
    }

    private void saveNotebook() {
        String name = nameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Vui lòng nhập tên sổ tay", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo sổ tay mới
        Notebook newNotebook = new Notebook(
                UUID.randomUUID().toString(),
                name,
                new ArrayList<>()
        );

        // Thêm sổ tay vào danh sách
        DataProvider.addNotebook(newNotebook);

        Toast.makeText(getContext(), "Đã tạo sổ tay mới", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
