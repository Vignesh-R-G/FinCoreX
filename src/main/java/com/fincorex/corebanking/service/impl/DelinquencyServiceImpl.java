package com.fincorex.corebanking.service.impl;

import com.fincorex.corebanking.dto.DelinquencyProfileRqDTO;
import com.fincorex.corebanking.dto.DelinquencyProfileRsDTO;
import com.fincorex.corebanking.dto.DelinquencyStageRqDTO;
import com.fincorex.corebanking.dto.DelinquencyStageRsDTO;
import com.fincorex.corebanking.entity.DelinquencyFeature;
import com.fincorex.corebanking.entity.DelinquencyStage;
import com.fincorex.corebanking.entity.LoanDetails;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.DelinquencyProfileNotFoundException;
import com.fincorex.corebanking.exception.DelinquencyStageNotFoundException;
import com.fincorex.corebanking.repository.DelinquencyFeatureRepo;
import com.fincorex.corebanking.repository.DelinquencyStageRepo;
import com.fincorex.corebanking.repository.LoanDetailsRepo;
import com.fincorex.corebanking.service.DelinquencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DelinquencyServiceImpl implements DelinquencyService {

    @Autowired
    private DelinquencyFeatureRepo delinquencyFeatureRepo;

    @Autowired
    private DelinquencyStageRepo delinquencyStageRepo;

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public DelinquencyProfileRsDTO createDelinquencyProfile(DelinquencyProfileRqDTO delinquencyProfileRqDTO) {
        DelinquencyFeature delinquencyFeature = DelinquencyFeature.builder()
                .profileDescription(delinquencyProfileRqDTO.getProfileDescription())
                .build();

        return buildDelinquencyProfileRsDTO(delinquencyFeatureRepo.save(delinquencyFeature));
    }

    @Override
    public DelinquencyProfileRsDTO fetchDelinquencyDetails(Long delinquencyProfileID) throws DelinquencyProfileNotFoundException {
        Optional<DelinquencyFeature> delinquencyFeature = delinquencyFeatureRepo.findById(delinquencyProfileID);
        if(delinquencyFeature.isEmpty())
            throw new DelinquencyProfileNotFoundException("Delinquency Profile Not Found");
        return buildDelinquencyProfileRsDTO(delinquencyFeature.get());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public DelinquencyProfileRsDTO updateDelinquencyProfile(Long delinquencyProfileID, DelinquencyProfileRqDTO delinquencyProfileRqDTO) throws DelinquencyProfileNotFoundException {
        Optional<DelinquencyFeature> delinquencyFeatureOptional = delinquencyFeatureRepo.findById(delinquencyProfileID);
        if(delinquencyFeatureOptional.isEmpty())
            throw new DelinquencyProfileNotFoundException("Delinquency Profile Not Found");
        DelinquencyFeature delinquencyFeature = delinquencyFeatureOptional.get();
        delinquencyFeature.setProfileDescription(delinquencyProfileRqDTO.getProfileDescription());
        return buildDelinquencyProfileRsDTO(delinquencyFeatureRepo.save(delinquencyFeature));
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public DelinquencyStageRsDTO createDelinquencyStage(DelinquencyStageRqDTO delinquencyStageRqDTO) throws DelinquencyProfileNotFoundException, BadRequestException {
        Optional<DelinquencyFeature> delinquencyFeature = delinquencyFeatureRepo.findById(delinquencyStageRqDTO.getDelinquencyProfileID());
        if(delinquencyFeature.isEmpty())
            throw new DelinquencyProfileNotFoundException("Delinquency Profile Not Found");
        validateOverDueDays(delinquencyStageRqDTO.getOverDueDays(), delinquencyFeature.get(), null);

        DelinquencyStage delinquencyStage = DelinquencyStage.builder()
                .stageDescription(delinquencyStageRqDTO.getStageDescription())
                .overDueDays(delinquencyStageRqDTO.getOverDueDays())
                .delinquencyFeature(delinquencyFeature.get())
                .build();
        return buildDelinquencyStageRsDTO(delinquencyStageRepo.save(delinquencyStage));
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public List<DelinquencyStageRsDTO> createBulkDelinquencyStage(List<DelinquencyStageRqDTO> delinquencyStageRqDTOList) throws BadRequestException, DelinquencyProfileNotFoundException {
        if(delinquencyStageRqDTOList == null || delinquencyStageRqDTOList.isEmpty())
            throw new BadRequestException("Delinquency Stage List should not be empty for the Delinquency Stage creation");
        List<DelinquencyStageRsDTO> delinquencyStageRsDTOList = new ArrayList<>();
        for(DelinquencyStageRqDTO delinquencyStageRqDTO : delinquencyStageRqDTOList){
            Optional<DelinquencyFeature> delinquencyFeature = delinquencyFeatureRepo.findById(delinquencyStageRqDTO.getDelinquencyProfileID());
            if(delinquencyFeature.isEmpty())
                throw new DelinquencyProfileNotFoundException("Delinquency Profile "+ delinquencyStageRqDTO.getDelinquencyProfileID()+" Not Found");
            validateOverDueDays(delinquencyStageRqDTO.getOverDueDays(), delinquencyFeature.get(), null);
            DelinquencyStage delinquencyStage = DelinquencyStage.builder()
                    .stageDescription(delinquencyStageRqDTO.getStageDescription())
                    .overDueDays(delinquencyStageRqDTO.getOverDueDays())
                    .delinquencyFeature(delinquencyFeature.get())
                    .build();
            delinquencyStageRsDTOList.add(buildDelinquencyStageRsDTO(delinquencyStageRepo.save(delinquencyStage)));
        }
        return delinquencyStageRsDTOList;
    }

    @Override
    public DelinquencyStageRsDTO fetchDelinquencyStage(Long delinquencyStageID) throws DelinquencyStageNotFoundException {
        Optional<DelinquencyStage> delinquencyStage = delinquencyStageRepo.findById(delinquencyStageID);
        if(delinquencyStage.isEmpty())
            throw new DelinquencyStageNotFoundException("Delinquency Stage Not Found");
        return buildDelinquencyStageRsDTO(delinquencyStage.get());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public DelinquencyStageRsDTO updateDelinquencyStage(Long delinquencyStageID, DelinquencyStageRqDTO delinquencyStageRqDTO) throws DelinquencyStageNotFoundException, BadRequestException {
        Optional<DelinquencyStage> delinquencyStageOptional = delinquencyStageRepo.findById(delinquencyStageID);
        if(delinquencyStageOptional.isEmpty())
            throw new DelinquencyStageNotFoundException("Delinquency Stage Not Found");
        DelinquencyStage delinquencyStage = delinquencyStageOptional.get();
        if(!delinquencyStage.getDelinquencyFeature().getDelinquencyProfileID().equals(delinquencyStageRqDTO.getDelinquencyProfileID())){
            throw new BadRequestException("Delinquency Profile cannot be updated for a Delinquency Stage");
        }
        validateOverDueDays(delinquencyStageRqDTO.getOverDueDays(), delinquencyStage.getDelinquencyFeature(), delinquencyStage.getStageID());
        delinquencyStage.setStageDescription(delinquencyStageRqDTO.getStageDescription());
        delinquencyStage.setOverDueDays(delinquencyStageRqDTO.getOverDueDays());
        return buildDelinquencyStageRsDTO(delinquencyStageRepo.save(delinquencyStage));
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public String deleteDelinquencyStage(Long delinquencyStageID) throws DelinquencyStageNotFoundException, BadRequestException {
        Optional<DelinquencyStage> delinquencyStageOptional = delinquencyStageRepo.findById(delinquencyStageID);
        if(delinquencyStageOptional.isEmpty())
            throw new DelinquencyStageNotFoundException("Delinquency Stage Not Found");
        DelinquencyStage delinquencyStage = delinquencyStageOptional.get();
        LoanDetails loanDetails = loanDetailsRepo.findByDelinquencyStage(delinquencyStage);
        if(loanDetails != null)
            throw new BadRequestException("This delinquency stage is already marked for an account. So, it cannot be deleted");
        delinquencyStageRepo.delete(delinquencyStage);
        return "Delinquency Stage Deleted Successfully";
    }

    public void validateOverDueDays(Long overDueDays, DelinquencyFeature delinquencyFeature, Long stageID) throws BadRequestException {
        for(DelinquencyStage delinquencyStage : delinquencyFeature.getDelinquencyStages()){
            if(!delinquencyStage.getStageID().equals(stageID) && delinquencyStage.getOverDueDays().equals(overDueDays))
                throw new BadRequestException("The requested overdue days has already been configured with different Delinquency Stage in this Delinquency Profile");
        }
    }

    public DelinquencyProfileRsDTO buildDelinquencyProfileRsDTO(DelinquencyFeature delinquencyFeature){
        return DelinquencyProfileRsDTO.builder()
                .delinquencyProfileID(delinquencyFeature.getDelinquencyProfileID())
                .profileDescription(delinquencyFeature.getProfileDescription())
                .delinquencyStages(delinquencyFeature.getDelinquencyStages().stream()
                        .map(this::buildDelinquencyStageRsDTO).collect(Collectors.toList()))
                .build();
    }

    public DelinquencyStageRsDTO buildDelinquencyStageRsDTO(DelinquencyStage delinquencyStage){
        return DelinquencyStageRsDTO.builder()
                .stageID(delinquencyStage.getStageID())
                .stageDescription(delinquencyStage.getStageDescription())
                .overDueDays(delinquencyStage.getOverDueDays())
                .build();
    }
}
