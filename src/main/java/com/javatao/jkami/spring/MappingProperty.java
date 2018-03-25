package com.javatao.jkami.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedCaseInsensitiveMap;

import com.javatao.jkami.utils.SqlUtils;

/**
 * 外置配置文件扫描
 * 
 * @author TLF
 */
public class MappingProperty implements InitializingBean {
    private static final Log logger = LogFactory.getLog(MappingProperty.class);
    // mapping
    private static Map<String, String> mappingEntity = new LinkedCaseInsensitiveMap<>();

    @javax.annotation.Resource
    public void setLocations(Resource[] locations) {
        for (Resource resource : locations) {
            loadFileMapping(resource);
        }
    }

    /**
     * 加载实体映射
     * 
     * @param path
     *            路径
     */
    public static void loadConfigMapping(String path) {
        if (path == null) {
            return;
        }
        URL url = SqlUtils.class.getClassLoader().getResource(path);
        try {
            if (url != null) {
                File mappingDir = new File(url.toURI());
                if (mappingDir.exists()) {
                    loadFileMapping(mappingDir);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载实体映射
     * 
     * @param file
     *            文件
     */
    public static void loadFileMapping(File file) {
        try {
            if (file.isFile()) {
                String fileName = file.getName();
                if (fileName.length() > 11) {
                    logger.info("loading " + file.getPath());
                    FileInputStream in = new FileInputStream(file);
                    String name = fileName.substring(0, fileName.length() - 11);
                    if (name.contains(".")) {
                        Properties mapping = new Properties();
                        mapping.load(in);
                        for (Object key : mapping.keySet()) {
                            String s = key.toString();
                            if (s.startsWith("[")) {
                                s = name + key;
                            } else {
                                s = name + "." + key;
                            }
                            mappingEntity.put(s, mapping.getProperty(key.toString()));
                        }
                    } else {
                        Properties mapping = new Properties();
                        mapping.load(in);
                        changeProperties(mapping);
                    }
                    in.close();
                }
            } else if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                for (File fe : listFiles) {
                    loadFileMapping(fe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载实体映射
     * 
     * @param resource
     *            文件
     */
    public static void loadFileMapping(Resource resource) {
        try {
            String fileName = resource.getFilename();
            if (fileName.length() > 11) {
                logger.info("loading " + resource.getURI());
                InputStream in = resource.getInputStream();
                String name = fileName.substring(0, fileName.length() - 11);
                if (name.contains(".")) {
                    Properties mapping = new Properties();
                    mapping.load(in);
                    for (Object key : mapping.keySet()) {
                        String s = key.toString();
                        if (s.startsWith("[")) {
                            s = name + key;
                        } else {
                            s = name + "." + key;
                        }
                        mappingEntity.put(s, mapping.getProperty(key.toString()));
                    }
                } else {
                    Properties mapping = new Properties();
                    mapping.load(in);
                    changeProperties(mapping);
                }
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void changeProperties(Properties mapping) {
        for (Object key : mapping.keySet()) {
            String s = key.toString();
            mappingEntity.put(s, mapping.getProperty(key.toString()));
        }
    }

    /**
     * 获取配置映射参数
     * 
     * @return 映射结果集合
     */
    public static Map<String, String> getConfigMapping() {
        return mappingEntity;
    }

    @Override
    public void afterPropertiesSet() throws Exception {}
}
