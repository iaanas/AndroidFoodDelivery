package ru.androidtestapp.androidfooddelivery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ru.androidtestapp.ViewHolder.FoodViewHolder;
import ru.androidtestapp.androidfooddelivery.Common.Common;
import ru.androidtestapp.androidfooddelivery.Database.Database;
import ru.androidtestapp.androidfooddelivery.Intarface.ItemClickListener;
import ru.androidtestapp.androidfooddelivery.Model.Food;

public class FoodList extends AppCompatActivity {
	
	RecyclerView recyclerView;
	RecyclerView.LayoutManager layoutManager;
	
	FirebaseDatabase database;
	DatabaseReference foodList;
	
	String categoryId="";
	
	FirebaseRecyclerAdapter< Food, FoodViewHolder > adapter;
	
	//Search Functionality
	FirebaseRecyclerAdapter< Food, FoodViewHolder > searchAdapter;
	List<String> suggestList = new ArrayList<>(  );
	MaterialSearchBar materialSearchBar;
	
	Database localDB;
	
	SwipeRefreshLayout swipeRefreshLayout;
	
	@SuppressLint( "ResourceAsColor" )
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_food_list );
		
//		Toolbar toolbar = (Toolbar ) findViewById( R.id.toolbar );
//		toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_black_24dp);
//		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent( FoodList.this, Home.class );
//				startActivity( intent );
//			}
//		});
		
		//FireBase
		database = FirebaseDatabase.getInstance();
		foodList = database.getReference("Food");
		
		//Local DB
		localDB = new Database( this );
		
		swipeRefreshLayout = (SwipeRefreshLayout ) findViewById( R.id.swipe_layout );
		swipeRefreshLayout.setColorSchemeColors( R.color.colorPrimary,
				android.R.color.holo_green_dark,
				android.R.color.holo_orange_dark,
				android.R.color.holo_blue_dark);
		
		swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener( ) {
			@Override
			public void onRefresh( ) {
				if(getIntent() != null) {
					categoryId = getIntent().getStringExtra( "CategoryId" );
				}
				if(!categoryId.isEmpty() && categoryId != null) {
					if( Common.isConnectedToInternet( getBaseContext() ) ){
						loadListFood(categoryId);
					} else {
						Toast.makeText( FoodList.this, "Пожалуйста, проверьте интернет-соединение!!!",
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
			}
		} );
		
		swipeRefreshLayout.post( new Runnable( ) {
			@Override
			public void run( ) {
				if(getIntent() != null) {
					categoryId = getIntent().getStringExtra( "CategoryId" );
				}
				if(!categoryId.isEmpty() && categoryId != null) {
					if( Common.isConnectedToInternet( getBaseContext() ) ){
						loadListFood(categoryId);
					} else {
						Toast.makeText( FoodList.this, "Пожалуйста, проверьте интернет-соединение!!!",
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				materialSearchBar = (MaterialSearchBar) findViewById( R.id.searchBar );
				materialSearchBar.setHint( "Начните вводить название продукта ..." );
				loadSuggest();
				materialSearchBar.setLastSuggestions( suggestList );
				materialSearchBar.setCardViewElevation( 10 );
				
				materialSearchBar.addTextChangeListener( new TextWatcher( ) {
					@Override
					public void beforeTextChanged( CharSequence s , int start , int count , int after ) {
					
					}
					
					@Override
					public void onTextChanged( CharSequence s , int start , int before , int count ) {
						List<String> suggest = new ArrayList <String>(  );
						for(String search:suggestList){
							if(search.toLowerCase().contains( materialSearchBar.getText().toLowerCase() ))
								suggest.add( search );
						}
						materialSearchBar.setLastSuggestions( suggest );
					}
					
					@Override
					public void afterTextChanged( Editable s ) {
					
					}
				} );
				
				materialSearchBar.setOnSearchActionListener( new MaterialSearchBar.OnSearchActionListener( ) {
					@Override
					public void onSearchStateChanged( boolean enabled ) {
						if(!enabled){
							recyclerView.setAdapter( adapter );
						}
					}
					
					@Override
					public void onSearchConfirmed( CharSequence text ) {
						startSearch( text );
					}
					
					@Override
					public void onButtonClicked( int buttonCode ) {
					
					}
				} );
				
			}
		} );
		
		recyclerView = (RecyclerView) findViewById( R.id.recycler_food );
		recyclerView.setHasFixedSize( true );
		layoutManager = new LinearLayoutManager( this );
		recyclerView.setLayoutManager( layoutManager );
		
		
	}
	private void startSearch(CharSequence text) {
		searchAdapter = new FirebaseRecyclerAdapter < Food, FoodViewHolder >(
				Food.class,
				R.layout.food_item,
				FoodViewHolder.class,
				foodList.orderByChild( "name" ).equalTo( text.toString() )
		) {
			@Override
			protected void populateViewHolder( FoodViewHolder viewHolder , Food model , int position ) {
				viewHolder.food_name.setText( model.getName() );
				Picasso.with( getBaseContext() ).load( model.getImage() )
						.into( viewHolder.food_image );
				final Food local = model;
				viewHolder.setItemClickListener( new ItemClickListener( ) {
					@Override
					public void onClick( View view , int position , boolean isLongClick ) {
						//Start new Activity
						Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
						foodDetail.putExtra( "FoodId", searchAdapter.getRef( position ).getKey() );
						startActivity( foodDetail );
					}
				} );
			}
		};
		recyclerView.setAdapter( searchAdapter );
	}
	
	private void loadSuggest( ) {
		foodList.orderByChild( "menuId").equalTo( categoryId )
				.addValueEventListener( new ValueEventListener( ) {
					@Override
					public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
						for(DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
							Food item = postSnapshot.getValue(Food.class);
							suggestList.add( item.getName() );
							
						}
					}
					
					@Override
					public void onCancelled( @NonNull DatabaseError databaseError ) {
					
					}
				} );
	}
	
	private void loadListFood( String categoryId ) {
		adapter = new FirebaseRecyclerAdapter < Food, FoodViewHolder >( Food.class, R.layout.food_item,
			FoodViewHolder.class, foodList.orderByChild( "menuId" ).equalTo( categoryId )	) {
			@Override
			protected void populateViewHolder( final FoodViewHolder viewHolder , final Food model , final int position ) {
				viewHolder.food_name.setText( model.getName() );
				viewHolder.food_price.setText( String.format( "$ %s", model.getPrice().toString() ) );
				Picasso.with( getBaseContext() ).load( model.getImage() )
						.into( viewHolder.food_image );
				
				//Add Favorites
				if(localDB.isFavorites( adapter.getRef( position ).getKey() )){
					viewHolder.fav_image.setImageResource( R.drawable.ic_favorite_border_black_24dp);
				}
				
				viewHolder.fav_image.setOnClickListener( new View.OnClickListener( ) {
					@Override
					public void onClick( View v ) {
						if(!localDB.isFavorites( adapter.getRef( position ).getKey() )){
							localDB.addToFavorites( adapter.getRef( position ).getKey() );
							viewHolder.fav_image.setImageResource( R.drawable.ic_favorite_border_black_24dp );
							Toast.makeText( FoodList.this, ""+model.getName()+" был добавлен в Избранные!!!", Toast.LENGTH_SHORT ).show();
						} else {
							localDB.removeFromFavorites( adapter.getRef( position ).getKey() );
							viewHolder.fav_image.setImageResource( R.drawable.ic_favorite_border_black_24dp );
							Toast.makeText( FoodList.this, ""+model.getName()+" был удален из Избранные!!!", Toast.LENGTH_SHORT ).show();
						}
					}
				} );
				
				final Food local = model;
				viewHolder.setItemClickListener( new ItemClickListener( ) {
					@Override
					public void onClick( View view , int position , boolean isLongClick ) {
						//Start new Activity
						Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
						foodDetail.putExtra( "FoodId", adapter.getRef( position ).getKey() );
						startActivity( foodDetail );
					}
				} );
			}
		};
		//Set Adapter
		Log.d("TAG", ""+adapter.getItemCount());
		recyclerView.setAdapter( adapter );
		swipeRefreshLayout.setRefreshing( false );
	}
}
