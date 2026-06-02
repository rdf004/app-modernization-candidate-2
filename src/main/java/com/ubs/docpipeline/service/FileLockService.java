package com.ubs.docpipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.
    OverlappingFileLockException;

/**
 * File-based locking mechanism using
 * /opt/ubs/locks/ to prevent duplicate
 * processing across multiple instances
 * co-located on the same RHEL host.
 *
 * This pattern assumes all instances share
 * the same filesystem — fundamentally
 * incompatible with container orchestration.
 */
@Service
public class FileLockService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            FileLockService.class
        );

    private static final String LOCK_DIR =
        "/opt/ubs/locks";

    /**
     * Acquire an exclusive file lock for the
     * given document ID. Returns null if the
     * document is already being processed by
     * another instance on this host.
     */
    public FileLock acquireLock(String docId)
            throws IOException {
        File lockDir = new File(LOCK_DIR);
        if (!lockDir.exists()) {
            lockDir.mkdirs();
        }

        String lockFile = LOCK_DIR
            + "/" + docId + ".lock";
        File f = new File(lockFile);

        RandomAccessFile raf =
            new RandomAccessFile(f, "rw");
        FileChannel ch = raf.getChannel();

        try {
            FileLock lock = ch.tryLock();
            if (lock == null) {
                LOG.warn(
                    "Doc {} locked by another "
                    + "instance",
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

    /**
     * Release the file lock and delete the
     * .lock file.
     */
    public void releaseLock(
            FileLock lock, String docId) {
        try {
            if (lock != null) {
                lock.release();
                lock.channel().close();
            }
            File lockFile = new File(
                LOCK_DIR + "/"
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
                "Failed to release lock "
                + "for " + docId,
                e
            );
        }
    }
}
