package Lop48K14_1.group2.brainnote;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import Lop48K14_1.group2.brainnote.sync.JsonSyncManager;
import Lop48K14_1.group2.brainnote.ui.Home.HomeFragment;
import Lop48K14_1.group2.brainnote.ui.Login.WelcomeFragment;
import Lop48K14_1.group2.brainnote.ui.MainHomeActivity;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra nếu đã đăng nhập
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (savedInstanceState == null) {
            if (currentUser != null) {
                syncNotebooksToFirebase(currentUser);
            } else {
                // Chưa đăng nhập → load WelcomeFragment
                loadFragment(new WelcomeFragment());
            }
        }
    }

    private void syncNotebooksToFirebase(FirebaseUser user) {
        // Kiểm tra kết nối mạng
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Không có kết nối mạng, sử dụng dữ liệu cục bộ", Toast.LENGTH_LONG).show();
            JsonSyncManager.importDataWithFallback(this, new JsonSyncManager.OnDataImported() {
                @Override
                public void onSuccess() {
                    proceedToMainHomeActivity();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MainActivity.this, "Lỗi nhập dữ liệu cục bộ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    proceedToMainHomeActivity();
                }
            });
            return;
        }

        // Nhập dữ liệu từ Firebase hoặc tệp cục bộ
        JsonSyncManager.importDataWithFallback(this, new JsonSyncManager.OnDataImported() {
            @Override
            public void onSuccess() {
                // Lưu cục bộ và tải lên Firebase
                JsonSyncManager.saveNotebooksToFile(MainActivity.this);
                JsonSyncManager.uploadNotebooksToFirebase();
                proceedToMainHomeActivity();
            }

            @Override
            public void onFailure(Exception e) {
                if (e.getMessage().contains("Permission denied") || e.getMessage().contains("User not logged in")) {
                    Toast.makeText(MainActivity.this, "Không có quyền truy cập dữ liệu, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    loadFragment(new WelcomeFragment());
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi nhập dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    proceedToMainHomeActivity();
                }
            }
        });
    }

    private void proceedToMainHomeActivity() {
        Intent intent = new Intent(MainActivity.this, MainHomeActivity.class);
        startActivity(intent);
        finish(); // Kết thúc MainActivity để tránh quay lại
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void loadHomeFragment() {
        loadFragment(new HomeFragment());
    }
}