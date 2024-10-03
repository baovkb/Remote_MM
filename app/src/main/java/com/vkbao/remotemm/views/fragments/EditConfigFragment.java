package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.databinding.ConfigValueBinding;
import com.vkbao.remotemm.databinding.FragmentEditConfigBinding;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.model.ModuleModel;
import com.vkbao.remotemm.viewmodel.WebsocketViewModel;
import com.vkbao.remotemm.views.activities.MainActivity;
import com.vkbao.remotemm.views.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class EditConfigFragment extends Fragment {
    private FragmentEditConfigBinding binding;
    private ModuleModel module;
    private WebsocketViewModel websocketViewModel;

    private static final String TAG = "EditConfigFragment";

    public EditConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            module = (ModuleModel) getArguments().getSerializable("module");
            Log.d(TAG, "module: " + module);
        }

        ((MainActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);

        if (module != null) {
            initMenuToolbar();
            binding.moduleName.setText(module.getModule());
            initPosition();
            initConfig(module.getConfig());
        }
    }

    public void initMenuToolbar() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.edit_config_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.save_config) {
                    showConfirmDialog();
                } else if (menuItem.getItemId() == android.R.id.home) {
                    getParentFragmentManager().popBackStack();
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    public void showConfirmDialog() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setMessage(getString(R.string.confirm_save_config));
        confirmDialog.setPositiveBtn(() -> {
            Gson gson = new Gson();
            Map<String, Object> payload = new LinkedTreeMap<>();
            payload.put("action", "request save config");
            payload.put("data", module);

            websocketViewModel.sendMessage(gson.toJson(payload));
        });

        confirmDialog.show(getChildFragmentManager(), null);
    }

    public void initPosition() {
        List<String> positionList = new ArrayList<>();
        positionList.add("top_bar");
        positionList.add("top_left");
        positionList.add("top_center");
        positionList.add("top_right");
        positionList.add("upper_third");
        positionList.add("middle_center");
        positionList.add("lower_third");
        positionList.add("bottom_left");
        positionList.add("bottom_center");
        positionList.add("bottom_right");
        positionList.add("bottom_bar");
        positionList.add("fullscreen_above");
        positionList.add("fullscreen_below");

        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(getActivity(), R.layout.position_item, positionList);
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.positionSpn.setAdapter(positionAdapter);

        binding.positionSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                module.setPosition(positionList.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //set selected module
        if (positionList.contains(module.getPosition())) {
            binding.positionSpn.setSelection(positionList.indexOf(module.getPosition()));
        }

    }

    public void initConfig(Map<String, Object> config) {
        valueLoop(config, binding.container);
    }

    public void valueLoop(Map<String, Object> value, ViewGroup container) {
        Stack<Map<String, Object>> mapStack = new Stack<>();
        Stack<ViewGroup> containerStack = new Stack<>();

        mapStack.push(value);
        containerStack.push(container);

        while (!mapStack.isEmpty()) {
            Map<String, Object> currentValue = mapStack.pop();
            ViewGroup currentContainer = containerStack.pop();

            for (Map.Entry<String, Object> entry: currentValue.entrySet()) {
                LinearLayout innerContainer = new LinearLayout(getContext());
                innerContainer.setOrientation(LinearLayout.HORIZONTAL);
                int paddingInPx = Helper.convertDpToPx(requireContext(), 8);
                innerContainer.setPadding(paddingInPx, 0, paddingInPx, 0);

                //key
                TextView keyTV = new TextView(getContext());
                keyTV.setText(entry.getKey());
                keyTV.setTextAppearance(R.style.text_key);
                innerContainer.addView(keyTV);

                //value
                if (entry.getValue() instanceof Map) {
                    innerContainer.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout nestedContainer = new LinearLayout(getContext());
                    nestedContainer.setOrientation(LinearLayout.VERTICAL);
                    nestedContainer.setBackgroundResource(R.drawable.left_border);
                    innerContainer.addView(nestedContainer);

                    mapStack.push((Map<String, Object>) entry.getValue());
                    containerStack.push(nestedContainer);
                } else {
                    if (entry.getValue() instanceof Boolean) {
                        Spinner spinner = new Spinner(getContext());
                        int padding = Helper.convertDpToPx(requireContext(), 4);
                        spinner.setPadding(padding, padding, padding, padding);
                        spinner.setBackgroundResource(R.drawable.darker_blue_outline);
                        spinner.setGravity(Gravity.CENTER);

                        ArrayAdapter<String> spinnerBoolAdapter = new ArrayAdapter<>(
                                getContext(),
                                R.layout.position_item,
                                new String[] {"false", "true"});
                        spinnerBoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                entry.setValue(i != 0);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                        spinner.setAdapter(spinnerBoolAdapter);
                        spinner.setSelection(((Boolean) entry.getValue()) ? 1 : 0);
                        innerContainer.addView(spinner);

                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) spinner.getLayoutParams();
                        params.setMargins(paddingInPx, 0, 0 ,0);
                        spinner.setLayoutParams(params);
                    } else {
                        ConfigValueBinding configValueBinding = ConfigValueBinding.inflate(LayoutInflater.from(getContext()), innerContainer, false);
                        if (entry.getValue() == null) {
                            configValueBinding.value.setText("null");
                        } else {
                            configValueBinding.value.setText(entry.getValue().toString());
                        }
                        configValueBinding.value.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                try {
                                    if (entry.getValue() instanceof Double) {
                                        entry.setValue(Double.valueOf(editable.toString()));
                                    } else if (entry.getValue() instanceof Integer) {
                                        entry.setValue(Integer.valueOf(editable.toString()));
                                    } else if (entry.getValue() instanceof ArrayList) {

                                    } else if (entry.getValue() instanceof String) {
                                        entry.setValue(editable.toString());
                                    }
                                } catch (Exception e) {
                                    configValueBinding.value.setError("");
                                }
                            }
                        });
                        innerContainer.addView(configValueBinding.getRoot());
                    }
                }

                currentContainer.addView(innerContainer);
            }
        }
    }

    public void valueRecursive(Map<String, Object> value, ViewGroup container) {
        for (Map.Entry<String, Object> entry: value.entrySet()) {
            LinearLayout innerContainer = new LinearLayout(getContext());
            innerContainer.setOrientation(LinearLayout.HORIZONTAL);
            int paddingInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8,
                    getResources().getDisplayMetrics()
            );
            innerContainer.setPadding(paddingInPx, 0, paddingInPx, 0);

            //key
            TextView keyTV = new TextView(getContext());
            keyTV.setText(entry.getKey());
            keyTV.setTextAppearance(R.style.text_key);
            innerContainer.addView(keyTV);

            //value
            if (entry.getValue() instanceof Map) {
                innerContainer.setOrientation(LinearLayout.VERTICAL);

                LinearLayout nestedContainer = new LinearLayout(getContext());
                nestedContainer.setOrientation(LinearLayout.VERTICAL);
                innerContainer.addView(nestedContainer);
                valueRecursive(
                        (Map<String, Object>) entry.getValue(),
                        nestedContainer);
            } else {
                if (entry.getValue() instanceof Boolean) {
                    Spinner spinner = new Spinner(getContext());
                    spinner.setPadding(paddingInPx, 0, 0, 0);

                    ArrayAdapter<String> spinnerBoolAdapter = new ArrayAdapter<>(
                            getContext(),
                            R.layout.position_item,
                            new String[] {"false", "true"});
                    spinnerBoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            entry.setValue(i != 0);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    spinner.setAdapter(spinnerBoolAdapter);
                    spinner.setSelection(((Boolean) entry.getValue()) ? 1 : 0);
                    innerContainer.addView(spinner);
                } else {
                    ConfigValueBinding configValueBinding = ConfigValueBinding.inflate(LayoutInflater.from(getContext()), innerContainer, false);
                    if (entry.getValue() == null) {
                        configValueBinding.value.setText("null");
                    } else {
                        configValueBinding.value.setText(entry.getValue().toString());
                    }
                    configValueBinding.value.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            try {
                                if (entry.getValue() instanceof Double) {
                                    entry.setValue(Double.valueOf(editable.toString()));
                                } else if (entry.getValue() instanceof Integer) {
                                    entry.setValue(Integer.valueOf(editable.toString()));
                                } else if (entry.getValue() instanceof ArrayList) {

                                } else if (entry.getValue() instanceof String) {
                                    entry.setValue(editable.toString());
                                }
                            } catch (Exception e) {
                                configValueBinding.value.setError("");
                            }
                        }
                    });
                    innerContainer.addView(configValueBinding.getRoot());
                }
            }

            container.addView(innerContainer);
        }
    }
}