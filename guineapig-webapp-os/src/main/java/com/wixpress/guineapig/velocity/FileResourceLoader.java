package com.wixpress.guineapig.velocity;


import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.io.UnicodeInputStream;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.StringUtils;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A version of FileResourceLoader which supports topology for loading templates
 *
 * @author shahart
 * @since 2/7/13
 */
public class FileResourceLoader extends ResourceLoader {
    /**
     * The paths to search for templates.
     */
    private Map paths = Collections.synchronizedMap(new HashMap());

    /**
     * Used to map the path that a template was found on
     * so that we can properly check the modification
     * times of the files. This is synchronizedMap
     * instance.
     */
    private Map templatePaths = Collections.synchronizedMap(new HashMap());

    /**
     * Shall we inspect unicode files to see what encoding they contain?.
     */
    private boolean unicode = false;

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
     */
    public void init(ExtendedProperties configuration) {

        log.trace("FileResourceLoader : initialization starting.");

        setPaths((Map<String, String>) configuration.getProperty("paths"));

        // unicode files may have a BOM marker at the start, but Java
        // has problems recognizing the UTF-8 bom. Enabling unicode will
        // recognize all unicode boms.
        unicode = configuration.getBoolean("unicode", false);

        log.debug("Do unicode file recognition:  " + unicode);

        log.trace("FileResourceLoader : initialization complete.");

    }

    public void setPaths(Map aliases) {
        log.trace("FileResourceLoader : setting new path.");
        paths.clear();
        paths.putAll(aliases);
        templatePaths.clear();
        log.trace("FileResourceLoader : setting new path.");


    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param templateName name of template to get
     * @return InputStream containing the template
     * @throws org.apache.velocity.exception.ResourceNotFoundException
     *          if template not found
     *          in the file template path.
     */
    public InputStream getResourceStream(String templateName)
            throws ResourceNotFoundException {
        InputStream inputStream = null;
        File template;
        try {
            template = loadTemplate(templateName);
            inputStream = inputStreamFromFile(template);
        } catch (IOException ioe) {
            String msg = "Exception while loading Template " + templateName;
            log.error(msg, ioe);
            throw new VelocityException(msg, ioe);
        }

        if (inputStream != null) {
            /*
             * Store the path that this template came
             * from so that we can check its modification
             * time.
             */
            templatePaths.put(templateName, template.getPath());
            return inputStream;
        }

        /*
         * We have now searched all the paths for
         * templates and we didn't find anything so
         * throw an exception.
         */
        throw new ResourceNotFoundException("FileResourceLoader : cannot find " + template);
    }

    private File loadTemplate(String templateName)
            throws ResourceNotFoundException {
        /*
         * Make sure we have a valid templateName.
         */
        if (org.apache.commons.lang.StringUtils.isEmpty(templateName)) {
            /*
             * If we don't get a properly formed templateName then
             * there's not much we can do. So we'll forget about
             * trying to search any more paths for the template.
             */
            throw new ResourceNotFoundException(
                    "Need to specify a file name or file path!");
        }

        String template = StringUtils.normalizePath(templateName);
        if (template == null || template.length() == 0) {
            String msg = "File resource error : argument " + template +
                    " contains .. and may be trying to access " +
                    "content outside of template root.  Rejected.";

            log.error("FileResourceLoader : " + msg);

            throw new ResourceNotFoundException(msg);
        }

        if (template.startsWith("/")) {
            template = template.substring(1);
        }

        String parts[] = template.split("/", 3);
        if (parts.length != 3) {
            throw new ResourceNotFoundException("No a legitimate file path");
        }

        String path = (String)paths.get(parts[1]);
        if (path == null) {
            throw new ResourceNotFoundException("No such static file alias!");
        }

        return getFile(path, parts[2]);
    }

    /**
     * Overrides superclass for better performance.
     *
     * @since 1.6
     */
    public boolean resourceExists(String templateName) {
        try {
            return loadTemplate(templateName).canRead();
        } catch (Exception ioe) {
            String msg = "Exception while checking for template " + templateName;
            log.debug(msg, ioe);
            return false;
        }
    }

    /**
     * Try to find a template given a normalized path.
     *
     * @param file     the template file
     * @return InputStream input stream that will be parsed
     */
    protected InputStream inputStreamFromFile(File file)
            throws IOException {
        try {
            if (file.canRead()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file.getAbsolutePath());

                    if (unicode) {
                        UnicodeInputStream uis = null;

                        try {
                            uis = new UnicodeInputStream(fis, true);

                            if (log.isDebugEnabled()) {
                                log.debug("File Encoding for " + file + " is: " + uis.getEncodingFromStream());
                            }

                            return new BufferedInputStream(uis);
                        } catch (IOException e) {
                            closeQuiet(uis);
                            throw e;
                        }
                    } else {
                        return new BufferedInputStream(fis);
                    }
                } catch (IOException e) {
                    closeQuiet(fis);
                    throw e;
                }
            } else {
                return null;
            }
        } catch (FileNotFoundException fnfe) {
            /*
             *  log and convert to a general Velocity ResourceNotFoundException
             */
            return null;
        }
    }

    private void closeQuiet(final InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ioe) {
                // Ignore
            }
        }
    }

    /**
     * How to keep track of all the modified times
     * across the paths.  Note that a file might have
     * appeared in a directory which is earlier in the
     * path; so we should search the path and see if
     * the file we find that way is the same as the one
     * that we have cached.
     *
     * @param resource
     * @return True if the source has been modified.
     */
    public boolean isSourceModified(Resource resource) {
        /*
         * we assume that the file needs to be reloaded;
         * if we find the original file and it's unchanged,
         * then we'll flip this.
         */
        boolean modified = true;

        String templateName = resource.getName();
        String path = (String) templatePaths.get(templateName);
        if (path == null) {
            return true;
        }

        File file = getFile("", path);
        File currentFile = null;
        try {
            currentFile = loadTemplate(templateName);
        } catch (Exception ioe) { /* noop */ }

        Boolean eq = currentFile.equals(file);

        if (currentFile == null || !currentFile.canRead() || !currentFile.exists()) {
            /*
             * noop: if the file is missing now (either the cached
             * file is gone, or the file can no longer be found)
             * then we leave modified alone (it's set to true); a
             * reload attempt will be done, which will either use
             * a new template or fail with an appropriate message
             * about how the file couldn't be found.
             */
        } else if (file.canRead() && file.exists() && currentFile.equals(file)) {
            /*
             * if only if currentFile is the same as file and
             * file.lastModified() is the same as
             * resource.getLastModified(), then we should use the
             * cached version.
             */
            modified = (file.lastModified() != resource.getLastModified());
        }

        /*
         * rsvc.debug("isSourceModified for " + fileName + ": " + modified);
         */
        return modified;
    }

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
     */
    public long getLastModified(Resource resource) {
        String path = (String) templatePaths.get(resource.getName());
        if (path == null) {
            return 0;
        }

        File file = getFile("", path);
        if (file.canRead()) {
            return file.lastModified();
        } else {
            return 0;
        }
    }


    /**
     * Create a File based on either a relative path if given, or absolute path otherwise
     */
    public File getFile(String path, String template) {

        File file = null;

        if ("".equals(path)) {
            file = new File(template);
        } else {
            /*
             *  if a / leads off, then just nip that :)
             */
            if (template.startsWith("/")) {
                template = template.substring(1);
            }

            file = new File(path, template);
        }

        return file;
    }
}
