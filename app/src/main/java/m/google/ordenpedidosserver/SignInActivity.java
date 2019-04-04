package m.google.ordenpedidosserver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.model.User;

public class SignInActivity extends AppCompatActivity {

    private MaterialEditText Edtx_phone, Edtx_password;
    private Button btnSign;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Edtx_phone= findViewById(R.id.IdPhone);
        Edtx_password= findViewById(R.id.IdPassword);
        btnSign= findViewById(R.id.IdSignIn);

        //Init firebase
        database= FirebaseDatabase.getInstance();
        databaseReference= database.getReference("user");

        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInUser(Edtx_phone.getText().toString(),Edtx_password.getText().toString());
            }
        });
    }

    private void SignInUser(String phone, final String password) {
        final ProgressDialog dialog= new ProgressDialog(SignInActivity.this);
        dialog.setMessage("Cargando...");
        dialog.show();

        final String localphone= phone;
        final String localpassword= password;


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localphone).exists()){
                    dialog.dismiss();

                    User user= dataSnapshot.child(localphone).getValue(User.class);
                    user.setPhone(localphone);
                    if (Boolean.parseBoolean(user.getIsStaff())) // if staff == true
                    {
                        if (user.getPassword().equals(localpassword)){
                            //Login ok
                            Intent HomeIntent= new Intent(SignInActivity.this, HomeActivity.class);
                            Common.currentUser= user;
                            startActivity(HomeIntent);
                            finish();
                        }else {
                            Toast.makeText(SignInActivity.this, "Password incorrecto.. ", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(SignInActivity.this, "por favor inicie sesión con la cuenta personal", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(SignInActivity.this, "El usario no existe en la base de datos", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
