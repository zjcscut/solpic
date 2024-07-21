package cn.vlts.solpic.core.spi;

import cn.vlts.solpic.core.logging.Logger;
import cn.vlts.solpic.core.logging.LoggerFactory;
import cn.vlts.solpic.core.util.Box;
import cn.vlts.solpic.core.util.Ordered;
import cn.vlts.solpic.core.util.ResourceLoader;
import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The SPI loader.
 *
 * @author throwable
 * @since 2024/7/19 星期五 15:00
 */
public class SpiLoader<T> {

    private static final ConcurrentMap<Class<?>, SpiLoader<?>> SPI_LOADER_CACHE = new ConcurrentHashMap<>(64);

    private static List<LoadingStrategy> LOADING_STRATEGY_LIST = StreamSupport
            .stream(ServiceLoader.load(LoadingStrategy.class).spliterator(), false)
            .collect(Collectors.toList());

    private final Logger logger = LoggerFactory.getLogger(SpiLoader.class);

    private final AtomicBoolean destroyed = new AtomicBoolean();

    private final Class<?> type;

    private final InstanceFactory instanceFactory;

    private String defaultServiceName;

    private final List<LoadingStrategy> loadingStrategyList = new ArrayList<>();

    private final List<SpiPostProcessor> postProcessorList = new ArrayList<>();

    private final ConcurrentMap<String, ServiceInfo> cachedServices = new ConcurrentHashMap<>();

    private final Box<Map<String, Class<?>>> cachedServiceTypes = new Box<>();

    private SpiLoader(Class<?> type,
                      InstanceFactory instanceFactory,
                      List<LoadingStrategy> loadingStrategyList,
                      List<SpiPostProcessor> postProcessorList) {
        this.type = type;
        this.instanceFactory = instanceFactory;
        this.loadingStrategyList.addAll(LOADING_STRATEGY_LIST);
        if (Objects.nonNull(loadingStrategyList) && !loadingStrategyList.isEmpty()) {
            this.loadingStrategyList.addAll(loadingStrategyList);
        }
        if (Objects.nonNull(postProcessorList) && !postProcessorList.isEmpty()) {
            this.postProcessorList.addAll(postProcessorList);
        }
        getServiceClasses();
    }

    public static <T> SpiLoader<T> getSpiLoader(Class<T> type) {
        return getSpiLoader(type, null, null);
    }

    public static <T> SpiLoader<T> getSpiLoader(Class<T> type, List<SpiPostProcessor> postProcessorList) {
        return getSpiLoader(type, null, postProcessorList);
    }

    @SuppressWarnings("unchecked")
    public static <T> SpiLoader<T> getSpiLoader(Class<T> type,
                                                List<LoadingStrategy> loadingStrategyList,
                                                List<SpiPostProcessor> postProcessorList) {
        return (SpiLoader<T>) SPI_LOADER_CACHE.computeIfAbsent(type, clazz -> newSpiLoader(type, loadingStrategyList,
                postProcessorList));
    }

    static <T> SpiLoader<T> newSpiLoader(Class<T> type,
                                         List<LoadingStrategy> loadingStrategyList,
                                         List<SpiPostProcessor> postProcessorList) {
        List<LoadingStrategy> loadingStrategyListToUse = Optional.ofNullable(loadingStrategyList)
                .orElse(Collections.emptyList());
        List<SpiPostProcessor> postProcessorListToUse = Optional.ofNullable(postProcessorList)
                .orElse(Collections.emptyList());
        InstanceFactory instanceFactory = StreamSupport
                .stream(ServiceLoader.load(InstanceFactory.class).spliterator(), false)
                .min(Ordered.COMPARATOR)
                .orElse(new DefaultInstanceFactory());
        return new SpiLoader<>(type, instanceFactory, loadingStrategyListToUse, postProcessorListToUse);
    }

    public static void setDefaultLoadingStrategies(List<LoadingStrategy> loadingStrategyList) {
        SpiLoader.LOADING_STRATEGY_LIST = loadingStrategyList;
    }

    public static List<LoadingStrategy> getDefaultLoadingStrategies() {
        return Collections.unmodifiableList(LOADING_STRATEGY_LIST);
    }

    public void setLoadingStrategies(List<LoadingStrategy> loadingStrategyList) {
        this.loadingStrategyList.clear();
        this.loadingStrategyList.addAll(loadingStrategyList);
    }

    public void addLoadingStrategy(LoadingStrategy loadingStrategy) {
        this.loadingStrategyList.add(loadingStrategy);
    }

    public void removeLoadingStrategy(LoadingStrategy loadingStrategy) {
        this.loadingStrategyList.removeIf(p -> p == loadingStrategy);
    }

