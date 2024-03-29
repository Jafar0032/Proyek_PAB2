package com.tugasakhirpab2.rjn.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tugasakhirpab2.rjn.R;
import com.tugasakhirpab2.rjn.databinding.ActivityUbahProfilBinding;
import com.tugasakhirpab2.rjn.model.User;

import java.util.Calendar;

public class UbahProfilActivity extends AppCompatActivity {
    ActivityUbahProfilBinding binding;
    private DatabaseReference mRoot, mRef;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    private String userId;
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUbahProfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userId = mAuth.getCurrentUser().getUid();
        mRoot = FirebaseDatabase.getInstance().getReference();
        mRef = mRoot.child("users").child(userId);

        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        binding.ibDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(UbahProfilActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        binding.etBirthDate.setText(String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year));
                        mYear = year;
                        mMonth = month;
                        mDay = dayOfMonth;
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        showUserData();

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDataUser();
            }
        });

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UbahProfilActivity.this, ProfilActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void editDataUser() {
        if(firebaseUser != null){
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mRef.child("fullName").setValue(binding.etFullName.getText().toString());
                    mRef.child("gender").setValue(binding.spinGender.getSelectedItem().toString());
                    mRef.child("birthDate").setValue(binding.etBirthDate.getText().toString());
                    mRef.child("address").setValue(binding.etAdddress.getText().toString());

                    Intent intent = new Intent(UbahProfilActivity.this, ProfilActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            Toast.makeText(this, "No User!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUserData() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String nama, jenisKelamin, birthDate, address;
                User user = snapshot.getValue(User.class);
                nama = user.getFullName();
                jenisKelamin = user.getGender();
                birthDate = user.getBirthDate();
                address = user.getAddress();

                binding.etFullName.setText(nama);
                binding.etGender.setText(jenisKelamin);
                binding.etBirthDate.setText(birthDate);
                binding.etAdddress.setText(address);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}