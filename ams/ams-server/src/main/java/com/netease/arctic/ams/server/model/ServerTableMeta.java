/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.netease.arctic.table.TableIdentifier;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * ams table meta.
 *
 */
public class ServerTableMeta {
  private String tableType;
  private TableIdentifier tableIdentifier;
  private List<AMSColumnInfo> schema;
  private List<AMSColumnInfo> pkList;
  private List<AMSPartitionField> partitionColumnList;
  private Map<String, String> properties;
  private Map<String, Object> changeMetrics;
  private Map<String, Object> baseMetrics;
  private Map<String, Object> tableSummary;
  private CdcMeta cdcMeta;
  private String baseLocation;
  private String filter;
  private long createTime;
  private String creator;


  public ServerTableMeta() {
  }

  @JsonIgnore
  public boolean isCdcEnable() {
    return cdcMeta != null && cdcMeta.isEnable();
  }

  public void validate() {
    Preconditions.checkNotNull(tableIdentifier, "table identifier can not be null");

    Preconditions.checkArgument(StringUtils.isNotBlank(tableIdentifier.getCatalog()),
            "catalog can not be blank, catalog = %s", tableIdentifier.getCatalog());

    Preconditions.checkArgument(StringUtils.isNotBlank(tableIdentifier.getDatabase()),
            "database can not be blank, database = %s", tableIdentifier.getDatabase());

    Preconditions.checkArgument(StringUtils.isNotBlank(tableIdentifier.getTableName()),
            "tableName can not be blank, tableName = %s", tableIdentifier.getTableName());

    Preconditions
            .checkArgument(CollectionUtils.isNotEmpty(schema),
                    "columnList can not be null or empty");

    if (cdcMeta != null) {
      cdcMeta.validate();
    }

  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public String getTableType() {
    return tableType;
  }

  public void setTableType(String tableType) {
    this.tableType = tableType;
  }

  public TableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public void setTableIdentifier(TableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  public List<AMSColumnInfo> getSchema() {
    return schema;
  }

  public void setSchema(List<AMSColumnInfo> schema) {
    this.schema = schema;
  }

  public List<AMSColumnInfo> getPkList() {
    return pkList;
  }

  public void setPkList(List<AMSColumnInfo> pkList) {
    this.pkList = pkList;
  }

  public List<AMSPartitionField> getPartitionColumnList() {
    return partitionColumnList;
  }

  public void setPartitionColumnList(
          List<AMSPartitionField> partitionColumnList) {
    this.partitionColumnList = partitionColumnList;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public CdcMeta getCdcMeta() {
    return cdcMeta;
  }

  public void setCdcMeta(CdcMeta cdcMeta) {
    this.cdcMeta = cdcMeta;
  }


  public String getBaseLocation() {
    return baseLocation;
  }

  public void setBaseLocation(String baseLocation) {
    this.baseLocation = baseLocation;
  }

  public Map<String, Object> getChangeMetrics() {
    return changeMetrics;
  }

  public void setChangeMetrics(Map<String, Object> changeMetrics) {
    this.changeMetrics = changeMetrics;
  }

  public Map<String, Object> getBaseMetrics() {
    return baseMetrics;
  }

  public void setBaseMetrics(Map<String, Object> baseMetrics) {
    this.baseMetrics = baseMetrics;
  }

  public Map<String, Object> getTableSummary() {
    return tableSummary;
  }

  public void setTableSummary(Map<String, Object> tableSummary) {
    this.tableSummary = tableSummary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServerTableMeta that = (ServerTableMeta) o;
    return Objects.equals(tableIdentifier, that.tableIdentifier) &&
            Objects.equals(schema, that.schema) &&
            Objects.equals(pkList, that.pkList) &&
            Objects.equals(partitionColumnList, that.partitionColumnList) &&
            Objects.equals(properties, that.properties) &&
            Objects.equals(cdcMeta, that.cdcMeta);
  }

  @Override
  public int hashCode() {
    return Objects
            .hash(tableIdentifier, schema, pkList, partitionColumnList, properties, cdcMeta);
  }

  @Override
  public String toString() {
    return "ArcticTableMeta{" +
            "tableIdentifier=" + tableIdentifier +
            ", schema=" + schema +
            ", pkList=" + pkList +
            ", partitionColumnList=" + partitionColumnList +
            ", properties=" + properties +
            ", cdcMeta=" + cdcMeta +
            '}';
  }
}
