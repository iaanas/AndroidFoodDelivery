package ru.androidtestapp.androidfooddelivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

import io.paperdb.Paper;
import ru.androidtestapp.androidfooddelivery.Common.Common;
import ru.androidtestapp.androidfooddelivery.Model.User;

public class SignIn extends AppCompatActivity {
	EditText edtPhone, edtPassword;
	Button btnSignIn;
	CheckBox ckbRemember;
	TextView txtForgotPwd;
	FirebaseDatabase database;
	DatabaseReference table_user;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_sign_in );
		
		edtPassword = ( MaterialEditText ) findViewById( R.id.edtPassword );
		edtPhone = (MaterialEditText) findViewById( R.id.edtPhone );
		btnSignIn = findViewById( R.id.btnSignIn );
		ckbRemember = (CheckBox) findViewById( R.id.ckbRemember );
		txtForgotPwd = (TextView ) findViewById( R.id.txtForgotPwd );
		
		Paper.init( this );
		
		//Init FireBase
		database = FirebaseDatabase.getInstance();
		table_user = database.getReference("User");
		
		txtForgotPwd.setOnClickListener( new View.OnClickListener( ) {
			@Override
			public void onClick( View v ) {
				showForgotPwdDialog();
			}
		} );
		
		btnSignIn.setOnClickListener( new View.OnClickListener( ) {
			@Override
			public void onClick( View v ) {
				
				if(Common.isConnectedToInternet( getBaseContext() )){
					
					//Save user & password
					if(ckbRemember.isChecked()){
						Paper.book().write( Common.USER_KEY, edtPhone.getText().toString() );
						Paper.book().write( Common.PWD_KEY, edtPassword.getText().toString() );
					}
					
					final ProgressDialog mDialog = new ProgressDialog( SignIn.this );
					mDialog.setMessage( "Please waiting... " );
					mDialog.show();
					
					table_user.addValueEventListener( new ValueEventListener( ) {
						@Override
						public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
							//Check if user not exist in database
							if(dataSnapshot.child( edtPhone.getText().toString() ).exists()) {
								
								//Get User information
								mDialog.dismiss( );
								User user = dataSnapshot.child( edtPhone.getText( ).toString( ) ).getValue( User.class );
								user.setPhone( edtPhone.getText().toString() );
								
								if ( Objects.requireNonNull( user ).getPassword( ).equals( edtPassword.getText( ).toString( ) ) ) {
									
									Intent homeIntent = new Intent( SignIn.this, Home.class );
									Common.currentUser = user;
									startActivity( homeIntent );
									finish();
									
									table_user.removeEventListener( this );
									
									
								} else {
									Toast.makeText( SignIn.this , "Не верен номер телефона или пароль" ,
											Toast.LENGTH_SHORT ).show( );
								}
							} else {
								mDialog.dismiss();
								Toast.makeText( SignIn.this , "Пользователь не зарегистрирован" ,
										Toast.LENGTH_SHORT ).show( );
							}
							
						}
						
						@Override
						public void onCancelled( @NonNull DatabaseError databaseError ) {
						
						}
					} );
				}
				else {
					Toast.makeText( SignIn.this, "Please check your connection !!!", Toast.LENGTH_SHORT ).show();
				}
			}
		} );
	}
	
	private void showForgotPwdDialog( ) {
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setTitle( "Забыли пароль?" );
		builder.setMessage( "Введите свой секретный код" );
		
		LayoutInflater inflater = this.getLayoutInflater();
		View forgot_view = inflater.inflate(R.layout.forgot_password_layout, null);
		
		builder.setView( forgot_view );
		builder.setIcon( R.drawable.ic_security_black_24dp );
		
		final MaterialEditText edtPhone = (MaterialEditText) forgot_view.findViewById( R.id.edtPhone );
		final MaterialEditText edtSecureCode = (MaterialEditText) forgot_view.findViewById( R.id.edtSecureCode );
		
		builder.setPositiveButton( "ДА" , new DialogInterface.OnClickListener( ) {
			@Override
			public void onClick( DialogInterface dialog , int which ) {
				table_user.addListenerForSingleValueEvent( new ValueEventListener( ) {
					@Override
					public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
						User user = dataSnapshot.child( edtPhone.getText().toString() )
								.getValue(User.class);
						if(user.getSecureCode().equals( edtSecureCode.getText().toString() )){
							Toast.makeText( SignIn.this, "Ваш пароль: "+user.getPassword(), Toast.LENGTH_LONG ).show();
						} else {
							Toast.makeText( SignIn.this, "Извините, секретный код не верен ...", Toast.LENGTH_LONG ).show();
						}
					}
					
					@Override
					public void onCancelled( @NonNull DatabaseError databaseError ) {
					
					}
				} );
			}
		} );
		builder.setNegativeButton( "НЕТ" , new DialogInterface.OnClickListener( ) {
			@Override
			public void onClick( DialogInterface dialog , int which ) {
			
			}
		} );
		builder.show();
		
	}
}
