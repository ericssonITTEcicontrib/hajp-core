package com.ericsson.jenkinsci.hajp.filechangedetectors;

import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.messages.credentials.CredentialsCreateMessage;
import com.ericsson.jenkinsci.hajp.processors.impl.CredentialMessageProcessor;
import com.google.inject.Inject;
import jenkins.model.Jenkins;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Callable;

@Log4j2 public class FileDirectoryWatcher implements Callable {
    @Setter private Jenkins jenkins = Jenkins.getInstance();
    @Setter private HajpClusterSender sender = new HajpClusterSender();

    @Inject private transient CredentialMessageProcessor credentialsProcessor;

    private Path jenkinsRootPath;
    private static final String CREDENTIALS_FILENAME = "credentials.xml";
    private Path credentialsFilePath;

    public FileDirectoryWatcher() {
        this.jenkinsRootPath = Paths.get(jenkins.getRootDir().getAbsolutePath());
        this.credentialsFilePath =
            Paths.get(jenkinsRootPath.toAbsolutePath().toString(), "/", CREDENTIALS_FILENAME);
    }

    @Override public Object call() throws Exception {
        try {
            WatchService watchService = jenkinsRootPath.getFileSystem().newWatchService();
            jenkinsRootPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            // loop forever to watch directory
            while (true) {
                if (credentialsFilePath != null && credentialsFilePath.toFile().exists()) {
                    WatchKey watchKey;
                    watchKey =
                        watchService.take(); // this call is blocking until events are present

                    // poll for file system events on the WatchKey
                    for (final WatchEvent<?> event : watchKey.pollEvents()) {
                        printEvent(event);
                    }

                    // if the watched directed gets deleted, get out of run method
                    if (!watchKey.reset()) {
                        log.error("No longer valid");
                        watchKey.cancel();
                        watchService.close();
                        break;
                    }
                }
            }

        } catch (InterruptedException ex) {
            System.out.println("interrupted. Goodbye");
            return false;
        } catch (IOException ex) {
            log.error(ex.getStackTrace());
            return false;
        }

        return true;
    }

    // print the events and the affected file
    private void printEvent(WatchEvent<?> event) throws IOException {
        final Path changed = (Path) event.context();
        if (changed.toAbsolutePath().endsWith(CREDENTIALS_FILENAME)) {
            WatchEvent.Kind<?> kind = event.kind();
            if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                Path pathCreated = (Path) event.context();
                log.debug("Entry created:" + pathCreated);
                CredentialsCreateMessage credentialsCreateMessage = new CredentialsCreateMessage(
                    Files.readAllBytes(credentialsFilePath.toAbsolutePath()),
                    credentialsProcessor.getCredentialsManager().packSecretsDir());
                sender.sendToOrchestrator(credentialsCreateMessage);
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                Path pathDeleted = (Path) event.context();
                log.debug("Entry deleted:" + pathDeleted);
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                Path pathModified = (Path) event.context();
                log.debug("Entry modified:" + pathModified);
            }
        }
    }
}
