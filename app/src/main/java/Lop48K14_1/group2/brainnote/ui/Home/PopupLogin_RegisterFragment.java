package Lop48K14_1.group2.brainnote.ui.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.sync.JsonSyncManager;
import Lop48K14_1.group2.brainnote.ui.Login.LoginFragment;
import Lop48K14_1.group2.brainnote.ui.Login.LoginPasswordFragment;
import Lop48K14_1.group2.brainnote.ui.MainHomeActivity;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;


public class PopupLogin_RegisterFragment extends BottomSheetDialogFragment {

    private static final String PREFS_NAME      = "BrainNotePrefs";
    private static final String KEY_LAST_EMAIL  = "last_email";

    private SharedPreferences prefs;
    private boolean login = true;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy từ google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popup_login__register, container, false);

        EditText emailEditText = view.findViewById(R.id.email_edit_text);
        EditText passwordEditText = view.findViewById(R.id.pass_edit_text);
        Button continueButton = view.findViewById(R.id.continue_button);
        CardView googleButton = (CardView) view.findViewById(R.id.googleSignInButton);

        TextView signupText = view.findViewById(R.id.signup_text);



        continueButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!isValidEmail(email)) {
                emailEditText.setError("Email không hợp lệ");
                emailEditText.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                passwordEditText.setError("Mật khẩu phải từ 6 ký tự trở lên");
                passwordEditText.requestFocus();
                return;
            }

            clearLocalUserData();
            // Đăng ký tài khoản mới Firebase
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Làm mới dữ liệu sau khi đăng ký tài khoản mới
                            DataProvider.clearData();
                            JsonSyncManager.uploadNotebooksToFirebase();
                            JsonSyncManager.uploadaccountcreatedAtToFirebase();
                            Toast.makeText(getContext(), "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();

                            Bundle bundle = new Bundle();
                            bundle.putString("email", email);
                            bundle.putBoolean("login", true); // chế độ đăng nhập

                            PopupLoginFragment loginFragment = new PopupLoginFragment();
                            loginFragment.setArguments(bundle);
                            loginFragment.show(getParentFragmentManager(), "PopupLogin");

                        }
                        else {
                            Toast.makeText(getContext(), "Không thể tạo tài khoản: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        });

        googleButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
        signupText.setOnClickListener(v -> {
            dismiss(); // đóng popup hiện tại trước
            PopupLoginFragment loginPopup = new PopupLoginFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("login", true);
            loginPopup.setArguments(bundle);
            loginPopup.show(requireActivity().getSupportFragmentManager(), loginPopup.getTag());

        });

        return view;
    }
    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("GOOGLE_SIGN_IN", "Google sign in failed", e);
                Toast.makeText(getContext(), "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveEmail(user.getEmail());
                        Toast.makeText(getContext(), "Chào mừng: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                        // Chuyển sang MainActivity
                        Intent intent = new Intent(getActivity(), MainHomeActivity.class);
                        startActivity(intent);

                    } else {
                        Toast.makeText(getContext(), "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveEmail(String email) {
        prefs.edit()
                .putString(KEY_LAST_EMAIL, email)
                .apply();
    }

    private void clearLocalUserData() {
        // Xóa hết dữ liệu trong SharedPreferences
        prefs.edit().clear().apply();
    }

}