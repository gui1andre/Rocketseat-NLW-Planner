package com.rocketseat.planner.link;

import com.rocketseat.planner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LinkService {

    @Autowired
    LinkRepository repository;

    public LinkResponse registerLink(LinkRequestPayload payload, Trip trip){
        Link newLink = new Link(payload.title(), payload.url(), trip);
        this.repository.save(newLink);
        return new LinkResponse(newLink.getId());
    }

    public List<LinkData> getAllLinksFromEvent(UUID tripID){
        return this.repository.findByTripId(tripID).stream().map(link -> new LinkData(link.getTitle(), link.getUrl())).toList();
    }
}