    public void addPostProcessor(SpiPostProcessor spiPostProcessor) {
        this.postProcessorList.add(spiPostProcessor);
    }

    public void removePostProcessor(SpiPostProcessor spiPostProcessor) {
        this.postProcessorList.removeIf(p -> p == spiPostProcessor);
    }

    public T getDefaultService() {
        return getService(defaultServiceName);
    }

    @SuppressWarnings("unchecked")
    public T getService(String name) {
        checkDestroyed();
        return (T) Objects.requireNonNull(cachedServices.computeIfAbsent(name, this::createServiceInfo)).getHolder().get();
    }

    public List<T> getAvailableServices() {
        Set<String> availableServiceNames = getAvailableServiceNames();
        List<T> services = new ArrayList<>(availableServiceNames.size());
        for (String serviceName : availableServiceNames) {
            services.add(getService(serviceName));
        }
        return Collections.unmodifiableList(services);
    }

    public String getDefaultServiceName() {
        checkDestroyed();
        return defaultServiceName;
    }

    public Set<String> getAvailableServiceNames() {
        checkDestroyed();
        return Collections.unmodifiableSet(getServiceClasses().keySet());
    }

    public Set<Class<?>> getAvailableServiceTypes() {
        checkDestroyed();
        return Collections.unmodifiableSet(new HashSet<>(getServiceClasses().values()));
    }

    public boolean hasService(String name) {
        checkDestroyed();
        return getServiceClasses().containsKey(name);
    }

    private ServiceInfo createServiceInfo(String serviceName) {
        Map<String, Class<?>> serviceClasses = getServiceClasses();
        if (!serviceClasses.containsKey(serviceName)) {
            throw new IllegalStateException("SPI service: '" + serviceName + "' could not be found");
        }
        boolean singleton = true;
        boolean defaultService = Objects.equals(defaultServiceName, serviceName);
        Class<?> serviceType = serviceClasses.get(serviceName);
        Spi spi = this.type.getAnnotation(Spi.class);
        if (Objects.nonNull(spi)) {
            singleton = spi.singleton();
        }
        Supplier<?> supplier;
        if (singleton) {
            T serviceInstance = createServiceInstance(serviceName, serviceType);
            supplier = () -> serviceInstance;
        } else {
            supplier = () -> createServiceInstance(serviceName, serviceType);
        }
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(serviceType);
        serviceInfo.setSingleton(singleton);
        serviceInfo.setDefaultService(defaultService);
        serviceInfo.setHolder(supplier);
        return serviceInfo;
    }

