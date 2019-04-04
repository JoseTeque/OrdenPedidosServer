package m.google.ordenpedidosserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m.google.ordenpedidosserver.ViewHolder.OrderViewHolder;
import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.model.DataMessage;
import m.google.ordenpedidosserver.model.MyResponse;
import m.google.ordenpedidosserver.model.Requests;
import m.google.ordenpedidosserver.model.Shipper;
import m.google.ordenpedidosserver.model.Token;
import m.google.ordenpedidosserver.remote.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatusActivity extends AppCompatActivity{

    private MaterialSpinner spinner, ShipperSpinner;
    private RecyclerView recyclerView_status;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private FirebaseRecyclerOptions<Requests> adapterOptions;
    private FirebaseRecyclerAdapter<Requests, OrderViewHolder> adapter;

    ApiService myService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        // init service

        myService= Common.getFCMservice();

        //firebase
        database=FirebaseDatabase.getInstance();
        databaseReference=database.getReference("Restaurantes").child(Common.currentUser.getRestaurantId()).child("Requests");

        recyclerView_status= findViewById(R.id.Id_RecyclerStatus);
        recyclerView_status.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView_status.setLayoutManager(layoutManager);

        loadOrder();
    }

    private void loadOrder() {

        adapterOptions= new FirebaseRecyclerOptions.Builder<Requests>()
                .setQuery(databaseReference, Requests.class).build();

        adapter = new FirebaseRecyclerAdapter<Requests, OrderViewHolder>(adapterOptions) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, final int position, @NonNull final Requests model) {

                holder.OrderName.setText(adapter.getRef(position).getKey());
                holder.OrderPhone.setText(model.getPhone());
                holder.OrderAddress.setText(model.getAddress());
                holder.OrderStatus.setText(Common.converCodeToStatus(model.getStatus()));
                holder.txtDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));

                holder.btnEditar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });

                holder.btnRemover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteCategory(adapter.getRef(position).getKey());
                    }
                });

                holder.btnDetalles.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent orderDetail = new Intent(OrderStatusActivity.this, OrderDetailActivity.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

             holder.btnDireccion.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent OrderStatusIntent = new Intent(OrderStatusActivity.this, TrackingOrderActivity.class);
                     Common.currentRequest = model;
                     startActivity(OrderStatusIntent);
                 }
             });

            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_layout,viewGroup,false);
                return new OrderViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView_status.setAdapter(adapter);
    }

    private void DeleteCategory(String key) {
        databaseReference.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void ShowUpdateDialog(String key, final Requests item) {
        final AlertDialog.Builder dialog= new AlertDialog.Builder(OrderStatusActivity.this);
        dialog.setTitle("Update Order");
        dialog.setMessage("Please choose status..");

        LayoutInflater inflater= this.getLayoutInflater();
        final View view= inflater.inflate(R.layout.update_order_layout,null);

        spinner=view.findViewById(R.id.IdStatusSpinner);
        spinner.setItems("Placed","On may way","Shipping");

        ShipperSpinner= view.findViewById(R.id.IdShipperSpinner);

        //load all shipper phone to spinner

        final List<String> shipperList= new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Shippers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    shipperList.add(snapshot.getKey());
                    ShipperSpinner.setItems(shipperList);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dialog.setView(view);

        final String localKey= key;

        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                if (item.getStatus().equals("2"))
                {
                    FirebaseDatabase.getInstance()
                            .getReference("OrderNeedShip").child(ShipperSpinner.getItems().get(ShipperSpinner.getSelectedIndex()).toString())
                            .child(localKey)
                            .setValue(item);


                    databaseReference.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged(); //ADD  TO UPDATE ITEM SIZE

                    SendOrderStatusToUse(localKey,item);
                    SendOrderShipperRequestToShipper(ShipperSpinner.getItems().get(ShipperSpinner.getSelectedIndex()).toString());

                }
                else {

                    databaseReference.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged(); //ADD  TO UPDATE ITEM SIZE

                    SendOrderStatusToUse(localKey,item);

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

    private void SendOrderShipperRequestToShipper(String shipperPhone) {

        databaseReference= database.getReference("Tokens");
        databaseReference.child(shipperPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists())
               {
                   Token token= dataSnapshot.getValue(Token.class);

                   Map<String,String> datasend= new HashMap<>();
                   datasend.put("Title","Jose Dev");
                   datasend.put("Message","Your have new order need ship"  );

                   DataMessage dataMessage= new DataMessage(token.getToken(),datasend);


                   myService.sendNotification(dataMessage).enqueue(new Callback<MyResponse>() {
                       @Override
                       public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                           if (response.body().succes==1 )
                           {
                               Toast.makeText(OrderStatusActivity.this, "Send to shipper", Toast.LENGTH_SHORT).show();
                               finish();
                           }
                           else
                           {
                               Toast.makeText(OrderStatusActivity.this, " failed to send notification ..!!", Toast.LENGTH_SHORT).show();
                           }
                       }

                       @Override
                       public void onFailure(Call<MyResponse> call, Throwable t) {
                           Log.e("ERROR",t.getMessage());
                       }
                   });
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendOrderStatusToUse(final String key, final Requests item) {

        databaseReference= database.getReference("Tokens");
        databaseReference.child(item.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    Token token= dataSnapshot.getValue(Token.class);

                    //make raw payload
                    //Notificatione notification= new Notificatione("Jose Dev", "Your order " + key + "was update"  );
                    //Sender content= new Sender(token.getToken(),notification);

                    Map<String,String> datasend= new HashMap<>();
                    datasend.put("Title","Jose Dev");
                    datasend.put("Message","Your order " + key + "was update"  );

                    DataMessage dataMessage= new DataMessage(token.getToken(),datasend);


                    myService.sendNotification(dataMessage).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.body().succes==1 )
                            {
                                Toast.makeText(OrderStatusActivity.this, "Order was update", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else
                            {
                                Toast.makeText(OrderStatusActivity.this, "Order was update but failed to send notification ..!!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                            Log.e("ERROR",t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
