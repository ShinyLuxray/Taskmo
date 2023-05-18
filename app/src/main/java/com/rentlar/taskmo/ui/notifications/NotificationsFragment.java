package com.rentlar.taskmo.ui.notifications;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rentlar.taskmo.MainActivity;
import com.rentlar.taskmo.R;
import com.rentlar.taskmo.SettingsFragment;
import com.rentlar.taskmo.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //remove the back arrow.
        if(getActivity().getClass().equals(MainActivity.class)){
            MainActivity ma = (MainActivity) getActivity();
            androidx.appcompat.app.ActionBar actionBar = ma.getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
        }


        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Display the fragment as the main content.
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, new SettingsFragment())
                .commit();


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}