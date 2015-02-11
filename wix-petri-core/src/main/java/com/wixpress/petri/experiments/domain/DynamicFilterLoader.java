package com.wixpress.petri.experiments.domain;

import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by talyas on 2/2/15.
 */
public class DynamicFilterLoader {

    private static final Logger logger = LoggerFactory.getLogger(DynamicFilterLoader.class);
    private final String petriPluginsPath = "petri-plugins";
    private final String petriPluginsPathJarPrefix = "extended-filters";
    private final String filtersPackage;

    public DynamicFilterLoader(String filtersPackage) {
        this.filtersPackage = filtersPackage;
    }

    public List<Class> loadFilterTypesFromJars() {
        List<Class> filterTypes = new ArrayList<>();

        try {
            File[] extendedFiltersJars = getExtendedFiltersJars(new File(petriPluginsPath));

            if (extendedFiltersJars != null) {
                URLClassLoader ucl = new URLClassLoader(getUrls(extendedFiltersJars), DynamicFilterLoader.class.getClassLoader());
                for (ClassPath.ClassInfo classInfo : ClassPath.from(ucl).getTopLevelClasses(filtersPackage)) {
                    filterTypes.add(classInfo.load());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filterTypes;
    }

    private File[] getExtendedFiltersJars(File pluginsFolder) {
        logger.info("scanning filters to load from folder - " + pluginsFolder.getAbsolutePath());
        return pluginsFolder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.getPath().toLowerCase().contains(petriPluginsPathJarPrefix);
            }
        });
    }

    private URL[] getUrls(File[] extendedFiltersJars) {
        URL[] urls = new URL[extendedFiltersJars.length];
        for (int i = 0; i < extendedFiltersJars.length; i++) {
            try {
                urls[i] = extendedFiltersJars[i].toURI().toURL();
                logger.info("scanning filters from jar - " + urls[i]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}
