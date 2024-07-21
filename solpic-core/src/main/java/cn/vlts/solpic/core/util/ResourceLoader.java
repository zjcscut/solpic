package cn.vlts.solpic.core.util;

import cn.vlts.solpic.core.logging.Logger;
import cn.vlts.solpic.core.logging.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Resource loader.
 *
 * @author throwable
 * @since 2024/7/20 00:27
 */
public class ResourceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoader.class);

    public static Map<ClassLoader, List<URL>> loadResources(String fileName, Collection<ClassLoader> classLoaders) {
        Map<ClassLoader, List<URL>> result = new HashMap<>();
        for (ClassLoader classLoader : classLoaders) {
            result.put(classLoader, loadClassLoaderResources(fileName, classLoader));
        }
        return result;
    }

    public static List<URL> loadClassLoaderResources(String fileName, ClassLoader classLoader) {
        List<URL> urlSet = new ArrayList<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(fileName);
            while (resources.hasMoreElements()) {
                urlSet.add(resources.nextElement());
            }
        } catch (IOException e) {
            LOGGER.error("Load classloader resources error, fileName: " + fileName, e);
        }
        return urlSet;
    }
}
