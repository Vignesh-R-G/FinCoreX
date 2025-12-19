package com.fincorex.corebanking.batch.interestapplication;

import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.InterestApplicationTag;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.repository.AccountRepo;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InterestApplicationItemProcessor implements ItemProcessor<InterestApplicationTag, InterestApplicationTag> {

    @Autowired
    private AccountRepo accountRepo;

    @Override
    public InterestApplicationTag process(InterestApplicationTag interestApplicationRecord) throws InterruptedException {
        String accountID = interestApplicationRecord.getAccountID();

        Account account = accountRepo.findById(accountID).get();
        String productType = account.getSubProduct().getProduct().getProductType();
        if(!productType.equals(ProductType.SA.name()) && !productType.equals(ProductType.CA.name()))
            return null;

        return interestApplicationRecord;
    }

}
