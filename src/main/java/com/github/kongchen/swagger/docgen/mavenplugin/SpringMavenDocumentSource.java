package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;

/**
 * @author tedleman
 *         01/21/15
 * @author chekong
 * 05/13/2013
 */
public class SpringMavenDocumentSource extends AbstractDocumentSource {

    public SpringMavenDocumentSource(ApiSource apiSource, Log log, String encoding) throws MojoFailureException {
        super(log, apiSource);
        if(encoding !=null) {
            this.encoding = encoding;
        }
    }

    @Override
    protected Set<Class<?>> getValidClasses() {
        Set<Class<?>> result = super.getValidClasses();
        addAllFiltered(result, apiSource.getValidClasses(RestController.class));
        addAllFiltered(result, apiSource.getValidClasses(ControllerAdvice.class));
        return result;
    }

	@Override
    protected ClassSwaggerReader resolveApiReader() throws GenerateException {
        String customReaderClassName = apiSource.getSwaggerApiReader();
        if (customReaderClassName == null) {
            SpringMvcApiReader reader = new SpringMvcApiReader(swagger, LOG);
            reader.setTypesToSkip(this.typesToSkip);
            reader.setUseEnhancedOperationId(this.apiSource.isUseEnhancedOperationId());
            return reader;
        } else {
            ClassSwaggerReader customApiReader = getCustomApiReader(customReaderClassName);
            if (customApiReader instanceof SpringMvcApiReader) {
                ((SpringMvcApiReader)customApiReader).setUseEnhancedOperationId(this.apiSource.isUseEnhancedOperationId());
            }
            return customApiReader;
        }
    }
	
	private void addAllFiltered(Set<Class<?>> targetSet, Set<Class<?>> classes) {
   	 if (this.apiSource.isSkipInheritingClasses()) {
        	for (Class<?> clazz : classes) {
        		if (!isSubClassOfAny(clazz, targetSet)) {
        			targetSet.add(clazz);
        		}
        	}
        } else {
       	 targetSet.addAll(classes);
        }
	}

	private boolean isSubClassOfAny(Class<?> clazz, Set<Class<?>> classes) {
   	for (Class<?> c : classes) {
   		if (c.isAssignableFrom(clazz)) {
   			return true;
   		}
		}
   	return false;
	}
}
