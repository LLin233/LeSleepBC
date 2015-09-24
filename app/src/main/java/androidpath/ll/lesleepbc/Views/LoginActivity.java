package androidpath.ll.lesleepbc.Views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import androidpath.ll.lesleepbc.R;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = LoginActivity.class.getSimpleName();
    private HashMap<String, String> map;
    //TODO design layout
    @Bind(R.id.login_username_input)
    protected EditText mUsername;
    @Bind(R.id.login_password_input)
    protected EditText mPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        map = new HashMap<String, String>();
        map.put("leonard", "test");
        map.put("Johnny", "test");
    }

    @OnClick(R.id.btn_login)
    void login() {
        String msg = "Username: " + mUsername.getText() +
                "\nPassword: " + mPassword.getText();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        final String username = mUsername.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        if(map.containsKey(username) && map.get(username).equals(password)) {
            SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
