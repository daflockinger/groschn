var con = new Mongo("172.18.0.2");
for(var i=1; i <= 4;i++) {
  db = con.getDB("groschn" + i);
  print("Current Block count for groschn" + i + ": " + db.blockchain.find().count());
}
con.close();
