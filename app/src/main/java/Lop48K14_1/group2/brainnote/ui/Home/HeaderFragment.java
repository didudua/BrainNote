package Lop48K14_1.group2.brainnote.ui.Home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Lop48K14_1.group2.brainnote.R;

public class HeaderFragment extends Fragment {

    private TextView tvUsername, tvEmail;
    private ImageView avatarImageView;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, container, false);
        avatarImageView = view.findViewById(R.id.avatarImageView);


        avatarImageView.setOnClickListener(v -> {
            View popupView = getLayoutInflater().inflate(R.layout.user_popup, null);

            tvUsername = popupView.findViewById(R.id.tvUsername);
            tvEmail= popupView.findViewById(R.id.tvEmail);
            loadUserData();
            // Create PopupWindow
            int width = (int) (200 * getResources().getDisplayMetrics().density); // 200dp -> px
            int height = ViewGroup.LayoutParams.WRAP_CONTENT; // Chiều cao vẫn là WRAP_CONTENT

            PopupWindow popupWindow = new PopupWindow(popupView, width, height);
            popupWindow.setFocusable(true);

            popupWindow.showAsDropDown(avatarImageView, 5, 5);

            // Set item click listeners
            LinearLayout profileBtn = popupView.findViewById(R.id.item_user);
            LinearLayout notificationsBtn = popupView.findViewById(R.id.item_notify);
            LinearLayout trashBtn = popupView.findViewById(R.id.item_trash);

            profileBtn.setOnClickListener(v1 -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_profile);
                popupWindow.dismiss();
            });
            notificationsBtn.setOnClickListener(v1 -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_notification);
                popupWindow.dismiss(); // Close the PopupWindow
            });

            trashBtn.setOnClickListener(v1 -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_trashcan);
                popupWindow.dismiss(); // Close the PopupWindow
            });

            // Show PopupWindow below the avatar image
            popupWindow.showAsDropDown(avatarImageView, 0, 0);
        });

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
                    } else {
                        Log.e("HomeFragment", "Không tìm thấy thông tin email hoặc username.");
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
    }
}