package com.vkbao.remotemm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vkbao.remotemm.databinding.ProfileItemBinding;
import com.vkbao.remotemm.helper.Helper;

import java.util.List;
import java.util.Objects;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<String> profileList;
    private String selectedProfile;
    private final Helper.Callback<String> onProfileSelected;
    private final Helper.Callback<String> onProfileDeleted;
    private Context context;

    public ProfileAdapter(List<String> profileList, Helper.Callback<String> onProfileSelected, Helper.Callback<String> onProfileDeleted) {
        this.profileList = profileList;
        this.onProfileSelected = onProfileSelected;
        this.onProfileDeleted = onProfileDeleted;

        if (profileList.size() > 0) {
            this.selectedProfile = profileList.get(0);
        } else {
            this.selectedProfile = "";
        }
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ProfileItemBinding binding = ProfileItemBinding.inflate(LayoutInflater.from(context), parent, false);

        return new ProfileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        String profileName = profileList.get(position);
        holder.binding.name.setText(profileName);

        if (Objects.equals(profileName, selectedProfile)) {
            holder.binding.radioButton.setChecked(true);
            holder.binding.deleteBtn.setVisibility(View.GONE);
            onProfileSelected.onCall(profileName);
        } else {
            holder.binding.radioButton.setChecked(false);
            holder.binding.deleteBtn.setVisibility(View.VISIBLE);
        }

        View.OnClickListener clickListener = v -> {
            int oldSelected = profileList.indexOf(selectedProfile);
            selectedProfile = profileList.get(holder.getAdapterPosition());

            notifyItemChanged(oldSelected);
            notifyItemChanged(holder.getAdapterPosition());
        };

        holder.binding.radioButton.setOnClickListener(clickListener);
        holder.binding.itemContainer.setOnClickListener(clickListener);

        holder.binding.deleteBtn.setOnClickListener(view -> {
            onProfileDeleted.onCall(profileName);
        });
    }

    public void setProfileList(List<String> profileList) {
        this.profileList = profileList;
        if (this.selectedProfile.isEmpty() && profileList.size() > 0) {
            this.selectedProfile = profileList.get(0);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder {
        private ProfileItemBinding binding;

        public ProfileViewHolder(@NonNull ProfileItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
