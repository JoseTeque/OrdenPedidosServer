package m.google.ordenpedidosserver;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import info.hoang8f.widget.FButton;
import m.google.ordenpedidosserver.ViewHolder.BannerViewHolder;
import m.google.ordenpedidosserver.ViewHolder.FoodViewHolder;
import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.model.Banner;
import m.google.ordenpedidosserver.model.Food;

public class BannerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CoordinatorLayout coordinatorLayout;

    private FloatingActionButton fb;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerOptions<Banner> options;
    private FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    //add new Banner
    //new Food layout
    private MaterialEditText edtxnombre,edtxFooid;
    private FButton btnSelect,btnUpload;

    private Banner newBanner;
    private Uri saveUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        //Firebase
        database= FirebaseDatabase.getInstance();
        databaseReference= database.getReference("Restaurantes").child(Common.currentUser.getRestaurantId()).child("Banner");
        storage= FirebaseStorage.getInstance();
        storageReference= storage.getReference();

        //Init View
        recyclerView=findViewById(R.id.IdRecyclerBanner);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        coordinatorLayout=findViewById(R.id.IdCoordina);

        fb=findViewById(R.id.Id_BtnFloLista);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBanner();
            }
        });

        loadListBanner();
    }

    private void loadListBanner() {
        options= new FirebaseRecyclerOptions.Builder<Banner>().setQuery(databaseReference,Banner.class).build();

        adapter= new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder holder, int position, @NonNull Banner model) {
                holder.txtNameBanner.setText(model.getName());
                Picasso.get().load(model.getImagen()).into(holder.imageView);

            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.banner_layout,viewGroup,false);
                return new BannerViewHolder(view);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    private void showAddBanner() {

        AlertDialog.Builder dialog= new AlertDialog.Builder(BannerActivity.this);
        dialog.setTitle("Add new Banner..");
        dialog.setMessage("Please fill full information..");

        LayoutInflater inflater= this.getLayoutInflater();
        View add_food_layout= inflater.inflate(R.layout.add_new_banner,null);

        edtxnombre=add_food_layout.findViewById(R.id.Id_food_Name);
        edtxFooid=add_food_layout.findViewById(R.id.Id_food_Id);

        btnSelect=add_food_layout.findViewById(R.id.IdbtnSelectBanner);
        btnUpload=add_food_layout.findViewById(R.id.IdbtnUploadBanner);

        dialog.setView(add_food_layout);
        dialog.setIcon(R.drawable.ic_laptop_black_24dp);
        
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShooseImage();
            }
        });
        
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });


        dialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              if (newBanner!=null) {
                  databaseReference.push().setValue(newBanner);
                  Snackbar.make(coordinatorLayout, "New food" + newBanner.getName() + "Fue añadido ", Snackbar.LENGTH_SHORT).show();
                  loadListBanner();
              }


            }
        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
             dialog.dismiss();
             newBanner=null;
             loadListBanner();
            }
        });

        dialog.show();
    }

    private void UploadImage() {
        if (saveUri!=null){
            final ProgressDialog dialog= new ProgressDialog(BannerActivity.this);
            dialog.setMessage("Cargando...!");
            dialog.show();

            String imageNombre= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("image/"+ imageNombre);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Subido...!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @TargetApi(Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for new category if image upload and we can get download link
                                    newBanner= new Banner();
                                    newBanner.setName(Objects.requireNonNull(edtxnombre.getText()).toString());
                                    newBanner.setId(Objects.requireNonNull(edtxFooid.getText()).toString());
                                    newBanner.setImagen(uri.toString());

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BannerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress= (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                    dialog.setMessage("subido"+ progress+"%");
                }
            });
        }
    }

    private void ShooseImage() {
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"SELECT PICTURE"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData() !=null){
            saveUri= data.getData();
            btnSelect.setText("Image Selected..!");

        }
    }

    @Override
    protected void onStop() {
        if (adapter!=null)
            adapter.stopListening();
        super.onStop();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
            ShowUpdateBannerDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if(item.getTitle().equals(Common.DELETE)){
            DeleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void ShowUpdateBannerDialog(final String key, final Banner item) {

        AlertDialog.Builder dialog= new AlertDialog.Builder(BannerActivity.this);
        dialog.setTitle("Edit Banner..");
        dialog.setMessage("Please fill full information..");

        LayoutInflater inflater= this.getLayoutInflater();
        View add_food_layout= inflater.inflate(R.layout.add_new_banner,null);

        edtxnombre=add_food_layout.findViewById(R.id.Id_food_Name);
        edtxFooid=add_food_layout.findViewById(R.id.Id_food_Id);

        btnSelect=add_food_layout.findViewById(R.id.IdbtnSelectBanner);
        btnUpload=add_food_layout.findViewById(R.id.IdbtnUploadBanner);

        dialog.setView(add_food_layout);
        dialog.setIcon(R.drawable.ic_laptop_black_24dp);

        //Nombre for default

        edtxnombre.setText(item.getName());
        edtxFooid.setText(item.getId());

        //Event for button

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShooseImage();// let user select image from gallery and save Uri of this image
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeImageFood(item);
            }
        });

        //set button
        dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //update Information

                item.setName(edtxnombre.getText().toString());
                item.setId(edtxFooid.getText().toString());

                //make update
                Map<String,Object> update= new HashMap<>();
                update.put("id",item.getId());
                update.put("imagen",item.getImagen());
                update.put("name",item.getName());

                databaseReference.child(key).updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Snackbar.make(coordinatorLayout, "UPDATE" + item.getName() + "Fue modificado ", Snackbar.LENGTH_SHORT).show();
                        loadListBanner();
                    }
                });
            }
        });



        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                loadListBanner();
            }
        });

        dialog.show();

    }

    private void DeleteCategory(String key) {
        databaseReference.child(key).removeValue();
        Toast.makeText(this, "Item Delete..!!", Toast.LENGTH_SHORT).show();
    }

    private void ChangeImageFood(final Banner item){

        if (saveUri!=null){
            final ProgressDialog dialog= new ProgressDialog(BannerActivity.this);
            dialog.setMessage("Cargando...!");
            dialog.show();

            String imageNombre= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("image/"+ imageNombre);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Subido...!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for new category if image upload and we can get download link
                                    item.setImagen(uri.toString());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BannerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress= (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                    dialog.setMessage("subido"+ progress+"%");
                }
            });
        }

    }
}
