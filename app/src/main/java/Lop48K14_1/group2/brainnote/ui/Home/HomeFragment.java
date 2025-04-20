package Lop48K14_1.group2.brainnote.ui.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Lop48K14_1.group2.brainnote.R;

public class HomeFragment extends Fragment {
    private TextView dateTextView;
    private RelativeLayout btnNewNote, btnNewTask, btnNewNotebook;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        dateTextView     = view.findViewById(R.id.dateTextView);
        btnNewNote       = view.findViewById(R.id.home_new_note);
        btnNewTask       = view.findViewById(R.id.home_new_task);
        btnNewNotebook   = view.findViewById(R.id.home_new_notebook);

        // 1) Format ngày
        String pat = "EEEE, 'Ngày' dd 'Tháng' MM, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(pat, new Locale("vi","VN"));
        String txt = sdf.format(new Date());
        txt = txt.substring(0,1).toUpperCase() + txt.substring(1);
        dateTextView.setText(txt);
        // 2) NavController từ chính fragment
        NavController nav = NavHostFragment.findNavController(this);

        // 3) Gắn click
        btnNewNote.setOnClickListener(v ->
                nav.navigate(R.id.nav_new_note)    // id của màn tạo note
        );
//        btnNewTask.setOnClickListener(v ->
//                nav.navigate(R.id.nav_new_task)
//        );
        btnNewNotebook.setOnClickListener(v ->
                nav.navigate(R.id.nav_newnotebook)
        );


        // Thêm HeaderFragment nếu chưa được thêm
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.headerContainer, new HeaderFragment())
                    .commit();
        }

        return view;
    }

}