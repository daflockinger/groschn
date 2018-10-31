package com.flockinger.groschn.gateway.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.FullNode;

@RestController
public class NodeStatisticsController {

  @Autowired
  private NetworkStatistics statistics;
  
  @RequestMapping(method = RequestMethod.GET, path = "/nodes", produces = {"application/json"})
  public ResponseEntity<List<FullNode>> getActiveFullNodes() {
    var activeFullNodes = statistics.activeFullNodes();
    return new ResponseEntity<List<FullNode>>(activeFullNodes, HttpStatus.OK);
  }
  
}
