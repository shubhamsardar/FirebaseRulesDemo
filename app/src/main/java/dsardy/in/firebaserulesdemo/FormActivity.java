package dsardy.in.firebaserulesdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;


public class FormActivity extends AppCompatActivity implements AddImage, EasyImagePickUP.ImagePickerListener {

    EditText name, age;
    Button submit;
    String uid;
    FirebaseFirestore db;
    PartnerPojo partnerPojo;
    private FirebaseAuth mAuth;
    boolean canSubmit = true;
    boolean areImagesUploaded = false;
    RecyclerView recyclerView;
    List<ImageData> images;
    List<Uri> imagesUriList;
    EasyImagePickUP easyImagePickUP;
    ImagesRecyclarAdapter imagesRecyclarAdapter;
    int position;
    private StorageReference mStorageRef;
    ProgressDialog progressDialog;
    List<String> mUrlList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        easyImagePickUP = new EasyImagePickUP(this);
        imagesUriList = new ArrayList<>();


        uid = mAuth.getUid();
        name = findViewById(R.id.editTextName);
        age = findViewById(R.id.editTextAge);
        submit = findViewById(R.id.submit);


        recyclerView = findViewById(R.id.recyclarview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mUrlList = new ArrayList<>();
        images = new ArrayList<>();
        images.add(new ImageData());
        images.add(new ImageData());
        images.add(new ImageData());
        imagesRecyclarAdapter = new ImagesRecyclarAdapter(images, this, this);




        // mAuth.signOut(); //if we sign out the user no data is allowed to read.


        db.collection("partners").document(uid).addSnapshotListener(FormActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                //Log.e("onEvent",e.getMessage());

                if (documentSnapshot.exists()) {
                    partnerPojo = documentSnapshot.toObject(PartnerPojo.class);
                    if (partnerPojo != null) {
                        name.setText(partnerPojo.getmName());
                        age.setText(partnerPojo.getmAge());


                        mUrlList = partnerPojo.getmList();
                        for(int i=0;i<mUrlList.size();i++){
                            images.get(i).setmImageUrl(mUrlList.get(i));
                        }
                        imagesRecyclarAdapter.notifyDataSetChanged();

                        
                        if (!partnerPojo.getVerified()) {
                            submit.setAlpha(0.5f);
                            canSubmit = false;
                        } else {
                            submit.setAlpha(1f);
                            canSubmit = true;
                        }
                    }
                } else {
                    partnerPojo = new PartnerPojo();
                }

            }
        });


        recyclerView.setAdapter(imagesRecyclarAdapter);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //upload to firebase databse
                partnerPojo.setmAge(age.getText().toString());
                partnerPojo.setmName(name.getText().toString());
                partnerPojo.setmList(mUrlList);

                // upload data to persons document
                if (areImagesUploaded) {
                    db.collection("partners").document(uid).set(partnerPojo);

                } else {
                    Log.e("tagg", "cant submit images not uploaded");

                }
                //db.collection("partners").document("4gnCtFGAihagMJhN29bi5QwpyL93").set(partnerPojo); //delhi wale ki uid
                // cant write on anothers document using there uid

            }
        });
    }

    public void signout(View view) {
        mAuth.signOut();
        finish();
    }

    @Override
    public void onPickClicked(int position) {
        easyImagePickUP.imagepicker(1);
        Log.e("tagg", "onPickClicked");
        this.position = position;

    }

    @Override
    public void onCancelClicked(int position) {
        Log.e("tagg", "onCancel");

        images.set(position, new ImageData());
        imagesRecyclarAdapter.notifyDataSetChanged();

    }

    @Override
    public void onPicked(int i, String s, Bitmap bitmap, Uri uri) {
        Log.e("tagg", "onPicked" + s);
        images.set(position, new ImageData(uri, bitmap));
        imagesUriList.add(uri);
        imagesRecyclarAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCropped(int i, String s, Bitmap bitmap, Uri uri) {
        Log.e("tagg", "onPicked" + s);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        easyImagePickUP.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        easyImagePickUP.request_permission_result(requestCode, permissions, grantResults);
    }

    public void uploadImages(View view) {
        //check images
        boolean areImagesSet = true;
        for (ImageData imageData : images) {
            if (!imageData.getSet()) {
                areImagesSet = false;
            }
        }
        if (areImagesSet) {
            //upload to firebase storage here
            progressDialog = new ProgressDialog(FormActivity.this);
            upload(0);

        } else {
            Log.e("tagg", "all images not set");
        }
    }

    void upload(final int index) {
        Uri file = images.get(index).getmImageUri();
        if (mAuth.getCurrentUser() != null) {
            StorageReference riversRef = mStorageRef.child(mAuth.getUid()).child(index + ".jpeg");
            progressDialog.setTitle("uploading image " + index);
            progressDialog.setCancelable(false);
            progressDialog.show();
            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            mUrlList.add(downloadUrl.toString());
                            progressDialog.hide();
                            if (index + 1 <= 2){
                                upload(index + 1);
                            }else {
                                areImagesUploaded = true;
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("onSuccessUpload..", exception.toString());

                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
    }
}
