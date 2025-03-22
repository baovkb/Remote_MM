package com.vkbao.remotemm.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.vkbao.remotemm.views.fragments.MainFragment;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private List<Fragment> fragments;
    private List<String> fragmentNames;

    public ViewPagerAdapter(@NonNull Fragment fragment, List<Fragment> fragments, List<String> fragmentNames) {
        super(fragment);
        this.fragments = fragments;
        this.fragmentNames = fragmentNames;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public String getFragmentName(int position) { return fragmentNames.get(position); }
}
