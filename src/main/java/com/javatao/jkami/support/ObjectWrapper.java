package com.javatao.jkami.support;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

/**
 * 冒号参数冲突
 * 
 * @author tao
 */
public class ObjectWrapper extends DefaultObjectWrapper {
    public final static String COLON = ":";
    public final static String COLON_WRAP = "&#&";

    public ObjectWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    public TemplateModel wrap(Object obj) throws TemplateModelException {
        TemplateModel wrap = super.wrap(obj);
        if (wrap instanceof SimpleScalar) {
            String s = ((SimpleScalar) wrap).getAsString();
            if (s.contains(COLON)) {
                s = s.replace(COLON, COLON_WRAP);
                return new SimpleScalar(s);
            } else {
                return wrap;
            }
        }
        return wrap;
    }
}