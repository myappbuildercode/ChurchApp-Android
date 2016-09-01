package org.apache.cordova.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class NativeGalleryAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater infalter;
	ImageLoader imageLoader;

	public NativeGalleryAdapter(Context c, ImageLoader imageLoader2) {
		infalter = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = c;
		this.imageLoader = imageLoader2;
		clearCache();
	}

	@Override
	public int getCount() {
		return Constant.data.size();
	}

	@Override
	public CustomGallery getItem(int position) {
		return Constant.data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addAll(ArrayList<CustomGallery> files) {

		try {
			Constant.data.clear();
			Constant.data.addAll(files);

		} catch (Exception e) {
			e.printStackTrace();
		}

		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		try {

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = infalter.inflate(com.churchapp.church.R.layout.inflate_galleryitem,
						null);
				holder.imgQueue = (ImageView) convertView
						.findViewById(com.churchapp.church.R.id.imgQueue);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.imgQueue.setTag(position);

			//holder.imgQueue.setImageResource(R.drawable.feb_logo);
			/*Bitmap split = Constant.data.get(position).sdcardPath;
			holder.imgQueue.setImageBitmap(split);*/
			
			
			String split = Constant.data.get(position).sdcardPath;
			if (split.contains(".mp4")) {
				Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(split,
						MediaStore.Video.Thumbnails.MINI_KIND);
				holder.imgQueue.setImageBitmap(bitmap);
			} else {
				imageLoader.displayImage(
						"file://" + Constant.data.get(position).sdcardPath,
						holder.imgQueue);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	public class ViewHolder {
		ImageView imgQueue;
		ImageView imgQueueMultiSelected;
		TextView imgQueueTextView;
	}

	public void clearCache() {
		imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();
	}

	public void clear() {
		Constant.data.clear();
		notifyDataSetChanged();
	}
}
