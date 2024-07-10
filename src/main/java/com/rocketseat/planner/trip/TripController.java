package com.rocketseat.planner.trip;

import com.rocketseat.planner.activity.ActivityData;
import com.rocketseat.planner.activity.ActivityRequestPayload;
import com.rocketseat.planner.activity.ActivityResponse;
import com.rocketseat.planner.activity.ActivityService;
import com.rocketseat.planner.link.LinkData;
import com.rocketseat.planner.link.LinkRequestPayload;
import com.rocketseat.planner.link.LinkResponse;
import com.rocketseat.planner.link.LinkService;
import com.rocketseat.planner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private LinkService linkService;

    @Autowired
    private TripRepository repository;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload){
        Trip newTrip = new Trip(payload);

        this.repository.save(newTrip);
        this.participantService.RegisterParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID tripId){
        Optional<Trip> trip = this.repository.findById(tripId);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID tripId, @RequestBody TripRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(tripId);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setDestination(payload.destination());
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));

            this.repository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{tripId}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID tripId){

        Optional<Trip> trip = this.repository.findById(tripId);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.repository.save(rawTrip);

            participantService.TriggerConfirmationEmailToParticipant(tripId);
            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{tripId}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID tripId, @RequestBody ParticipantRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(tripId);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            ParticipantCreateResponse participantCreateResponse = this.participantService.RegisterParticipantToEvent(payload.email(), rawTrip);
            if(rawTrip.getIsConfirmed()){
                this.participantService.TriggerConfirmationEmailToParticipant(payload.email());
            }
            return ResponseEntity.ok(participantCreateResponse);

        }

        return ResponseEntity.notFound().build();

    }

    @GetMapping("/{tripId}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID tripId){
        List<ParticipantData> participantsList = this.participantService.getAllParticipantsFromEvent(tripId);
        return ResponseEntity.ok(participantsList);
    }

    @PostMapping("/{tripId}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID tripId, @RequestBody ActivityRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(tripId);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            ActivityResponse activityResponse = this.activityService.registerActivity(payload, rawTrip);
            return ResponseEntity.ok(activityResponse);

        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{tripId}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID tripId){
        return ResponseEntity.ok(this.activityService.getAllActivitiesFromEvent(tripId));
    }

    @PostMapping("/{tripId}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID tripId, @RequestBody LinkRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(tripId);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            LinkResponse linkResponse = this.linkService.registerLink(payload, rawTrip);
            return ResponseEntity.ok(linkResponse);

        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{tripId}/links")
    public ResponseEntity<List<LinkData>> getAllLink(@PathVariable UUID tripId){
        Optional<Trip> trip = this.repository.findById(tripId);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            List<LinkData> linkData = this.linkService.getAllLinksFromEvent(rawTrip.getId());
            return ResponseEntity.ok(linkData);

        }

        return ResponseEntity.notFound().build();
    }
}
