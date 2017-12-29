package com.walkap.x_android.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.firestore.FirebaseFirestore;
import com.walkap.x_android.R;
import com.walkap.x_android.dao.userDao.UserDao;
import com.walkap.x_android.dao.userDao.UserDaoImpl;
import com.walkap.x_android.fragment.AddFavoriteCoursesFragment;
import com.walkap.x_android.fragment.AddScheduleFragment;
import com.walkap.x_android.fragment.HomeFragment;
import com.walkap.x_android.fragment.OptionsFragment;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        AddScheduleFragment.OnFragmentInteractionListener,
        OptionsFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        AddFavoriteCoursesFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    //Google Api Sign in
    private GoogleApiClient mGoogleApiClient;
    // index to identify current nav menu item
    private static int navItemIndex = 0;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private View header;
    private TextView tvUserName, tvEmail;
    private ImageView ivUserProfile;
    private String[] arrayNavigation;
    private String name, email, uid;
    private Uri photoUrl;

    //Firestore instance
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Set a toolbar to replace action bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Find our drawer view
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //Enable hamburger toggle button
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //Find navigation drawer
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        //Navigation listener
        navigationView.setNavigationItemSelectedListener(this);
        //Header View (aat the top of drawer layout)
        header = navigationView.getHeaderView(0);
        arrayNavigation = getResources().getStringArray(R.array.navigation_array);
        tvUserName = (TextView) header.findViewById(R.id.tvUserName);
        tvEmail = (TextView) header.findViewById(R.id.tvEmail);
        ivUserProfile = (ImageView) header.findViewById(R.id.imageView);
        //Check if the user is logged in
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            // Name, email address, and profile photo Url
            name = mFirebaseUser.getDisplayName();
            email = mFirebaseUser.getEmail();
            photoUrl = mFirebaseUser.getPhotoUrl();
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            uid = mFirebaseUser.getUid();
            loadNavHeader();
        }
        //Google sign connection
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        //Check if the user has the basic information
        UserDao userDao = new UserDaoImpl();
        if(userDao.hasBasicInfo(uid)){
            Class fragmentClass = OptionsFragment.class;
            navItemIndex = 3;
            setToolbarTitle();
            loadFragment(fragmentClass);
        }

        //Default fragment to display
        if (savedInstanceState == null) {
            navItemIndex = 0;
            Class fragmentClass = HomeFragment.class;
            loadFragment(fragmentClass);
        }
    }

    /**
     * This method load the header information
     * inside the slidebar such a name, email and photo
     */
    private void loadNavHeader() {
        tvUserName.setText(name);
        tvEmail.setText(email);
        if (photoUrl != null) {
            Glide.with(this).load(photoUrl)
                    .thumbnail(1f)
                    .transition(withCrossFade())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserProfile);
        } else {
            ivUserProfile.setImageResource(R.drawable.ic_account_circle_white_48px);
        }
    }

    /**
     * This method load the right fragment based on the menu clicking
     * in the slider bar, set the right title and close the older fragment
     *
     * @param fragmentClass - Class
     */
    public void loadFragment(Class fragmentClass) {
        setToolbarTitle();
        Fragment newFragment = null;
        try {
            newFragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.flContent, newFragment);
        fragmentTransaction.commitAllowingStateLoss();
        // Set the right menu item checked
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
        // Set action bar title
        setToolbarTitle();
    }

    /**
     * This method is a listener for the menu at the top
     * the only one button present is the sign out button
     * once we push we are redirect to the SingInActivity
     *
     * @param item - Menu Item
     * @return - boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method retrieve a log message when the connection failed
     *
     * @param connectionResult - ConnectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    /**
     * Menu inflater
     *
     * @param menu - Menu
     * @return - boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * This method handle the app behavior when the back button
     * is pressed, and navigates through the fragments
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (navItemIndex != 0) {
            navItemIndex = 0;
            Class fragmentClass = HomeFragment.class;
            loadFragment(fragmentClass);
            return;
        }
        super.onBackPressed();
    }

    /**
     * This method is a listener for the slidebar menu
     * and set the right fragement when a menu item
     * is selected
     *
     * @param item - MenuItem
     * @return - boolean
     */
    public boolean onNavigationItemSelected(MenuItem item) {
        Class fragmentClass;
        switch (item.getItemId()) {
            case R.id.home:
                navItemIndex = 0;
                fragmentClass = HomeFragment.class;
                break;
            case R.id.add_schedule:
                navItemIndex = 1;
                fragmentClass = AddScheduleFragment.class;
                break;
            case R.id.add_school_subject:
                navItemIndex = 2;
                fragmentClass = AddFavoriteCoursesFragment.class;
                break;
            case R.id.options:
                navItemIndex = 3;
                fragmentClass = OptionsFragment.class;
                break;
            default:
                navItemIndex = 0;
                fragmentClass = HomeFragment.class;
                break;
        }
        Log.d(TAG, "onNavigationItemSelected() The index is: " + navItemIndex);
        loadFragment(fragmentClass);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * This method set the right toolbar title
     * based on the fragment selected
     */
    private void setToolbarTitle() {
        getSupportActionBar().setTitle(arrayNavigation[navItemIndex]);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Need this to make the fragment work
    }
}