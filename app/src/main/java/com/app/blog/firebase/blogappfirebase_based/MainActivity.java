package com.app.blog.firebase.blogappfirebase_based;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rv_posts;
    private DatabaseReference mDbRef;
    private DatabaseReference users_db;
    private DatabaseReference likes_db;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean mProcessLike = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        rv_posts = (RecyclerView) findViewById(R.id.rv_posts);
        rv_posts.setHasFixedSize(true);
        //todo:this is original layoutManager
        // rv_posts.setLayoutManager(new LinearLayoutManager(this));
        rv_posts.setLayoutManager(new GridLayoutManager(this, 2));
        rv_posts.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        rv_posts.setItemAnimator(new DefaultItemAnimator());

        mDbRef = FirebaseDatabase.getInstance().getReference().child("blog").child("posts");
        likes_db = FirebaseDatabase.getInstance().getReference().child("blog").child("likess");

        users_db.keepSynced(true);

        chekUserExist();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "MainA.BlogViewHolder";
        View mView;
        TextView tv_post_title;
        TextView tv_post_userName;
        ImageButton ib_post_like;
        private DatabaseReference likes_db;
        private FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);

            likes_db = FirebaseDatabase.getInstance().getReference().child("likes");
            mAuth = FirebaseAuth.getInstance();

            likes_db.keepSynced(true);

            mView = itemView;
            ib_post_like = (ImageButton) mView.findViewById(R.id.ib_post_like);
            tv_post_userName = (TextView) mView.findViewById(R.id.tv_post_userName);
            tv_post_title = (TextView) mView.findViewById(R.id.tv_post_title);
            tv_post_userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "userName Clicked");
                }
            });

            tv_post_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "Title Clicked");
                }
            });

        }

        void setLikeButtonState(final String postKey) {
            likes_db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(mAuth.getCurrentUser().getUid())) {
                        ib_post_like.setImageResource(R.mipmap.liked);
                    }
                    else {
                        ib_post_like.setImageResource(R.mipmap.like);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        void setTitle(String title) {
            tv_post_title.setText(title);
        }

        void setDescription(String description) {
            TextView tv_post_title = (TextView) mView.findViewById(R.id.tv_post_description);
            tv_post_title.setText(description);
        }

        void setUserName(String username) {
            tv_post_userName.setText(username);
        }

        void setImg(Context context, String imgUrl) {
            ImageView iv_post_img = (ImageView) mView.findViewById(R.id.iv_post);
            Picasso.with(context).load(imgUrl).into(iv_post_img);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(Blog.class, R.layout.blog_row, BlogViewHolder.class, mDbRef) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, final int position) {


                //post_key contains the key name of the post in the db
                final String post_key = getRef(position).getKey();
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImg(getApplicationContext(), model.getImage());
                viewHolder.setUserName(model.getUserName());
                viewHolder.setLikeButtonState(post_key);
                viewHolder.ib_post_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike = true;

                        likes_db.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessLike) {
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        likes_db.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;
                                    } else {
                                        likes_db.child(post_key).child(mAuth.getCurrentUser().getUid()).child("randomValue");
                                        mProcessLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }


                });
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "you clicked a view! p = " + post_key, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        };
        rv_posts.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(this, AddPostActivity.class));
        }
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    private void chekUserExist() {


        if (mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();
            users_db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(userId)) {
                        Toast.makeText(MainActivity.this, "you need to setup your account ", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, ProfileSetupActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


}
