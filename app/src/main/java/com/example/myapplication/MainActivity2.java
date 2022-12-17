package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.tools.RemoteFetch;
import com.google.android.material.tabs.TabLayout;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

TabLayout tabLayout;
ViewPager2 viewPager2;
MyViewPadgerAdapter myViewPadgerAdapter;
Button button2;
Button button3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(this);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_padger);
        myViewPadgerAdapter = new MyViewPadgerAdapter(this);
        viewPager2.setAdapter(myViewPadgerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.button2:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            case R.id.button3:
                Intent intent1  =new Intent(this, RemoteFetch.class);
                startActivity(intent1);
            default:
                break;
        }
    }
}