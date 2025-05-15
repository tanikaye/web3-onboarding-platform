package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.model.NFTData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NFTDataRepository extends JpaRepository<NFTData, Long> {
    NFTData findByContractAddressAndTokenId(String contractAddress, String tokenId);
}