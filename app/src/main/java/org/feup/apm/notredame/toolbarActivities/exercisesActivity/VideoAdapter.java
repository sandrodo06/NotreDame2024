package org.feup.apm.notredame.toolbarActivities.exercisesActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;



import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.feup.apm.notredame.R;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final Context context;
    private final List<VideoItem> videoList;

    // Constructor
    public VideoAdapter(Context context, List<VideoItem> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    // Creates a layout for every individual video in the RecyclerView
    @NonNull
    @Override
    public VideoAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_layout, parent, false);
        return new VideoViewHolder(view);
    }

    // Associates the information fetched to its respective video
    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder, int position) {
        VideoItem video = videoList.get(position);

        // Shows the title on each video layout
        holder.title.setText(video.title);

        // Shows the description on each video layout
        holder.description.setText(video.description);
        Glide.with(context)
                .load(video.thumbnailAddress)
                .into(holder.thumbnail);

        // Expands/minimizes the description when clicked
        holder.description.setOnClickListener(v -> {
            if (holder.isDescriptionExpanded) {

                holder.description.setMaxLines(4);
                holder.isDescriptionExpanded = false;
            } else {

                holder.description.setMaxLines(Integer.MAX_VALUE);
                holder.isDescriptionExpanded = true;
            }
        });

        // When clicking on the image, the video starts playing
        holder.thumbnail.setOnClickListener(v -> {
            holder.thumbnail.setVisibility(View.GONE);
            holder.webView.setVisibility(View.VISIBLE);
            String videoUrl = "https://www.youtube.com/embed/" + video.id + "?autoplay=1&controls=1&rel=0&modestbranding=1&showinfo=0";
            holder.webView.loadUrl(videoUrl);
        });
    }

    // Gets the number of videos that should be displayed
    @Override
    public int getItemCount() {
        return videoList.size();
    }

    // Binds the video information fetched to the respective spot on the video layout
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        WebView webView;
        TextView title, description;
        ImageView thumbnail;

        boolean isDescriptionExpanded = false;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.videoTitle);
            description = itemView.findViewById(R.id.videoDescription);
            thumbnail = itemView.findViewById(R.id.thumbnailImage);
            webView = itemView.findViewById(R.id.videoWebView);

            // Configure WebView settings
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            webView.setWebViewClient(new WebViewClient());
            webView.setVisibility(View.GONE);
        }
    }

    // Adds each fecthed video to the video list
    public void addVideo(VideoItem video) {
        videoList.add(video);
        notifyItemInserted(videoList.size() - 1);
    }

}
