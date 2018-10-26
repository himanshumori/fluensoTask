package com.fluencsotest.ui.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fluencsotest.R;
import com.fluencsotest.Util;
import com.fluencsotest.customui.DynamicSineWaveView;
import com.fluencsotest.models.ConversationItem;
import com.fluencsotest.models.PersonaConvITem;
import com.fluencsotest.models.UserConvItem;
import com.fluencsotest.speechhandler.RecorderUtility;
import com.fluencsotest.speechhandler.listeners.PersonaSpeaksListener;
import com.fluencsotest.ui.adapters.ConversationLearnAdapter;

import java.util.ArrayList;

public class ConversationLearnFragment extends Fragment implements PersonaSpeaksListener {

    // User states
    private int U_IDLE = 11;
    private int U_SPEAKING = 12;
    private int U_DATA_ANALYSIS = 13;
    private int U_SPOKEN_TEXT = 14;

    private int recordingState = U_IDLE; // default
    private String recordedPath; // default

    private ArrayList<ConversationItem> conversationItems = new ArrayList<>();
    private ConversationLearnAdapter conversationLearnAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private FloatingActionButton mic_ib;
    private TextView mic_status_text;
    private DynamicSineWaveView wavesView;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_conversation_learn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecorderUtility.Companion.getInstance();
        initView(view);
    }

    private void initView(View view) {

        mic_status_text = view.findViewById(R.id.mic_status_text);
        mic_ib = view.findViewById(R.id.mic_ib);

        recyclerView = view.findViewById(R.id.conversation_rv);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        conversationLearnAdapter = new ConversationLearnAdapter(conversationItems, this);
        recyclerView.setAdapter(conversationLearnAdapter);

        float stroke = Util.Companion.dipToPixels(getContext(), 2);
        wavesView = view.findViewById(R.id.view_sine_wave);
        wavesView.addWave(0.5f, 0.5f, 0, 0, 0); // Fist wave is for the shape of other waves.
        wavesView.addWave(0.5f, 2f, 0.5f, getResources().getColor(android.R.color.holo_red_dark), stroke);
        wavesView.addWave(0.1f, 2f, 0.7f, getResources().getColor(android.R.color.holo_blue_dark), stroke);
        wavesView.setBaseWaveAmplitudeScale(1);

        Button dummy_persona_button = view.findViewById(R.id.dummy_persona_button);
        dummy_persona_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addPersonaDummyItem();
            }
        });

        mic_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (recordingState == U_IDLE) {

                    recordingState = U_SPEAKING;
                    displayMicStatus();
                    recordedPath = Util.Companion.generateRandomWAVFileName();
                    RecorderUtility.Companion.getInstance().startRecording(recordedPath);
                    final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.ding_sound_speak);
                    mp.start();

//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            // record after ding sound
//                            RecorderUtility.Companion.getInstance().startRecording(recordedPath);
//                        }
//                    },2500);

                } else if (recordingState == U_SPEAKING) {

                    recordingState = U_DATA_ANALYSIS;
                    RecorderUtility.Companion.getInstance().stopRecording();
                    displayMicStatus();
                    analyseVoiceData();
                }
            }
        });

        displayMicStatus();
    }

    private void displayMicStatus() {

        if (recordingState == ConversationLearnAdapter.P_SPEAKING) {

            mic_status_text.setText("");
            mic_ib.setEnabled(false);
            mic_ib.setImageResource(R.drawable.mic_button);

        } else if (recordingState == U_IDLE) {

            mic_status_text.setText(R.string.tap_to_speak);
            mic_ib.setEnabled(true);
            mic_ib.setImageResource(R.drawable.mic_button);

        } else if (recordingState == U_SPEAKING) {

            mic_status_text.setText("");
            mic_ib.setEnabled(true);
            mic_ib.setImageResource(R.drawable.stop_self_speak);
            wavesView.startAnimation();
            wavesView.setVisibility(View.VISIBLE);

        } else if (recordingState == U_DATA_ANALYSIS) {

            mic_status_text.setText(R.string.analysing);
            mic_ib.setEnabled(false);
            mic_ib.setImageResource(R.drawable.mic_button);
            wavesView.stopAnimation();
            wavesView.setVisibility(View.INVISIBLE);

        } else if (recordingState == U_SPOKEN_TEXT) {

            // add Dummy Item with recorded data
            addUserDummyItem();
        }
    }

    private void analyseVoiceData() {

        // analyse anything dummy for few seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                recordingState = U_SPOKEN_TEXT;
                displayMicStatus();
            }
        }, 1000);
    }

    // DUMMY BLOCKs
    private void addPersonaDummyItem() {

        ConversationItem conversationItem = new ConversationItem();
        PersonaConvITem personaConvITem = new PersonaConvITem();
        personaConvITem.setVoiceFilePath("/storage/emulated/0/fluenso/Final_intro_session.wav");
        personaConvITem.setVoiceText("Please speak OnePlus");
        personaConvITem.setConversationState(ConversationLearnAdapter.P_SPEAKING);
        conversationItem.setPersonaConvITem(personaConvITem);
        conversationItem.setItemType(Util.PERSONA);

        conversationItems.add(conversationItem);
        conversationLearnAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(conversationLearnAdapter.getItemCount());
    }

    private void addUserDummyItem() {

        ConversationItem conversationItem = new ConversationItem();
        UserConvItem userConvItem = new UserConvItem();
        userConvItem.setVoiceFilePath(recordedPath);
        userConvItem.setVoiceText("OnePlus");
        userConvItem.setRating(3);
        userConvItem.setShowText(true);
        userConvItem.setConversationState(ConversationLearnAdapter.P_SPEAKING);
        userConvItem.setFeedBackText("good job");
        conversationItem.setUserConvItem(userConvItem);
        conversationItem.setItemType(Util.USER);

        conversationItems.add(conversationItem);
        conversationLearnAdapter.notifyDataSetChanged();
        linearLayoutManager.scrollToPositionWithOffset(conversationLearnAdapter.getItemCount(),0);

        recordingState = U_IDLE;
        displayMicStatus();
    }

    @Override
    public void onPersonaStatusChange(int personaStatus) {

        // TODO Add more filter based on conditions

        if (personaStatus == ConversationLearnAdapter.P_SPEAKING) {
            recordingState = personaStatus;
            displayMicStatus();
        } else {
            recordingState = U_IDLE;
            displayMicStatus();
        }
    }
}
