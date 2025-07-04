package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.ViewPagerAdapter;
import com.vkbao.remotemm.databinding.FragmentMainBinding;
import com.vkbao.remotemm.helper.CustomInterceptor;
import com.vkbao.remotemm.viewmodel.WebsocketViewModel;
import com.vkbao.remotemm.views.activities.MainActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;
    private WebsocketViewModel websocketViewModel;
    private String url;
    private String username;
    private String password;
    private Gson gson;

    private static final String TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gson = new Gson();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            url = getArguments().getString("url");
            username = getArguments().getString("username");
            password = getArguments().getString("password");

            try {
                URI uri = new URI(url);
                URI newUri = new URI(
                        "http",
                        uri.getUserInfo(),
                        uri.getHost(),
                        9091,
                        uri.getPath(),
                        uri.getQuery(),
                        uri.getFragment()
                );
                CustomInterceptor customInterceptor = CustomInterceptor.getInstance();

                customInterceptor.setBaseUrl(newUri.toString());
                customInterceptor.setPort(9091);
            } catch (URISyntaxException e) {
                Log.d(TAG, "http url is invalid");
            }
        }

        ((MainActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);

        initConnectionState();

        initTabs(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_TAB_INDEX", binding.tabLayout.getSelectedTabPosition());
    }

    public void initConnectionState() {
        CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (url != null) {
                    websocketViewModel.connect(url);
                }
            }
        };


        websocketViewModel.getConnectionStateLiveData().observe(getViewLifecycleOwner(), connectionState -> {
            switch (connectionState) {
                case CONNECTING:
                    binding.lostConnectionLayout.setVisibility(View.VISIBLE);
                    break;
                case DISCONNECTED:
                    countDownTimer.start();
                    break;
                case UNAUTHENTICATED:
                    Map<String, Object> msg = new LinkedTreeMap<>();
                    msg.put("action", "login");
                    msg.put("username", username);
                    msg.put("password", password);
                    websocketViewModel.sendMessage(gson.toJson(msg));
                    break;

                case AUTHENTICATION_FAILED:
                    Toast.makeText(requireActivity(), getResources().getString(R.string.toast_authen_failed), Toast.LENGTH_SHORT).show();
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.main, new ConnectFragment())
                            .commit();
                    break;
                case CONNECTED:
                    countDownTimer.cancel();
                    binding.lostConnectionLayout.setVisibility(View.GONE);
                default:
            }
        });
    }

    public void initTabs(Bundle savedInstanceState) {
        List<Fragment> fragments = new ArrayList<>();
        List<String> tabNames = new ArrayList<>();

        fragments.add(new HomeFragment());
        tabNames.add(getResources().getString(R.string.text_home));

        fragments.add(new FileFragment());
        tabNames.add(getString(R.string.text_face));

        fragments.add(new ProfileConfigFragment());
        tabNames.add(getResources().getString(R.string.text_profile_config));

        fragments.add(new BackupAndRestoreFragment());
        tabNames.add(getResources().getString(R.string.text_backup_and_restore));

        ViewPagerAdapter pageAdapter = new ViewPagerAdapter(this, fragments, tabNames);
        binding.viewpager.setOffscreenPageLimit(1);
        binding.viewpager.setAdapter(pageAdapter);

        new TabLayoutMediator(
                binding.tabLayout,
                binding.viewpager,
                (tab, position) -> tab.setText(pageAdapter.getFragmentName(position))
        ).attach();

        if (savedInstanceState != null) {
            int currentTabIndex = savedInstanceState.getInt("CURRENT_TAB_INDEX", 0);
            binding.viewpager.setCurrentItem(currentTabIndex, false);
        }
    }
}