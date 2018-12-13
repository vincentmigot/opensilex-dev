//******************************************************************************
//                                 EventDTO.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 13 nov. 2018
// Contact: andreas.garcia@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.resources.dto.event;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import phis2ws.service.configuration.DateFormat;
import phis2ws.service.configuration.DateFormats;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;
import phis2ws.service.utils.dates.Dates;
import phis2ws.service.view.model.phis.Event;

/**
 * DTO representing an event
 * 
 * @author Andréas Garcia<andreas.garcia@inra.fr>
 */
public class EventDTO extends AbstractVerifiedClass {
    
    private final String uri;
    private final String type;
    private final String concerns;
    private final String dateTimeString;
    
    /**
     * Constructor to create DTO from an Event model
     * @param event 
     */
    public EventDTO(Event event) {
        this.uri = event.getUri();
        this.type = event.getType();
        this.concerns = event.getConcerns();
        
        DateTimeFormatter dateTimeJsonFormatter = DateTimeFormat.forPattern(
                DateFormats.DATETIME_JSON_SERIALISATION_FORMAT);
        this.dateTimeString = dateTimeJsonFormatter.print(event.getDateTime());
    }

    /**
     * Generates an event model from de DTO
     * @return the Event model
     */
    @Override
    public Event createObjectFromDTO() {
        return new Event(this.uri, this.type, this.concerns
                , Dates.stringToDateTimeWithGivenPattern(
                    this.dateTimeString, DateFormats.DATETIME_JSON_SERIALISATION_FORMAT));
    }
}
