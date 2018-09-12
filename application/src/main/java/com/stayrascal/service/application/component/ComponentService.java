package com.stayrascal.service.application.component;

import com.stayrascal.service.application.common.FileImporter;
import com.stayrascal.service.application.domain.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ComponentService extends FileImporter {
    Optional<Component> searchComp(String name);

    List<Component> searchCompLike(String nameLike);

    Optional<Component> searchComp(int id);

    List<Component> searchComps(String desc, int rows);

    Component updateComp(Component component);

    void deleteComp(String name);

    Component addComp(Component component);

    CompletableFuture<Void> rebuild();
}
