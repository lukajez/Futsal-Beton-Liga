package com.example.mosis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.random.Random;

public class MyClusterManagerRenderer<GlideDrawable, GlideAnimation> extends DefaultClusterRenderer<ClusterMarker> {

    private final IconGenerator iconGenerator;
    //private final CircleImageView circleImageView;
    private final ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;
    private Context context;


    public MyClusterManagerRenderer(Context context, GoogleMap map,
                                    ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);

        this.context = context;
        Log.d("37. MyClusterRenderer: ", context.toString());

        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        markerWidth = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);

    }

    /**
     * Update the GPS coordinate of a ClusterItem
     * @param clusterMarker
     */
    public void setUpdateMarker(ClusterMarker clusterMarker) {
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setPosition(clusterMarker.getPosition());
        }
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterMarker item, @NonNull MarkerOptions markerOptions) {

        Log.d("52. MyClusterRenderer: onBeforeClusterItemRendered", item.getUser().toString());

        //imageView.setImageURI(Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_profile%2Fic_default_profile.svg?alt=media&token=e931ec19-7ba0-4522-ad10-c68cfbeb9547"));
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getSnippet() + item.getUser().getUsername()).snippet("TEAM: " + item.getUser().getTeam());
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<ClusterMarker> cluster) {
        return false;
    }

    @Override
    protected void onClusterItemRendered(@NonNull ClusterMarker clusterItem, @NonNull final Marker marker) {
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
            imageView.setImageURI(Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_profile%2Fprofile_icon.png?alt=media&token=4065dfe1-c40c-4314-9abb-e4e6c15670f6"));
        }
    }

//    private void makeCircle() {
//        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
//        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
//        Canvas canvas1 = new Canvas(bmp);
//
//
//// paint defines the text color, stroke width and size
//        Paint color = new Paint();
//        color.setTextSize(35);
//        color.setColor(Color.BLACK);
//
//// modify canvas
//        canvas1.drawBitmap(BitmapFactory.decodeResource(context.getResources(),
//                R.drawable.ic_profile_user), 0,0, color);
//        canvas1.drawText("User Name!", 30, 40, color);
//
//// add marker to Map
//        mMap.addMarker(new MarkerOptions()
//                .position(USER_POSITION)
//                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
//                // Specifies the anchor to be at a particular point in the marker image.
//                .anchor(0.5f, 1));
//    }
}
