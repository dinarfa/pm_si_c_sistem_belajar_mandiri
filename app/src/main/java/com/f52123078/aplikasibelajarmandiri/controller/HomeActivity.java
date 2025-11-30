package com.f52123078.aplikasibelajarmandiri.controller; // Sesuaikan package

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.f52123078.aplikasibelajarmandiri.R;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityHomeBinding;
import com.f52123078.aplikasibelajarmandiri.model.HomeModel;
import com.f52123078.aplikasibelajarmandiri.view.AccountFragment;
import com.f52123078.aplikasibelajarmandiri.view.ForumFragment;
import com.f52123078.aplikasibelajarmandiri.view.HomeFragment;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private ActivityHomeBinding binding;
    private HomeModel homeModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate: Activity started");

        homeModel = new HomeModel();


        setupBottomNavigation();

        // Muat fragment awal (HomeFragment)
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: Loading initial fragment (HomeFragment)");
            loadFragment(new HomeFragment(), "HOME");
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }

    // HAPUS METHOD setupToolbar() INI KARENA binding.toolbarHome SUDAH TIDAK ADA
    /*
    private void setupToolbar() {
        Log.d(TAG, "setupToolbar: Setting up toolbar");
        binding.toolbarHome.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                homeModel.logout();
                goToLogin();
                return true;
            }
            return false;
        });
    }
    */

    private void setupBottomNavigation() {
        Log.d(TAG, "setupBottomNavigation: Setting up bottom navigation");
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            return handleNavigationSelection(item.getItemId());
        });

        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            Log.d(TAG, "BottomNav: Item reselected, doing nothing.");
        });
    }

    private boolean handleNavigationSelection(int itemId) {
        Fragment selectedFragment = null;
        String tag = null;

        if (itemId == R.id.navigation_home) {
            selectedFragment = findOrCreateFragment(HomeFragment.class, "HOME");
            tag = "HOME";

        } else if (itemId == R.id.navigation_resources) {
            // Masih membuka Activity baru untuk Resources
            Intent intent = new Intent(this, ResourceBrowseActivity.class);
            startActivity(intent);
            return false;

        } else if (itemId == R.id.navigation_forum) {
            selectedFragment = findOrCreateFragment(ForumFragment.class, "FORUM");
            tag = "FORUM";

        } else if (itemId == R.id.navigation_account) {
            selectedFragment = findOrCreateFragment(AccountFragment.class, "ACCOUNT");
            tag = "ACCOUNT";
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment, tag);
            return true;
        } else {
            return false;
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment != null && currentFragment.getClass() == fragment.getClass() && currentFragment.isVisible()) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commit();
    }

    private <T extends Fragment> T findOrCreateFragment(Class<T> fragmentClass, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null) {
            try {
                return fragmentClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not instantiate fragment " + fragmentClass.getName(), e);
            }
        } else {
            if (fragmentClass.isInstance(fragment)) {
                return fragmentClass.cast(fragment);
            } else {
                try { return fragmentClass.newInstance(); }
                catch (Exception e) {
                    throw new RuntimeException("Could not instantiate fallback fragment " + fragmentClass.getName(), e);
                }
            }
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}