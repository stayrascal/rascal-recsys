package com.stayrascal.service.application.component;

import com.stayrascal.service.application.common.AbstractFileImporter;
import com.stayrascal.service.application.config.SolrProperties;
import com.stayrascal.service.application.constraints.EnvVariables;
import com.stayrascal.service.application.domain.Component;
import com.stayrascal.service.application.repository.ComponentRepository;
import com.stayrascal.service.common.parse.CompDocumentParser;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ComponentServiceImpl extends AbstractFileImporter<Component> implements ComponentService {
    private final SolrClient solrClient;
    private final ComponentRepository componentRepository;
    private final SolrProperties properties;

    @Autowired
    public ComponentServiceImpl(SolrClient solrClient, ComponentRepository componentRepository, SolrProperties properties) {
        this.solrClient = solrClient;
        this.componentRepository = componentRepository;
        this.properties = properties;
    }

    @Override
    protected Reader<Component> getReader() {
        return paths -> {
            CompDocumentParser parser = new CompDocumentParser();
            return paths.parallel().flatMap(path -> {
                try {
                    if (parser.parse(Jsoup.parse(path.toFile(), EnvVariables.DEFAULT_CHARSET))) {
                        return parser.getRecords().stream().map(record -> new Component(record.name, record.desc));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            });
        };
    }

    @Override
    protected Writer<Component> getWriter() {
        return components -> components.forEach(componentRepository::addComponent);
    }

    @Override
    public Optional<Component> searchComp(String name) {
        try {
            Component component = componentRepository.getComponentByName(name);
            return component == null ? Optional.empty() : Optional.of(component);
        } catch (DataAccessException e) {
            throw new ComponentException("Fail to load component: " + name, e);
        }
    }

    @Override
    public List<Component> searchCompLike(String nameLike) {
        return componentRepository.getComponentByNameLike(nameLike);
    }

    @Override
    public Optional<Component> searchComp(int id) {
        Component component = componentRepository.getComponentById(id);
        return component == null ? Optional.empty() : Optional.of(component);
    }

    @Override
    public List<Component> searchComps(String desc, int rows) {
        SolrQuery query = new SolrQuery("describe:" + desc);
        query.addField("id");
        query.addField("describe");
        query.setRows(rows);
        QueryResponse response;
        try {
            response = solrClient.query(properties.getCollectionName(), query);
        } catch (SolrServerException | IOException e) {
            throw new ComponentException("Fail to load component.", e);
        }
        SolrDocumentList documents = response.getResults();
        return documents.stream()
                .map(doc -> new Component((String) doc.get("id"), (String) doc.get("describe")))
                .collect(Collectors.toList());

    }

    @Override
    public Component updateComp(Component component) {
        try {
            Component oldComp = componentRepository.getComponentByName(component.getName());
            if (oldComp != null) {
                componentRepository.updateComponent(component);
                return oldComp;
            } else {
                throw new ComponentNotFoundException("Fail to update component as the component: " + component.getName() + "does not exist.");
            }
        } catch (DataAccessException e) {
            throw new ComponentException("Fail to update component: " + component.getId(), e);
        }
    }

    @Override
    public void deleteComp(String name) {
        try {
            componentRepository.deleteComponentByName(name);
        } catch (DataAccessException e) {
            throw new ComponentException("Fail to delete component: " + name, e);
        }
    }

    @Override
    public Component addComp(Component component) {
        try {
            Component quantum = componentRepository.getComponentByName(component.getName());
            if (quantum != null) {
                throw new ComponentAlreadyExistsException(String.format("The component: %s already exists, can not be added.", component.getName()));
            }
            componentRepository.addComponent(component);
            return component;
        } catch (DataAccessException e) {
            throw new ComponentException("Fail to add component: " + component.getId(), e);
        }
    }

    @Override
    @Transactional
    @Async
    public CompletableFuture<Void> rebuild() {
        int offset = 0;
        int limit = 150;
        while (true) {
            List<Component> page = componentRepository.getPagedComponents(offset, limit);
            if (page.size() > 0) {
                page.parallelStream().forEach(componentRepository::updateComponent);
                offset += limit;
            } else {
                break;
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    @Override
    public void importFromFiles(Stream<Path> paths) {
        super.importFromFiles(paths);
    }
}
