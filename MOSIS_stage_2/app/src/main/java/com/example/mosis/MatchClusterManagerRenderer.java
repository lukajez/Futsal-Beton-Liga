package com.example.mosis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MatchClusterManagerRenderer<GlideDrawable, GlideAnimation> extends DefaultClusterRenderer<ClusterMatchMarker> {

    private final IconGenerator iconGenerator;
    //private final CircleImageView circleImageView;
    private final ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;
    private Context context;

    public MatchClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMatchMarker> clusterManager) {
        super(context, map, clusterManager);

        this.context = context;
        Log.d("37. MyClusterRenderer: ", context.toString());

        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        markerWidth = (int) context.getResources().getDimension(R.dimen.custom_match_marker_image_width);
        markerHeight = (int) context.getResources().getDimension(R.dimen.custom_match_marker_image_height);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(0, 0, 0, 0);
        iconGenerator.setContentView(imageView);
    }

    public void setUpdateMarker(ClusterMatchMarker clusterMarker) {
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setPosition(clusterMarker.getPosition());
        }
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterMatchMarker item, @NonNull MarkerOptions markerOptions) {

        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getSnippet() + item.getMatch().getName());
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<ClusterMatchMarker> cluster) {
        return false;
    }

    @Override
    protected void onClusterItemRendered(@NonNull ClusterMatchMarker clusterItem, @NonNull final Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

        if(clusterItem.getImageUrl().toString().length() > 0) {
            Glide.with(context.getApplicationContext())
                    .load(clusterItem.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable drawable, Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(drawable);
                            Bitmap icon = iconGenerator.makeIcon();
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                        }
                    });
        } else {
            imageView.setImageURI(Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_profile%2Fic_default_profile.svg?alt=media&token=e931ec19-7ba0-4522-ad10-c68cfbeb9547"));
        }
    }
}
