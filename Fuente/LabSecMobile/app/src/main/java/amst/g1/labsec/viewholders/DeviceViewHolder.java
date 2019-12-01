package amst.g1.labsec.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import amst.g1.labsec.R;

public class DeviceViewHolder extends RecyclerView.ViewHolder {

    public final TextView tvName;
    public final TextView tvBrand;
    public final TextView tvModel;
    public final ImageView ivState;

    public DeviceViewHolder(@NonNull View itemView) {
        super(itemView);
        ivState = itemView.findViewById(R.id.ivItemDeviceState);
        tvName = itemView.findViewById(R.id.tvItemDeviceName);
        tvBrand= itemView.findViewById(R.id.tvItemDeviceBrand);
        tvModel= itemView.findViewById(R.id.tvItemDeviceModel);
    }
}
