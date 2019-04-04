package m.google.ordenpedidosserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import m.google.ordenpedidosserver.ViewHolder.FoodViewHolder;
import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.interfaz.InterfaceClickListener;
import m.google.ordenpedidosserver.model.Category;
import m.google.ordenpedidosserver.model.Food;

public class FoodListActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CoordinatorLayout coordinatorLayout;

    private FloatingActionButton fb;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerOptions<Food> options;
    private FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    //new Food layout
    private MaterialEditText edtxnombrefood,edtxDescription,edtxPrecio,edtxDescuento;
    private Button btnSelectFood,btnUploadFood;

    private Food newFood;
    private Uri saveUri;


    String categoriaId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        database= FirebaseDatabase.getInstance();
        databaseReference= database.getReference("Restaurantes").child(Common.currentUser.getRestaurantId()).child("Detalles").child("Foods");
        storage= FirebaseStorage.getInstance();
        storageReference= storage.getReference();

        //Init View

        recyclerView=findViewById(R.id.IdRecyclerListaFood);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        coordinatorLayout=findViewById(R.id.IdCoordina);

        fb=findViewById(R.id.Id_BtnFloLista);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showAddFoodDialog();
            }
        });

        //Inten
        if (getIntent() != null)
            categoriaId = getIntent().getStringExtra("CategoriaId");
        if (!categoriaId.isEmpty()) {
            loadListaFood(categoriaId);
        }
    }

    private void showAddFoodDialog() {
        AlertDialog.Builder dialog= new AlertDialog.Builder(FoodListActivity.this);
        dialog.setTitle("Add new food..");
        dialog.setMessage("Please fill full information..");

        LayoutInflater inflater= this.getLayoutInflater();
        View add_food_layout= inflater.inflate(R.layout.add_new_food_layout,null);

        edtxnombrefood=add_food_layout.findViewById(R.id.Id_edt_nameFood);
        edtxDescription=add_food_layout.findViewById(R.id.Id_edt_Description);
        edtxPrecio=add_food_layout.findViewById(R.id.Id_edt_Precio);
        edtxDescuento=add_food_layout.findViewById(R.id.Id_edt_Descuento);
        btnSelectFood=add_food_layout.findViewById(R.id.IdbtnSelectFoodList);
        btnUploadFood=add_food_layout.findViewById(R.id.IdbtnUploadFoodLis);

        dialog.setView(add_food_layout);
        dialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);



        //Event for button

        btnSelectFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShooseImage();// let user select image from gallery and save Uri of this image
            }
        });

        btnUploadFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });

        //set button
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (newFood!=null){
                    databaseReference.push().setValue(newFood);
                    Snackbar.make(coordinatorLayout,"New food" + newFood.getName() + "Fue añadido ",Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void UploadImage() {
        if (saveUri!=null){
            final ProgressDialog dialog= new ProgressDialog(FoodListActivity.this);
            dialog.setMessage("Cargando...!");
            dialog.show();

            String imageNombre= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("image/"+ imageNombre);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "Subido...!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for new category if image upload and we can get download link
                                    newFood= new Food();
                                    newFood.setName(edtxnombrefood.getText().toString());
                                    newFood.setImage(uri.toString());
                                    newFood.setDescription(edtxDescription.getText().toString());
                                    newFood.setPrice(edtxPrecio.getText().toString());
                                    newFood.setDiscount(edtxDescuento.getText().toString());
                                    newFood.setMenuId(categoriaId);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FoodListActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void loadListaFood(String categoriaId) {
        options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(databaseReference.orderByChild("menuId").equalTo(categoriaId), Food.class).build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.txtNameFoodList.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.imageView);

                holder.setClickListener(new InterfaceClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLognClick) {

                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item, viewGroup, false);
                return new FoodViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData() !=null){
            saveUri= data.getData();
            btnSelectFood.setText("Image Selected..!");

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
            ShowUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if(item.getTitle().equals(Common.DELETE)){
            DeleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void DeleteCategory(String key) {
        databaseReference.child(key).removeValue();
        Toast.makeText(this, "Item Delete..!!", Toast.LENGTH_SHORT).show();
    }

    private void ShowUpdateFoodDialog(final String key, final Food item) {
        AlertDialog.Builder dialog= new AlertDialog.Builder(FoodListActivity.this);
        dialog.setTitle("Edit food..");
        dialog.setMessage("Please fill full information..");

        LayoutInflater inflater= this.getLayoutInflater();
        View add_food_layout= inflater.inflate(R.layout.add_new_food_layout,null);

        edtxnombrefood=add_food_layout.findViewById(R.id.Id_edt_nameFood);
        edtxDescription=add_food_layout.findViewById(R.id.Id_edt_Description);
        edtxPrecio=add_food_layout.findViewById(R.id.Id_edt_Precio);
        edtxDescuento=add_food_layout.findViewById(R.id.Id_edt_Descuento);
        btnSelectFood=add_food_layout.findViewById(R.id.IdbtnSelectFoodList);
        btnUploadFood=add_food_layout.findViewById(R.id.IdbtnUploadFoodLis);

        dialog.setView(add_food_layout);
        dialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);


        //Nombre for default

        edtxnombrefood.setText(item.getName());
        edtxDescription.setText(item.getDescription());
        edtxDescuento.setText(item.getDiscount());
        edtxPrecio.setText(item.getPrice());


        //Event for button

        btnSelectFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShooseImage();// let user select image from gallery and save Uri of this image
            }
        });

        btnUploadFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeImageFood(item);
            }
        });

        //set button
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //update Information

                item.setName(edtxnombrefood.getText().toString());
                databaseReference.child(key).setValue(item);
            }
        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void ChangeImageFood(final Food item){

        if (saveUri!=null){
            final ProgressDialog dialog= new ProgressDialog(FoodListActivity.this);
            dialog.setMessage("Cargando...!");
            dialog.show();

            String imageNombre= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("image/"+ imageNombre);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "Subido...!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for new category if image upload and we can get download link
                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FoodListActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
