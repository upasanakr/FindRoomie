package com.example.roomfinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SeekerAdapter extends RecyclerView.Adapter<SeekerAdapter.ViewHolder> {

    private List<ListerHomePageActivity.Seeker> seekers;

    public SeekerAdapter(List<ListerHomePageActivity.Seeker> seekers) {
        this.seekers = seekers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seeker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListerHomePageActivity.Seeker seeker = seekers.get(position);
        holder.tvName.setText(seeker.name);
        holder.tvEmail.setText("Email: " + seeker.email);
        holder.tvPhoneNumber.setText("Phone: " + seeker.phone_number);
    }

    @Override
    public int getItemCount() {
        return seekers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhoneNumber;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvPhoneNumber = itemView.findViewById(R.id.tv_phone_number);
        }
    }
}
