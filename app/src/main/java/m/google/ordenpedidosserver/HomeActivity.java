package m.google.ordenpedidosserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import m.google.ordenpedidosserver.ViewHolder.MenuViewHolder;
import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.interfaz.InterfaceClickListener;
import m.google.ordenpedidosserver.model.Category;
import m.google.ordenpedidosserver.model.Token;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView txtNameUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerOptions<Category> options;
    private FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    //new menu layout
    private MaterialEditText edtx_nombre;
    private Button btnSelect, btnUpload;

    private Category newCategory;
    private Uri saveUri;


    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Gestion de menu");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDialog();
            }
        });

         drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_viewHome);
        navigationView.setNavigationItemSelectedListener(this);

        //set name for user
        View headerView= navigationView.getHeaderView(0);
        txtNameUser=headerView.findViewById(R.id.IdTxtNameHeader);
        txtNameUser.setText(Common.currentUser.getNombre());// accediendo al nombre de la persona actual

        //Firebase
        database= FirebaseDatabase.getInstance();
        databaseReference= database.getReference("Restaurantes").child(Common.currentUser.getRestaurantId()).child("Detalles").child("categoria");
        storage= FirebaseStorage.getInstance();
        storageReference= storage.getReference();

        //init recyclerview
        recyclerView= findViewById(R.id.IdRecyclerMenu);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // cargar menu

        loadMenu();

        //send token

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {

                String token= instanceIdResult.getToken();
                  UpdateToken(token);
            }
        });
    }

    private void UpdateToken(String tokenrefresh) {
        FirebaseDatabase DB= FirebaseDatabase.getInstance();
        DatabaseReference tokens= DB.getReference("Tokens");

        Token token= new Token(tokenrefresh,true);//false because this tokensend from client app
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }

    private void ShowDialog() {

        AlertDialog.Builder dialog= new AlertDialog.Builder(HomeActivity.this);
        dialog.setTitle("Add new category..");
        dialog.setMessage("Please fill full information..");

        LayoutInflater inflater= this.getLayoutInflater();
        View add_menu_layout= inflater.inflate(R.layout.add_new_menu_layout,null);

        edtx_nombre=add_menu_layout.findViewById(R.id.Id_edt_name);
        btnSelect=add_menu_layout.findViewById(R.id.IdbtnSelect);
        btnUpload=add_menu_layout.findViewById(R.id.IdbtnUpload);

        dialog.setView(add_menu_layout);
        dialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);



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
                UploadImage();
            }
        });

        //set button
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();

              if (newCategory!=null){
                   databaseReference.push().setValue(newCategory);
                   Snackbar.make(drawer,"New Category" + newCategory.getName() + "Fue añadido ",Snackbar.LENGTH_SHORT).show();
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
            final ProgressDialog dialog= new ProgressDialog(HomeActivity.this);
            dialog.setMessage("Cargando...!");
            dialog.show();

            String imageNombre= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("image/"+ imageNombre);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "Subido...!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for new category if image upload and we can get download link
                                    newCategory= new Category(edtx_nombre.getText().toString(),uri.toString());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    //PRES CTRL+O


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData() !=null){
            saveUri= data.getData();
            btnSelect.setText("Image Selected..!");

        }
    }

    private void ShooseImage() {

        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"SELECT PICTURE"), Common.PICK_IMAGE_REQUEST);
    }

    private void loadMenu() {
       options= new FirebaseRecyclerOptions.Builder<Category>()
               .setQuery(databaseReference, Category.class).build();

       adapter= new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
           @Override
           protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Category model) {
            holder.txtNameMenu.setText(model.getName());
            Picasso.get().load(model.getImagen()).into(holder.imageView);

            holder.setClickListener(new InterfaceClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLognClick) {
                        //send category Id and Star new activity
                       Intent foodlistacTivity= new Intent(HomeActivity.this,FoodListActivity.class);
                       foodlistacTivity.putExtra("CategoriaId",adapter.getRef(position).getKey());
                       startActivity(foodlistacTivity);

                }
            });

           }

           @NonNull
           @Override
           public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

               View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.memu_item,viewGroup,false);
               return new MenuViewHolder(view);
           }
       };

       adapter.startListening();
       adapter.notifyDataSetChanged();
       recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {

        } else if (id == R.id.nav_orders) {

            Intent intentOrder= new Intent(HomeActivity.this,OrderStatusActivity.class);
            startActivity(intentOrder);

        } else if (id == R.id.nav_logout) {

        }else if (id == R.id.nav_banner) {
            Intent intentOrder= new Intent(HomeActivity.this,BannerActivity.class);
            startActivity(intentOrder);

        }else if (id == R.id.nav_message) {
            Intent intentOrder= new Intent(HomeActivity.this,MessageActivity.class);
            startActivity(intentOrder);

        }else if (id == R.id.nav_shipper) {
            Intent intentOrder= new Intent(HomeActivity.this,ShipperManagementActivity.class);
            startActivity(intentOrder);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Update / Delete


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {
            ShowUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if(item.getTitle().equals(Common.DELETE)){
            DeleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);


    }

    private void DeleteCategory(String key) {

        //first, we need get all food in category
        DatabaseReference reference= database.getReference("Foods");
        Query foodInCategory= reference.orderByChild("menuId").equalTo(key);
        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    postSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        databaseReference.child(key).removeValue();
        Toast.makeText(this, "Item Delete..!!", Toast.LENGTH_SHORT).show();
    }

    private void ShowUpdateDialog(final String key, final Category item) {
        //copy and paste the showDialog

            AlertDialog.Builder dialog= new AlertDialog.Builder(HomeActivity.this);
            dialog.setTitle("Update category..");
            dialog.setMessage("Please fill full information..");

            LayoutInflater inflater= this.getLayoutInflater();
            View add_menu_layout= inflater.inflate(R.layout.add_new_menu_layout,null);

            edtx_nombre=add_menu_layout.findViewById(R.id.Id_edt_name);
            btnSelect=add_menu_layout.findViewById(R.id.IdbtnSelect);
            btnUpload=add_menu_layout.findViewById(R.id.IdbtnUpload);

            dialog.setView(add_menu_layout);
            dialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

            //Nombre for default

        edtx_nombre.setText(item.getName());

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
                    ChangeImage(item);
                }
            });

            //set button
            dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                   //update Information

                    item.setName(edtx_nombre.getText().toString());
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

    private void ChangeImage(final Category item) {

        if (saveUri!=null){
            final ProgressDialog dialog= new ProgressDialog(HomeActivity.this);
            dialog.setMessage("Cargando...!");
            dialog.show();

            String imageNombre= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("image/"+ imageNombre);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "Subido...!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