    @SuppressWarnings("unchecked")
    private T createServiceInstance(String serviceName, Class<?> serviceType) {
        try {
            T instance = (T) instanceFactory.newInstance(serviceType);
            for (SpiPostProcessor postProcessor : postProcessorList) {
                postProcessor.postProcessBeforeInitialization(instance, serviceName);
            }
            if (instance instanceof InitialingBean) {
                ((InitialingBean) instance).init();
            }
            for (SpiPostProcessor postProcessor : postProcessorList) {
                postProcessor.postProcessAfterInitialization(instance, serviceName);
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create instance of " + serviceType.getName(), e);
        }
    }

    private Map<String, Class<?>> getServiceClasses() {
        Map<String, Class<?>> serviceTypes = cachedServiceTypes.getValue();
        if (Objects.isNull(serviceTypes)) {
            synchronized (cachedServiceTypes) {
                serviceTypes = cachedServiceTypes.getValue();
                if (Objects.isNull(serviceTypes)) {
                    serviceTypes = loadServiceClasses();
                }
                cachedServiceTypes.setValue(serviceTypes);
            }
        }
        return serviceTypes;
    }

    private Map<String, Class<?>> loadServiceClasses() {
        checkDestroyed();
        cacheDefaultServiceName();
        Map<String, Class<?>> servicesClasses = new HashMap<>();
        ClassLoader spiLoaderClassLoader = SpiLoader.class.getClassLoader();
        for (LoadingStrategy loadingStrategy : loadingStrategyList) {
            String fileName = loadingStrategy.location() + type.getName();
            List<ClassLoader> classLoadersToUse = new ArrayList<>();
            if (Objects.nonNull(loadingStrategy.classLoader())) {
                classLoadersToUse.add(loadingStrategy.classLoader());
            }
            if (loadingStrategy.spiLoaderClassLoaderPreferred()) {
                if (ClassLoader.getSystemClassLoader() != spiLoaderClassLoader) {
                    classLoadersToUse.add(spiLoaderClassLoader);
                }
            }
            if (loadingStrategy.contextClassLoaderPreferred()) {
                classLoadersToUse.add(Thread.currentThread().getContextClassLoader());
            }
            if (classLoadersToUse.isEmpty()) {
                classLoadersToUse.add(spiLoaderClassLoader);
            }
            Map<ClassLoader, List<URL>> resources = ResourceLoader.loadResources(fileName, classLoadersToUse);
            resources.forEach((classLoader, urls) -> loadServiceClassesInternal(
                    servicesClasses,
                    classLoader,
                    urls,
                    loadingStrategy.includePackages(),
                    loadingStrategy.excludePackages(),
                    loadingStrategy.overridden()
            ));
        }
        return servicesClasses;
    }

    private void loadServiceClassesInternal(Map<String, Class<?>> servicesClasses,
                                            ClassLoader classLoader,
                                            List<URL> urls,
                                            String[] includePackages,
                                            String[] excludePackages,
                                            boolean overridden) {
        if (!urls.isEmpty()) {
            for (URL url : urls) {
                List<String> contentLines = getResourceContentAsLines(url);
                for (String line : contentLines) {
                    String name = null;
                    String className;
                    int idx = line.indexOf("=");
                    if (idx > 0) {
                        name = line.substring(0, idx).trim();
                        className = line.substring(idx + 1).trim();
                    } else {
                        className = line.trim();
                    }
                    if (!className.isEmpty() &&
                            isInclude(className, includePackages) &&
                            !isExclude(className, excludePackages)) {
                        try {
                            if (Objects.isNull(name)) {
                                int lastDotIdx = className.lastIndexOf(".");
                                if (lastDotIdx > 0) {
                                    name = className.substring(lastDotIdx + 1);
                                } else {
                                    name = className;
                                }
                                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                            }
                            Class<?> serviceType = Class.forName(className, true, classLoader);
                            Class<?> cacheType = servicesClasses.get(name);
                            if (Objects.isNull(cacheType) || overridden) {
                                servicesClasses.put(name, serviceType);
                            } else if (cacheType != serviceType) {
                                throw new IllegalStateException("Duplicate SPI service name: " + name + " " +
                                        "was found for cached type: " + cacheType + " and type: " + serviceType);
                            }
                        } catch (Throwable e) {
                            if (e instanceof RuntimeException) {
                                throw (RuntimeException) e;
                            }
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
        }
    }

    private boolean isInclude(String className, String[] includePackages) {
        if (Objects.nonNull(includePackages) && includePackages.length > 0) {
            return Stream.of(includePackages).anyMatch(className::startsWith);
        }
        return true;
    }

    private boolean isExclude(String className, String[] excludePackages) {
        if (Objects.nonNull(excludePackages) && excludePackages.length > 0) {
            return Stream.of(excludePackages).anyMatch(className::startsWith);
        }
        return false;
    }

    private List<String> getResourceContentAsLines(URL resource) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while (Objects.nonNull(line = reader.readLine())) {
                int idx = line.indexOf('#');
                if (idx >= 0) {
                    line = line.substring(0, idx);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + resource, e);
        }
        return lines;
    }

    private void cacheDefaultServiceName() {
        Spi spi = this.type.getAnnotation(Spi.class);
        if (Objects.isNull(spi)) {
            return;
        }
        String value = spi.value().trim();
        if (!value.isEmpty()) {
            String[] names = value.split(",");
            if (names.length > 1) {
                throw new IllegalArgumentException("More than 1 default service for type: '" + type.getName() + "'.");
            }
            if (names.length == 1) {
                defaultServiceName = names[0];
            }
        }
    }

    private void checkDestroyed() {
        if (isDestroyed()) {
            throw new IllegalStateException("SpiLoader has been destroyed.");
        }
    }

    public boolean isDestroyed() {
        return destroyed.get();
    }

    public void destroy() {
        if (destroyed.compareAndSet(false, true)) {
            cachedServices.forEach((type, serverInfo) -> {
                if (serverInfo.isSingleton()) {
                    Optional.ofNullable(serverInfo.getHolder()).map(Supplier::get).ifPresent(instance -> {
                        if (instance instanceof DisposableBean) {
                            try {
                                ((DisposableBean) instance).destroy();
                            } catch (Exception e) {
                                logger.error("Error destroying SPI service, type: " + type, e);
                            }
                        }
                    });
                }
                serverInfo.setHolder(null);
            });
            cachedServices.clear();
        }
    }

    public static void destroyOnShutdown() {
        SPI_LOADER_CACHE.forEach((k, v) -> v.destroy());
        SPI_LOADER_CACHE.clear();
    }

    @Data
    private static class ServiceInfo {

        private String serviceName;

        private Class<?> serviceType;

        private boolean singleton;

        private boolean defaultService;

        private Supplier<?> holder;
    }
}
