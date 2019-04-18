package ru.androidtestapp.androidfooddelivery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import ru.androidtestapp.androidfooddelivery.Common.Common;
import ru.androidtestapp.androidfooddelivery.Database.Database;
import ru.androidtestapp.androidfooddelivery.Model.Food;
import ru.androidtestapp.androidfooddelivery.Model.Order;
import ru.androidtestapp.androidfooddelivery.Model.Rating;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {
	TextView food_name, food_price, food_description;
	ImageView food_image;
	CollapsingToolbarLayout collapsingToolbarLayout;
	FloatingActionButton btnCart, btnRating;
	ElegantNumberButton numberButton;
	
	RatingBar ratingBar;
	
	String foodId="";
	FirebaseDatabase database;
	DatabaseReference foods;
	DatabaseReference ratingTbl;
	
	Food currentFood;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_food_default);
		
//		Toolbar toolbar = (Toolbar ) findViewById( R.id.toolbar );
//		toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_black_24dp);
//		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent( FoodDetail.this, Home.class );
//				startActivity( intent );
//			}
//		});
		
		
		//FireBase
		database = FirebaseDatabase.getInstance();
		foods = database.getReference("Food");
		ratingTbl = database.getReference("Rating");
		
		//Init view
		numberButton = findViewById( R.id.number_button );
		
		btnCart = findViewById( R.id.btnCart );
		btnRating = (FloatingActionButton ) findViewById( R.id.btn_rating );
		ratingBar = (RatingBar ) findViewById( R.id.ratingBar );
		
		btnRating.setOnClickListener( new View.OnClickListener( ) {
			@Override
			public void onClick( View v ) {
				showRatingDialog();
			}
		} );
		
		btnCart.setOnClickListener( new View.OnClickListener( ) {
			@Override
			public void onClick( View v ) {
				new Database( getBaseContext() ).addToCart( new Order(
						foodId,
						currentFood.getName(),
						numberButton.getNumber(),
						currentFood.getPrice(),
						currentFood.getDiscount()
						
				) );
				Toast.makeText( FoodDetail.this, "Добавлен в бланк заказа",
						Toast.LENGTH_SHORT).show();
			}
		} );
		
		food_description = findViewById( R.id.food_description );
		food_name = findViewById( R.id.food_name );
		food_price = findViewById( R.id.food_price );
		food_image = findViewById( R.id.img_food );
		
		collapsingToolbarLayout = findViewById( R.id.collapsing );
		collapsingToolbarLayout.setExpandedTitleTextAppearance( R.style.ExpandedAppbar );
		collapsingToolbarLayout.setCollapsedTitleTextAppearance( R.style.CollapsedAppbar );
		
		//Get Food Id from Intent
		if(getIntent() != null){
			foodId = getIntent().getStringExtra( "FoodId" );
		}
		if(!foodId.isEmpty()) {
			if( Common.isConnectedToInternet( getBaseContext() ) ){
				getDetailFood(foodId);
				getRatingFood(foodId);
				
			} else {
				Toast.makeText( FoodDetail.this, "Пожалуйста, проверьте интернет-соединение!!!",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}
	
	private void getRatingFood( String foodId ) {
		Query foodRating = ratingTbl.orderByChild( "foodId" ).equalTo( foodId );
		
		foodRating.addValueEventListener( new ValueEventListener( ) {
			int count=0, sum=0;
			@Override
			public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
				for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
					Rating item = postSnapshot.getValue(Rating.class);
					sum+=Integer.parseInt( item.getRateValue() );
					count++;
				}
				if(count != 0){
					float average = sum/count;
					ratingBar.setRating( average );
				}
				
			}
			
			@Override
			public void onCancelled( @NonNull DatabaseError databaseError ) {
			
			}
		} );
		
	}
	
	private void showRatingDialog( ) {
		new AppRatingDialog.Builder()
				.setPositiveButtonText( "Отправить" )
				.setNegativeButtonText( "Закрыть" )
				.setNoteDescriptions( Arrays.asList( "Плохо", "Нормально", "Хорошо", "Очень хорошо", "Превосходно" ) )
				.setDefaultRating( 1 )
				.setTitle( "Рейтинг продукта" )
				.setDescription( "Пожалуйста, дайте совокупную оценку продукту" )
				.setTitleTextColor( R.color.colorPrimary )
				.setDescriptionTextColor( R.color.colorPrimary )
				.setHint( "Пожалуйста, оставьте здесь свой комментарий ... " )
				.setHintTextColor( R.color.colorAccent )
				.setCommentTextColor( android.R.color.white )
				.setCommentBackgroundColor( R.color.colorPrimaryDark )
				.setWindowAnimation( R.style.RatingDialogFadeAnim )
				.create( FoodDetail.this )
				.show();
	}
	
	private void getDetailFood( String foodId ) {
		foods.child( foodId ).addValueEventListener( new ValueEventListener( ) {
			@Override
			public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
				currentFood = dataSnapshot.getValue( Food.class );
				
				//Set Image
				Picasso.with( getBaseContext() ).load( currentFood.getImage() )
						.into( food_image );
				
				collapsingToolbarLayout.setTitle( "Lyncmed Russia" );
				
				food_price.setText( currentFood.getPrice() );
				food_name.setText( currentFood.getName() );
				food_description.setText( currentFood.getDescription() );
				
			}
			
			@Override
			public void onCancelled( @NonNull DatabaseError databaseError ) {
			
			}
		} );
	}
	
	@Override
	public void onNegativeButtonClicked( ) {
	
	}
	
	@Override
	public void onPositiveButtonClicked( int value , @NotNull String comments ) {
		final Rating rating = new Rating(Common.currentUser.getPhone(),
				foodId,
				String.valueOf( value ),
				comments);
		ratingTbl.child( Common.currentUser.getPhone() ).addValueEventListener( new ValueEventListener( ) {
			@Override
			public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
				if(dataSnapshot.child( Common.currentUser.getPhone() ).exists()){
					
					//Remove old value
					ratingTbl.child( Common.currentUser.getPhone() ).removeValue();
					//Update new value
					ratingTbl.child( Common.currentUser.getPhone() ).setValue( rating );
					
				} else {
					//Update new value
					ratingTbl.child( Common.currentUser.getPhone() ).setValue( rating );
				}
				Toast.makeText( FoodDetail.this, "Спасибо за Ваш отзыв!", Toast.LENGTH_SHORT ).show();
			}
			
			@Override
			public void onCancelled( @NonNull DatabaseError databaseError ) {
			
			}
		} );
	}
	
}
