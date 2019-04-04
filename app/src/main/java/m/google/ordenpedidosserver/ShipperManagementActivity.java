package m.google.ordenpedidosserver;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import m.google.ordenpedidosserver.ViewHolder.ShipperViewHolder;
import m.google.ordenpedidosserver.interfaz.InterfaceClickListener;
import m.google.ordenpedidosserver.model.Shipper;

public class ShipperManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private FirebaseRecyclerOptions<Shipper> options;
    private FirebaseRecyclerAdapter<Shipper, ShipperViewHolder> adapter;

    private FloatingActionButton fb;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_management);

        //Init Floating and view

        fb= findViewById(R.id.fav_addShipper);
        recyclerView= findViewById(R.id.IdRecyclerShipper);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Init firebase

        database= FirebaseDatabase.getInstance();
        reference= database.getReference("Shippers");

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateShipperLayout();
            }
        });

        loadAllShippers();

    }

    private void loadAllShippers() {

        options= new FirebaseRecyclerOptions.Builder<Shipper>()
                .setQuery(reference,Shipper.class).build();

        adapter= new FirebaseRecyclerAdapter<Shipper, ShipperViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ShipperViewHolder holder, final int position, @NonNull final Shipper model) {
                holder.txtName.setText(model.getName());
                holder.txtPhone.setText(model.getPhone());

                holder.btnEditar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowEditDialog(adapter.getRef(position).getKey(),model);
                    }
                });

                holder.btnRemover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         ShowRemoveDialog(adapter.getRef(position).getKey());
                    }
                });

                holder.setClickListener(new InterfaceClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLognClick) {

                    }
                });
            }

            @NonNull
            @Override
            public ShipperViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.shipper_layout,viewGroup,false);
                return new ShipperViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);



    }

    private void ShowEditDialog(final String key, Shipper model) {

        AlertDialog.Builder dialog= new AlertDialog.Builder(ShipperManagementActivity.this);
        dialog.setTitle("Update Shipper");

        LayoutInflater inflater= this.getLayoutInflater();
        View view= inflater.inflate(R.layout.crear_shipper_layout,null);

        final MaterialEditText edtname= view.findViewById(R.id.IdedtxNameshipperAccount);
        final MaterialEditText edtphone= view.findViewById(R.id.IdPhoneAccountShipper);
        final MaterialEditText edtPassword= view.findViewById(R.id.IdPasswordAccountShipper);

        edtname.setText(model.getName());
        edtphone.setText(model.getPhone());
        edtPassword.setText(model.getPassword());

        dialog.setView(view);
        dialog.setIcon(R.drawable.ic_local_shipping_black_24dp);

        dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Map<String,Object> update= new HashMap<>();
                update.put("name",edtname.getText().toString());
                update.put("phone",edtphone.getText().toString());
                update.put("password",edtPassword.getText().toString());



                reference.child(edtphone.getText().toString()).updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShipperManagementActivity.this, "Se actualizo shipper", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagementActivity.this, "No se actualizo shipper", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void  ShowRemoveDialog(String key) {

        reference.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ShipperManagementActivity.this, "Se removio shippers", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShipperManagementActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void showCreateShipperLayout() {
        AlertDialog.Builder dialog= new AlertDialog.Builder(ShipperManagementActivity.this);
        dialog.setTitle("Create Shipper");

        LayoutInflater inflater= this.getLayoutInflater();
        View view= inflater.inflate(R.layout.crear_shipper_layout,null);

        final MaterialEditText edtname= view.findViewById(R.id.IdedtxNameshipperAccount);
        final MaterialEditText edtphone= view.findViewById(R.id.IdPhoneAccountShipper);
        final MaterialEditText edtPassword= view.findViewById(R.id.IdPasswordAccountShipper);

        dialog.setView(view);
        dialog.setIcon(R.drawable.ic_local_shipping_black_24dp);

        dialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                Shipper shipper= new Shipper();
                shipper.setName(edtname.getText().toString());
                shipper.setPhone(edtphone.getText().toString());
                shipper.setPassword(edtPassword.getText().toString());

                reference.child(edtphone.getText().toString()).setValue(shipper).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShipperManagementActivity.this, "Shipper Creado", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagementActivity.this, "Shipper no creado" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
            }
        });

        dialog.show();

    }

    @Override
    protected void onStop() {
        if (adapter!=null)
            adapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter!=null)
            adapter.startListening();
    }
}
