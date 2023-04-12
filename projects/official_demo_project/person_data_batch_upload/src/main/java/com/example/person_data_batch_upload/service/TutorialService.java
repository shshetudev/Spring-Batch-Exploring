//package com.example.person_data_batch_upload.service;
//
//import com.example.person_data_batch_upload.model.dto.TutorialAddDTO;
//import com.example.person_data_batch_upload.model.entity.Tutorial;
//import com.example.person_data_batch_upload.repository.TutorialRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class TutorialService {
//    private TutorialRepository tutorialRepository;
//
//    public Tutorial addTutorial(TutorialAddDTO tutorial) {
//        // todo: Change it using model mapper
//        Tutorial newTutorial = new Tutorial();
//        newTutorial.setTitle(tutorial.getTitle());
//
//        Tutorial persistedTutorial = tutorialRepository.save(newTutorial);
//        log.info("Persisted tutorial: {}", persistedTutorial);
//        return newTutorial;
//    }
//}
