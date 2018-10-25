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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fluencsotest.R;
import com.fluencsotest.Util;
import com.fluencsotest.models.ConvEntityItem;
import com.fluencsotest.models.ConversationItem;
import com.fluencsotest.models.PersonaConvITem;
import com.fluencsotest.models.UserConvItem;
import com.fluencsotest.speechhandler.PlayerUtility;

import java.util.ArrayList;

public class ConversationLearnAdapter extends RecyclerView.Adapter<ConversationLearnAdapter.MyViewHolder> implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private ArrayList<ConversationItem> conversationItems;
    private Handler handler = new Handler();

    // states of conversation
    // Persona
    private int P_SPEAKING = 1;
    private int P_SPOKEN = 2;

    private int currentPlayerPosition;

    public ConversationLearnAdapter(ArrayList<ConversationItem> conversationItems) {
        this.conversationItems = conversationItems;
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

    private void setViewStates(ConversationLearnAdapter.MyViewHolder holder, final int position) {

        if (getItemViewType(position) == Util.PERSONA) {

            final PersonaConvITem personaConvITem = conversationItems.get(position).getPersonaConvITem();

            if (personaConvITem.getConversationState() == P_SPEAKING) {

                holder.voice_text_rl.setVisibility(View.GONE);
                holder.media_play_ib.setVisibility(View.GONE);

            } else if (personaConvITem.getConversationState() == P_SPOKEN) {

                holder.voice_text_rl.setVisibility(View.VISIBLE);
                holder.media_play_ib.setVisibility(View.VISIBLE);

                if (personaConvITem.getPlayerState() == PlayerUtility.Companion.getPLAYING()) {

                    holder.media_play_ib.setImageResource(R.drawable.ic_launcher_background);
                } else {
                    holder.media_play_ib.setImageResource(R.drawable.ic_launcher_background);
                }
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

            holder.voice_graph_rl.setBackgroundResource(R.drawable.persona_gradient);

        } else if (getItemViewType(position) == Util.USER) {

            UserConvItem userConvItem = conversationItems.get(position).getUserConvItem();

            holder.voice_feedback_rating_tv.setText(Util.Companion.getRatingText(userConvItem.getRating()));
            holder.voice_feedback_tv.setText(userConvItem.getFeedBackText());
            holder.voice_text_tv.setText(userConvItem.getVoiceText());
            holder.voice_text_tv.setVisibility(userConvItem.isShowText() ? View.VISIBLE : View.GONE);

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

            if (userConvItem.getPlayerState() == PlayerUtility.Companion.getPLAYING()) {

                holder.media_play_ib.setImageResource(R.drawable.ic_launcher_background);

            } else {

                holder.media_play_ib.setImageResource(R.drawable.ic_launcher_background);

            }

            int color;
            if (userConvItem.getRating() <= 2) {

                color = ContextCompat.getColor(holder.voice_graph_rl.getContext(),
                        R.color.bad_review_color);

            } else {
                color = ContextCompat.getColor(holder.voice_graph_rl.getContext(),
                        R.color.good_review_color);
            }

            holder.respeak_ib.setBackgroundColor(color);
            holder.voice_graph_rl.setBackgroundColor(color);
        }
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

            final int position = getLayoutPosition();

            media_play_ib.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    ConvEntityItem item = conversationItems.get(position).getConvEntityItem();

                    if (item.getPlayerState() == PlayerUtility.Companion.getSTOPPED()) {

                        currentPlayerPosition = position;
                        playThisFile(item.getVoiceFilePath());
                        item.setPlayerState(PlayerUtility.Companion.getPLAYING());

                    } else {

                        stopPlaying();
                        item.setPlayerState(PlayerUtility.Companion.getSTOPPED());
                    }

                    notifyItemChanged(position);
                }
            });
        }
    }

    private void playThisFile(String voiceFilePath) {

        PlayerUtility.Companion.getInstance().startPlaying(voiceFilePath);
    }

    private void stopPlaying() {

        PlayerUtility.Companion.getInstance().stopPlaying();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        conversationItems.get(currentPlayerPosition).getConvEntityItem().setPlayerState(PlayerUtility.Companion.getSTOPPED());

        // notify
        mp.release();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        conversationItems.get(currentPlayerPosition).getConvEntityItem().setPlayerState(PlayerUtility.Companion.getSTOPPED());
        // notify

        return true;
    }
}
