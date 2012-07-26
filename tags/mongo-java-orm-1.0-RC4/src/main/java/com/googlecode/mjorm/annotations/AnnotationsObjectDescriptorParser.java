package com.googlecode.mjorm.annotations;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.jot.ReflectionUtil;
import com.googlecode.mjorm.DiscriminatorType;
import com.googlecode.mjorm.MappingException;
import com.googlecode.mjorm.MappingUtil;
import com.googlecode.mjorm.ObjectDescriptor;
import com.googlecode.mjorm.PropertyDescriptor;

/**
 * "Parses" objects with mapping annotations and
 * returns {@link ObjectDescriptor}s.
 */
public class AnnotationsObjectDescriptorParser {

	/**
	 * Parses the given {@link Class<?>}s and returns {@link ObjectDescriptor}s
	 * for them.
	 * @param classes the classes to parse
	 * @return the {@link ObjectDescriptor}s
	 */
	public List<ObjectDescriptor> parseClasses(Class<?>... classes) {
		List<ObjectDescriptor> ret = new ArrayList<ObjectDescriptor>();
		for (Class<?> clazz : classes) {
			ret.add(parseClass(clazz, ret));
		}
		return ret;
	}

	/**
	 * Parses the given {@link Class<?>}s and returns {@link ObjectDescriptor}s
	 * for them.
	 * @param classes the classes to parse
	 * @return the {@link ObjectDescriptor}s
	 */
	public List<ObjectDescriptor> parseClasses(Collection<Class<?>> classes) {
		return parseClasses(classes.toArray(new Class<?>[0]));
	}

	/**
	 * Parses the given {@link Class<?>} and returns an {@link ObjectDescriptor}
	 * for it.
	 * @param clazz the class to parse
	 * @return the {@link ObjectDescriptor}
	 */
	private ObjectDescriptor parseClass(Class<?> clazz, List<ObjectDescriptor> descriptors) {

		// get the entity annotation
		Entity entity = clazz.getAnnotation(Entity.class);
		if (entity==null) {
			throw new MappingException(
				clazz.getName()+" does not have an "+Entity.class.getName()+" annotation");
		}

		// parse entity date
		String discriminatorName = entity.discriminatorName();
		DiscriminatorType discriminatorType = entity.discriminatorType();
		SubClass[] subClasses = entity.subClasses();

		// create an object descriptor
		ObjectDescriptor desc = new ObjectDescriptor();
		desc.setType(clazz);
		desc.setDiscriminatorName(discriminatorName);
		desc.setDiscriminatorType(discriminatorType.toString());

		// get all of the methods
		for (Method getter : clazz.getMethods()) {

			// look for getter
			String name = getter.getName();
			if (!name.startsWith("is") && !name.startsWith("get")) {
				continue;
			} else if (name.startsWith("is")) {
				name = name.substring(2);
			} else if (name.startsWith("get")) {
				name = name.substring(3);
			}
			name = name.substring(0, 1).toLowerCase()+name.substring(1);

			// look for the annotations
			Property property = getter.getAnnotation(Property.class);
			Id id = getter.getAnnotation(Id.class);
			if (property==null) {
				continue;
			}

			// find a setter
			Method setter = ReflectionUtil.findSetter(clazz, name, getter.getReturnType());
			if (setter==null) {
				throw new MappingException(
					"Setter method not found for "+name+" with annotated getter: "+getter.getName());
			}

			// "parse" data
			String propField = !property.field().equals("")
				? property.field()
				: name;
			Class<?> propClass = !property.type().equals(void.class)
				? property.type()
				: getter.getReturnType();
			Type propGenericType = !property.type().equals(void.class)
				? property.type()
				: getter.getGenericReturnType();
			Class<?>[] propParamTypes = property.paramTypes().length>0
				? property.paramTypes()
				: getter.getParameterTypes();
			boolean propIsIdentifier = (id!=null);
			boolean propIsAutoGen = (id!=null && id.autoGenerated());

			// get the hints
			Map<String, Object> hints = new HashMap<String, Object>();
			if (property.translationHints()!=null) {
				for (TranslationHint hint : property.translationHints()) {
					hints.put(hint.name(), hint.stringValue());
				}
			}

			// create the PropertyDescriptor
			PropertyDescriptor prop = new PropertyDescriptor();
			prop.setName(name);
			prop.setPropColumn(propField);
			prop.setGetter(getter);
			prop.setSetter(setter);
			prop.setGenericType(propGenericType);
			prop.setIdentifier(propIsIdentifier);
			prop.setType(propClass);
			prop.setParameterTypes(propParamTypes);
			prop.setAutoGenerated(propIsAutoGen);
			prop.setTranslationHints(hints);

			// add to descriptor
			desc.addPropertyDescriptor(prop);
		}

		// parse subclasses
		for (SubClass subClassAnot : subClasses) {

			// get discriminator value
			Object discriminatorValue = MappingUtil.parseDiscriminator(
				subClassAnot.discriminiatorValue(), discriminatorType);

			// parse sub class
			ObjectDescriptor subClass = parseClass(subClassAnot.entityClass(), descriptors);

			// add subclass
			desc.addSubClassObjectDescriptor(discriminatorValue, subClass);
			descriptors.add(subClass);
		}

		// return it
		return desc;
	}

}
