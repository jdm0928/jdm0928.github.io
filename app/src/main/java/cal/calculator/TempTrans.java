package cal.calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class TempTrans extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;

    TextView tv_Expression;
    TextView tv_Result;
    List<Integer> checkList; // -1: 이콜, 0: 연산자, 1: 숫자, 2: . / 예외 발생을 막는 리스트

    Spinner sp_Exp, sp_Res;

    int exp = 0, res = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_trans);

        this.init();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.layout_drawer);
        navigationView = findViewById(R.id.nav);
        navigationView.setItemIconTintList(null);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                String title = item.getTitle().toString();

                if(id == R.id.menu_defCal){
                    Intent in = new Intent(getApplicationContext(), DefaultCal.class);
                    startActivity(in);
                    finish();
                }
                return true;
            }
        });

        // 입력값 스피너 구현
        sp_Exp = (Spinner) findViewById(R.id.exp_select);
        String[] exp_str = getResources().getStringArray(R.array.temp_array);
        ArrayAdapter<String> exp_adapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown_item, exp_str);
        exp_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_Exp.setAdapter(exp_adapter);

        sp_Exp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                exp = position;
                changeSpinner(exp, res);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 출력값 스피너 구현
        sp_Res = (Spinner) findViewById(R.id.res_select);
        String[] res_str = getResources().getStringArray(R.array.temp_array);
        ArrayAdapter<String> res_adapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown_item, res_str);
        res_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_Res.setAdapter(res_adapter);

        sp_Res.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                res = position;
                changeSpinner(exp, res);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void negaClick (View v){
        if (tv_Expression.length() == 0 || tv_Expression.getText().toString().equals("0")) {
            Toast.makeText(getApplicationContext(), "마지막 입력값이 숫자여야 사용가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] ex = tv_Expression.getText().toString().split("");

        List<String> li = new ArrayList<String>();
        Collections.addAll(li, ex);

        if(!isNumber(ex[0])){
            li.remove(0);
            tv_Expression.setText(TextUtils.join("", li));
        }
        else {
            tv_Expression.setText("-" + TextUtils.join("", li));
        }
        changeSpinner(exp, res);
    }

    // 0. 섭씨  1. 화씨  2. 절대 온도
    public void changeSpinner(int exp, int res){
        double temp = Double.parseDouble(tv_Expression.getText().toString());
        switch(exp){
            case 0:
                switch(res){
                    case 0: // 섭씨 -> 섭씨
                        tv_Result.setText(temp + "");
                        break;
                    case 1: // 섭씨 -> 화씨
                        tv_Result.setText(String.valueOf(Math.round((temp * 1.8) + 32 * 100) / 100.0));
                        break;
                    case 2: // 섭씨 -> 절대 온도
                        tv_Result.setText(String.valueOf(Math.round((temp + 273.15) * 100) / 100.0));
                        break;
                }
                break;
            case 1:
                switch(res){
                    case 0: // 화씨 -> 섭씨
                        tv_Result.setText(String.valueOf(Math.round((temp - 32) / 1.8 * 100) / 100.0));
                        break;
                    case 1: // 화씨 -> 화씨
                        tv_Result.setText(temp + "");
                        break;
                    case 2: // 화씨 -> 절대온도
                        tv_Result.setText(String.valueOf(Math.round((((temp - 32) / 1.8) + 273.15) * 100) / 100.0));
                        break;
                }
                break;
            case 2:
                switch(res){
                    case 0: // 절대 온도 -> 섭씨
                        tv_Result.setText(String.valueOf(Math.round((temp - 273.15) * 100) / 100.0));
                        break;
                    case 1: // 절대 온도 -> 화씨
                        tv_Result.setText(String.valueOf(Math.round((((temp - 273.15) * 1.8) + 32) * 100) / 100.0));
                        break;
                    case 2: // 절대 온도 -> 절대 온도
                        tv_Result.setText(temp + "");
                        break;
                }
                break;
        }
    }

    void init() {
        tv_Expression = findViewById(R.id.tv_expression);
        tv_Result = findViewById(R.id.tv_result);
        checkList = new ArrayList<>();
    }

    public void btClick(View v) {
        int gId = v.getId();

        if(checkList.isEmpty()){
            tv_Expression.setText("");
        }

        if (gId == R.id.bt_1) addNumber("1");
        else if (gId == R.id.bt_2) addNumber("2");
        else if (gId == R.id.bt_3) addNumber("3");
        else if (gId == R.id.bt_4) addNumber("4");
        else if (gId == R.id.bt_5) addNumber("5");
        else if (gId == R.id.bt_6) addNumber("6");
        else if (gId == R.id.bt_7) addNumber("7");
        else if (gId == R.id.bt_8) addNumber("8");
        else if (gId == R.id.bt_9) addNumber("9");
        else if (gId == R.id.bt_0) addNumber("0");
        else if (gId == R.id.bt_point) addPoint(".");
    }

    public void clearClick (View v){
        checkList.clear();
        tv_Expression.setText("0");
        tv_Result.setText("");
        changeSpinner(exp, res);
    }

    public void deleteClick (View v){
        if (tv_Expression.length() != 0) {
            if(checkList.isEmpty())
                return;
            checkList.remove(checkList.size() - 1);
            String[] ex = tv_Expression.getText().toString().split("");
            List<String> li = new ArrayList<String>();
            Collections.addAll(li, ex);
            li.remove(li.size() - 1);

            if (li.size() > 0 && !isNumber(li.get(li.size() - 1)))
                li.remove(li.size() - 1);
            tv_Expression.setText(TextUtils.join("", li));
        }
        if (tv_Expression.length() == 0){
            tv_Expression.setText("0");
            checkList.clear();
        }
        changeSpinner(exp, res);
    }

    // 숫자 버튼
    void addNumber (String str){
        checkList.add(1); // 숫자가 들어왔는지 체크리스트에 표시
        tv_Expression.append(str); // UI
        changeSpinner(exp, res);
    }

    void addPoint (String str){
        if (checkList.isEmpty()) {
            Toast.makeText(getApplicationContext(), ". 을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 하나의 수에 . 이 여러 개 오는 것을 막기
        for (int i = checkList.size() - 2; i >= 0; i--) {
            int check = checkList.get(i);
            if (check == 2) {
                Toast.makeText(getApplicationContext(), ". 을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (check == 0) break;
            if (check == 1) continue;
        }
        checkList.add(2);
        tv_Expression.append(str); // UI
    }

    boolean isNumber (String str){
        boolean result = true;
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            result = false;
        }
        return result;
    }
}