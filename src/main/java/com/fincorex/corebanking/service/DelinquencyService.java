package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.DelinquencyProfileRqDTO;
import com.fincorex.corebanking.dto.DelinquencyProfileRsDTO;
import com.fincorex.corebanking.dto.DelinquencyStageRqDTO;
import com.fincorex.corebanking.dto.DelinquencyStageRsDTO;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.DelinquencyProfileNotFoundException;
import com.fincorex.corebanking.exception.DelinquencyStageNotFoundException;

import java.util.List;

public interface DelinquencyService {
    public DelinquencyProfileRsDTO createDelinquencyProfile(DelinquencyProfileRqDTO delinquencyProfileRqDTO);
    public DelinquencyProfileRsDTO fetchDelinquencyDetails(Long delinquencyProfileID) throws DelinquencyProfileNotFoundException;
    public DelinquencyProfileRsDTO updateDelinquencyProfile(Long delinquencyProfileID, DelinquencyProfileRqDTO delinquencyProfileRqDTO) throws DelinquencyProfileNotFoundException;
    public DelinquencyStageRsDTO createDelinquencyStage(DelinquencyStageRqDTO delinquencyStageRqDTO) throws DelinquencyProfileNotFoundException, BadRequestException;
    public List<DelinquencyStageRsDTO> createBulkDelinquencyStage(List<DelinquencyStageRqDTO> delinquencyStageRqDTOList) throws BadRequestException, DelinquencyProfileNotFoundException;
    public DelinquencyStageRsDTO fetchDelinquencyStage(Long delinquencyStageID) throws DelinquencyStageNotFoundException;
    public DelinquencyStageRsDTO updateDelinquencyStage(Long delinquencyStageID, DelinquencyStageRqDTO delinquencyStageRqDTO) throws DelinquencyStageNotFoundException, BadRequestException;
    public String deleteDelinquencyStage(Long delinquencyStageID) throws DelinquencyStageNotFoundException, BadRequestException;
}
