/*
 * Copyright 2022 signald contributors
 * SPDX-License-Identifier: GPL-3.0-only
 * See included LICENSE file
 *
 */

package io.finn.signald.clientprotocol.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.finn.signald.annotations.Doc;
import io.finn.signald.db.Database;
import io.finn.signald.db.IServersTable;
import io.finn.signald.exceptions.InvalidProxyException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.whispersystems.util.Base64;

@Doc("a Signal server")
public class Server {
  @Doc("A unique identifier for the server, referenced when adding accounts. Must be a valid UUID. Will be generated if not specified when creating.") public UUID uuid;
  @JsonProperty("service_url") public String serviceURL;
  @JsonProperty("cdn_urls") public List<ServerCDN> cdnURLs;
  @JsonProperty("contact_discovery_url") public String contactDiscoveryURL;
  @JsonProperty("key_backup_url") public String keyBackupURL;
  @JsonProperty("storage_url") public String storageURL;
  @Doc("base64 encoded ZKGROUP_SERVER_PUBLIC_PARAMS value") @JsonProperty("zk_param") public String zkParams;
  @Doc("base64 encoded") @JsonProperty("unidentified_sender_root") public String unidentifiedSenderRoot;
  public String proxy;
  @Doc("base64 encoded trust store, password must be 'whisper'") public String ca;
  @JsonProperty("key_backup_service_name") String keyBackupServiceName;
  @Doc("base64 encoded") @JsonProperty("key_backup_service_id") String keyBackupServiceId;
  @JsonProperty("key_backup_mrenclave") String keyBackupMrenclave;
  @JsonProperty("cds_mrenclave") String cdsMrenclave;
  @Doc("base64 encoded trust store, password must be 'whisper'") @JsonProperty("ias_ca") String iasCa;

  public Server() {}

  public Server(IServersTable.AbstractServer server) {
    uuid = server.getUuid();
    serviceURL = server.getServiceURL();
    cdnURLs = server.getCdnURLs().entrySet().stream().map(ServerCDN::new).collect(Collectors.toList());
    contactDiscoveryURL = server.getContactDiscoveryURL();
    keyBackupURL = server.getKeyBackupURL();
    storageURL = server.getStorageURL();
    zkParams = server.getZkParams() == null ? null : Base64.encodeBytes(server.getZkParams());
    proxy = server.getProxy();
    ca = server.getCa() == null ? null : Base64.encodeBytes(server.getCa());
    keyBackupServiceName = server.getKeyBackupServiceName();
    keyBackupServiceId = server.getKeyBackupServiceId() == null ? null : Base64.encodeBytes(server.getKeyBackupServiceId());
    keyBackupMrenclave = server.getKeyBackupMrenclave();
    cdsMrenclave = server.getCdsMrenclave();
    iasCa = server.getIasCa() == null ? null : Base64.encodeBytes(server.getIasCa());
  }

  @JsonIgnore
  public IServersTable.AbstractServer getServer() throws IOException, InvalidProxyException {
    var cdns = new HashMap<Integer, String>();
    for (ServerCDN cdn : cdnURLs) {
      cdns.put(cdn.number, cdn.url);
    }

    byte[] decodedZkParams = Base64.decode(zkParams);
    byte[] decodedUnidentifiedSenderRoot = Base64.decode(unidentifiedSenderRoot);
    byte[] decodedCa = Base64.decode(ca);
    byte[] decodedKeyBackupServiceId = keyBackupServiceId == null ? null : Base64.decode(keyBackupServiceId);
    byte[] decodedIasCa = iasCa == null ? null : Base64.decode(iasCa);

    return Database.NewServer(uuid, serviceURL, cdns, contactDiscoveryURL, keyBackupURL, storageURL, decodedZkParams, decodedUnidentifiedSenderRoot, proxy, decodedCa,
                              keyBackupServiceName, decodedKeyBackupServiceId, keyBackupMrenclave, cdsMrenclave, decodedIasCa);
  }
}
