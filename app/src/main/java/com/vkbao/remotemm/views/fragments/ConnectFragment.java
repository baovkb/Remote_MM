package com.vkbao.remotemm.views.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.databinding.FragmentConnectBinding;
import com.vkbao.remotemm.viewmodel.WebsocketViewModel;
import com.vkbao.remotemm.views.activities.MainActivity;
import com.vkbao.remotemm.views.activities.ScanQrActivity;

import java.util.Map;


public class ConnectFragment extends Fragment {
    private FragmentConnectBinding binding;
    private WebsocketViewModel websocketViewModel;
    private String url;
    private String username;
    private String password;
    private Gson gson;
    ActivityResultLauncher<Intent> someActivityResultLauncher;

    public ConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            String url = data.getStringExtra("QR_CODE_VALUE");
                            if (url != null)
                                binding.addressInput.setText(url);
                        }
                    }
                });

        gson = new Gson();

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

        handleConnect();
    }

    public void handleConnect() {
        websocketViewModel.getConnectionStateLiveData().observe(getViewLifecycleOwner(), connectionState -> {
            Log.d("connect fragment", connectionState.toString());

            switch (connectionState) {
                case CONNECTING:
                    binding.progressConnect.setVisibility(View.VISIBLE);
                    deactiveConnectBtn();
                    break;
                case ERROR_URL:
                    Toast.makeText(requireActivity(), getResources().getString(R.string.toast_wrong_url), Toast.LENGTH_SHORT).show();
                    binding.progressConnect.setVisibility(View.GONE);
                    activeConnectBtn();
                    break;
                case DISCONNECTED:
                    Toast.makeText(requireActivity(), getResources().getString(R.string.toast_connect_fail), Toast.LENGTH_SHORT).show();
                    binding.progressConnect.setVisibility(View.GONE);
                    activeConnectBtn();
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
                    binding.progressConnect.setVisibility(View.GONE);
                    activeConnectBtn();
                    break;
                case CONNECTED:
                    Bundle bundle = new Bundle();
                    bundle.putString("url", url);
                    bundle.putString("username", username);
                    bundle.putString("password", password);
                    MainFragment mainFragment = new MainFragment();
                    mainFragment.setArguments(bundle);

                    //navigate to main fragment
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.main, mainFragment)
                            .commit();
                default:
            }
        });

        binding.connectBtn.setOnClickListener(view -> {
            url = binding.addressInput.getText().toString();
            username = binding.username.getText().toString();
            password = binding.password.getText().toString();
            websocketViewModel.connect(url);
        });


        binding.addressLayout.setEndIconOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ScanQrActivity.class);
            someActivityResultLauncher.launch(intent);
        });

        binding.progressConnect.setVisibility(View.GONE);

        binding.passwordLayout.setEndIconOnClickListener(view -> {
            Log.d("aa", "pass is pressed");

            int inputType = InputType.TYPE_CLASS_TEXT | binding.password.getInputType();
            if (inputType == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
                binding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                binding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            binding.password.setSelection(binding.password.getText().length());
        });
    }

    public void activeConnectBtn() {
        binding.connectBtn.setEnabled(true);
        binding.connectBtn.setBackgroundResource(R.drawable.blue_bg);
        binding.connectBtn.setTextColor(getResources().getColor(R.color.white, null));
    }

    public void deactiveConnectBtn() {
        binding.connectBtn.setEnabled(false);
        binding.connectBtn.setBackgroundResource(R.drawable.deactive_bg);
        binding.connectBtn.setTextColor(getResources().getColor(R.color.dark_blue, null));
    }
}