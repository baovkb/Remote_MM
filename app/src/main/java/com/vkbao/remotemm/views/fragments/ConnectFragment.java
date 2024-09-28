package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vkbao.remotemm.R;
import com.vkbao.remotemm.databinding.FragmentConnectBinding;
import com.vkbao.remotemm.viewmodel.WebsocketViewModel;
import com.vkbao.remotemm.views.activities.MainActivity;


public class ConnectFragment extends Fragment {
    private FragmentConnectBinding binding;
    private WebsocketViewModel websocketViewModel;

    public ConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);

        connectAction();
    }

    public void connectAction() {
        websocketViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message.equals("failure")) {
                Toast.makeText(requireActivity(), getResources().getString(R.string.toast_connect_fail), Toast.LENGTH_SHORT).show();
            } else if (message.equals("error_url")) {
                Toast.makeText(requireActivity(), getResources().getString(R.string.toast_wrong_url), Toast.LENGTH_SHORT).show();
            } else {
                //navigate to main fragment
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.main, new MainFragment())
                        .commit();
            }
        });

        binding.connectBtn.setOnClickListener(view -> {
            String url = binding.addressInput.getText().toString();
            websocketViewModel.connect(url);
        });
    }
}