package com.example.hayoung.a20190507_1146;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistActivity extends AppCompatActivity {

    private EditText JoinID;
    private EditText JoinName;
    private EditText JoinPassW;
    private EditText JoinPassWcheck;
    private EditText JoinNum;
    private  Button btnJoinCom;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        getSupportActionBar().setTitle("Join");

        JoinName=(EditText)findViewById(R.id.JoinName);
        JoinID=(EditText)findViewById(R.id.JoinID);
        JoinPassW=(EditText)findViewById(R.id.JoinPassW);
        JoinPassWcheck=(EditText)findViewById(R.id.JoinPassWcheck);
        JoinNum=(EditText)findViewById(R.id.JoinNum);
        btnJoinCom = (Button) findViewById(R.id.btnJoinCom);

        JoinPassWcheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = JoinPassW.getText().toString();
                String confirm = JoinPassWcheck.getText().toString();

                if (password.equals(confirm)) {
                    JoinPassW.setBackgroundColor(Color.GREEN);
                    JoinPassWcheck.setBackgroundColor(Color.GREEN);

                }
                else {
                    JoinPassW.setBackgroundColor(Color.RED);
                    JoinPassWcheck.setBackgroundColor(Color.RED);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnJoinCom.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //이름 입력 확인
                if(JoinName.getText().toString().length()==0){
                    Toast.makeText(RegistActivity.this,"이름을 입력하세요!",Toast.LENGTH_SHORT).show();
                    JoinName.requestFocus();
                    return;
                }
                //아이디 입력 확인
                if(JoinID.getText().toString().length()==0){
                    Toast.makeText(RegistActivity.this,"ID를 입력하세요!",Toast.LENGTH_SHORT).show();
                    JoinID.requestFocus();
                    return;
                }
                //비밀번호 입력 확인
                if(JoinPassW.getText().toString().length()==0){
                    Toast.makeText(RegistActivity.this,"비밀번호를 입력하세요!",Toast.LENGTH_SHORT).show();
                    JoinPassW.requestFocus();
                    return;
                }
                //비밀번호 확인 입력 확인
                if(JoinPassWcheck.getText().toString().length()==0){
                    Toast.makeText(RegistActivity.this,"비밀번호 확인을 입력하세요!",Toast.LENGTH_SHORT).show();
                    JoinPassWcheck.requestFocus();
                    return;
                }
                //비밀번호와 비밀번호 확인이 같은지 확인
                if(!JoinPassW.getText().toString().equals(JoinPassWcheck.getText().toString())){
                    Toast.makeText(RegistActivity.this,"비밀번호가 일치하지 않습니다!",Toast.LENGTH_SHORT).show();
                    JoinPassW.setText("");
                    JoinPassWcheck.setText("");
                    JoinPassW.requestFocus();
                    return;
                }
                //전화번호 11자리 맞는지 확인
                if(JoinNum.getText().toString().length()!=11){
                    Toast.makeText(RegistActivity.this,"전화번호를 잘못 입력하셨습니다!",Toast.LENGTH_SHORT).show();
                    JoinNum.requestFocus();
                    return;
                }
              //회원가입 후 로그인 화면으로 가면 아이디가 작성되있도록 수행
                Intent result = new Intent();
                result.putExtra("ID",JoinID.getText().toString());

                setResult(RESULT_OK,result);
                finish();
            }
        });
    }
}

