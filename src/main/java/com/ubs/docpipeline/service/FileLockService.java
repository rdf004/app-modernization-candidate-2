package com.ubs.docpipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels
    .OverlappingFileLockException;

@Service
public class FileLockService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            FileLockService.class
        );

    @Value("${docpipeline.dirs.locks:"
        + "/opt/ubs/locks}")
    private String lockDir;

    public FileLock acquireLock(String docId)
            throws IOException {
        File dir = new File(lockDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String lockFile =
            lockDir + "/" + docId + ".lock";
        File f = new File(lockFile);

        RandomAccessFile raf =
            new RandomAccessFile(f, "rw");
        FileChannel ch = raf.getChannel();

        try {
            FileLock lock = ch.tryLock();
            if (lock == null) {
                LOG.warn(
                    "Doc {} locked by another"
                    + " instance",
                    docId
                );
                raf.close();
                return null;
            }
            LOG.info(
                "Acquired lock for {}",
                docId
            );
            return lock;
        } catch (
            OverlappingFileLockException e
        ) {
            LOG.warn(
                "Overlapping lock for {}",
                docId
            );
            raf.close();
            return null;
        }
    }

    public void releaseLock(
            FileLock lock, String docId) {
        try {
            if (lock != null) {
                lock.release();
                lock.channel().close();
            }
            File lockFile = new File(
                lockDir + "/"
                + docId + ".lock"
            );
            if (lockFile.exists()) {
                lockFile.delete();
            }
            LOG.info(
                "Released lock for {}",
                docId
            );
        } catch (IOException e) {
            LOG.error(
                "Failed to release lock"
                + " for {}",
                docId,
                e
            );
        }
    }
}
