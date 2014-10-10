package com.shipit.motorgator;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.squareup.picasso.Picasso;

import java.util.Random;

/**
 * Created by sumeet on 9/17/14.
 */

public class ImageFeedAdapter extends ArrayAdapter <BingResultContainer.BingImage> {

    private final Random mRandom = new Random();
    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

    private static class ViewHolder {
        public DynamicHeightImageView imageView;
        public TextView titleView;
    }

    public ImageFeedAdapter(Context context) {
        super(context, R.layout.item_feed);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_feed, null);

            holder = new ViewHolder();
            holder.imageView = (DynamicHeightImageView) convertView.findViewById(R.id.dhiv_feed_image);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.titleView = (TextView) convertView.findViewById(R.id.tv_feed_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        double positionHeight = getPositionRatio(position);
        holder.imageView.setHeightRatio(positionHeight);

        BingResultContainer.BingImage image = getItem(position);
        Picasso.with(getContext())
                .load(image.url())
                .into(holder.imageView);

        holder.titleView.setText(image.title());

        return convertView;
    }

    private double getPositionRatio(final int position) {
        double ratio = sPositionHeightRatios.get(position, 0.0);
        // if not yet done generate and stash the columns height
        // in our real world scenario this will be determined by
        // some match based on the known height and width of the image
        // and maybe a helpful way to get the column height!
        if (ratio == 0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
        }
        return ratio;
    }

    private double getRandomHeightRatio() {
        return (mRandom.nextDouble() / 2.0) + 1.0; // height will be 1.0 - 1.5 the width
    }
}
