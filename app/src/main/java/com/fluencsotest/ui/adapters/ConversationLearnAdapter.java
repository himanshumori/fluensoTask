package com.fluencsotest.ui.adapters;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fluencsotest.R;
import com.fluencsotest.Util;
import com.fluencsotest.customui.waveformview.PlaybackThread;
import com.fluencsotest.customui.waveformview.WaveformView;
import com.fluencsotest.models.ConvEntityItem;
import com.fluencsotest.models.ConversationItem;
import com.fluencsotest.models.PersonaConvITem;
import com.fluencsotest.models.UserConvItem;
import com.fluencsotest.speechhandler.PlayerUtility;
import com.fluencsotest.speechhandler.listeners.MediaProgressListener;
import com.fluencsotest.speechhandler.listeners.PersonaSpeaksListener;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.util.ArrayList;

public class ConversationLearnAdapter extends RecyclerView.Adapter<ConversationLearnAdapter.MyViewHolder> implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private ArrayList<ConversationItem> conversationItems;
    private PersonaSpeaksListener speaksListener;
    private Handler handler = new Handler();

    // states of conversation
    // Persona
    public static int P_SPEAKING = 1;
    public static int P_SPOKEN = 2;

    private int currentPlayerPosition = -1;

    public ConversationLearnAdapter(ArrayList<ConversationItem> conversationItems, PersonaSpeaksListener speaksListener) {
        this.conversationItems = conversationItems;
        this.speaksListener = speaksListener;
        PlayerUtility.Companion.getInstance().setMediaListeners(this, this);
    }

    @Override
    public ConversationLearnAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == Util.USER) {

            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_conv_item, parent, false));

        } else if (viewType == Util.PERSONA) {

            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.persona_conv_item, parent, false));

        } else {

            // can add more types later.. as of now persona is default
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.persona_conv_item, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return conversationItems.get(position).getItemType();
    }

    @Override
    public void onBindViewHolder(ConversationLearnAdapter.MyViewHolder holder, int position) {

        setViewStates(holder, position);
    }

    @Override
    public int getItemCount() {
        return conversationItems.size();
    }

    private void setViewStates(final ConversationLearnAdapter.MyViewHolder holder, final int position) {

        if (getItemViewType(position) == Util.PERSONA) {

            final PersonaConvITem personaConvITem = conversationItems.get(position).getPersonaConvITem();

            if (personaConvITem.getConversationState() == P_SPEAKING) {

                holder.voice_text_rl.setVisibility(View.GONE);
                holder.media_play_ib.setVisibility(View.GONE);
                playThisFile(personaConvITem.getVoiceFilePath(), position, holder.progressBar);
                currentPlayerPosition = position;
                speaksListener.onPersonaStatusChange(P_SPEAKING);
                holder.voice_graph_rl.setBackgroundResource(R.drawable.persona_speaking_gradient);

                personaConvITem.setConversationState(P_SPOKEN);
                // above will be notified after completion

            } else if (personaConvITem.getConversationState() == P_SPOKEN) {

                holder.voice_text_rl.setVisibility(View.VISIBLE);
                holder.media_play_ib.setVisibility(View.VISIBLE);

                if (personaConvITem.getPlayerState() == PlayerUtility.Companion.getPLAYING()) {

                    holder.media_play_ib.setImageResource(R.drawable.ic_launcher_background);
                } else {
                    holder.media_play_ib.setImageResource(R.drawable.ic_launcher_background);
                }

                holder.voice_graph_rl.setBackgroundResource(R.drawable.persona_spoken_gradient);
                speaksListener.onPersonaStatusChange(P_SPOKEN);
            }

            holder.voice_text_tv.setText(personaConvITem.getVoiceText());
            holder.watch_text_ib.setVisibility(!personaConvITem.isShowText() ? View.VISIBLE : View.GONE);
            holder.voice_text_tv.setVisibility(personaConvITem.isShowText() ? View.VISIBLE : View.GONE);

            holder.watch_text_ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    personaConvITem.setShowText(!personaConvITem.isShowText());

                    ConversationLearnAdapter.this.notifyItemChanged(position);
                }
            });

        } else if (getItemViewType(position) == Util.USER) {

            UserConvItem userConvItem = conversationItems.get(position).getUserConvItem();

            holder.voice_feedback_rating_tv.setText(Util.Companion.getRatingText(userConvItem.getRating()));
            holder.voice_feedback_tv.setText(userConvItem.getFeedBackText());
            holder.voice_text_tv.setText(userConvItem.getVoiceText());
            holder.voice_text_tv.setVisibility(View.VISIBLE);
            holder.watch_text_ib.setVisibility(View.GONE);

            holder.respeak_ib.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            conversationItems.remove(position);

                            notifyItemRemoved(position);
                        }
                    });
                }
            });

            int buttonBackground, graphBackColor,triangleColor;

            if (userConvItem.getRating() <= 1) {

                triangleColor = R.drawable.right_triangle_bad;
                buttonBackground = R.drawable.bad_button_background;
                graphBackColor = ContextCompat.getColor(holder.voice_graph_rl.getContext(),
                        R.color.bad_review_color);
            } else if (userConvItem.getRating() <= 3) {

                triangleColor = R.drawable.right_triangle_mid;
                buttonBackground = R.drawable.medium_button_background;
                graphBackColor = ContextCompat.getColor(holder.voice_graph_rl.getContext(),
                        R.color.mid_review_color);
            } else {

                graphBackColor = ContextCompat.getColor(holder.voice_graph_rl.getContext(),
                        R.color.good_review_color);
                triangleColor = R.drawable.right_triangle_green;
                buttonBackground = R.drawable.good_button_background;
            }

            holder.respeak_ib.setBackgroundResource(buttonBackground);
            holder.voice_graph_rl.setBackgroundColor(graphBackColor);
            holder.id_sender.setImageResource(triangleColor);
        }

        holder.media_play_ib.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                ConvEntityItem item = conversationItems.get(position).getConvEntityItem();

                if (item.getPlayerState() == PlayerUtility.Companion.getSTOPPED()) {

                    currentPlayerPosition = position;
                    playThisFile(item.getVoiceFilePath(), position, holder.progressBar);
                    item.setPlayerState(PlayerUtility.Companion.getPLAYING());

                } else {

                    stopPlaying();
                    item.setPlayerState(PlayerUtility.Companion.getSTOPPED());
                }

                notifyItemChanged(position);
            }
        });

        if (conversationItems.get(position).getConvEntityItem().getPlayerState() == PlayerUtility.Companion.getPLAYING()) {

            holder.progressBar.setVisibility(View.VISIBLE);
            holder.media_play_ib.setImageResource(R.drawable.stop_self_speak);
        } else {

            holder.progressBar.setVisibility(View.GONE);
            holder.media_play_ib.setImageResource(R.drawable.playback);
        }

        holder.mPlaybackView.setChannels(1);
        holder.mPlaybackView.setSampleRate(PlaybackThread.SAMPLE_RATE);
        try {
            holder.mPlaybackView.setSamples(getAudioSample(conversationItems.get(position).getConvEntityItem().getVoiceFilePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private short[] getAudioSample(String path) throws IOException {
        byte[] data;
        data = Files.readAllBytes(new File(path).toPath());
        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        return samples;
    }

    // TODO later construct separate viewHolder for Persona and User
    public class MyViewHolder extends RecyclerView.ViewHolder {

        // TODO will rename all these later.. as per JAVA naming conventions
        TextView voice_feedback_tv;
        TextView voice_feedback_rating_tv;
        TextView voice_text_tv;
        Button respeak_ib;
        ImageButton watch_text_ib;
        ImageButton media_play_ib;
        RelativeLayout voice_text_rl, voice_graph_rl;
        ProgressBar progressBar;
        WaveformView mPlaybackView;
        ImageView id_sender;
        public MyViewHolder(View v) {
            super(v);

            voice_feedback_rating_tv = v.findViewById(R.id.voice_feedback_rating_tv);
            voice_feedback_tv = v.findViewById(R.id.voice_feedback_tv);
            voice_text_tv = v.findViewById(R.id.voice_text_tv);
            respeak_ib = v.findViewById(R.id.respeak_ib);
            watch_text_ib = v.findViewById(R.id.watch_text_ib);
            media_play_ib = v.findViewById(R.id.media_play_ib);
            voice_text_rl = v.findViewById(R.id.voice_text_rl);
            voice_graph_rl = v.findViewById(R.id.voice_graph_rl);
            progressBar = v.findViewById(R.id.progressBar);
            mPlaybackView = v.findViewById(R.id.playbackWaveformView);
            id_sender = v.findViewById(R.id.id_sender);
        }
    }

    private void playThisFile(String voiceFilePath, final int position, final ProgressBar progressBar) {

        MediaPlayer mediaPlayer = PlayerUtility.Companion.getInstance().startPlaying(voiceFilePath);

        setProgressUpdates(mediaPlayer.getDuration(), new MediaProgressListener() {

            @Override
            public void onProgressChanged(int percentage) {

                // TODO this isnt affecting any ui changes
                if (!(percentage == -1)) {
                    progressBar.setProgress(percentage);
                }
            }
        });
    }

    private void stopPlaying() {

        PlayerUtility.Companion.getInstance().stopPlaying();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (currentPlayerPosition != -1) {
            conversationItems.get(currentPlayerPosition).getConvEntityItem().setPlayerState(PlayerUtility.Companion.getSTOPPED());
            notifyItemChanged(currentPlayerPosition);

            if (conversationItems.get(currentPlayerPosition).getItemType() == Util.PERSONA) {

                speaksListener.onPersonaStatusChange(P_SPOKEN);
            }
        }
        mp.reset();
        mp.release();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        if (currentPlayerPosition != -1) {
            conversationItems.get(currentPlayerPosition).getConvEntityItem().setPlayerState(PlayerUtility.Companion.getSTOPPED());
            notifyItemChanged(currentPlayerPosition);
        }

        return true;
    }

    private float mediaDurationUpdater = 0;

    void setProgressUpdates(final int duration, final MediaProgressListener mediaProgressListener) {

        final int amoungToupdate = duration / 100;
        mediaDurationUpdater = 0;

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (mediaDurationUpdater <= duration) {

                    mediaDurationUpdater = mediaDurationUpdater + duration / 100f;

                    int percentage = (int) ((mediaDurationUpdater / duration) * 100);

                    mediaProgressListener.onProgressChanged(percentage);

                    handler.postDelayed(this, amoungToupdate);
                } else {

                    mediaProgressListener.onProgressChanged(-1);
                }
            }
        };
        handler.postDelayed(runnable, amoungToupdate);
    }

}
