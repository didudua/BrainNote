package Lop48K14_1.group2.brainnote.ui.Home;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Lop48K14_1.group2.brainnote.MainActivity;
import Lop48K14_1.group2.brainnote.R;

public class ProfileFragment extends Fragment {
    private static final String PREFS_NAME      = "BrainNotePrefs";

    private TextView tvUsername, tvEmail, tv_Username, tv_Email;
    private LinearLayout btnLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Khởi tạo TextView
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail= view.findViewById(R.id.tvEmail);
        tv_Username = view.findViewById(R.id.tv_Username);
        tv_Email= view.findViewById(R.id.tv_Email);

        // Gắn sự kiện nút quay về
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_homeFragment);
        });
        LinearLayout addAccountLayout = view.findViewById(R.id.addAccount);
        addAccountLayout.setOnClickListener(v -> {
            PopupLoginFragment dialog = new PopupLoginFragment();
            dialog.show(getParentFragmentManager(), "login_popup");
        });

        // Gọi hàm tải dữ liệu người dùng
        loadUserData();

        return view;
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Sử dụng đúng DatabaseReference cho Firebase Realtime Database
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")  // Chú ý là node gốc là "users"
                    .child(uid);  // Dùng userID của người dùng hiện tại để lấy dữ liệu

            // Đọc dữ liệu người dùng từ Firebase
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Lấy giá trị email và username từ DataSnapshot
                    String email = snapshot.child("email").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);

                    // Kiểm tra và hiển thị dữ liệu người dùng
                    if (email != null && username != null) {
                        tvUsername.setText( username );
                        tvEmail.setText(email);
                        tv_Username.setText(username);
                        tv_Email.setText(email);
                    } else {
                        Log.e("ProfileFragment", "Không tìm thấy thông tin email hoặc username.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
                }
            });
        } else {
            Log.e("FirebaseAuth", "Người dùng chưa đăng nhập");
        }


        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();  // Xóa tất cả dữ liệu trong SharedPreferences
            editor.apply();

            tvUsername.setText("");
            tvEmail.setText("");
            tv_Username.setText("");
            tv_Email.setText("");

            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), MainActivity.class); // hoặc LoginActivity nếu bạn có
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear hết stack
            startActivity(intent);
        });


    }
}