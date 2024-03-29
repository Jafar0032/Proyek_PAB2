package com.tugasakhirpab2.rjn.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tugasakhirpab2.rjn.CustomProgressDialog;
import com.tugasakhirpab2.rjn.R;
import com.tugasakhirpab2.rjn.databinding.ActivityCvactivityBinding;
import com.tugasakhirpab2.rjn.model.FileInModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.muddz.styleabletoast.StyleableToast;

public class CVActivity extends AppCompatActivity {

    private static final String TAG = CVActivity.class.getSimpleName();

    private ActivityCvactivityBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCvactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("pdfs");

        List<String> list = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showProgressBar();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                    String data = dataSnapshot.child("userId").getValue().toString();
                    Log.d(TAG, data);
                    list.add(data);
                }

                if(list.contains(userId))
                {
                    binding.llAdaCv.setVisibility(View.VISIBLE);
                    binding.llNothingCv.setVisibility(View.INVISIBLE);
                    String namaFile = snapshot.child(userId).child("fileName").getValue().toString();
                    binding.tvNamaFile.setText(namaFile);
                    hideProgressBar();
                    binding.tvNamaFile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setType("application/pdf");
                            intent.setData(Uri.parse(snapshot.child(userId).child("fileUrl").getValue().toString()));
                            startActivity(intent);
                        }
                    });
                }
                else
                {
                    hideProgressBar();
                    binding.llAdaCv.setVisibility(View.INVISIBLE);
                    binding.llNothingCv.setVisibility(View.VISIBLE);
                    binding.btnBrowseFile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            selectPDF();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.ivHapusFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bsl_hapus_cv);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnHapus = dialog.findViewById(R.id.btn_hapus);
        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child(userId).removeValue();
                Intent intent = new Intent(CVActivity.this, CVActivity.class);
                startActivity(intent);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void selectPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF files"), 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            Uri uri = data.getData();

            // Extract name of the pdf file
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String displayName = null;

            if(uriString.startsWith("content://"))
            {
                Cursor cursor = null;
                try {
                    cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if(cursor != null && cursor.moveToFirst())
                    {
                        displayName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
            else if(uriString.startsWith("file//"))
            {
                displayName = myFile.getName();
            }
            uploadPDF(data.getData(), displayName);
        }
    }

    private void uploadPDF(Uri data, String fileName) {

//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setTitle("File Uploading...");
//        pd.show();
        CustomProgressDialog customProgressDialog = new CustomProgressDialog(CVActivity.this);
        customProgressDialog.show();

        final StorageReference reference = storageReference.child("uploads/"+ System.currentTimeMillis() + ".pdf");
        // store in upload folder of the Firebase Storage
        reference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isComplete());
                        Uri uri = uriTask.getResult();

                        FileInModel fileInModel = new FileInModel(userId, fileName, uri.toString()); // Get the views from the model class
                        databaseReference.child(userId).setValue(fileInModel); // Push the value into realtime database
                        StyleableToast.makeText(CVActivity.this, "Upload berhasil", Toast.LENGTH_LONG, R.style.mytoast).show();
                        customProgressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    }
                });
    }
    private void hideProgressBar(){
        binding.loLoad.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    private void showProgressBar(){
        binding.loLoad.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}