package com.rocketseat.planner.participant;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantService {
    public void RegisterParticipantsToEvent(
            List<String> participantsToInvite,
            UUID tripID){}

    public void TriggerConfirmationEmailToParticipant(UUID tripId){}
}
