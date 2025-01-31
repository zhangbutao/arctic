package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@PrepareForTest({
    JDBCSqlSessionFactoryProvider.class
})
@PowerMockIgnore({"org.apache.logging.log4j.*", "javax.management.*", "org.apache.http.conn.ssl.*",
    "com.amazonaws.http.conn.ssl.*",
    "javax.net.ssl.*", "org.apache.hadoop.*", "javax.*", "com.sun.org.apache.*", "org.apache.xerces.*"})
public class TestIcebergMinorOptimizeCommit extends TestIcebergBase {
  @Test
  public void testNoPartitionTableMinorOptimizeCommit() throws Exception {
    icebergNoPartitionTable.asUnkeyedTable().updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / 1000 + "")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), dataFiles);
    Set<String> oldDataFilesPath = new HashSet<>();
    Set<String> oldDeleteFilesPath = new HashSet<>();
    icebergNoPartitionTable.asUnkeyedTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          if (fileScanTask.file().fileSizeInBytes() <= 1000) {
            oldDataFilesPath.add((String) fileScanTask.file().path());
            fileScanTask.deletes().forEach(deleteFile -> oldDeleteFilesPath.add((String) deleteFile.path()));
          }
        });

    IcebergMinorOptimizePlan optimizePlan = new IcebergMinorOptimizePlan(icebergNoPartitionTable,
        new TableOptimizeRuntime(icebergNoPartitionTable.id()),
        icebergNoPartitionTable.asUnkeyedTable().newScan().planFiles(),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();

    List<DataFile> resultDataFiles = insertOptimizeTargetDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 10);
    List<DeleteFile> resultDeleteFiles = insertPosDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), resultDataFiles);
    List<ContentFile<?>> resultFiles = new ArrayList<>();
    resultFiles.addAll(resultDataFiles);
    resultFiles.addAll(resultDeleteFiles);
    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      if (resultFiles != null) {
        optimizeRuntime.setNewFileSize(resultFiles.get(0).fileSizeInBytes());
        optimizeRuntime.setTargetFiles(resultFiles.stream().map(SerializationUtil::toByteBuffer).collect(Collectors.toList()));
      }
      List<ByteBuffer> finalTargetFiles = optimizeRuntime.getTargetFiles();
      optimizeRuntime.setTargetFiles(finalTargetFiles);
      optimizeRuntime.setNewFileCnt(finalTargetFiles.size());
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    IcebergOptimizeCommit optimizeCommit = new IcebergOptimizeCommit(icebergNoPartitionTable, partitionTasks);
    optimizeCommit.commit(icebergNoPartitionTable.asUnkeyedTable().currentSnapshot().snapshotId());

    Set<String> newDataFilesPath = new HashSet<>();
    Assert.assertNotEquals(oldDataFilesPath, newDataFilesPath);
  }

  @Test
  public void testPartitionTableMinorOptimizeCommit() throws Exception {
    icebergPartitionTable.asUnkeyedTable().updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / 1000 + "")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergPartitionTable.asUnkeyedTable(), dataFiles);
    Set<String> oldDataFilesPath = new HashSet<>();
    Set<String> oldDeleteFilesPath = new HashSet<>();
    icebergPartitionTable.asUnkeyedTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          if (fileScanTask.file().fileSizeInBytes() <= 1000) {
            oldDataFilesPath.add((String) fileScanTask.file().path());
            fileScanTask.deletes().forEach(deleteFile -> oldDeleteFilesPath.add((String) deleteFile.path()));
          }
        });

    IcebergMinorOptimizePlan optimizePlan = new IcebergMinorOptimizePlan(icebergPartitionTable,
        new TableOptimizeRuntime(icebergPartitionTable.id()),
        icebergPartitionTable.asUnkeyedTable().newScan().planFiles(),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();

    List<DataFile> resultDataFiles = insertOptimizeTargetDataFiles(icebergPartitionTable.asUnkeyedTable(), 10);
    List<DeleteFile> resultDeleteFiles = insertPosDeleteFiles(icebergPartitionTable.asUnkeyedTable(), resultDataFiles);
    List<ContentFile<?>> resultFiles = new ArrayList<>();
    resultFiles.addAll(resultDataFiles);
    resultFiles.addAll(resultDeleteFiles);
    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      if (resultFiles != null) {
        optimizeRuntime.setNewFileSize(resultFiles.get(0).fileSizeInBytes());
        optimizeRuntime.setTargetFiles(resultFiles.stream().map(SerializationUtil::toByteBuffer).collect(Collectors.toList()));
      }
      List<ByteBuffer> finalTargetFiles = optimizeRuntime.getTargetFiles();
      optimizeRuntime.setTargetFiles(finalTargetFiles);
      optimizeRuntime.setNewFileCnt(finalTargetFiles.size());
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    IcebergOptimizeCommit optimizeCommit = new IcebergOptimizeCommit(icebergPartitionTable, partitionTasks);
    optimizeCommit.commit(icebergPartitionTable.asUnkeyedTable().currentSnapshot().snapshotId());

    Set<String> newDataFilesPath = new HashSet<>();
    Assert.assertNotEquals(oldDataFilesPath, newDataFilesPath);
  }
}
