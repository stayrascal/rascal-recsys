package com.stayrascal.service.application.dict;

import static com.stayrascal.service.application.constraints.SynSignal.SYN_DONE;

import com.stayrascal.service.application.component.ComponentService;
import com.stayrascal.service.application.constraints.SynSignal;
import com.stayrascal.service.application.item.ItemService;
import com.stayrascal.service.common.zookeeper.SynSignerReceiver;
import com.stayrascal.service.common.zookeeper.SynSignerSender;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DictStateSynService implements Consumer<String> {
    private SynSignerSender sender;
    private ComponentService componentService;
    private ItemService itemService;

    @Autowired
    public DictStateSynService(ComponentService componentService, ItemService itemService) {
        this.componentService = componentService;
        this.itemService = itemService;
    }

    public void init(String zkHosts, String zkPath) {
        try {
            sender = new SynSignerSender(zkHosts, zkPath);
            new SynSignerReceiver(zkHosts, zkPath, this).process();
        } catch (InterruptedException | KeeperException | IOException e) {
            throw new DictSynException("Cannot init dict synchronize service", e);
        }
    }

    public void runIndexSyncJob() {
        componentService.rebuild();
        itemService.rebuild();
        try {
            sender.sendSynSignal(SYN_DONE.name());
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    public void requestSync() {
        try {
            sender.sendSynSignal(SynSignal.DICT_SYN_REQ.name());
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept(String s) {
        try {
            switch (SynSignal.valueOf(s)) {
                case DICT_SYN_DONE:
                    CompletableFuture.runAsync(this::runIndexSyncJob);
            }
        } catch (IllegalArgumentException e) {
        }
    }
}

