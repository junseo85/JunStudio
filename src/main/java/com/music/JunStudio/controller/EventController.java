package com.music.JunStudio.controller;

import com.music.JunStudio.model.Event;
import com.music.JunStudio.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller//return THymeleaf template
@RequestMapping("/api/event")
public class EventController {
    @Autowired
    private EventRepository eventRepository;

    @PostMapping("/add")
    @ResponseBody
    public String addEvent(Event event){
        eventRepository.save(event);
        return "Event added successfully!";
    }


    @RequestMapping("/all")
    @ResponseBody
    public Iterable<Event> getAllEvents(){
        return eventRepository.findAll();
    }

    @GetMapping
    public String showEventSchedule(Model model){
        model.addAttribute("events", eventRepository.findAll());
        return "event";
    }

}
