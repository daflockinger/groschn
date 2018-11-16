package com.flockinger.groschn.blockchain.messaging.sync.impl;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import static org.apache.commons.collections4.CollectionUtils.*;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResponse;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.sync.ChainSelector;

@Component
public class BlockChainSelector implements ChainSelector {
  
  @Override
  public Optional<BlockInfoResult> choose(List<BlockInfoResponse> infoResponses) {
    List<List<BlockInfoResponse>> nonConflictingResponses = new ArrayList<>();
    infoResponses.stream()
        .filter(response -> isNotEmpty(response.getBlockInfos()))
        .filter(this::hasNoGaps)
        .forEach(response -> addToNonConflictingGroups(response, nonConflictingResponses));
    return nonConflictingResponses.stream()
        .reduce(this::findMajorityGroup)
        .map(this::mapToResult);
  }
  
  private boolean hasNoGaps(BlockInfoResponse infoResponses) {
    var positions = infoResponses.getBlockInfos().stream()
        .map(BlockInfo::getPosition).collect(Collectors.toSet());
    
    LongSummaryStatistics stats = new LongSummaryStatistics();
    positions.forEach(stats::accept);
    
    var correctPositionIterator = LongStream.range(stats.getMin(), stats.getMax() + 1)
            .mapToObj(Long::valueOf).collect(Collectors.toSet()).iterator();
    return positions.stream()
        .allMatch(position -> correctPositionIterator.hasNext() 
              && position == correctPositionIterator.next());
  }
  
  private List<BlockInfoResponse> findMajorityGroup(List<BlockInfoResponse> peaceGroupOne, List<BlockInfoResponse> peaceGroupTwo) {
    if(peaceGroupOne.size() > peaceGroupTwo.size()) {
      return peaceGroupOne;
    } else if (peaceGroupOne.size() < peaceGroupTwo.size()) {
      return peaceGroupTwo;
    } else {
      return findGroupWithMostBlocks(peaceGroupOne, peaceGroupTwo);
    }
  }
  
  private List<BlockInfoResponse> findGroupWithMostBlocks(List<BlockInfoResponse> peaceGroupOne, List<BlockInfoResponse> peaceGroupTwo) {
    var groupOneInfoSize = peaceGroupOne.stream().map(BlockInfoResponse::getBlockInfos).map(List::size).reduce(Math::min).orElse(0);
    var groupTwoInfoSize = peaceGroupTwo.stream().map(BlockInfoResponse::getBlockInfos).map(List::size).reduce(Math::min).orElse(0);
    return groupOneInfoSize > groupTwoInfoSize ? peaceGroupOne : peaceGroupTwo;
  }
  
  void addToNonConflictingGroups(BlockInfoResponse response, List<List<BlockInfoResponse>> nonConflictingResponses) {
    var peaceGroup = nonConflictingResponses.stream()
        .filter(nonConflictingGroup -> isPeacefullWithGroup(nonConflictingGroup, response))
        .findFirst();
    if(peaceGroup.isPresent()) {
      peaceGroup.get().add(response);
    } else {
      var freshPeaceGroup = new ArrayList<BlockInfoResponse>();
      nonConflictingResponses.add(freshPeaceGroup);
      freshPeaceGroup.add(response);
    }
  }
  
  private boolean isPeacefullWithGroup(List<BlockInfoResponse> group, BlockInfoResponse response) {
    return group.stream().allMatch(member -> isPeacefullWithGroupMember(member, response));
  }
  
  private boolean isPeacefullWithGroupMember(BlockInfoResponse member, BlockInfoResponse response) {
    return member.getBlockInfos().stream().allMatch(memberInfo -> {
      var responseHash = getHashOfPosition(memberInfo.getPosition(), response.getBlockInfos());
      return !responseHash.isPresent() || equalsIgnoreCase(responseHash.get(), memberInfo.getBlockHash());
    });
  }
  
  private Optional<String> getHashOfPosition(long position, List<BlockInfo> infos) {
    return infos.stream()
        .filter(info -> info.getPosition() == position)
        .map(BlockInfo::getBlockHash)
        .findFirst();
  }
  
  private BlockInfoResult mapToResult(List<BlockInfoResponse> majorGroup) {
    var nodes = majorGroup.stream().map(BlockInfoResponse::getNodeId).collect(Collectors.toList());
    var infos = majorGroup.stream()
        .map(BlockInfoResponse::getBlockInfos)
        .reduce((infosOne, infosTwo) -> infosOne.size() < infosTwo.size() ? infosOne : infosTwo)
        .orElse(new ArrayList<>());
    return new BlockInfoResult(nodes, infos);
  }
}
