package com.unisoc.ccsa;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import com.unisoc.ccsa.about.AboutFragment;
import com.unisoc.ccsa.crypt.CryptBoxFragment;
import com.unisoc.ccsa.phone.PhoneNumberFragment;
import com.unisoc.ccsa.rdc.RemoteDataControlFragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentTransaction mFragmentTransaction = null;
    private DrawerLayout mDrawer = null;
    private ActionBar mActionbar = null;
    private Fragment mCurrentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mFragmentTransaction = getFragmentManager().beginTransaction();

        mFragmentTransaction.add(R.id.content_main, RemoteDataControlFragment.getInstance());
        mFragmentTransaction.add(R.id.content_main, CryptBoxFragment.getInstance());
        mFragmentTransaction.add(R.id.content_main, PhoneNumberFragment.getInstance());
        mFragmentTransaction.add(R.id.content_main, AboutFragment.getInstance());

        mFragmentTransaction.hide(CryptBoxFragment.getInstance());
        mFragmentTransaction.hide(PhoneNumberFragment.getInstance());
        mFragmentTransaction.hide(AboutFragment.getInstance());
        mFragmentTransaction.show(RemoteDataControlFragment.getInstance());
        mFragmentTransaction.commitAllowingStateLoss();
        mCurrentFragment = RemoteDataControlFragment.getInstance();

        mActionbar = getSupportActionBar();
        mActionbar.setTitle(R.string.menu_permission_settings);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        mFragmentTransaction = getFragmentManager().beginTransaction();

        int id = item.getItemId();
        switch (id) {
            case R.id.crypt_box:
                mFragmentTransaction.hide(mCurrentFragment).show(CryptBoxFragment.getInstance()).commitAllowingStateLoss();
                mCurrentFragment = CryptBoxFragment.getInstance();
                break;
            case R.id.remote_data_control:
                mFragmentTransaction.hide(mCurrentFragment).show(RemoteDataControlFragment.getInstance()).commitAllowingStateLoss();
                mCurrentFragment = RemoteDataControlFragment.getInstance();
                break;
            case R.id.phone_number:
                mFragmentTransaction.hide(mCurrentFragment).show(PhoneNumberFragment.getInstance()).commitAllowingStateLoss();
                mCurrentFragment = PhoneNumberFragment.getInstance();
                break;
            case R.id.about_ccsa:
                mFragmentTransaction.hide(mCurrentFragment).show(AboutFragment.getInstance()).commitAllowingStateLoss();
                mCurrentFragment = AboutFragment.getInstance();
                break;
            default:
                break;
        }

        mActionbar.setTitle(item.getTitle());
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
