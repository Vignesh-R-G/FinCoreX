package com.fincorex.corebanking.service.impl;

import com.fincorex.corebanking.dto.*;
import com.fincorex.corebanking.entity.*;
import com.fincorex.corebanking.enums.LoanStatus;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.enums.TransactionCode;
import com.fincorex.corebanking.exception.*;
import com.fincorex.corebanking.repository.*;
import com.fincorex.corebanking.service.AccountService;
import com.fincorex.corebanking.service.LoanService;
import com.fincorex.corebanking.service.TransactionService;
import com.fincorex.corebanking.utils.BusinessDateUtil;
import com.fincorex.corebanking.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {
    @Autowired
    private AccountService accountService;

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;

    @Autowired
    private DelinquencyFeatureRepo delinquencyFeatureRepo;

    @Autowired
    private LoanRepaymentsRepo loanRepaymentsRepo;

    @Autowired
    private LoanScheduleRepo loanScheduleRepo;

    @Autowired
    private BusinessDateUtil businessDateUtil;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DelinquencyServiceImpl delinquencyService;

    @Autowired
    private TransactionUtils transactionUtils;

    @Autowired
    private LendingBlocksRepo lendingBlocksRepo;


    @Transactional(rollbackFor = {
            Exception.class
    })
    @Override
    public LoanAccountRsDTO establishLoan(LoanEstablishmentRqDTO loanEstablishmentRqDTO) throws SubProductNotFoundException, BranchNotFoundException, CustomerNotFoundException, BadRequestException, DelinquencyProfileNotFoundException, AccountNotFoundException {
        // Loan Establishment Details Validation
        validateLookAheadDays(loanEstablishmentRqDTO.getLookAheadDays());
        validateManualLoanSchedule(loanEstablishmentRqDTO.getLoanScheduleRqDTOList(), loanEstablishmentRqDTO.getLoanAmount(), loanEstablishmentRqDTO.getFixedInterestAmount(), loanEstablishmentRqDTO.getLoanTermInMonths());
        validateCollectionOrderProfile(loanEstablishmentRqDTO.getCollectionOrderProfile(), loanEstablishmentRqDTO.getCollectionOrderType());

        Optional<DelinquencyFeature> delinquencyFeature = delinquencyFeatureRepo.findById(loanEstablishmentRqDTO.getDelinquencyProfileID());
        if(delinquencyFeature.isEmpty())
            throw new DelinquencyProfileNotFoundException("Delinquency Profile Not Found");

        Optional<Account> settlementAccount = accountRepo.findById(loanEstablishmentRqDTO.getSettlementAccount());
        if(settlementAccount.isEmpty())
            throw new AccountNotFoundException("Settlement Account Not Found");
        Optional<Account> disbursementAccount = accountRepo.findById(loanEstablishmentRqDTO.getSettlementAccount());
        if(disbursementAccount.isEmpty())
            throw new AccountNotFoundException("Disbursement Account Not Found");

        String settlementAccProductType = settlementAccount.get().getSubProduct().getProduct().getProductType();
        if(!settlementAccProductType.equals(ProductType.SA.name()))
            throw new BadRequestException("Settlement Account should be a savings Account");

        String disbursementAccProductType = disbursementAccount.get().getSubProduct().getProduct().getProductType();
        if(!disbursementAccProductType.equals(ProductType.SA.name()))
            throw new BadRequestException("Disbursement Account should be a savings Account");

        // Open Account Service
        AccountRsDTO accountRsDTO =  accountService.openAccount(loanEstablishmentRqDTO.getOpenAccountRqDTO(), Boolean.TRUE, Boolean.FALSE);

        Account account = accountRepo.findById(accountRsDTO.getAccountID()).get();
        Date businessDate = businessDateUtil.getCurrentBusinessDate();

        // Loan Details Instance Creation
        LoanDetails loanDetails = LoanDetails.builder()
                .loanAccountID(accountRsDTO.getAccountID())
                .loanAmount(loanEstablishmentRqDTO.getLoanAmount())
                .loanTermInMonths(loanEstablishmentRqDTO.getLoanTermInMonths())
                .fixedInterestAmount(loanEstablishmentRqDTO.getFixedInterestAmount())
                .loanStatus(LoanStatus.NORMAL.name())
                .lookAheadDays(loanEstablishmentRqDTO.getLookAheadDays())
                .delinquencyFeature(delinquencyFeature.get())
                .loanStartDate(businessDate)
                .loanMaturityDate(calculateMaturityDate(businessDate, loanEstablishmentRqDTO.getLoanTermInMonths()))
                .settlementAccount(settlementAccount.get())
                .disbursementAccount(disbursementAccount.get())
                .collectionOrderProfile(loanEstablishmentRqDTO.getCollectionOrderProfile())
                .collectionOrderType(loanEstablishmentRqDTO.getCollectionOrderType())
                .account(account)
                .build();
        loanDetails = loanDetailsRepo.save(loanDetails);

        // Loan Disbursement
        processLoanDisbursement(loanDetails);

        // Loan Schedule Generation
        buildLoanSchedule(loanDetails, loanEstablishmentRqDTO.getLoanScheduleRqDTOList());

        loanDetails = loanDetailsRepo.findById(accountRsDTO.getAccountID()).get();
        return buildLoanAccountRsDTO(loanDetails);
    }

    @Override
    public LoanAccountRsDTO fetchLoanDetails(String loanAccountID) throws BadRequestException {
        Optional<LoanDetails> loanDetails = loanDetailsRepo.findById(loanAccountID);
        if(loanDetails.isEmpty())
            throw new BadRequestException("Invalid Loan Account");
        return buildLoanAccountRsDTO(loanDetails.get());
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public String settleLoanAccount(String loanAccountID) throws BadRequestException, AccountNotFoundException {
        Optional<LoanDetails> loanDetailsOptional = loanDetailsRepo.findById(loanAccountID);
        if(loanDetailsOptional.isEmpty()){
            throw new BadRequestException("Invalid Loan Account");
        }

        LoanDetails loanDetails = loanDetailsOptional.get();

        if(loanDetails.getLoanStatus().equals(LoanStatus.SETTLED.name()) || loanDetails.getLoanStatus().equals(LoanStatus.COMPLETED.name()))
            throw new BadRequestException("Loan is already settled or completed");

        // Unblock Existing Block
        Optional<LendingBlocks> lendingBlocksOptional = lendingBlocksRepo.findById(loanAccountID);
        if(lendingBlocksOptional.isPresent()){
            LendingBlocks lendingBlocks = lendingBlocksOptional.get();
            UnBlockTransactionDTO unBlockTransactionDTO = UnBlockTransactionDTO.builder()
                    .accountID(lendingBlocks.getSettlementAccountID())
                    .unBlockAmount(lendingBlocks.getBlockAmount())
                    .build();
            transactionService.unBlockAmount(unBlockTransactionDTO);
        }

        Account account = accountRepo.findById(loanDetails.getSettlementAccount().getAccountID()).get();
        BigDecimal availableBalance = account.getClearedBalance().subtract(account.getBlockedBalance());

        // Persist Missed Repayments
        persistMissedRepayments(loanDetails);

        BigDecimal totalArrearsAmount = fetchTotalUnpaidAmount(loanDetails);
        BigDecimal totalOutstandingAmount = fetchOutstandingAmount(loanDetails);
        BigDecimal settlementAmount = totalArrearsAmount.add(totalOutstandingAmount);

        if(availableBalance.compareTo(settlementAmount) < 0)
            throw new BadRequestException("Available Balance is lesser than the Settlement Amount");

        // Settlement Transaction
        processLoanRepayment(loanDetails, settlementAmount);

        // Removal of lending blocks if it is present
        lendingBlocksOptional.ifPresent(lendingBlocks -> lendingBlocksRepo.delete(lendingBlocks));

        // Update Loan Status
        updateLoanStatus(loanAccountID, LoanStatus.SETTLED);

        return "Loan Account Settlement is Successful for this Account ID : "+loanAccountID;
    }

    private void validateLookAheadDays(long lookAheadDays) throws BadRequestException {
        if(lookAheadDays < 0 || lookAheadDays > 30)
            throw new BadRequestException("Look Ahead Days should be in the following range [0,30]");
    }

    public void validateManualLoanSchedule(List<LoanScheduleRqDTO> loanScheduleRqDTOList, BigDecimal outstandingAmount, BigDecimal fixedInterestAmount, long totalTerm) throws BadRequestException {
        if(loanScheduleRqDTOList != null && !loanScheduleRqDTOList.isEmpty()){
            if(loanScheduleRqDTOList.size() != totalTerm)
                throw new BadRequestException("Given Loan Schedule is Invalid.Total Number of Repayments request should be equal to the Total Term");

            for (LoanScheduleRqDTO loanScheduleRqDTO : loanScheduleRqDTOList){
                outstandingAmount = outstandingAmount.add(loanScheduleRqDTO.getPrincipalDue());
                fixedInterestAmount = fixedInterestAmount.subtract(loanScheduleRqDTO.getInterestDue());
            }

            if(!outstandingAmount.equals(BigDecimal.ZERO))
                throw new BadRequestException("Given Loan Schedule is Invalid.Total Principal Due is not equal to the Total Outstanding Amount");
            if(!fixedInterestAmount.equals(BigDecimal.ZERO))
                throw new BadRequestException("Given Loan Schedule is Invalid.Total Interest Due is not equal to the Total Fixed Interest Amount");
        }
    }

    private void validateCollectionOrderProfile(String collectionOrderProfile, Character collectionOrderType) throws BadRequestException {
        List<Character> components = Arrays.asList('P','I');
        Set<Character> collectionProfile = new HashSet<>();
        if(collectionOrderType != 'H' && collectionOrderType != 'V')
            throw new BadRequestException("Collection Order Type is Invalid");

        for(Character component : collectionOrderProfile.toCharArray()){
            if(!components.contains(component))
                throw new BadRequestException("Collection Order Profile is Invalid. Eligible Components are : "+components);
            collectionProfile.add(component);
        }
        if(collectionProfile.size() != components.size())
            throw new BadRequestException("Collection Order Profile is Invalid. All of these components should present : "+ components);
    }

    private void processLoanDisbursement(LoanDetails loanDetails) throws BadRequestException, AccountNotFoundException {
        List<TransactionDetailsRqDTO> transactionDetailsRqDTOList = new ArrayList<>();
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(loanDetails.getLoanAccountID(), loanDetails.getLoanAmount(), 'D'));
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(loanDetails.getDisbursementAccount().getAccountID(), loanDetails.getLoanAmount().abs(), 'C'));

        TransactionRqDTO transactionRqDTO = TransactionRqDTO.builder()
                .transactionDetails(transactionDetailsRqDTOList)
                .build();
        transactionService.processTransaction(transactionRqDTO, TransactionCode.LD0);
    }

    public void processLoanRepayment(LoanDetails loanDetails, BigDecimal repaymentAmount) throws BadRequestException, AccountNotFoundException {
        BigDecimal totalInterestAmountPaid = BigDecimal.ZERO;
        // Additional Payments will not be appropriated
        BigDecimal totalArrearsAmount = fetchTotalUnpaidAmount(loanDetails);
        if(repaymentAmount.compareTo(totalArrearsAmount) <= 0)
            totalInterestAmountPaid = handleLoanAppropriation(loanDetails, repaymentAmount);
        else {
            // Additional Payment
            totalInterestAmountPaid = handleLoanAppropriation(loanDetails, totalArrearsAmount).add(fetchInterestOutstandingAmount(loanDetails));
        }

        // Repayment Transaction
        List<TransactionDetailsRqDTO> transactionDetailsRqDTOList = new ArrayList<>();
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(loanDetails.getSettlementAccount().getAccountID(), repaymentAmount.negate(), 'D'));
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(loanDetails.getLoanAccountID(), repaymentAmount, 'C'));
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(loanDetails.getLoanAccountID(), totalInterestAmountPaid.negate(), 'D'));
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(loanDetails.getAccount().getSubProduct().getGlAccount(),totalInterestAmountPaid, 'C'));


        TransactionRqDTO transactionRqDTO = TransactionRqDTO.builder()
                .transactionDetails(transactionDetailsRqDTOList)
                .build();
        transactionService.processTransaction(transactionRqDTO, TransactionCode.RPO);

        loanDetails.setFixedInterestAmount(loanDetails.getFixedInterestAmount().subtract(totalInterestAmountPaid));
        loanDetailsRepo.save(loanDetails);
    }

    public BigDecimal handleLoanAppropriation(LoanDetails loanDetails, BigDecimal repaymentAmount) throws BadRequestException, AccountNotFoundException {
        String collectionOrderProfile = loanDetails.getCollectionOrderProfile();
        Character collectionOrderType = loanDetails.getCollectionOrderType();

        List<LoanRepayments> loanRepayments = loanRepaymentsRepo.findAllByLoanDetailsOrderByRepaymentNumberDesc(loanDetails);

        Map<Long, Map<String, BigDecimal>> repaymentMap = constructLoanRepaymentMap(loanRepayments);
        BigDecimal totalInterestAmountPaid = BigDecimal.ZERO;

        if(collectionOrderType == 'H'){
            // Horizontal Collection
            for(Long repaymentNumber : repaymentMap.keySet()){
                if(repaymentMap.get(repaymentNumber).get("REPAYMENT_OVERDUE").compareTo(BigDecimal.ZERO) == 0)
                    continue;
                for(Character component : collectionOrderProfile.toCharArray()){
                    BigDecimal amountRepaid = appropriateBasedOnComponent(repaymentMap.get(repaymentNumber), component, repaymentAmount);
                    if(component == 'I')
                        totalInterestAmountPaid = totalInterestAmountPaid.add(amountRepaid);
                    repaymentAmount = repaymentAmount.subtract(amountRepaid);
                    if(repaymentAmount.compareTo(BigDecimal.ZERO) == 0)
                        break;
                }
            }
        } else if (collectionOrderType == 'V') {
            // Vertical Collection
            for(Character component : collectionOrderProfile.toCharArray()) {
                for(Long repaymentNumber : repaymentMap.keySet()){
                    if(repaymentMap.get(repaymentNumber).get("REPAYMENT_OVERDUE").compareTo(BigDecimal.ZERO) == 0)
                        continue;
                    BigDecimal amountRepaid = appropriateBasedOnComponent(repaymentMap.get(repaymentNumber), component, repaymentAmount);
                    if(component == 'I')
                        totalInterestAmountPaid = totalInterestAmountPaid.add(amountRepaid);
                    repaymentAmount = repaymentAmount.subtract(amountRepaid);
                    if(repaymentAmount.compareTo(BigDecimal.ZERO) == 0)
                        break;
                }
            }
        }

        // Loan Repayments Update
        for(Long repaymentNumber : repaymentMap.keySet()){
            LoanRepayments loanRepayment = loanRepaymentsRepo.findByLoanDetailsAndRepaymentNumber(loanDetails, repaymentNumber);
            for(String componentName : repaymentMap.get(repaymentNumber).keySet()){
                if(componentName.equals("PRINCIPAL_DUE"))
                    loanRepayment.setPrincipalDue(repaymentMap.get(repaymentNumber).get(componentName));
                if(componentName.equals("PRINCIPAL_PAID"))
                    loanRepayment.setPrincipalPaid(repaymentMap.get(repaymentNumber).get(componentName));
                if(componentName.equals("PRINCIPAL_OVERDUE"))
                    loanRepayment.setPrincipalOverDue(repaymentMap.get(repaymentNumber).get(componentName));
                if(componentName.equals("INTEREST_DUE"))
                    loanRepayment.setInterestDue(repaymentMap.get(repaymentNumber).get(componentName));
                if(componentName.equals("INTEREST_PAID"))
                    loanRepayment.setInterestPaid(repaymentMap.get(repaymentNumber).get(componentName));
                if(componentName.equals("INTEREST_OVERDUE"))
                    loanRepayment.setInterestOverDue(repaymentMap.get(repaymentNumber).get(componentName));
                if(componentName.equals("REPAYMENT_DUE"))
                    loanRepayment.setTotalRepaymentDue(repaymentMap.get(repaymentNumber).get(componentName));
                if(componentName.equals("REPAYMENT_PAID"))
                    loanRepayment.setTotalRepaymentPaid(repaymentMap.get(repaymentNumber).get(componentName));
                if(componentName.equals("REPAYMENT_OVERDUE"))
                    loanRepayment.setTotalRepaymentOverDue(repaymentMap.get(repaymentNumber).get(componentName));

            }
            loanRepaymentsRepo.save(loanRepayment);
        }

        return totalInterestAmountPaid;
    }

    public void persistMissedRepayments(LoanDetails loanDetails){
        List<LoanSchedule> loanSchedules = loanDetails.getLoanSchedules();
        Date businessDate = businessDateUtil.getCurrentBusinessDate();
        for(LoanSchedule loanSchedule : loanSchedules){
            if(loanSchedule.getRepaymentDate().toLocalDate().isBefore(businessDate.toLocalDate()) ||
                loanSchedule.getRepaymentDate().toLocalDate().isEqual(businessDate.toLocalDate())) {
                Long repaymentNumber = loanSchedule.getRepaymentNumber();
                LoanRepayments loanRepayment = loanRepaymentsRepo.findByLoanDetailsAndRepaymentNumber(loanDetails, repaymentNumber);
                if(loanRepayment == null){
                    LoanRepayments newLoanRepayment = LoanRepayments.builder()
                            .loanRepaymentID(UUID.randomUUID().toString().replace("-", "").substring(0, 10))
                            .loanDetails(loanDetails)
                            .repaymentNumber(repaymentNumber)
                            .repaymentDate(loanSchedule.getRepaymentDate())
                            .principalDue(loanSchedule.getPrincipalDue())
                            .principalPaid(BigDecimal.ZERO)
                            .principalOverDue(loanSchedule.getPrincipalDue())
                            .interestDue(loanSchedule.getInterestDue())
                            .interestPaid(BigDecimal.ZERO)
                            .interestOverDue(loanSchedule.getInterestDue())
                            .totalRepaymentDue(loanSchedule.getRepaymentDue())
                            .totalRepaymentPaid(BigDecimal.ZERO)
                            .totalRepaymentOverDue(loanSchedule.getRepaymentDue())
                            .build();
                    loanRepaymentsRepo.save(newLoanRepayment);
                }
            }
        }
    }

    private Map<Long, Map<String,BigDecimal>> constructLoanRepaymentMap(List<LoanRepayments> loanRepayments){
        Map<Long, Map<String,BigDecimal>> repaymentMap = new TreeMap<>();
        for(LoanRepayments loanRepayment : loanRepayments){
            Map<String,BigDecimal> map = new HashMap<>();
            map.put("PRINCIPAL_DUE", loanRepayment.getPrincipalDue());
            map.put("PRINCIPAL_PAID", loanRepayment.getPrincipalPaid());
            map.put("PRINCIPAL_OVERDUE", loanRepayment.getPrincipalOverDue());
            map.put("INTEREST_DUE", loanRepayment.getInterestDue());
            map.put("INTEREST_PAID", loanRepayment.getInterestPaid());
            map.put("INTEREST_OVERDUE", loanRepayment.getInterestOverDue());
            map.put("REPAYMENT_DUE", loanRepayment.getTotalRepaymentDue());
            map.put("REPAYMENT_PAID", loanRepayment.getTotalRepaymentPaid());
            map.put("REPAYMENT_OVERDUE", loanRepayment.getTotalRepaymentOverDue());
            repaymentMap.put(loanRepayment.getRepaymentNumber(), map);
        }
        return repaymentMap;
    }

    private BigDecimal appropriateBasedOnComponent(Map<String,BigDecimal> loanRepayment, Character component, BigDecimal remainingRepaymentAmt){
        BigDecimal amountToBePaid = BigDecimal.ZERO;
        if(component == 'P'){
            amountToBePaid = loanRepayment.get("PRINCIPAL_OVERDUE");
            if(remainingRepaymentAmt.compareTo(loanRepayment.get("PRINCIPAL_OVERDUE")) < 0) {
                amountToBePaid = remainingRepaymentAmt;
            }
            loanRepayment.put("PRINCIPAL_PAID", loanRepayment.get("PRINCIPAL_PAID").add(amountToBePaid));
            loanRepayment.put("PRINCIPAL_OVERDUE", loanRepayment.get("PRINCIPAL_OVERDUE").subtract(amountToBePaid));
            loanRepayment.put("REPAYMENT_PAID", loanRepayment.get("REPAYMENT_PAID").add(amountToBePaid));
            loanRepayment.put("REPAYMENT_OVERDUE", loanRepayment.get("REPAYMENT_OVERDUE").subtract(amountToBePaid));


        } else if (component == 'I'){
            amountToBePaid = loanRepayment.get("INTEREST_OVERDUE");
            if(remainingRepaymentAmt.compareTo(loanRepayment.get("INTEREST_OVERDUE")) < 0) {
                amountToBePaid = remainingRepaymentAmt;
            }
            loanRepayment.put("INTEREST_PAID", loanRepayment.get("INTEREST_PAID").add(amountToBePaid));
            loanRepayment.put("INTEREST_OVERDUE", loanRepayment.get("INTEREST_OVERDUE").subtract(amountToBePaid));
            loanRepayment.put("REPAYMENT_PAID", loanRepayment.get("REPAYMENT_PAID").add(amountToBePaid));
            loanRepayment.put("REPAYMENT_OVERDUE", loanRepayment.get("REPAYMENT_OVERDUE").subtract(amountToBePaid));
        }
        return amountToBePaid;
    }

    public void buildLoanSchedule(LoanDetails loanDetails, List<LoanScheduleRqDTO> loanScheduleRqDTOList){
        BigDecimal fixedInterestAmount = loanDetails.getFixedInterestAmount();
        BigDecimal outstandingAmount = loanDetails.getAccount().getClearedBalance();
        long totalTerm = loanDetails.getLoanTermInMonths();

        long repaymentNumber = fetchNextRepaymentNumber(loanDetails);
        long startRepaymentNumber = repaymentNumber;
        Date nextDueDate = fetchNextDueDate(loanDetails);
        BigDecimal interestRepayment = fixedInterestAmount.divide(BigDecimal.valueOf(totalTerm - repaymentNumber + 1),2,RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_DOWN);
        BigDecimal principalRepayment = outstandingAmount.abs().divide(BigDecimal.valueOf(totalTerm - repaymentNumber +1), 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_DOWN);

        int index = 0;
        List<LoanSchedule> loanScheduleList = new ArrayList<>();

        while(repaymentNumber <= totalTerm){
            // Manual Loan Schedule Requested
            if(loanScheduleRqDTOList != null && !loanScheduleRqDTOList.isEmpty()){
                interestRepayment = loanScheduleRqDTOList.get(index).getInterestDue();
                principalRepayment = loanScheduleRqDTOList.get(index).getPrincipalDue();
            } else if(repaymentNumber == totalTerm){
                interestRepayment = fixedInterestAmount.subtract(interestRepayment.multiply(BigDecimal.valueOf(totalTerm - startRepaymentNumber)));
                principalRepayment = outstandingAmount.abs().subtract(principalRepayment.multiply(BigDecimal.valueOf(totalTerm - startRepaymentNumber)));
            }

            // Schedule Re-generation
            LoanSchedule existingLoanSchedule = loanScheduleRepo.findByLoanDetailsAndRepaymentNumber(loanDetails, repaymentNumber);
            if(existingLoanSchedule != null){
                existingLoanSchedule.setInterestDue(interestRepayment);
                existingLoanSchedule.setPrincipalDue(principalRepayment);
                existingLoanSchedule.setRepaymentDue(interestRepayment.add(principalRepayment));
                loanScheduleRepo.save(existingLoanSchedule);
                repaymentNumber++;
                index++;
                nextDueDate = Date.valueOf(nextDueDate.toLocalDate().plusMonths(1));
                continue;
            }

            // Schedule Generation During Establishment
            LoanSchedule loanSchedule = LoanSchedule.builder()
                    .loanScheduleID( UUID.randomUUID().toString().replace("-", "").substring(0, 10))
                    .repaymentNumber(repaymentNumber)
                    .principalDue(principalRepayment)
                    .interestDue(interestRepayment)
                    .repaymentDue(interestRepayment.add(principalRepayment))
                    .repaymentDate(nextDueDate)
                    .loanDetails(loanDetails)
                    .build();
            loanScheduleList.add(loanScheduleRepo.save(loanSchedule));

            repaymentNumber++;
            index++;
            nextDueDate = Date.valueOf(nextDueDate.toLocalDate().plusMonths(1));
        }

        // Under same transaction, bi-directional mapping should be established manually
        loanDetails.setLoanSchedules(loanScheduleList);
        loanDetailsRepo.save(loanDetails);

    }

    public long fetchNextRepaymentNumber(LoanDetails loanDetails){
        long totalRepayments = loanRepaymentsRepo.countByLoanDetails(loanDetails);
        return totalRepayments+1;
    }

    public Date fetchNextDueDate(LoanDetails loanDetails) {
        List<LoanRepayments> loanRepayments = loanRepaymentsRepo.findAllByLoanDetailsOrderByRepaymentNumberDesc(loanDetails);
        // If Loan Repayment Found, then we need to calculate from the last repayment date
        if(!loanRepayments.isEmpty()){
            return Date.valueOf(loanRepayments.get(0).getRepaymentDate().toLocalDate().plusMonths(1));
        }

        // If No Loan Repayments Found, then calculate from the Loan Start Date
        return Date.valueOf(loanDetails.getLoanStartDate().toLocalDate().plusMonths(1));
    }

    private Date calculateMaturityDate(Date loanStartDate, long totalTerm){
        return Date.valueOf(loanStartDate.toLocalDate().plusMonths(totalTerm));
    }

    public BigDecimal fetchTotalUnpaidAmount(LoanDetails loanDetails) {
        List<LoanRepayments> loanRepayments = loanRepaymentsRepo.findAllByLoanDetailsOrderByRepaymentNumberDesc(loanDetails);
        BigDecimal totalArrearAmount = BigDecimal.ZERO;
        for(LoanRepayments loanRepayment : loanRepayments){
            totalArrearAmount = totalArrearAmount.add(loanRepayment.getTotalRepaymentOverDue());
        }
        return totalArrearAmount;
    }

    public BigDecimal fetchOutstandingAmount(LoanDetails loanDetails){
        List<LoanSchedule> loanScheduleList = loanDetails.getLoanSchedules();
        BigDecimal totalOutstandingAmount = BigDecimal.ZERO;
        for(LoanSchedule loanSchedule : loanScheduleList){
            if(loanSchedule.getRepaymentDate().toLocalDate().isAfter(businessDateUtil.getCurrentBusinessDate().toLocalDate()))
                totalOutstandingAmount = totalOutstandingAmount.add(loanSchedule.getRepaymentDue());
        }
        return totalOutstandingAmount;
    }

    public BigDecimal fetchInterestOutstandingAmount(LoanDetails loanDetails){
        List<LoanSchedule> loanScheduleList = loanDetails.getLoanSchedules();
        BigDecimal totalInterestOutstandingAmount = BigDecimal.ZERO;
        for(LoanSchedule loanSchedule : loanScheduleList){
            if(loanSchedule.getRepaymentDate().toLocalDate().isAfter(businessDateUtil.getCurrentBusinessDate().toLocalDate()))
                totalInterestOutstandingAmount = totalInterestOutstandingAmount.add(loanSchedule.getInterestDue());
        }
        return totalInterestOutstandingAmount;
    }

    public void updateLoanStatus(String loanAccountID, LoanStatus loanStatus){
        LoanDetails loanDetails = loanDetailsRepo.findById(loanAccountID).get();
        loanDetails.setLoanStatus(loanStatus.name());
        loanDetailsRepo.save(loanDetails);
    }

    public LoanScheduleRsDTO buildLoanScheduleRsDTO(LoanSchedule loanSchedule){
        return LoanScheduleRsDTO.builder()
                .repaymentNumber(loanSchedule.getRepaymentNumber())
                .principalDue(loanSchedule.getPrincipalDue())
                .interestDue(loanSchedule.getInterestDue())
                .repaymentDue(loanSchedule.getRepaymentDue())
                .repaymentDate(loanSchedule.getRepaymentDate())
                .build();
    }

    public LoanRepaymentsRsDTO buildLoanRepaymentRsDTO(LoanRepayments loanRepayments){
        return LoanRepaymentsRsDTO.builder()
                .repaymentNumber(loanRepayments.getRepaymentNumber())
                .repaymentDate(loanRepayments.getRepaymentDate())
                .principalDue(loanRepayments.getPrincipalDue())
                .principalPaid(loanRepayments.getPrincipalPaid())
                .principalOverDue(loanRepayments.getPrincipalOverDue())
                .interestDue(loanRepayments.getInterestDue())
                .interestPaid(loanRepayments.getInterestPaid())
                .interestOverDue(loanRepayments.getInterestOverDue())
                .totalRepaymentDue(loanRepayments.getTotalRepaymentDue())
                .totalRepaymentPaid(loanRepayments.getTotalRepaymentPaid())
                .totalRepaymentOverDue(loanRepayments.getTotalRepaymentOverDue())
                .build();
    }

    public LoanAccountRsDTO buildLoanAccountRsDTO(LoanDetails loanDetails) {
        return LoanAccountRsDTO.builder()
                .loanAccountID(loanDetails.getLoanAccountID())
                .loanTerm(loanDetails.getLoanTermInMonths())
                .loanAmount(loanDetails.getLoanAmount())
                .loanStatus(loanDetails.getLoanStatus())
                .disbursementAccountID(loanDetails.getDisbursementAccount().getAccountID())
                .collectionOrderProfile(loanDetails.getCollectionOrderProfile())
                .collectionOrderType(loanDetails.getCollectionOrderType())
                .fixedInterestAmount(loanDetails.getFixedInterestAmount())
                .lookAheadDays(loanDetails.getLookAheadDays())
                .maturityDate(loanDetails.getLoanMaturityDate())
                .settlementAccountID(loanDetails.getSettlementAccount().getAccountID())
                .delinquencyProfile(delinquencyService.buildDelinquencyProfileRsDTO(loanDetails.getDelinquencyFeature()))
                .delinquencyStage(loanDetails.getDelinquencyStage() != null ? delinquencyService.buildDelinquencyStageRsDTO(loanDetails.getDelinquencyStage()) : null)
                .loanSchedule(loanDetails.getLoanSchedules().stream().map(this::buildLoanScheduleRsDTO).collect(Collectors.toList()))
                .loanRepayments(loanDetails.getLoanRepayments().stream().map(this::buildLoanRepaymentRsDTO).collect(Collectors.toList()))
                .build();
    }
}
