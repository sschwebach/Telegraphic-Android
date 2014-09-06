package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sam.wisc.edu.telegraphic.R;

public class LoginActivity extends Activity {
    LoginHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mHandler = new LoginHandler((Context) this, this);
        doSetup();
    }

    public void doSetup(){
        final CheckBox rememberCheck = (CheckBox) findViewById(R.id.check_remember);
        Button loginButton = (Button) findViewById(R.id.button_login);
        Button registerButton = (Button) findViewById(R.id.button_register);
        final EditText userNameText = (EditText) findViewById(R.id.edit_text_username);
        final EditText passwordText = (EditText) findViewById(R.id.edit_text_password);
        if (mHandler.getLogin()){
            userNameText.setText(mHandler.userName);
            passwordText.setText(mHandler.pwordHash);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.userName = userNameText.getText().toString();
                mHandler.pwordHash = passwordText.getText().toString();
                DataHolder.username = userNameText.getText().toString();
                DataHolder.password = passwordText.getText().toString();
                if (rememberCheck.isChecked()) {
                    mHandler.remember = true;
                }
                mHandler.login();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.userName = userNameText.getText().toString();
                mHandler.pwordHash = passwordText.getText().toString();
                DataHolder.username = userNameText.getText().toString();
                DataHolder.password = passwordText.getText().toString();
                if (rememberCheck.isChecked()) {
                    mHandler.remember = true;
                }
                mHandler.register();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
