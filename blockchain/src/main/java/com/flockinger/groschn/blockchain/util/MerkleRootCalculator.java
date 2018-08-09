package com.flockinger.groschn.blockchain.util;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Hashable;

@Component
public class MerkleRootCalculator {
  
  @Autowired
  private HashGenerator hasher;
  
  
  public <T extends Hashable> String calculateMerkleRootHash(List<T> entities) {
    List<MerkleNode> nodes = createHashPairs(entities.stream()
        .map(hasher::generateHash).collect(Collectors.toList()));
    while(nodes.size() > 1) {
      nodes = createNextUpperTreeBranch(nodes);
    }
    if(nodes.isEmpty()) {
      throw new HashingException("Cannot build hash of empty list!");
    }
    return hasher.generateHash(nodes.stream().findFirst().get());
  }
  
  private List<MerkleNode> createHashPairs(List<String> hashes) {
    return ListUtils.partition(hashes, 2)
        .stream()
        .map(this::dualListToMerkleNode)
        .collect(Collectors.toList());
  }
  
  private MerkleNode dualListToMerkleNode(List<String> hashes) {
    String leftNode = hashes.get(0);
    String rightNode = leftNode;
    if(hashes.size() > 1) {
      rightNode = hashes.get(1);
    }
    return MerkleNode.build().leftNode(leftNode).rightNode(rightNode);
  }
  
  private List<MerkleNode> createNextUpperTreeBranch(List<MerkleNode> currentBranch) {
    List<String> currentHashes = currentBranch.stream()
        .map(node -> hasher.generateHash(node))
        .collect(Collectors.toList());
    return createHashPairs(currentHashes);
  }
  
  private final static class MerkleNode implements Hashable{
    private String leftNode;
    private String rightNode;
    
    private MerkleNode() {}
    
    public static MerkleNode build() {
      return new MerkleNode();
    }
    
    public String getLeftNode() {
      return leftNode;
    }
    public MerkleNode leftNode(String leftNode) {
      this.leftNode = leftNode;
      return this;
    }
    public String getRightNode() {
      return rightNode;
    }
    public MerkleNode rightNode(String rightNode) {
      this.rightNode = rightNode;
      return this;
    }
  }
}
