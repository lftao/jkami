package com.javatao.jkami.utils;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.javatao.jkami.JkException;
import com.javatao.jkami.support.ObjectWrapper;

import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Freemarker引擎工具类
 * 
 * @author tao
 */
public class FKParse {
    private static final Log logger = LogFactory.getLog(FKParse.class);
    // 默认日期格式
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private static final String ENCODE = "utf-8";
    /**
     * 文件缓存
     */
    private static final Configuration _tplConfig = new Configuration(Configuration.VERSION_2_3_23);
    /**
     * SQL 缓存
     */
    private static final Configuration _sqlConfig = new Configuration(Configuration.VERSION_2_3_23);
    private static StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
    // 使用内嵌的(?ms)打开单行和多行模式
    private final static Pattern p = Pattern.compile("(?ms)/\\*.*?\\*/|^\\s*//.*?$");
    static {
        ObjectWrapper ow = new ObjectWrapper(Configuration.VERSION_2_3_23);
        _tplConfig.setClassForTemplateLoading(FKParse.class, "/");
        _tplConfig.setLocale(Locale.CHINESE);
        _tplConfig.setLocalizedLookup(false);
        _tplConfig.setNumberFormat("#");
        _tplConfig.setClassicCompatible(true);
        _tplConfig.setDateFormat(dateFormat);
        _tplConfig.setDateTimeFormat(dateFormat);
        _tplConfig.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        _tplConfig.setObjectWrapper(ow);
        // --
        _sqlConfig.setTemplateLoader(stringTemplateLoader);
        _sqlConfig.setNumberFormat("#");
        _sqlConfig.setLocalizedLookup(false);
        _sqlConfig.setLocale(Locale.CHINESE);
        _sqlConfig.setClassicCompatible(true);
        _sqlConfig.setDateFormat(dateFormat);
        _sqlConfig.setDateTimeFormat(dateFormat);
        _sqlConfig.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        _sqlConfig.setObjectWrapper(ow);
    }

    /**
     * 判断模板是否存在
     * 
     * @param tplName
     *            模板
     * @return true/false
     */
    public static boolean isExistTemplate(String tplName) {
        try {
            Template mytpl = _tplConfig.getTemplate(tplName, "UTF-8");
            if (mytpl == null) {
                return false;
            }
        } catch (Exception e) {
            throw new JkException(e);
        }
        return true;
    }

    /**
     * 解析模板
     *
     * @param tplName
     *            模板名
     * @param paras
     *            参数
     * @return 结果字符串
     */
    public static String parseTemplate(String tplName, Map<String, Object> paras) {
        try {
            StringWriter swriter = new StringWriter();
            Template mytpl = _tplConfig.getTemplate(tplName, ENCODE);
            mytpl.process(paras, swriter);
            return getSqlText(swriter.toString());
        } catch (Exception e) {
            logger.error("模板 " + tplName, e);
            throw new RuntimeException("解析SQL模板异常", e);
        }
    }

    /**
     * 获取模板原始字符串
     *
     * @param tplName
     *            模板名
     * @return 结果字符串
     */
    public static String getTemplateaString(String tplName) {
        try {
            Template mytpl = _tplConfig.getTemplate(tplName, ENCODE);
            return mytpl.toString();
        } catch (Exception e) {
            throw new RuntimeException("模板 " + tplName, e);
        }
    }

    /**
     * 解析模板
     *
     * @param tplContent
     *            模板内容
     * @param paras
     *            参数
     * @return String 模板解析后内容
     */
    public static String parseTemplateContent(String tplContent, Map<String, Object> paras) {
        try {
            StringWriter swriter = new StringWriter();
            String key = "sql_" + tplContent.hashCode();
            if (stringTemplateLoader.findTemplateSource(key) == null) {
                stringTemplateLoader.putTemplate(key, tplContent);
            }
            Template mytpl = _sqlConfig.getTemplate(key, ENCODE);
            mytpl.process(paras, swriter);
            return getSqlText(swriter.toString());
        } catch (Exception e) {
            logger.error("模板key:" + tplContent, e);
            throw new RuntimeException("解析SQL模板异常", e);
        }
    }

    /**
     * 除去无效字段，去掉注释 不然批量处理可能报错 去除无效的等于
     * 
     * @param sql
     *            字符串
     * @return 处理后的字符串
     */
    private static String getSqlText(String sql) {
        sql = p.matcher(sql).replaceAll("");
        return sql;
    }

    /***
     * 拿到静态Class的Model
     * 
     * @param config
     *            Configuration
     * @param className
     *            className
     * @return TemplateModel
     */
    public static TemplateModel useClass(Configuration config, String className) {
        BeansWrapper wrapper = (BeansWrapper) config.getObjectWrapper();
        TemplateHashModel staticModels = wrapper.getStaticModels();
        try {
            return staticModels.get(className);
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(className);
    }
}
