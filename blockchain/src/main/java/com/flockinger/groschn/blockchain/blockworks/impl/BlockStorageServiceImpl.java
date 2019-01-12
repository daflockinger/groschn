package com.flockinger.groschn.blockchain.blockworks.impl;

import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.exception.validation.ValidationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.impl.InnerBlockValidator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BlockStorageServiceImpl implements BlockStorageService {

  @Autowired
  @Qualifier("innerBlockValidator")
  private InnerBlockValidator validator;
  @Autowired
  private BlockchainRepository dao;
  @Autowired
  private ModelMapper mapper;
  @Autowired
  private TransactionManager transactionManager;

  private final static Logger LOG = LoggerFactory.getLogger(BlockStorageServiceImpl.class);

  @PostConstruct
  public void initBlockchain() {
    if (dao.count() == 0) {
      StoredBlock genesisBlock = mapToStoredBlock(Block.GENESIS_BLOCK());
      dao.save(genesisBlock);
    }
  }

  @Override
  public StoredBlock saveInBlockchain(Block block) throws ValidationException {
    if(isGenesisBlock(block)) {
      return mapper.map(block, StoredBlock.class);
    }
    validateBlock(block);
    return saveUnchecked(block);
  }

  private void validateBlock(Block block) {
    Assessment assessment = validator.validate(block);
    if (!assessment.isValid()) {
      throw new AssessmentFailedException(assessment.getReasonOfFailure(), assessment.getFailure());
    }
  }

  @Override
  public StoredBlock saveUnchecked(Block block) {
    if(isGenesisBlock(block)) {
      return mapper.map(block, StoredBlock.class);
    }
    transactionManager
        .updateTransactionStatuses(block.getTransactions(), TransactionStatus.EMBEDDED_IN_BLOCK);
    StoredBlock storedBlock = mapToStoredBlock(block);

    var possiblyExistingBlock = dao.findByPosition(block.getPosition());
    if (possiblyExistingBlock.isPresent()) {
      storedBlock.setId(possiblyExistingBlock.get().getId());
    }
    storedBlock = dao.save(storedBlock);
    LOG.info("Block successfully stored with position {}", block.getPosition());
    return storedBlock;
  }

  private boolean isGenesisBlock(Block block) {
    return Objects.equals(block.getPosition(), Block.GENESIS_BLOCK().getPosition());
  }

  private StoredBlock mapToStoredBlock(Block block) {
    return mapper.map(block, StoredBlock.class);
  }

  @Override
  public Block getLatestBlock() {
    return mapToBlock(dao.findFirstByOrderByPositionDesc().get());
  }

  private Block mapToBlock(StoredBlock block) {
    return mapper.map(block, Block.class);
  }

  @Override
  public Block getLatestProofOfWorkBlock() {
    return dao
        .findTop3ByConsentTypeOrderByPositionDesc(ConsensusType.PROOF_OF_WORK)
        .stream().map(this::mapToBlock)
        .findFirst().get();
  }

  @Override
  public Block getLatestProofOfWorkBlockBelowPosition(Long position) {
    long realPosition = Math.max(2, position);
    return dao
        .findFirstByPositionLessThanAndConsentTypeOrderByPositionDesc(realPosition,
            ConsensusType.PROOF_OF_WORK)
        .stream().map(this::mapToBlock)
        .findFirst().get();
  }

  @Override
  public List<Block> findBlocks(long fromPosition, long quantity) {
    var from = Math.max(fromPosition, 1);
    var until = Math.max(from + quantity - 1, from);
    return dao.findByPositionBetweenInclusive(from, until).stream()
        .map(this::mapToBlock).collect(Collectors.toList());
  }

  @Override
  public void removeBlock(long position) {
    if (dao.findByPosition(position).isPresent() && position > 1) {
      dao.removeByPosition(position);
    }
  }
}
